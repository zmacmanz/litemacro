# Litemacro

Fabric client mod for Minecraft Java Edition 26.1.x. This is a MineBot-style drag-and-drop macro builder with an in-game canvas.

## Build

From this folder:

```powershell
.\build.ps1 build
```

If PowerShell blocks scripts on your machine, run:

```powershell
.\build.bat build
```

## Install

1. Install Fabric Loader for Minecraft Java Edition 26.1.x.
2. Put the matching Fabric API for your Minecraft version in `.minecraft\mods`.
3. Put `build\libs\Litemacro-1.0.0.jar` in `.minecraft\mods`.
4. Start Minecraft with the Fabric profile.

## Commands

Primary command:

```text
/litemacro
```

Aliases:

```text
/lm
/litemacro
/macrobuilder
/macro_builder
```

Supported chat commands:

- `/litemacro help` shows the command list.
- `/litemacro` opens the macro UI.
- `/litemacro open` opens the macro UI.
- `/litemacro macro <1-100>` selects a macro.
- `/litemacro select <1-100>` also selects a macro.
- `/litemacro run <1-100>` toggles a macro on or off.
- `/litemacro start <1-100>` selects and starts a macro.
- `/litemacro resume` resumes the selected macro.
- `/litemacro stop` stops the running macro.
- `/litemacro reload` reloads the selected macro from disk.
- `/litemacro copy <1-100>` copies a macro slot.
- `/litemacro paste <1-100>` pastes into a macro slot.
- `/litemacro reset <1-100>` resets a macro slot.
- `/litemacro export <1-100>` exports a macro JSON to Downloads.
- `/litemacro import <1-100>` imports the newest macro JSON from Downloads.
- `/litemacro chat on`, `/litemacro chat off`, or `/litemacro chat toggle` controls chat notifications.

## Use

- Press `B`, or run `/litemacro open`, to open the Macros page.
- Pick any macro from the paged list. The mod supports Macro 1 through Macro 100.
- Use `Copy` and `Paste` on the macro list to duplicate the selected macro into another slot.
- Use `Download` to export the selected macro to your Windows Downloads folder, and `Import` to choose which `.json` macro file from Downloads should load into the selected slot.
- Use `Reset` on the macro list to clear the selected macro back to a new blank macro.
- Click `Edit` or `Edit Selected` to open the builder.
- Search components in the left panel, or open groups like `Player`, `Inventory`, and `World`, then drag components onto the canvas.
- In the builder, use `Copy` / `Paste` or `Ctrl+C` / `Ctrl+V` to duplicate the selected component with the same settings.
- Scroll the left component palette if the list is taller than your screen.
- Drag empty canvas space to pan the editor.
- Use the mouse wheel over the canvas to zoom in or out.
- Use `Reset View` to return to normal zoom and position.
- Click a component to edit its settings on the right.
- Set the top-bar `Delay` to pause between every connected component in the macro.
- Use `Set All` next to `Delay` to copy the global delay into every component delay.
- Select a component and set `Component delay` to override the global delay for only that component. Leave it blank to use the global delay.
- For item fields, use `Pick Item` to choose from an icon grid instead of typing the Minecraft item id.
- Use `Set started`, `Set completed`, `Set failed`, `Set true`, or `Set false`, then click another component to connect it.
- Save with `Save` or `Ctrl+S`.
- Run with `Resume`.
- Stop with `Stop` or `/litemacro stop`.
- Use `/litemacro macro <number>` to select a macro from chat.
- Use `/litemacro run <number>` to toggle a macro from chat.
- Assign macro hotkeys from the Macros page with the `Key` button next to each macro. These macro hotkeys are stored in this mod's UI, not in Minecraft's Controls menu.
- Toggle chat messages with the `Chat: On` / `Chat: Off` button on the Macros page.

Macro 1 is preloaded as `Example` from the provided `minebot_macro_export.json` on new installs. If an older config still has that bundled example in Macro 2, Litemacro moves it to Macro 1 only when Macro 1 is still blank.

If you press `Back` after editing a macro, Litemacro asks whether to save, discard, or keep editing.

## Supported Components

The builder palette includes the component types found in `minebot_macro_export (3).json`, including player, inventory, world, entity, misc, notification, login, and event blocks. The currently runnable client-side blocks are:

