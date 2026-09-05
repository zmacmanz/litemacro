# Marketplace

The Litemacro marketplace is an online macro gallery. It lets players share macros they made and load macros made by other players.

It does not share your Minecraft account. It only uploads the selected Litemacro macro data, plus the name, author, description, and tags you type into the submit window.

## What It Is For

Use the marketplace when you want to:

- Share a macro with other Litemacro users.
- Find a macro someone else already made.
- Save time by loading a community macro and editing it for your own setup.
- Keep useful examples in one place.

Always check a downloaded macro before running it. A macro can click, type commands, move items, open GUIs, reconnect, or run loops depending on how it was built.

## Open The Marketplace

From the Litemacro menu:

1. Open Litemacro.
2. Click `Market`.

From the macro builder:

1. Open the builder.
2. Click the marketplace button in the top menu.

## Browse And Load A Macro

1. Open `Market`.
2. Use the search box to find a macro by name, author, or tags.
3. Click a macro card to select it.
4. Click `Load`.
5. Choose where to save or import it.
6. Open the macro in the builder and inspect it before running.

Good things to check before running:

- What starts the macro.
- Whether it has loops or repeat components.
- Whether it sends chat messages or commands.
- Whether it clicks inside GUIs.
- Whether it reconnects or runs from the main menu.

## Submit A Macro

1. Select the macro slot you want to share.
2. Open `Market`.
3. Click `Submit`.
4. Fill in the submit form.
5. Click `Submit` again.

The submit form has these fields:

- `Name`: The macro name shown on the marketplace card.
- `Author`: The name people see as the creator.
- `Description`: A short explanation of what the macro does.
- `Tags`: Search words separated by commas.

Example:

```text
Name: Wool Deposit Loop
Author: letgio
Description: Shears sheep, deposits wool, then returns to work.
Tags: wool, sheep, deposit, loop
```

Current upload limit:

```text
5 uploads per user/install
```

## Tags

Tags help users find your macro.

Use simple words:

```text
mining, deposit, sheep, wool, chat, rejoin, gui
```

Do not put a long sentence in tags. Put the long explanation in the description.

## If Upload Fails

Look at the red status message at the bottom of the marketplace screen. It usually tells you the real problem.

Common causes:

- The macro name is blank.
- The author field is blank.
- The macro slot is empty.
- The upload limit has already been reached.
- The marketplace database setup is missing.
- The internet connection or Supabase request failed.

If the error says a database function or column is missing, the Supabase setup needs to be fixed by the marketplace admin.

## If Old Macros Still Show

The marketplace list comes from the Supabase database, not only from storage files.

If a macro was removed from storage but still appears in the marketplace, its database row probably still exists and is still published.

The current mod lists marketplace rows where:

```text
storage_bucket = database
published = true
```

If old rows still show after they were removed, make sure you are running the newest Litemacro jar and refresh the marketplace.

## Admin: Hide Or Delete A Macro

Marketplace rows live in Supabase table:

```text
public.marketplace_presets
```

To hide a macro without deleting the row:

```sql
update public.marketplace_presets
set published = false
where id = 'PASTE_MACRO_ID_HERE';
```

To fully delete the row:

```sql
delete from public.marketplace_presets
where id = 'PASTE_MACRO_ID_HERE';
```

Find marketplace rows:

```sql
select id, name, author_name, slug, storage_bucket, published, updated_at
from public.marketplace_presets
order by updated_at desc;
```

Deleting a storage file is not enough. The marketplace list reads from `marketplace_presets`, so the row must be unpublished or deleted.
