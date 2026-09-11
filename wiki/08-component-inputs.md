# Component Inputs

This page shows what to type into each component's settings.

## How Fields Work

When you click a component in the builder, the right panel shows its editable fields.

- `Field 1` means the first text box for that component.
- `Field 2` means the second text box for that component.
- Empty means leave the field blank.
- Use spaces between coordinates: `X Y Z`.
- Use Minecraft item IDs like `minecraft:diamond_pickaxe`.
- Connect every important output, especially `failed`, so the macro does not stop with no explanation.

## Component Data Inputs And Outputs

Some components save a value while the macro is running. A later component can use that value by typing its name into a field.

You can use saved values as the whole field:

```text
last.slot
repair_sword.item
${player.pos}
```

You can also put live values inside messages:

```text
/msg letgio ${player.name} is at ${player.pos}
Macro ${macro.number} finished on ${server.ip}
Looking at ${target.block.id} at ${target.block.pos}
```

Useful values:

```text
last.item          last item ID saved by a component
last.slot          player inventory slot 1-36 saved by a component
last.inventory_slot same as last.slot
last.hotbar_slot   hotbar slot 1-9, only saved when the item was placed in the hotbar
last.action        what the component did, such as inventory or drop
```

Live values:

```text
player.name        your username
player.x           your block X
player.y           your block Y
player.z           your block Z
player.pos         your block X Y Z
player.exact_pos   your exact X Y Z
player.xp_level    your current XP level
player.held_item   item ID in your main hand
player.selected_slot selected hotbar slot 1-9
target.block.pos   block X Y Z you are looking at
target.block.id    block ID you are looking at
target.entity.name entity name you are looking at
target.entity.id   entity type you are looking at
server.ip          current or last server IP
screen.name        current screen title
macro.name         current macro name
macro.number       current macro slot number
macro.status       current macro status text
```

You can also use a named component output. If you name an `Auto Grindstone` component `Repair Sword`, the same values are available as:

```text
repair_sword.item
repair_sword.slot
repair_sword.hotbar_slot
```

Examples:

```text
Auto Grindstone
  Result: Inv
  completed -> Hotbar Select

Hotbar Select
  Item ID or blank: last.item
  Slot 1-9: blank
```

Use `last.item` when the macro should find the item by item ID. Use `last.slot` when you want Hotbar Select to select the exact saved slot. If `last.slot` is 10-36, Litemacro moves that stack into the hotbar first.

## Coordinates

Use Minecraft's debug screen to get coordinates.

1. Press `F3`.
2. Find your block position or the block you are looking at.
3. Type it as `X Y Z`.

Example:

```text
-11879 26 12100
```

For area components, use two corners of the box:

```text
From X Y Z: -11879 26 12100
To X Y Z:   -11864 41 12115
```

The order does not matter. Litemacro finds the low and high corner automatically.

Coordinate fields also support live values and relative coordinates:

```text
${player.pos}
${target.block.pos}
~ ~-1 ~
~5 ~2 ~5
```

`~` starts from your current player block position. For example, `~ ~-1 ~` means the block under your feet.

## Mine Area

Use `Mine Area` to mine every block inside a small box.

Fields:

```text
Field 1: From X Y Z
Field 2: To X Y Z plus options
```

Good example:

```text
Field 1: -11879 26 12100
Field 2: -11864 41 12115
Tool Low: 10
Move: On
Auto Tool: On
```

Relative example around your current position:

```text
Field 1: ~-3 ~-1 ~-3
Field 2: ~3 ~2 ~3 move=false auto_tool=true tool=10
```

Options:

```text
move=true   walk toward blocks before mining
move=false  only mine blocks already in reach
auto_tool=true   switch to the best hotbar tool before each block
auto_tool=false  keep using the held item
tool=0      do not stop for low tool durability
tool=10     send the tool_low output when the selected tool has 10 durability left
```

Limits and behavior:

