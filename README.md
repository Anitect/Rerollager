# Rerollager

Reroll a professional villager's trades **in place** — without breaking its
workstation — gated by a configurable cooldown and/or cost. Built for the
**latest** Minecraft (Paper 26.1.2, Java 25), MIT-licensed, published under
[Anitect](https://github.com/Anitect).

Sneak + right-click a villager that has a profession and trades; its offers are
regenerated at its current level. Profession, level, XP, and custom name are
preserved — so you can fish a Master librarian for Mending without re-leveling
it.

## Why it exists

The two existing reroll plugins
([VillagerRefresher](https://modrinth.com/plugin/villagerrefresher),
[Villager Trade Reroll](https://modrinth.com/plugin/villager-trade-reroll)) are
both All-Rights-Reserved, feature-thin, and stop at the old 1.21.x version
scheme — **neither supports Minecraft 26.x**. Rerollager fills that gap with a
maintained, open-source plugin that ships the depth the others only plan.

| | VillagerRefresher | Villager Trade Reroll | **Rerollager** |
|---|---|---|---|
| Reroll a maxed/locked villager in place | ✓ | likely ✗ | **✓** |
| Cooldown | per-player | planned | **per-(player, villager)** |
| Cost (items / XP, level-scaled) | ✗ | planned | **✓** |
| Permissions | ✗ | planned | **✓** |
| Latest MC (26.x) | ✗ (1.21.8) | ✗ (1.21.11) | **✓** |
| License | ARR | ARR | **MIT** |

## v1 feature set

- **Reroll on sneak + right-click** a professional villager. Configurable: empty
  hand (default) or require a specific item in hand.
- **Preserves** profession, level, XP, and custom name; regenerates offers for
  all unlocked tiers at the current level, fully stocked.
- **Cooldown** keyed by (player, villager), on by default, stored on the villager
  itself so it survives restarts and self-cleans when the villager dies.
- **Cost** (off by default): consumed items and/or XP, optionally scaled by
  villager level or profession.
- **Trade locking**: sneak + right-click with a configurable item (default
  Tripwire Hook) to lock/unlock a villager so it can't be accidentally rerolled.
- **Permissions** for use, locking, and bypassing lock/cooldown/cost.
- **MiniMessage** feedback, configurable success & lock sounds, bStats, update
  checker.

## Not in v1 (roadmap)

- Vault/economy money cost
- Per-villager lifetime reroll limit
- Per-world / region gating
- Admin trade inspection
- Wandering Trader reroll

## Stack

Paper-first via paperweight-userdev (Mojang mappings) · Java 25 · Gradle Kotlin
DSL + foojay resolver · `paper-plugin.yml` · Brigadier `BasicCommand` ·
MiniMessage · `run-paper` for test servers. CI on GitHub Actions (JDK 25);
later auto-publish to Modrinth + Hangar + CurseForge.

The version-fragile NMS reroll lives behind a `RerollStrategy` interface
(see [ADR 0001](docs/adr/0001-reroll-engine-nms-behind-strategy-interface.md)).
Decisions: [docs/adr/](docs/adr/). Domain glossary: [CONTEXT.md](CONTEXT.md).
