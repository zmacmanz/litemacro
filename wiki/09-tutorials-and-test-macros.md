# Tutorials And Test Macros

This page gives simple patterns players can copy, test, and upload to the marketplace after editing names, tags, and server-specific details.

Use automation only where the server rules allow it.

## Test Macro Files

Ready-to-import examples are in the repo `test-macros` folder:

| File | What it tests |
| --- | --- |
| `zombie-attack-loop.json` | Finds nearby zombies, attacks, waits, and checks again. |
| `hold-left-until-inventory-full.json` | Holds left click until inventory is full. |
| `shear-sheep-deposit-wool-loop.json` | Shears sheep, deposits wool, and loops. |
| `mob-farm-xp30-attack-enchant-loop.json` | Attacks mobs until XP level 30, opens an enchanting table, enchants the held item with lapis, then returns to grinding. |
| `batch-enchant-6-iron-swords.json` | Opens a nearby enchanting table and enchants up to six unenchanted iron swords, skipping already-enchanted ones. |
| `mine-area-deposit-chest-template.json` | Mines a small coordinate box, opens a nearby chest, deposits inventory, and loops. Edit the coordinates before running. |
| `mine-area-auto-tool-template.json` | Mines a small area while switching to the best hotbar tool for stone, dirt, wood, and similar blocks. |
| `mine-area-stop-before-block.json` | Mines an area but stops the whole macro before breaking a selected protected block. Includes editing notes and block ID examples. |
| `auto-fish-repair-home.json` | Auto fishes with spare-rod switching, a rod-low repair-command path, and an inventory-full home-command path. |
| `mine-deposit-wool-nearest-chest-loop.json` | Mines, opens the nearest chest, deposits wool, and loops. |
| `working-mine-area-16x16x16.json` | Safe Mine Area test for one max-size area. |
| `working-mine-area-full-chunked.json` | Larger Mine Area example split into smaller chunks. |
| `runtime-data-grindstone-output-hotbar.json` | Tests named runtime outputs. Auto Grindstone saves `clean_item.slot`, then Select Hotbar Slot uses it. |
| `runtime-data-relative-mine-area-auto-tool.json` | Tests relative coordinates with Mine Area and Auto Tool. |
| `runtime-data-target-block-message.json` | Tests live values in messages, including player position, target block, world, and server. |
| `advanced-xp30-enchant-repair-loop.json` | Holds attack until XP 30, repairs low-durability held items, enchants, then loops. |
| `advanced-relative-quarry-deposit-loop.json` | Mines a relative quarry with Auto Tool, deposits to a nearby chest, and handles low tools. |
| `advanced-kick-rejoin-watchdog.json` | Always-on idle watcher that rejoins after a kick or disconnect. |
| `advanced-gui-shop-sell-template.json` | Server shop GUI template that waits for item count, opens sell GUI, clicks, closes, and loops. |
| `whole-macro-wait-for-chat-then-run.json` | Shows a macro that does nothing until a chat message appears, then starts two action branches. |
| `whole-macro-wait-for-item-then-run.json` | Shows a macro that waits for an inventory item before starting the rest of the macro. |
| `whole-macro-wait-for-player-then-run.json` | Shows a macro that waits for a nearby player before starting. |
| `parallel-mine-with-safety-checker.json` | Shows a mining branch plus a safety checker branch for water, health, hunger, and tool repair commands. |
| `player-near-webhook-mine-spawners-echest-logoff.json` | Waits for a nearby player, sends a webhook, mines nearby spawners, deposits them, sends a done webhook, then logs off. |
| `mob-farm-xp30-auto-enchant-loop.json` | Clicks mobs until XP 30, opens the nearest enchanting table, enchants, then loops. |
| `mob-farm-xp30-auto-grindstone-repair-loop.json` | XP farm pattern that opens a grindstone and repairs or clears items. |
| `rejoin-last-server-watchdog.json` | Simple rejoin watcher using the last server. |
| `kick-rejoin-test.json` | Basic kick/disconnect rejoin test. |
| `event-schedule-10-second-test.json` | Waits 10 seconds, then shows a local confirmation message. |
| `bossbar-auto-start-stop-watcher.json` | Starts itself when any or a matching boss bar appears, watches it, and stops when it disappears. Includes editing notes. |
| `bossbar-trigger-test.json` | One-time named boss-bar trigger test using `Litemacro Test`. |
| `status-effect-trigger-test.json` | Starts itself when Speed I or higher appears and displays the effect runtime values. |

