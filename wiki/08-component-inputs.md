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
Field 2: -11864 41 12115 tool=0 move=true
```

Options:

```text
move=true   walk toward blocks before mining
move=false  only mine blocks already in reach
tool=0      do not stop for low tool durability
tool=10     send the tool_low output when the held tool has 10 durability left
```

Limits:

- Max size is `16x16x16` for one `Mine Area`.
- Bigger mines must be split into multiple `Mine Area` components.
- If one block does not break after about 15 seconds, the component fails so the macro does not get stuck forever.

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
| `Player At Location` | X Y Z | Max distance | `100 64 100` and `5` |
| `Player Nearby` | Names or regex | Mode | `Steve` and `any` |
| `Scoreboard Contains` | Text or regex | Mode | `Purse:` and `contains` |

### Flow

| Component | Field 1 | Field 2 | Example |
| --- | --- | --- | --- |
| `Wait` | Duration ms | Empty | `1000` |
| `Repeat Macro` | Count or forever | Empty | `forever` |
| `Repeat` | Count or forever | Empty | `3` |
| `Idle Until` | Condition | Filter | `chat` and `sold` |
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
| `Chat / Command` | Message or command | Empty | `/home farm` |
| `Local Message` | Message | Empty | `Could not open chest` |

### Inventory

| Component | Field 1 | Field 2 | Example |
| --- | --- | --- | --- |
| `Click GUI Item` | Item, tag, price, or slot | Shift click true/false | `slot 10` and `false` |
| `Hotbar Select` | Item ID or blank | Slot 1-9 | `minecraft:diamond_pickaxe` or `1` |
| `Hotbar Use` | Item ID or blank | Slot 1-9 | `minecraft:ender_pearl` and `2` |
| `Drop Items` | Item ID, slot, or blank | Drop stack true/false | `minecraft:cobblestone` and `true` |
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
| `Auto Grindstone` | Shift-click result true/false | Close GUI true/false | `true` and `false` |
| `Open Inventory` | Empty | Empty | Opens your inventory. |
| `Close GUI` | Empty | Empty | Closes the open GUI. |
| `Select Hotbar Slot` | Slot 1-9 | Empty | `1` |
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
Put the item in the left slot.
Put lapis in the lapis slot.
Auto Enchant Field 1: 1, 2, 3, or best
Auto Enchant Field 2: minimum XP level, such as 30
```

Auto grindstone:

```text
Open a grindstone first.
Put the item in the grindstone input slot.
Auto Grindstone Field 1: true to shift-click the output, false to pick it up
Auto Grindstone Field 2: true to close the GUI after taking the result
```

### World

| Component | Field 1 | Field 2 | Example |
| --- | --- | --- | --- |
| `Interact With Block` | Target X Y Z | Button 0/1 | `100 64 100` and `1` |
| `Mine Block` | Target X Y Z | Select tool true/false | `100 64 100` and `true` |
| `Mine Area` | From X Y Z | To X Y Z plus options | `-11879 26 12100` and `-11864 41 12115 tool=0 move=true` |
| `Place Block` | Target X Y Z | Select block/item | `100 64 100` and `minecraft:dirt` |
| `Jump And Place Block` | Select block/item | Empty | `minecraft:dirt` |
| `Farm Area` | Radius or From X Y Z | To X Y Z plus options | `8`, or `10 64 10` and `25 79 25 replant move=true` |
| `Open Nearest Container` | Radius 1-16 | Move/type | `8` and `move=true type=chest` |
| `Auto Bone Meal` | Item ID | Options | `minecraft:bone_meal` and `refill radius=8` |
| `Block At Location Is` | X Y Z | Block ID | `100 64 100` and `minecraft:stone` |
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
| `Discord Notification` | Webhook URL | Message | `https://discord.com/api/webhooks/...` and `Macro finished` |

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