- `Macro Entry Point`
- `Respawn`
- `Is Account`
- `Player In Action`
- `Player Is Alive`
- `Set Crouch`
- `Set Sprint`
- `Jump`
- `Inventory Is Full`
- `Inventory Has Item`
- `Held Item Is`
- `Slot Has Item`
- `Item/Slot Has Tag`
- `Empty Slots At Least`
- `Has Open GUI`
- `Open GUI Is Full`
- `Open GUI Has Item`
- `Health Below`
- `Food Below`
- `XP Level At Least`
- `Player On Ground`
- `Player In Water`
- `Player At Location`
- `Player Nearby`
- `Wait`
- `Chat / Command`
- `Click GUI Item`
- `Hotbar Select`
- `Hotbar Use`
- `Drop Items`
- `Withdraw Items`
- `Deposit Items`
- `Close GUI`
- `Select Hotbar Slot`
- `Drop Selected Item`
- `Farm Area`
- `Open Nearest Container`
- `Auto Bone Meal`
- `Mine Area`
- `Block At Location Is`
- `Looking At Block`
- `Entity Nearby`
- `Local Message`
- `Stop Macro`
- `Random True/False`
- `Start Macro Slot`
- `Stop Macro Slot`
- `Discord Notification` as an in-game notification
- `Login Repeat` as a pass-through block
- `If Chat Same`

World, entity, low-level movement/look/mouse, disconnect, sign update, and event listener blocks run where client/server state allows it. Connect `failed` outputs to handle missing targets, bad values, closed GUIs, or blocked actions. Most true/false check components also include a `failed` output when the check cannot run cleanly.

`Farm Area` can scan a radius from 1 to 8 blocks around the player, or farm inside the box between two XYZ points. Put `From X Y Z` in the first field and `To X Y Z / options` in the second field, for example `10 64 10` and `13 65 13 replant deposit move=true`. The default options are `replant deposit move=true`, which harvests mature wheat, carrots, potatoes, beetroot, and nether wart, walks toward crops in the area, replants when the matching seed/item is in the hotbar, and opens the nearest chest, trapped chest, barrel, ender chest, or shulker box when inventory is full. Use `move=false` if you only want it to farm crops already in reach. For safety, each side of the farm box is capped at 16 blocks.

`Open Nearest Container` scans up to radius 16 for the closest matching container. Use explicit move and type options, for example `move=true type=chest`, `move=true type=barrel`, `move=true type=ender_chest`, or `move=true type=shulker`. `move=false` still opens the selected container when it is already in reach; it only stops walking toward it.

`Auto Bone Meal` uses the selected item id, usually `minecraft:bone_meal`, on the block you are looking at. Its options support `refill radius=8`, `no refill`, and `target=X Y Z`; refill opens the nearest container and withdraws the selected item when the hotbar is empty.

`Mine Area` mines inside the box between two XYZ points. Set `From X Y Z` in the first field and `To X Y Z / tool low / move` in the second field, for example `10 64 10` and `13 65 10 tool=10 move=true`. With `move=true`, it walks toward blocks in the selected area before mining. With `move=false`, it sends `failed` when the next block is out of reach. The `tool_low` output runs when the held tool has that many durability points left. For safety, each side of the mine box is capped at 16 blocks.

Build info:

```text
Name: Litemacro
Version: 1.0.54
Built for: letgio
Build date: 2026-05-09
Minecraft compatibility: 26.1.x
Extra jar: 1.21.11
```

Macros are saved at:

```text
config\litemacro\macro1.json
...
config\litemacro\macro100.json
```

Use automation only where server rules allow it.

## Failsafe Checks

Litemacro routes bad component inputs to the `failed` output when a component has one, or stops the macro with a clear message when no failed path is connected. Current safeguards include:

- Stops and releases held keys if the player or world is unavailable.
- Releases movement, attack, use, and sprint keys when a macro stops or fails.
- Catches component runtime errors so a bad macro step does not crash the client.
- Stops immediately on broken links to missing components.
- Validates chat/command text before sending it.
- Validates wait times, item ids, slot numbers, booleans, and Discord webhook URLs.
- Times out GUI clicks, movement, mining, entity targeting, closing GUI, and Discord webhooks.
- Prevents withdraw loops when the player inventory cannot accept more items.
- Prevents specific deposit/withdraw from silently completing when the item id is invalid.

## License And Code Protection

Litemacro uses a restricted copyright license. People may use the official jar
for personal gameplay, but they may not reupload, resell, rename, or distribute
modified jars without permission from letgio.

Release builds do not include a source jar. Java jars can still be decompiled by
advanced users, so the license is the rule that controls copying and
redistribution.