To use one:

1. Open Litemacro.
2. Pick an empty macro slot.
3. Import or load the JSON file.
4. Read the yellow note component.
5. Edit any server-specific fields.
6. Save, test, then upload to the marketplace if it works.

## Tutorial: Event Schedule

Use `Event: Schedule` when a branch should wait for a delay or a specific local time.

```text
Macro Entry Point
  started -> Event: Schedule

Event: Schedule
  Time: 10s
  triggered -> Local Message

Local Message
  Message: Schedule test worked after 10 seconds.
```

Start with `10s` because it is easy to verify. The macro must stay running and remain on `Event: Schedule` while it waits.

Accepted values:

```text
10s       relative delay in seconds
2m        relative delay in minutes
14:30     24-hour local clock time
* * * * * once per wall-clock minute; first activation may be immediate
```

Full cron syntax is not supported. Import `test-macros/event-schedule-10-second-test.json` for a ready-to-run example.

## Tutorial: Attack Mobs Near You

Use this when mobs are already close enough, such as a grinder or small farm.

```text
Macro Entry Point
  started -> Entity Nearby

Entity Nearby
  Target/filter: minecraft:zombie
  Max distance: 6
  true -> Attack Entity
  false -> Wait
  failed -> Local Message

Attack Entity
  Target/filter: minecraft:zombie
  Move true/false: false
  completed -> Wait
  failed -> Local Message

Wait
  Duration ms: 700
  completed -> Entity Nearby
```

Set `Move true/false` to `false` for mob grinders so the player does not walk into the farm. Use `true` only when you want the player to chase the target.

Useful target filters:

```text
minecraft:zombie
minecraft:skeleton
minecraft:spider
minecraft:blaze
```

## Tutorial: Grind XP Then Enchant

This pattern is for a mob farm where the player stands in one place and attacks mobs until level 30.

```text
Macro Entry Point
  started -> XP Level At Least

XP Level At Least
  Level: 30
  true -> Open Nearest Enchanting Table
  false -> Entity Nearby

Entity Nearby
  Target/filter: minecraft:zombie
  Max distance: 6
  true -> Attack Entity
  false -> Wait

Attack Entity
  Target/filter: minecraft:zombie
  Move true/false: false
  completed -> Wait

Open Nearest Enchanting Table
  Radius 1-16: 8
  Move true/false: move=true
  completed -> Auto Enchant

Auto Enchant
  Option 1-3: 3
  Min XP: 30
  Item: held
  Lapis: minecraft:lapis_lazuli
  Close: On
```

Setup:

- Hold the item you want enchanted.
- Keep lapis in your inventory.
- Put an enchanting table within the radius.
- Stand where your attack can hit mobs.

## Tutorial: Enchant Several Items In A Row

Use this when you have several unenchanted copies of the same item.

```text
Macro Entry Point
  started -> Open Nearest Enchanting Table

Open Nearest Enchanting Table
  Radius 1-16: 8
  Move true/false: move=true
  completed -> Auto Enchant

Auto Enchant
  Option 1-3: 1
  Min XP: 1
  Item: minecraft:iron_sword
  Lapis: minecraft:lapis_lazuli
  Close: On
  completed -> Repeat

Repeat
  Repeat count: 6
  repeat -> Open Nearest Enchanting Table
  completed -> Local Message
```

Auto Enchant skips items that are already enchanted. For a different batch, change `minecraft:iron_sword` to the item you are enchanting.

## Tutorial: Grindstone Result Then Select It

Use this to test component outputs. The grindstone saves the result item and slot, then the hotbar step uses that saved value.

```text
Macro Entry Point
  started -> Open Nearest Grindstone

Open Nearest Grindstone
  Radius 1-16: 8
  Move true/false: move=true
  completed -> Auto Grindstone

Auto Grindstone
  Mode: Remove Enchants
  Input item: held
  Result: Inv
  Close: On
  completed -> Hotbar Select
  failed -> Local Message

Hotbar Select
  Item ID or blank: last.item
  Slot 1-9: blank
  completed -> Local Message
  failed -> Local Message
```

