# Beginner Macros

These examples teach the basic patterns.

## 1. Send A Chat Command

Use this to learn how links work.

```text
Macro Entry Point
  started -> Chat / Command

Chat / Command
  Message: /home
```

Steps:

1. Add `Chat / Command`.
2. Connect `Macro Entry Point.started` to `Chat / Command`.
3. Put `/home` in the message field.
4. Save and run.

## 2. Wait Then Send A Message

```text
Macro Entry Point
  started -> Wait

Wait
  Duration ms: 3000
  completed -> Chat / Command

Chat / Command
  Message: Ready
```

`3000` means 3 seconds.

## 3. Select A Hotbar Slot

```text
Macro Entry Point
  started -> Select Hotbar Slot

Select Hotbar Slot
  Slot 1-9: 1
```

Minecraft hotbar slots are `1` through `9`.

## 4. Click A GUI Item

Use this when a chest, menu, or auction GUI is already open.

```text
Macro Entry Point
  started -> Has Open GUI

Has Open GUI
  true -> Click GUI Item
  false -> Local Message

Click GUI Item
  Item id, tag, price, or GUI slot: minecraft:water_bucket
  Shift click: false
```

If you want a numbered GUI slot, use a slot value such as:

```text
slot 10
```

## 5. Deposit One Item Type

This needs a chest, barrel, or other container GUI open.

```text
Macro Entry Point
  started -> Deposit Items

Deposit Items
  Mode: specific
  Specific item: minecraft:white_wool
  Shift: On
  Fast: On
```

`Fast: On` tries to move all matching stacks in one run.

## 6. Local Message On Failure

Use this pattern to debug.

```text
Open Nearest Container
  failed -> Local Message

Local Message
  Message: Could not find a chest
```

Add a local message to any failed path when you are not sure why a macro stops.

