# Component Reference

This page explains what each component group is for.

## Entry

| Component | Use |
| --- | --- |
| `Macro Entry Point` | First block every macro starts from. Connect `started` to the first component. |

## Player

| Component | Use |
| --- | --- |
| `Respawn` | Clicks respawn when dead. |
| `Move` | Moves in a direction, to a place, or toward a target. |
| `Look` | Turns the player toward yaw/pitch or a target. |
| `Mouse Button` | Presses or releases left/right click. Good for hold mining. |
| `Shoot Bow` | Aims and fires a bow. |
| `Is Account` | Checks the current username against names. |
| `Player In Action` | Checks state such as sneaking, using, dead, or alive. |
| `Player Is Alive` | True when the player is alive. |
| `Set Crouch` | Turns crouch on or off. |
| `Set Sprint` | Turns sprint on or off. |
| `Jump` | Jumps once. |
| `Auto Click` | Left/right clicking with count, CPS, interval, or hold time. |
| `Press Key` | Presses or holds a keyboard key, including Shift, Ctrl, and Alt. |

## Logic

| Component | Use |
| --- | --- |
| `Inventory Is Full` | True when inventory has no empty slots. |
| `Inventory Has Item` | Checks for an item and minimum count. |
| `Health Below` | True when health is at or below the value. |
| `Food Below` | True when hunger is at or below the value. |
| `XP Level At Least` | True when XP level is high enough. |
| `Player On Ground` | True when touching ground. |
| `Player In Water` | True when in water. |
| `Player At Location` | Checks if player is near coordinates. |
| `Player Nearby` | Checks for nearby players by name or regex. |
| `Scoreboard Contains` | Checks scoreboard text with contains, exact, or regex mode. |

## Flow

| Component | Use |
| --- | --- |
| `Wait` | Pauses for a number of milliseconds. |
| `Repeat Macro` | Repeats the whole macro. |
| `Repeat` | Repeats a connected section. |
| `Idle Until` | Waits until chat, player nearby, inventory full, or kicked. |
| `End Connection` | Ends one branch without stopping other branches. |
| `Stop Macro` | Stops the current macro. |

## Action

| Component | Use |
| --- | --- |
| `Chat / Command` | Sends chat text or a command. |
| `Local Message` | Shows a local message for debugging or status. |

## Inventory

| Component | Use |
| --- | --- |
| `Click GUI Item` | Clicks a matching item, tag, price, or GUI slot in an open GUI. |
| `Hotbar Select` | Selects a hotbar item or slot. |
| `Hotbar Use` | Uses a hotbar item. |
| `Drop Items` | Drops matching inventory or hotbar items. |
| `Withdraw Items` | Moves items from an open GUI into inventory. |
| `Deposit Items` | Moves inventory items into an open GUI. |
| `Held Item Is` | Checks the item in the main hand. |
| `Slot Has Item` | Checks an inventory slot for an item. |
| `Item/Slot Has Tag` | Checks item text/tag data and can check GUI slots and price ranges. |
| `Item Durability` | Checks durability lower or higher than a percent. |
| `Empty Slots At Least` | Checks free inventory slot count. |
| `Has Open GUI` | True when a GUI or chest is open. |
| `Open GUI Is Full` | True when the open GUI has no empty slots. |
| `Open GUI Has Item` | Checks for an item in the open GUI. |
| `Open Inventory` | Opens the player inventory. |
| `Close GUI` | Closes the current GUI. |
| `Select Hotbar Slot` | Selects hotbar slot 1-9. |
| `Drop Selected Item` | Drops the held hotbar item. |

### Slot Notes

Inventory slots usually use:

```text
0-8 hotbar
9-35 inventory
0-53 common chest/menu GUI slots
```

## World

| Component | Use |
| --- | --- |
| `Interact With Block` | Right-clicks or attacks a target block. Useful for anvils and containers. |
| `Mine Block` | Mines a target block. |
| `Mine Area` | Mines between two XYZ points, with a `tool_low` output. |
| `Place Block` | Places a selected block on a target. |
| `Jump And Place Block` | Jumps and places below the player. |
| `Farm Area` | Harvests crops between two XYZ points and can move. |
| `Open Nearest Container` | Finds and opens chest, barrel, ender chest, or shulker. |
| `Auto Bone Meal` | Uses bone meal and can refill from nearby containers. |
| `Block At Location Is` | Checks block ID at coordinates. |
| `Looking At Block` | Checks the block currently targeted. |

## Entity

| Component | Use |
| --- | --- |
| `Attack Entity` | Attacks a matching mob or player. |
| `Interact Entity` | Right-clicks a matching mob or player. |
| `Entity Nearby` | Checks for matching nearby entities. |

## Misc

| Component | Use |
| --- | --- |
| `Disconnect` | Disconnects from the server. |
| `Rejoin Server` | Reconnects to the last server or a typed IP. |
| `Join Server` | Connects to the typed server IP from the main menu or in game. |
| `Stop Macro Slot` | Stops another macro slot. |
| `Start Macro Slot` | Starts another macro slot. |
| `Random True/False` | Randomly chooses true or false. |
| `Random Output 3` | Randomly chooses one of three outputs. |
| `Update Sign` | Updates the sign you are looking at. |
| `Is In Lobby` | Checks if a named player is you. |

## Notification

| Component | Use |
| --- | --- |
| `Discord Notification` | Sends a Discord webhook message. |

## Login

| Component | Use |
| --- | --- |
| `Login Repeat` | Pass-through login repeat marker. |

## Event

| Component | Use |
| --- | --- |
| `If Chat Same` | Checks latest chat against text. |
| `Event: Chat` | Triggers when matching chat appears. |
| `If Kicked` | Triggers after a kick or disconnect. |
| `Event: Death` | Triggers when you die. |
| `Event: Damage` | Triggers when you take damage. |
| `Event: Teleport` | Triggers after teleport or world change. |
| `Event: Schedule` | Triggers on a timer or clock time. |
| `Event: Player Spawned` | Triggers when a matching player appears. |
| `Event: Player Despawned` | Triggers when a matching player leaves. |
