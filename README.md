# Litemacro

Litemacro is a Fabric client mod for Minecraft Java Edition. It adds a drag-and-drop macro builder, macro slots, component-based logic, marketplace sharing, notes, and in-game macro controls.

## Source Layout

- `versions/mc26.2` - Litemacro for Minecraft 26.2.
- `versions/mc1.21.11` - Litemacro for Minecraft 1.21.11.
- `versions/mc1.21.1` - Litemacro for Minecraft 1.21.1.
- `wiki` - user guide and component reference.
- `database` - Supabase SQL setup for the Litemacro marketplace.
- `config` - example marketplace configuration.
- `test-macros` - example/test macro files.

## Build

Open PowerShell in the version folder you want to build, then run:

```powershell
.\gradlew.bat build
```

The built jar appears in that version folder under `build\libs`.

## Privacy

See `PRIVACY.md`.

## License

Litemacro uses a restricted copyright license. See `LICENSE`.
