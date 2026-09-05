# Common Macro Recipes

These are starting templates. Adjust item IDs, slots, coordinates, and delays for your server.

## Mine Until Inventory Is Full

Goal: hold left click for mining, wait until inventory is full, then continue to deposit.

```text
Macro Entry Point
  started -> Mouse Button

Mouse Button
  State: press
  Button: left
  completed -> Idle Until

Idle Until
  Condition: full
  Filter:
  triggered -> Mouse Button

Mouse Button
  State: release
  Button: left
  completed -> Open Nearest Container

Open Nearest Container
  Radius 1-16: 8
  Move/type: move=true type=chest
  completed -> Deposit Items
  failed -> Local Message

Deposit Items
  Mode: all
  Fast: On
  completed -> Close GUI
  failed -> Local Message
```

Why this works:

- The first `Mouse Button` starts holding left click.
- `Idle Until` waits instead of stopping.
- The second `Mouse Button` releases left click before opening a chest.

## Shear Sheep, Deposit Wool, Continue

```text
Macro Entry Point
  started -> Hotbar Select

Hotbar Select
  Item id or blank: minecraft:shears
  completed -> Auto Click

Auto Click
  Button: right
  Count/cps or hold ms: hold 2000
  completed -> Inventory Is Full

Inventory Is Full
  true -> Open Nearest Container
  false -> Repeat

Open Nearest Container
  Radius 1-16: 8
  Move/type: move=true type=chest
  completed -> Deposit Items

Deposit Items
  Mode: specific
  Specific item: minecraft:white_wool
  Shift: On
  Fast: On
  completed -> Close GUI

Close GUI
  completed -> Repeat
```

Use more `Deposit Items` components for other wool colors.

## Rejoin After Kick

Use this when a macro should reconnect after a kick or disconnect.

```text
Macro Entry Point
  started -> Idle Until

Idle Until
  Condition: kicked
  Filter:
  triggered -> Rejoin Server

Rejoin Server
  Delay ms: 5000
  Server IP blank=last:
  completed -> Repeat Macro
  failed -> Local Message
```

Leave `Server IP` blank to use the last multiplayer server.

## Join A Server By IP

Use this when a macro should join a specific server from the main menu or switch to a typed server while in game.

```text
Macro Entry Point
  started -> Join Server

Join Server
  Server IP: play.example.net
  Delay ms: 1000
  completed -> next component
  failed -> Local Message
```

`Join Server` needs the IP filled in. Use `Rejoin Server` instead when you want a blank IP to reuse the last server.

## Start When Chat Appears

```text
Macro Entry Point
  started -> Idle Until

Idle Until
  Condition: chat
  Filter: inventory full
  triggered -> Chat / Command

Chat / Command
  Message: /home
```

Use `If Chat Same` when you only want to check the latest chat line once. Use `Idle Until` when you want the macro to wait.

## Buy GUI Items Under A Price

Use `Item/Slot Has Tag` or `Click GUI Item` with a price rule.

Examples:

```text
$price <= 5000
$price 2k-5k
$price <= 10m
```

For a GUI slot range:

```text
gui 0-53
```

Recommended flow:

```text
Macro Entry Point
  started -> Item/Slot Has Tag

Item/Slot Has Tag
  Item id, held, slot, or GUI slots: gui 0-53
  Tag/text or $price range: $price <= 5000
  true -> Click GUI Item
  false -> Wait

Click GUI Item
  Item id, tag, price, or GUI slot: $price <= 5000
  Shift click: false
```

## Scoreboard Trigger

```text
Macro Entry Point
  started -> Scoreboard Contains

Scoreboard Contains
  Text or regex: Money
  Mode: contains
  true -> Local Message
  false -> Wait
```

Use this when server information appears on the sidebar scoreboard.
