# Troubleshooting

## Macro Stops Early

Most often this means a component failed and no `failed` output was connected.

Fix:

1. Click the component before the stop.
2. Connect its `failed` output to `Local Message`.
3. Put a clear message such as `Could not open chest`.
4. Run again and see which message appears.

## Click GUI Item Does Nothing

Check:

- A GUI is open.
- The item ID is correct, for example `minecraft:water_bucket`.
- If using slots, the value is a GUI slot such as `slot 10` or `gui 0-53`.
- If using price text, the tooltip actually contains a price.
- The price rule uses the number you mean.

Examples:

```text
$price <= 5000
$price 2k-5k
$price <= 10m
```

Remember:

```text
500 = five hundred
2k = two thousand
2m = two million
2b = two billion
2t = two trillion
```

## Deposit Items Does Not Move Everything

Use:

```text
Fast: On
Shift: On
```

`Fast: On` tries to move every matching stack it can in one run.

Use `Exclude slots` when you do not want some inventory slots moved.

Example:

```text
0-8, 35
```

## Drop Items Only Drops Hotbar Items

Use `Drop Items`, not only `Drop Selected Item`.

`Drop Selected Item` drops the held item. `Drop Items` can drop matching items from inventory.

For same-item cleanup, set the item ID and enable the option that drops all matching stacks.

## Rejoin Does Not Work

Check:

- Use `If Kicked` or `Idle Until` with condition `kicked`.
- Connect `triggered` to `Rejoin Server`.
- Leave server IP blank to use the last multiplayer server.
- Add a delay such as `5000` ms before reconnecting.

If the server kicks with a permanent rule message, reconnecting may keep failing. That is server-side behavior.

## Main Menu Opens But Macro Cannot Edit

Use the current jar and open the main Litemacro screen, then select a macro slot and press `Edit`.

If only run controls are visible, check GUI scale and whether the selected macro slot is active.

## GUI Scale Looks Too Big

Minecraft GUI scale changes how much room the mod has.

Try:

- GUI scale `2` or `3` for editing big macros.
- Use canvas zoom for the builder.
- Use `Reset View` if the canvas is off-screen.
- Move big groups of components into columns so connection lines are easier to read.

## Marketplace Upload Succeeds But New Upload Is Missing

Check:

- Clear the marketplace search box.
- Press the marketplace refresh/list button.
- Make sure the row in Supabase has `published = true`.
- Make sure `storage_bucket = database`.
- Make sure you are running the latest jar.

## Discord Webhook Fails

Check:

- The webhook URL is complete.
- The webhook was not deleted in Discord.
- The message field is not blank.
- Your network can reach Discord.

Do not put private webhooks in macros you upload publicly.

## A Component Needs World State

Some components need an active world and player. They will not work from the main menu.

World-required examples:

- Inventory checks
- GUI clicks
- Entity actions
- Block interactions
- Mining
- Placing blocks
- Player movement

Use `Rejoin Server`, `Wait`, `Idle Until`, or event components for main-menu-safe flows.

