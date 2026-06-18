# Rerollager

A lightweight Paper plugin for rerolling villager trades. Sneak and right click a villager to refresh its trades on the spot, with no breaking and replacing the workstation. Keep rerolling until you get the enchanted book, tool, or price you want.

Made for the latest Minecraft (Paper 26.2, Java 25). No dependencies. MIT licensed.

## What it does

In vanilla you can only reroll a villager's trades before you have traded with it, and the usual trick is to keep breaking and replacing its lectern or workstation. Rerollager skips that. Sneak and right click an employed villager and its trades regenerate at its current level, keeping the same profession, level, XP, and name. It also works on villagers you have already traded with and leveled up, so you can reroll a maxed librarian without starting over.

## Features

- Reroll trades in place with a sneak and right click. No workstation breaking.
- Keeps the villager's profession, level, XP, and custom name.
- Works on villagers you have already traded with, not just fresh ones.
- Optional cooldown, tracked per player and per villager.
- Optional cost per reroll: items, XP, or both, with optional scaling by villager level or profession.
- Trade locking so a villager cannot be rerolled by accident. Lock and unlock with a configurable item.
- Permission based. Nothing is granted to players by default, so you decide who can use it.
- Lightweight with no external dependencies.

## How to use

1. Find or make an employed villager by placing a job site block next to it (a lectern makes a Librarian).
2. Sneak and right click it with an empty hand. Its trades reroll.
3. To lock a villager, sneak and right click it while holding the lock item (a Tripwire Hook by default). Do it again to unlock.

Out of the box only operators can reroll and lock. Grant `rerollager.use` (and `rerollager.lock`) to the players or groups you want to give access to.

## Commands

| Command | Description |
| --- | --- |
| `/rerollager info` | Show the current cooldown, cost, and lock settings |
| `/rerollager reload` | Reload the config |

Alias: `/rrl`.

## Permissions

Nothing is granted to regular players automatically. Assign these with your permissions plugin (for example LuckPerms) to decide who can use the plugin.

| Permission | Default | Description |
| --- | --- | --- |
| `rerollager.use` | op | Reroll villager trades |
| `rerollager.lock` | op | Lock and unlock a villager |
| `rerollager.bypass.cooldown` | none | Reroll without waiting for the cooldown |
| `rerollager.bypass.cost` | none | Reroll without paying the cost |
| `rerollager.bypass.lock` | none | Reroll a villager whose trades are locked |
| `rerollager.admin` | op | Use `/rerollager reload` |

## Configuration

The defaults are safe to run as is. The cooldown is on at 30 seconds, and cost and locking are ready to turn on whenever you want them.

```yaml
trigger:
  # Require this item in the main hand to reroll. Leave empty ("") to reroll with an empty hand.
  require-item: ""
  consume-required-item: false

cooldown:
  # Per player, per villager. Stored on the villager, so it survives restarts.
  enabled: true
  seconds: 30

lock:
  # Sneak + right click while holding this item to lock or unlock a villager.
  # Must be different from trigger.require-item.
  enabled: true
  item: "TRIPWIRE_HOOK"

cost:
  # Charge a price per reroll. Off by default.
  enabled: false
  items: {}          # e.g. EMERALD: 3
  xp:
    enabled: false
    mode: levels      # "levels" or "points"
    amount: 5
  scaling:
    by-level: false
    level-multipliers: [1.0, 1.5, 2.0, 3.0, 5.0]
    by-profession: false
    profession-multipliers:
      librarian: 2.0

messages:
  # MiniMessage formatting. Leave a message empty ("") to hide it.
  on-cooldown: "<red>You must wait <time> before rerolling this villager again.</red>"
  cannot-afford: "<red>You can't afford to reroll this villager.</red>"
  success: "<green>Trades rerolled.</green>"
  locked: "<green>Trades locked.</green>"
  unlocked: "<yellow>Trades unlocked.</yellow>"
  reroll-denied-locked: "<red>This villager's trades are locked. Unlock them first.</red>"
```

See [config.yml](src/main/resources/config.yml) for the full list of options.

## Requirements

- Paper or a Paper fork such as Purpur, for Minecraft 26.2
- Java 25

## Building

```bash
./gradlew build
```

The plugin jar lands in `build/libs`. Run `./gradlew runServer` to start a test server with the plugin loaded.

## License

MIT. See [LICENSE](LICENSE).
