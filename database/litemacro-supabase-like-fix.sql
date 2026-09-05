-- Litemacro Marketplace Like button fix.
-- Run this once in Supabase SQL Editor for project klhkfpdtpoipafarugop.

create table if not exists public.litemacro_marketplace_likes (
    preset_id uuid not null references public.marketplace_presets(id) on delete cascade,
    liker_install_id text not null,
    created_at timestamptz not null default now(),
    primary key (preset_id, liker_install_id)
);

create index if not exists litemacro_marketplace_likes_liker_idx
on public.litemacro_marketplace_likes (liker_install_id, created_at desc);

create or replace function public.like_litemacro_marketplace_preset(
    target_preset_id uuid,
    liker_install_id text
)
returns jsonb
language plpgsql
security definer
set search_path = public, pg_temp
as $$
declare
    normalized_liker text;
    liked_preset public.marketplace_presets;
begin
    normalized_liker := nullif(trim(coalesce(liker_install_id, '')), '');
    if normalized_liker is null then
        raise exception 'Liker id is required.' using errcode = 'P0001';
    end if;

    insert into public.litemacro_marketplace_likes (preset_id, liker_install_id)
    select target_preset_id, normalized_liker
    where exists (
        select 1
        from public.marketplace_presets
        where id = target_preset_id
          and published = true
    )
    on conflict do nothing;

    update public.marketplace_presets presets
    set likes_count = (
            select count(*)
            from public.litemacro_marketplace_likes likes
            where likes.preset_id = presets.id
        ),
        updated_at = now()
    where presets.id = target_preset_id
      and presets.published = true
    returning * into liked_preset;

    if liked_preset.id is null then
        raise exception 'Marketplace macro was not found.' using errcode = 'P0001';
    end if;

    return to_jsonb(liked_preset);
end;
$$;

alter table public.litemacro_marketplace_likes enable row level security;

revoke all on public.litemacro_marketplace_likes from anon, authenticated;
grant execute on function public.like_litemacro_marketplace_preset(uuid, text) to anon, authenticated;

notify pgrst, 'reload schema';

select
    'Litemacro marketplace Like setup complete' as status,
    exists (
        select 1
        from pg_proc p
        join pg_namespace n on n.oid = p.pronamespace
        where n.nspname = 'public'
          and p.proname = 'like_litemacro_marketplace_preset'
    ) as like_rpc_installed,
    exists (
        select 1
        from information_schema.tables
        where table_schema = 'public'
          and table_name = 'litemacro_marketplace_likes'
    ) as likes_table_installed;
