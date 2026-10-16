-- Adds the Litemacro My Uploads marketplace view.
-- Safe to run more than once in the Supabase SQL editor.

create or replace function public.list_my_litemacro_marketplace_presets(
    requester_install_id text
)
returns setof public.marketplace_presets
language sql
security definer
set search_path = public, pg_temp
stable
as $$
    select presets.*
    from public.marketplace_presets presets
    where presets.uploader_install_id = nullif(trim(coalesce(requester_install_id, '')), '')
      and presets.storage_bucket = 'database'
    order by presets.updated_at desc;
$$;

grant execute on function public.list_my_litemacro_marketplace_presets(text) to anon, authenticated;

notify pgrst, 'reload schema';

select 'Litemacro My Uploads setup complete' as status;
