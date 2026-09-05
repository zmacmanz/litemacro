# Litemacro Wiki

This wiki is for players who want to build, run, share, and troubleshoot Litemacro macros.

Start with these pages:

- [Getting Started](01-getting-started.md) - open the menu, pick a macro slot, edit, save, and run.
- [Builder Basics](02-builder-basics.md) - components, links, outputs, delays, random delay, and keybinds.
- [Beginner Macros](03-beginner-macros.md) - simple macros to learn the system.
- [Common Macro Recipes](04-common-recipes.md) - mining, depositing, rejoin, GUI clicking, shearing, and chat triggers.
- [Component Reference](05-component-reference.md) - what each component category is for.
- [Marketplace](06-marketplace.md) - upload, load, delete, and manage community macros.
- [Troubleshooting](07-troubleshooting.md) - common issues and how to fix them.

## What Litemacro Does

Litemacro is a client-side Minecraft Fabric mod for building macros with components. A macro starts at a `Macro Entry Point`, follows connected lines through actions and checks, and can branch on outputs like `completed`, `failed`, `true`, `false`, or `triggered`.

Macros can automate repeated client actions such as clicking, pressing keys, selecting hotbar slots, checking inventory state, depositing items, sending commands, reacting to chat, reconnecting, and opening nearby containers.

Use automation only where the server rules allow it.

## Quick Example

A basic chat macro looks like this:

```text
Macro Entry Point
  started -> Chat / Command

Chat / Command
  Message: /home base
```

A safer macro usually connects failed paths too:

```text
Macro Entry Point
  started -> Open Nearest Container

Open Nearest Container
  completed -> Deposit Items
  failed -> Local Message

Deposit Items
  completed -> Close GUI
  failed -> Local Message
```

