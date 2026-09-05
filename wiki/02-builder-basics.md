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

- `Component delay` overrides the global delay for one component.
- `Enabled` lets you turn a component on or off for testing.
- `Pick Item` helps fill item IDs without typing them.
- `Connections` shows and clears links from the selected component.

## Global Delay

The top bar `Delay` is the default wait time between components.

Use `Set All` to copy the global delay into every component delay.

## Random Delay

Use `Random` next to the global delay to set a random min and max delay.

This makes each run vary the delay between components. Keep the range reasonable so your macro still works.

## Repeat

Use `Repeat Macro` to run the whole macro more than once.

Use `Repeat` to repeat only a section connected to that component.

Both support a repeat count. Use `forever` or the repeat indefinitely option when you want an endless loop.

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