If you want to select by slot instead, put `last.slot` into `Select Hotbar Slot`. If the result lands in inventory slot 10-36, Litemacro swaps it into the hotbar first.

You can also name the Auto Grindstone component and use that name later:

```text
Auto Grindstone component name:
Clean Item

Select Hotbar Slot
  Slot 1-36 or runtime value: clean_item.slot

Local Message
  Message: Cleaned ${clean_item.item} in slot ${clean_item.slot}
```

Component names are turned into lowercase data keys. Spaces become underscores, so `Clean Item` becomes `clean_item`.

## Tutorial: Wait For Something, Then Start Everything

Use `Idle Until` at the very start when the whole macro should wait before any real work begins.

Example: wait until a player is near, then run both branches.

```text
Macro Entry Point
  started -> Idle Until

Idle Until
  Condition: player
  Filter: blank for any player, or type a name

Discord Notification
  Name: Start Webhook
  Webhook URL: paste your private webhook
  Message: Player near ${player.name} at ${player.pos}

Idle Until triggered -> Discord Notification named Start Webhook
Idle Until triggered -> first mining/action component
```

The important part is that the branches are connected after `Idle Until`. They cannot run early because nothing reaches them until the trigger happens.

Good starting conditions:

```text
chat       wait for chat text
actionbar  wait for actionbar text
player     wait for a nearby player
item       wait until an item exists in inventory
xp         wait until XP reaches a level
water      wait until the player is in water
kicked     wait until kicked or disconnected
```

## Tutorial: Mine With A Safety Checker

Use this when one branch mines while another branch watches for danger.

```text
Macro Entry Point
  started -> Mine Area
  started -> Player In Water

Mine Area
  From: ~-2 ~-1 ~-2
  To: ~2 ~-1 ~2
  Move: On
  Auto Tool: On
  tool_low -> Chat / Command

Player In Water
  true -> Chat / Command
  false -> Health Below

Health Below
  Health: 10
  true -> Chat / Command
  false -> Food Below
```

Example recovery commands:

```text
Water path: /home
Low health path: /heal
Tool low path: /repair
```

This does not magically override server rules or permissions. The commands must work on the server.

## Tutorial: Use Actionbar Text

Use actionbar checks when a server writes status above the hotbar.

```text
Macro Entry Point
  started -> Event: Actionbar

Event: Actionbar
  Filter text/regex: Sold
  Mode: contains
  triggered -> Local Message
```

For a one-time check, use `Actionbar Contains`. For waiting, use `Event: Actionbar` or `Idle Until` with condition `actionbar`.

## Tutorial: Start And Stop On A Bossbar

Import `test-macros/bossbar-auto-start-stop-watcher.json`, keep its macro slot selected, leave `Auto` set to `Off`, and leave the macro stopped. Its blank filter means any visible boss bar can start it, including a Wither boss bar.

Edit both `Wake When Bossbar Appears` and `Is Bossbar Still Visible` when you only want a certain boss bar. The filter and mode must be identical on both components.

| Filter | Mode | Result |
| --- | --- | --- |
| empty | `contains` | Any visible boss bar |
| `Wither` | `contains` | Any boss-bar text containing `Wither` |
| `Wither` | `exact` | Only a boss bar whose complete name is `Wither` |
| `Wither|Dragon` | `regex` | A boss bar containing `Wither` or `Dragon` |
| `^(Wither|Ender Dragon)$` | `regex` | Only either complete boss-bar name |

Regex is case-insensitive and searches anywhere in the visible text. Use `^` at the beginning and `$` at the end for a complete-text match. Invalid regex uses the component's `failed` output.

To test safely, use a creative test world with cheats and create a harmless custom boss bar:

```text
/bossbar add litemacro:test {"text":"Litemacro Test"}
/bossbar set litemacro:test players @s
```

The macro should start itself and display the detected text. Hide the bar to test automatic stopping, show it to test re-arming, and remove it afterward:

```text
/bossbar set litemacro:test visible false
/bossbar set litemacro:test visible true
/bossbar remove litemacro:test
```

