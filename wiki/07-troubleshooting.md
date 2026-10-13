# Troubleshooting

## Macro Stops Early

Most often this means a component failed and no `failed` output was connected.

Fix:

1. Click the component before the stop.
2. Connect its `failed` output to `Local Message`.
3. Put a clear message such as `Could not open chest`.
4. Run again and see which message appears.

## Mine Area Stops After Some Time

`Mine Area` keeps working through single bad blocks, but it still stops when it reaches a real safety exit.

Common reasons:

- `tool=10` is enabled and the selected tool has 10 durability left. Connect `tool_low` to a repair/swap path, or use `tool=0` to disable this check.
- One block did not break or could not be reached. Current builds skip that block and continue, so check the status text for skipped blocks.
- The next block is out of reach and `move=false` is set. Use `move=true` if the macro should walk toward blocks.
- The selected area is bigger than `16x16x16`. Split the mine into multiple `Mine Area` components.
- The area is not loaded and the macro cannot move close enough.
- The component finished the selected area and no next component is connected.

Good Mine Area settings:

```text
From X Y Z: -11879 26 12100
To X Y Z: -11864 41 12115
Tool low durability: 0
Move: On
Auto Tool: On
```

Use `tool=10` only when you connect the `tool_low` output to something useful, such as a local message, repair macro, or stop path.

## Open Nearest Container Opens The Wrong Thing

Use the right container type:

- `Open Nearest Chest` only searches for chests.
- `Open Nearest Barrel` only searches for barrels.
- `Open Nearest Shulker` only searches for shulker boxes.
- `Open Nearest Container` uses the `Type` dropdown.

If the player should walk toward the container, turn `Move` on. If the macro should only open a container already in reach, turn `Move` off.

Supported generic container types are `Any`, `Chest`, `Barrel`, `Shulker`, `Ender Chest`, `Furnace`, `Blast Furnace`, `Smoker`, `Brewing Stand`, `Hopper`, `Dispenser`, and `Dropper`.

## Actionbar Or Repeat Until Does Not Trigger

Check:

- Use `Actionbar Contains` to check the latest actionbar once.
- Use `Event: Actionbar` or `Idle Until` with condition `actionbar` to wait for a future actionbar message.
- Use `Repeat Until` when a section should keep repeating until actionbar/chat/player/item/XP/health/food/water/full/kicked becomes true.
- If the actionbar text changes quickly, use a short wait before checking again.

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

## Event Schedule Does Not Trigger

Check:

- The macro is still running and has reached `Event: Schedule`.
- Use `10s` for a simple ten-second test.
- Use 24-hour local time such as `14:30`, not `2:30 PM`.
- Use exactly `* * * * *` for the minute wildcard.
- Leave the second field blank.
- Connect the `triggered` output to another component.

Full cron expressions such as `*/5 * * * *` are not supported. Use a relative delay with `Repeat` or `Repeat Macro` when you need a repeating interval.

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
