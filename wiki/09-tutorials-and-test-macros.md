# Tutorials And Test Macros

This page gives simple patterns players can copy, test, and upload to the marketplace after editing names, tags, and server-specific details.

Use automation only where the server rules allow it.

## Test Macro Files

Ready-to-import examples are in the repo `test-macros` folder:

| File | What it tests |
| --- | --- |
| `zombie-attack-loop.json` | Finds nearby zombies, attacks, waits, and checks again. |
| `mob-farm-xp30-attack-enchant-loop.json` | Attacks mobs until XP level 30, opens an enchanting table, enchants the held item with lapis, then returns to grinding. |
| `batch-enchant-6-iron-swords.json` | Opens a nearby enchanting table and enchants up to six unenchanted iron swords, skipping already-enchanted ones. |
| `mine-area-deposit-chest-template.json` | Mines a small coordinate box, opens a nearby chest, deposits inventory, and loops. Edit the coordinates before running. |

To use one:

1. Open Litemacro.
2. Pick an empty macro slot.
3. Import or load the JSON file.
4. Read the yellow note component.
5. Edit any server-specific fields.
6. Save, test, then upload to the marketplace if it works.

## Tutorial: Attack Mobs Near You

Use this when mobs are already close enough, such as a grinder or small farm.

```text
Macro Entry Point
  started -> Entity Nearby

Entity Nearby
  Target/filter: minecraft:zombie
  Max distance: 6
  true -> Attack Entity
  false -> Wait
  failed -> Local Message

Attack Entity
  Target/filter: minecraft:zombie
  Move true/false: false
  completed -> Wait
  failed -> Local Message

Wait
  Duration ms: 700
  completed -> Entity Nearby
```

Set `Move true/false` to `false` for mob grinders so the player does not walk into the farm. Use `true` only when you want the player to chase the target.

Useful target filters:

```text
minecraft:zombie
minecraft:skeleton
minecraft:spider
minecraft:blaze
```

## Tutorial: Grind XP Then Enchant

This pattern is for a mob farm where the player stands in one place and attacks mobs until level 30.

```text
Macro Entry Point
  started -> XP Level At Least

XP Level At Least
  Level: 30
  true -> Open Nearest Enchanting Table
  false -> Entity Nearby

Entity Nearby
  Target/filter: minecraft:zombie
  Max distance: 6
  true -> Attack Entity
  false -> Wait

Attack Entity
  Target/filter: minecraft:zombie
  Move true/false: false
  completed -> Wait

Open Nearest Enchanting Table
  Radius 1-16: 8
  Move true/false: move=true
  completed -> Auto Enchant

Auto Enchant
  Option 1-3: 3
  Min XP: 30
  Item: held
  Lapis: minecraft:lapis_lazuli
  Close: On
```

Setup:

- Hold the item you want enchanted.
- Keep lapis in your inventory.
- Put an enchanting table within the radius.
- Stand where your attack can hit mobs.

## Tutorial: Enchant Several Items In A Row

Use this when you have several unenchanted copies of the same item.

```text
Macro Entry Point
  started -> Open Nearest Enchanting Table

Open Nearest Enchanting Table
  Radius 1-16: 8
  Move true/false: move=true
  completed -> Auto Enchant

Auto Enchant
  Option 1-3: 1
  Min XP: 1
  Item: minecraft:iron_sword
  Lapis: minecraft:lapis_lazuli
  Close: On
  completed -> Repeat

Repeat
  Repeat count: 6
  repeat -> Open Nearest Enchanting Table
  completed -> Local Message
```

Auto Enchant skips items that are already enchanted. For a different batch, change `minecraft:iron_sword` to the item you are enchanting.

## Tutorial: Mine Area Coordinates

Use `Mine Area` when you want the macro to mine a box.

1. Stand at one corner of the area.
2. Press `F3`.
3. Copy the block position as `X Y Z`.
4. Put that in `From corner X Y Z`.
5. Move to the opposite corner.
6. Copy that block position.
7. Put it at the start of `To corner X Y Z + options`.

Example:

```text
From corner X Y Z:
100 64 100

To corner X Y Z + options:
107 67 107 tool=10 move=true
```

The first two corners can be in any order. Litemacro sorts the low and high corner for you.

Options:

```text
tool=10     use tool_low when the held tool has 10 durability left
tool=0      disable the tool_low check
move=true   walk toward blocks and unloaded areas
move=false  only mine what is already in reach
```

Keep one Mine Area at `16x16x16` or smaller. For a bigger mine, place several Mine Area components one after another.

## Good Marketplace Upload Checklist

Before uploading a macro:

- Rename it to what it actually does.
- Add a note component that explains setup.
- Remove private server IPs, Discord webhooks, and personal commands.
- Connect failed paths to a local message or end connection.
- Test it once in singleplayer or a safe server area.
