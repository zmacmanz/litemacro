# Getting Started

## Open Litemacro

Default key:

```text
B
```

Commands:

```text
/litemacro
/lm
/macrobuilder
/macro_builder
```

The main menu can also open Litemacro, so you can edit or start macros before joining a server.

## Pick A Macro Slot

Litemacro supports macro slots from `1` through `100`.

On the macro list screen:

- `Edit` opens the selected macro in the builder.
- `Run` starts or stops that macro.
- `Key Unbound` lets you choose a keybind for that macro.
- `Import` loads a macro JSON from your Downloads folder.
- `Download` exports the selected macro JSON to Downloads.
- `Copy` and `Paste` duplicate macros between slots.
- `Reset` clears the selected slot.
- `Market` opens the marketplace.

## Create A New Macro

1. Open Litemacro with `B`.
2. Pick an empty macro slot.
3. Click `Edit`.
4. Keep the `Macro Entry Point`; every macro needs one.
5. Drag components from the left panel onto the canvas.
6. Click an output on one component, then click the next component to connect it.
7. Click a component to edit its settings on the right.
8. Click `Save`.
9. Click `Run` or go back to the macro list and press `Run`.

## Save Your Work

Use `Save` in the builder. Litemacro also auto-saves while building so progress is less likely to be lost after a kick or disconnect.

Macro files are saved in:

```text
config/litemacro/macro1.json
config/litemacro/macro2.json
...
config/litemacro/macro100.json
```

## Import And Export

Export:

1. Select a macro slot.
2. Click `Download`.
3. The macro JSON is written to your Downloads folder.

Import:

1. Select the slot you want to replace.
2. Click `Import`.
3. Choose the macro JSON.
4. Save after checking the macro.

## Run From Chat

Useful commands:

```text
/litemacro macro 1
/litemacro run 1
/litemacro start 1
/litemacro stop
/litemacro export 1
/litemacro import 1
```