- Max size is `16x16x16` for one `Mine Area`.
- Bigger mines must be split into multiple `Mine Area` components.
- `Auto Tool` checks your hotbar and switches between pickaxe, shovel, axe, or another faster tool based on the block being mined.
- If one block is protected, unreachable, or will not break, Litemacro skips that block and keeps checking the rest of the area.
- If the whole area is unloaded and `move=true`, Litemacro walks toward the area for a limited time. If it still cannot load the area, the component fails instead of walking forever.

Outputs:

```text
completed -> next step after the area is mined
tool_low  -> repair, swap tool, local message, or stop path
failed    -> local message or recovery path
```

## Component Field Guide

### Entry

| Component | Field 1 | Field 2 | Example |
| --- | --- | --- | --- |
| `Macro Entry Point` | Empty | Empty | Connect `started` to the first real component. |

### Player

| Component | Field 1 | Field 2 | Example |
| --- | --- | --- | --- |
| `Respawn` | Empty | Empty | Clicks respawn when dead. |
| `Move` | Direction or target | Distance | `forward` and `5`, or `100 64 100` |
| `Look` | Yaw pitch or target | Target type | `0 0`, or `100 64 100` |
| `Mouse Button` | `press` or `release` | `left` or `right` | `press` and `left` |
| `Shoot Bow` | Target/filter | Equip bow true/false | `minecraft:zombie` and `true` |
| `Is Account` | Player names | Empty | `letgio, altname` |
| `Player In Action` | State | Empty | `sneaking`, `using`, `dead`, `alive` |
| `Player Is Alive` | Empty | Empty | Sends `true` or `false`. |
| `Set Crouch` | true/false | Empty | `true` |
| `Set Sprint` | true/false | Empty | `true` |
| `Jump` | Empty | Empty | Jumps once. |
| `Auto Click` | Button | Count, CPS, interval, or hold | `left` and `count=20 cps=8` |
| `Press Key` | Key name | Press/hold duration | `shift` and `hold 2000` |

### Logic

| Component | Field 1 | Field 2 | Example |
| --- | --- | --- | --- |
| `Inventory Is Full` | Empty | Empty | Sends `true` when inventory has no empty slots. |
| `Inventory Has Item` | Item ID | Minimum count | `minecraft:bone` and `300` |
| `Health Below` | Health value | Empty | `10` |
| `Food Below` | Food value | Empty | `6` |
| `XP Level At Least` | Level | Empty | `30` |
| `Player On Ground` | Empty | Empty | Sends `true` or `false`. |
| `Player In Water` | Empty | Empty | Sends `true` or `false`. |
| `Player At Location` | X Y Z, `~`, or runtime value | Max distance | `${player.pos}` and `5` |
| `Player Nearby` | Names or regex | Mode | `Steve` and `any` |
| `Scoreboard Contains` | Text or regex | Mode | `Purse:` and `contains` |

### Flow

| Component | Field 1 | Field 2 | Example |
| --- | --- | --- | --- |
| `Wait` | Duration ms | Empty | `1000` |
| `Repeat Macro` | Count or forever | Empty | `forever` |
| `Repeat` | Count or forever | Empty | `3` |
| `Idle Until` | Condition | Filter | `chat` and `sold` |
| `Skip If True` | true/false | Component name or id to skip | `true` and `Wait Before Sell` |
| `Stop At XP Level` | Target level | `release` or `stop` | `30` and `release` |
| `End Connection` | Empty | Empty | Ends this branch without failing the macro. |
| `Note` | Text | Empty | `Repair path starts here` |
| `Stop Macro` | Empty | Empty | Stops the current macro run. |

Idle conditions:

```text
chat
player
full
kicked
```

### Action

| Component | Field 1 | Field 2 | Example |
| --- | --- | --- | --- |
| `Chat / Command` | Message or command | Empty | `/msg letgio ${player.name} at ${player.pos}` |
| `Local Message` | Message | Empty | `Could not open chest at ${player.pos}` |

### Inventory

