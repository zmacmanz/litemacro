-- Allow an anonymous Litemacro installation to delete one of its own uploads.
-- Run this once in the Supabase SQL Editor for the Litemacro project.

create or replace function public.delete_litemacro_marketplace_preset(
    target_preset_id uuid,
    requester_install_id text
)
returns jsonb
language plpgsql
security definer
set search_path = public, pg_temp
as $$
declare
    normalized_requester text;
    deleted_name text;
begin
    normalized_requester := nullif(trim(coalesce(requester_install_id, '')), '');
    if normalized_requester is null then
        raise exception 'Uploader id is required.' using errcode = 'P0001';
    end if;

    delete from public.marketplace_presets presets
    where presets.id = target_preset_id
      and presets.uploader_install_id = normalized_requester
    returning presets.name into deleted_name;

    if deleted_name is null then
        raise exception 'Only the original uploader can delete this marketplace macro.' using errcode = 'P0001';
    end if;

    return jsonb_build_object(
        'deleted', true,
        'message', 'Deleted ' || deleted_name || ' from the marketplace.'
    );
end;
$$;

grant execute on function public.delete_litemacro_marketplace_preset(uuid, text) to anon, authenticated;

notify pgrst, 'reload schema';

select
    'Litemacro marketplace delete setup complete' as status,
    exists (
        select 1
        from pg_proc p
        join pg_namespace n on n.oid = p.pronamespace
        where n.nspname = 'public'
          and p.proname = 'delete_litemacro_marketplace_preset'
    ) as delete_rpc_installed;