The watcher checks every 500 milliseconds. `Bossbar Contains` sends `true` back to the wait while a matching bar remains visible. It sends `false` to `Stop Macro` when the matching bar disappears. Runtime text can use `${bossbar.text}`, `${bossbar.percent}`, and `${bossbar.progress}`.

Use `test-macros/bossbar-trigger-test.json` instead when you only need the original one-time `Litemacro Test` trigger.

## Tutorial: Start On A Status Effect

Import `test-macros/status-effect-trigger-test.json`, select it, and leave it stopped. Give yourself Speed I:

```text
/effect give @s minecraft:speed 30 0
```

`Event: Status Effect` should start the selected macro once and show the effect ID, level, and remaining seconds. Remove the effect before testing again. Use `Has Status Effect` for a normal `true` or `false` check inside a running macro.

## Tutorial: Container Dropdown

Use the generic component when users should be able to change the target container without replacing the component.

```text
Open Nearest Container
  Radius: 8
  Move: On
  Type: Barrel
```

Use the direct components when the macro should always target one kind:

```text
Open Nearest Chest
Open Nearest Barrel
Open Nearest Shulker
```

## Advanced Editable Templates

These templates are bigger on purpose. Each one has yellow notes and component names starting with `EDIT` so players know exactly what to change.

| File | Main components to edit | What to change |
| --- | --- | --- |
| `advanced-xp30-enchant-repair-loop.json` | `EDIT XP Level`, `EDIT Durability Check`, `EDIT Open Grindstone`, `EDIT Open Table`, `Repair Item`, `Enchant Item` | XP target, durability percent, table/grindstone radius, repair mode, enchant option, lapis item. |
| `advanced-relative-quarry-deposit-loop.json` | `EDIT Empty Space`, `EDIT Mine Box`, `EDIT Chest Search`, `EDIT Deposit` | Empty slot limit, relative mine corners, tool-low number, chest radius/type, deposit mode and excluded hotbar slots. |
| `advanced-kick-rejoin-watchdog.json` | `EDIT Kick Filter`, `EDIT Rejoin`, `Loop Wait` | Kick reason filter, reconnect delay, optional fixed server IP, wait before watching again. |
| `advanced-gui-shop-sell-template.json` | `EDIT Sell Item Count`, `EDIT Sell Command`, `EDIT GUI Click`, `Wait For GUI`, `Wait For Items` | Item ID, minimum count, command, GUI slot/item, wait times. |

Good names for editable components:

```text
EDIT Sell Item Count
EDIT Mine Box
EDIT Rejoin
EDIT Open Table
```

Good names for data-output components:

```text
Repair Item -> repair_item.slot
Clean Item  -> clean_item.item
Find Target -> find_target.pos
```

Spaces become underscores and the name becomes lowercase.

## Tutorial: Mine Area Coordinates

Use `Mine Area` when you want the macro to mine a box.

1. Stand at one corner of the area.
2. Press `F3`.
3. Copy the block position as `X Y Z`.
4. Put that in `From corner X Y Z`.
5. Move to the opposite corner.
6. Copy that block position.
7. Put it in `To corner X Y Z`.
8. Turn `Move` on if the macro should walk toward blocks.
9. Turn `Auto Tool` on if the macro should switch hotbar tools.
10. Set `Tool low durability` to a safe number like `10`, or `0` to disable the low-tool output.

Example:

```text
From corner X Y Z:
100 64 100

To corner X Y Z:
107 67 107

Tool low durability:
10

Move: On
Auto Tool: On
```

The first two corners can be in any order. Litemacro sorts the low and high corner for you.

Options:

```text
tool=10     use tool_low when the selected tool has 10 durability left
tool=0      disable the tool_low check
move=true   walk toward blocks and unloaded areas
move=false  only mine what is already in reach
auto_tool=true   switch to pickaxe, shovel, axe, or fastest hotbar tool
auto_tool=false  keep using the held item
```

Keep one Mine Area at `16x16x16` or smaller. For a bigger mine, place several Mine Area components one after another.

## Good Marketplace Upload Checklist

Before uploading a macro:

- Rename it to what it actually does.
- Add a note component that explains setup.
- Remove private server IPs, Discord webhooks, and personal commands.
- Connect failed paths to a local message or end connection.
- Test it once in singleplayer or a safe server area.