| Component | Field 1 | Field 2 | Example |
| --- | --- | --- | --- |
| `Click GUI Item` | Item, tag, price, or slot | Shift click true/false | `slot 10` and `false` |
| `Hotbar Select` | Item ID, runtime value, or blank | Slot 1-36 or runtime value | `minecraft:diamond_pickaxe`, `last.item`, or `last.slot` |
| `Hotbar Use` | Item ID, runtime value, or blank | Slot 1-36 or runtime value | `minecraft:ender_pearl`, `last.item`, or `last.slot` |
| `Drop Items` | Item ID, slot, runtime value, or blank | Drop stack true/false | `minecraft:cobblestone`, `last.slot`, and `true` |
| `Withdraw Items` | `all` or `specific` | Specific item | `specific` and `minecraft:bone` |
| `Deposit Items` | `all` or `specific` | Specific item | `specific` and `minecraft:white_wool` |
| `Held Item Is` | Item ID | Empty | `minecraft:shears` |
| `Slot Has Item` | Slot | Item ID | `1` and `minecraft:diamond_pickaxe` |
| `Item/Slot Has Tag` | Item, held, slot, or GUI slots | Tag/text or price range | `held` and `Efficiency` |
| `Item Durability` | Item, held, slot, or GUI slots | Lower/Higher than percent | `held` and `lower 20` |
| `Empty Slots At Least` | Minimum empty slots | Empty | `3` |
| `Has Open GUI` | Empty | Empty | Sends `true` when a GUI is open. |
| `Open GUI Is Full` | Empty | Empty | Sends `true` when the open GUI has no empty slots. |
| `Open GUI Has Item` | Item ID | Minimum count | `minecraft:bone` and `64` |
| `Auto Enchant` | Option 1-3 | Minimum XP level | `3` and `30` |
| `Auto Grindstone` | Mode: remove enchants or repair | Input item selector | `remove enchants` and `held` |
| `Open Inventory` | Empty | Empty | Opens your inventory. |
| `Close GUI` | Empty | Empty | Closes the open GUI. |
| `Select Hotbar Slot` | Slot 1-36 or runtime value | Empty | `1` or `last.slot` |
| `Drop Selected Item` | Drop stack true/false | Empty | `true` |

Slot examples:

```text
slot 10
gui 0-53
0-8
9-35
```

Price examples:

```text
$price <= 5000
$price 2k-5k
$price <= 10m
```

Auto enchanting:

```text
Open an enchanting table first.
Auto Enchant Field 1: 1, 2, 3, or best
Auto Enchant Field 2: minimum XP level, such as 30
Enchant item: held, any, or an item ID like minecraft:iron_sword
Lapis item: minecraft:lapis_lazuli
```

When Auto Enchant loads items itself, it skips items that are already enchanted. For batch enchanting, set `Enchant item` to the item ID, such as `minecraft:iron_sword`, and keep several unenchanted copies in inventory.

Auto grindstone:

```text
Use Open Nearest Grindstone first if you want the macro to open the GUI.
Mode button: Remove Enchants or Repair
Input item: held, selected, any, or an item ID
Repair item: same, held, any, or an item ID
Result: Inv takes one output item and moves it back into inventory
Result: Drop throws the output out instead
Close: On closes the grindstone after taking the result
```

Auto Grindstone only handles one grindstone output each time the component runs. Connect it back into a loop only when you want it to keep processing more tools.

Auto Grindstone saves output values after it takes the result:

```text
last.item          result item ID
last.slot          inventory slot where the result was placed, 1-36
last.hotbar_slot   hotbar slot, 1-9, only if the result went to the hotbar
last.action        inventory or drop
```

To select the output item after grindstone:

```text
Hotbar Select Field 1: last.item
Hotbar Select Field 2: blank
```

To select by exact saved slot:

```text
Select Hotbar Slot Field 1: last.slot
```

If `last.slot` is 10-36, Litemacro swaps that stack into an empty hotbar slot when possible, then selects it. If the hotbar is full, it swaps into the currently selected hotbar slot.

### World

