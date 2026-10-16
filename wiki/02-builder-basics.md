# Builder Basics

## Components

Components are the boxes on the canvas. Each component does one job, such as waiting, clicking, checking inventory, sending a command, or opening a container.

The left panel groups components into categories:

- `Entry`
- `Player`
- `Logic`
- `Flow`
- `Action`
- `Inventory`
- `World`
- `Entity`
- `Misc`
- `Notification`
- `Login`
- `Event`

## Links And Outputs

A link connects one output to another component.

To make a link, drag from the small output dot on the right side of a component and release over the next component. A preview line follows the mouse while you drag. You can also click an output dot or a connection button, then click the target component.

Common outputs:

- `started` - the start output from `Macro Entry Point`.
- `completed` - the component finished successfully.
- `failed` - the component could not finish.
- `true` - a check matched.
- `false` - a check did not match.
- `triggered` - an event or idle condition happened.

Good macros usually connect both success and failure paths. If a component fails and there is no failed path, the macro may stop.

## Required Start

Every macro needs:

```text
Macro Entry Point
```

Connect `started` to the first real component.

## Component Settings

Click a component to edit it on the right side.

Common settings:

- `Name` gives a component a custom label, useful when you have many `Wait`, `Move`, or `Click` components.
- `Component delay` overrides the global delay for one component.
- `Enabled` lets you turn a component on or off for testing.
- `Pick Item` helps fill item IDs without typing them.
- Some components have buttons or dropdowns instead of raw text. For example, `Mine Area` has `Move` and `Auto Tool` buttons, and `Open Nearest Container` has a `Move` button plus a `Type` dropdown.
- `Connections` shows and clears links from the selected component.

Useful builder shortcuts:

- `Ctrl+C` copies the selected component or selected group.
- `Ctrl+V` pastes into the current macro. You can copy in one macro slot and paste in another.
- `Delete` removes the selected component or group. Extra `Macro Entry Point` components can be deleted.
- `Ctrl+Z` undo.
- `Ctrl+Y` redo.
- `D`, `F`, and `T` start a link from the selected component's `done/completed`, `failed`, or `true` output when that output exists.

Notes support normal text editing: select text with the mouse, copy/paste selected text, use Up/Down to move lines, and use Ctrl+Left/Ctrl+Right to jump by words.

## Global Delay

The top bar `Delay` is the default wait time between components.

Use `Set All` to copy the global delay into every component delay.

## Random Delay

Use `Random` next to the global delay to set a random min and max delay.

This makes each run vary the delay between components. Keep the range reasonable so your macro still works.

## Running Overlay

The top-right `Litemacro Running` panel shows active macros and their current components.

Open the main macro menu and click `Overlay: On` to change it to `Overlay: Off`. This hides the panel without stopping any macros or changing chat notifications. The choice is saved for the next time Minecraft starts.

## Repeat

Use `Repeat Macro` to run the whole macro more than once.

Use `Repeat` to repeat only a section connected to that component.

Both support a repeat count. Use `forever` or the repeat indefinitely option when you want an endless loop.

Use `Repeat Until` when you want a linked section to keep repeating until a condition becomes true, such as chat text, actionbar text, XP level, health, food, water, item count, full inventory, player nearby, or kicked/disconnected.

## End Connection

`End Connection` ends one branch without treating it as an error and without stopping other running branches.

Use it when one optional branch should simply stop there.

## Main Menu Support

Some components can run from the main menu and some need an active world/player. Anything that interacts with inventory, blocks, entities, or player movement needs a loaded world.

Main menu friendly components include:

- `Macro Entry Point`
- `Wait`
- `Local Message`
- `Repeat`
- `Repeat Macro`
- `Idle Until`
- `Rejoin Server`
- `Join Server`
- `Start Macro Slot`
- `Stop Macro Slot`
- `Random True/False`
- `Random Output 3`
- `Discord Notification`
- `If Kicked`
- `Event: Chat`
- `Event: Actionbar`
- `Skip If True`

Macros keep running while Minecraft is not focused. Litemacro temporarily disables Minecraft's pause-on-lost-focus behavior while a macro is running, then restores the old setting when the macro stops. Macros can also keep ticking while chat or the ESC menu is open, but components that need the world, player, inventory, or GUI still need that state to exist.