| Component | Field 1 | Field 2 | Example |
| --- | --- | --- | --- |
| `Interact With Block` | Target X Y Z, `~`, or runtime value | Button 0/1 | `${target.block.pos}` and `1` |
| `Mine Block` | Target X Y Z, `~`, or runtime value | Select tool true/false | `~ ~-1 ~` and `true` |
| `Mine Area` | From X Y Z, `~`, or runtime value | To X Y Z plus options | `~-3 ~-1 ~-3` and `~3 ~2 ~3 tool=10 auto_tool=true` |
| `Place Block` | Target X Y Z | Select block/item | `100 64 100` and `minecraft:dirt` |
| `Jump And Place Block` | Select block/item | Empty | `minecraft:dirt` |
| `Farm Area` | Radius, X Y Z, `~`, or runtime value | To X Y Z plus options | `8`, or `~-4 ~-1 ~-4` and `~4 ~1 ~4 replant move=true` |
| `Open Nearest Container` | Radius 1-16 | Move/type | `8` and `move=true type=chest` |
| `Open Nearest Enchanting Table` | Radius 1-16 | Move true/false | `8` and `move=true` |
| `Open Nearest Grindstone` | Radius 1-16 | Move true/false | `8` and `move=true` |
| `Auto Bone Meal` | Item ID | Options | `minecraft:bone_meal` and `refill radius=8` |
| `Block At Location Is` | X Y Z, `~`, or runtime value | Block ID | `~ ~-1 ~` and `minecraft:stone` |
| `Looking At Block` | Block ID or blank | Empty | `minecraft:chest` |

Block button values:

```text
0 = left/attack
1 = right/use
```

### Entity

| Component | Field 1 | Field 2 | Example |
| --- | --- | --- | --- |
| `Attack Entity` | Target/filter | Move true/false | `minecraft:zombie` and `true` |
| `Interact Entity` | Target/filter | Move true/false | `minecraft:villager` and `true` |
| `Entity Nearby` | Target/filter | Max distance | `minecraft:sheep` and `8` |

### Misc

| Component | Field 1 | Field 2 | Example |
| --- | --- | --- | --- |
| `Disconnect` | Permanent true/false | Empty | `true` |
| `Rejoin Server` | Delay ms | Server IP blank=last | `5000` and empty |
| `Join Server` | Server IP | Delay ms | `play.example.net` and `3000` |
| `Stop Macro Slot` | Macro number | Empty | `2` |
| `Start Macro Slot` | Macro number | Empty | `2` |
| `Random True/False` | True percent | Empty | `50` |
| `Random Output 3` | Empty | Empty | Sends `one`, `two`, or `three`. |
| `Update Sign` | Sign text | Empty | `Line 1 | Line 2 | Line 3 | Line 4` |
| `Is In Lobby` | Player name | Empty | `letgio` |

### Notification

| Component | Field 1 | Field 2 | Example |
| --- | --- | --- | --- |
| `Discord Notification` | Webhook URL | Message | `https://discord.com/api/webhooks/...` and `${player.name} finished at ${player.pos}` |

Do not upload public macros with private webhook URLs.

### Login

| Component | Field 1 | Field 2 | Example |
| --- | --- | --- | --- |
| `Login Repeat` | Repeat count | Empty | `2` |

### Event

| Component | Field 1 | Field 2 | Example |
| --- | --- | --- | --- |
| `If Chat Same` | Chat text | Mode | `Sold` and `contains` |
| `Event: Chat` | Filter text/regex | Mode | `Welcome` and `contains` |
| `If Kicked` | Reason text/regex | Mode | `any` and `contains` |
| `Event: Death` | Empty | Empty | Triggers when you die. |
| `Event: Damage` | Empty | Empty | Triggers when you take damage. |
| `Event: Teleport` | Empty | Empty | Triggers after teleport/world change. |
| `Event: Schedule` | Time or cron | Target type | `12:30` or `* * * * *` |
| `Event: Player Spawned` | Player/filter | Mode | `Steve` and `contains` |
| `Event: Player Despawned` | Player/filter | Mode | `Steve` and `contains` |

Modes usually accept:

```text
contains
exact
regex
any
allow
block
```
