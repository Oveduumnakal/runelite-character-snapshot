# Export schema

The plugin writes one JSON file per character to the save folder (default
`.runelite/osrs-companion/`), named `<sanitized-username>.json`. The name is lower-cased with every
character outside `[a-z0-9_-]` replaced by `_`. The file is rewritten atomically (temp file + rename),
so a process polling it never reads a half-written document.

This document describes **schema version 2**. Every top-level section except `schemaVersion`,
`lastUpdated`, and `player` is omitted when its config toggle is off, so a consumer must treat
missing sections as "not exported", not "empty".

## Top level

| Field | Type | Notes |
|---|---|---|
| `schemaVersion` | int | `2` for this format. |
| `lastUpdated` | string | ISO-8601 instant the snapshot was built. |
| `player` | object | Always present while logged in. See below. |
| `skills` | object | Map of skill name → skill entry. Includes `OVERALL`. |
| `vitals` | object | Current hitpoints, prayer, run energy. |
| `bank` | object | Flat bank contents; only present after the bank is opened. |
| `inventory` | array | One entry per slot; empty slots have `itemId` -1. |
| `equipment` | object | Map of slot name → item entry. |
| `quests` | array | One entry per quest. |
| `questPoints` | int | Total quest points. |
| `achievementDiaries` | object | Map of region name → tier completion. |
| `combatAchievements` | object | Combat-achievement tier progress. |
| `slayer` | object | Current task, or absent when none is assigned. |
| `location` | object | Region and world coordinates. |
| `grandExchange` | array | Active GE offers, by slot. |

## player

| Field | Type | Notes |
|---|---|---|
| `username` | string | In-game display name. |
| `combatLevel` | int | |
| `world` | int | |
| `accountType` | string | `NORMAL`, `IRONMAN`, `ULTIMATE_IRONMAN`, `HARDCORE_IRONMAN`, `GROUP_IRONMAN`, `HARDCORE_GROUP_IRONMAN`; null when Account info is off. |
| `members` | boolean | Whether the current world is members'. |

## skills (map value) and vitals

`skills` maps an upper-case skill name (e.g. `ATTACK`, `HITPOINTS`, `OVERALL`) to:

| Field | Type | Notes |
|---|---|---|
| `level` | int | Real (unboosted) level. |
| `xp` | number | Experience. A JSON number; the `OVERALL` total exceeds 32 bits, so parse it as a 64-bit integer. |
| `boostedLevel` | int | Current boosted level; equals `level` when unboosted. |

`vitals`:

| Field | Type | Notes |
|---|---|---|
| `currentHitpoints` / `maxHitpoints` | int | Boosted / real hitpoints. |
| `currentPrayer` / `maxPrayer` | int | Boosted / real prayer. |
| `runEnergyPercent` | int | 0–100. |

## bank, inventory, equipment

An **item entry** is `{ itemId, name, quantity }`; `name` is null for an unresolved or empty item.

- `bank`: `{ totalItems, items[] }` — `items` is a flat list (bank tabs are not modelled).
- `inventory`: array of item entries with an extra `slot` (0–27); empty slots are included with
  `itemId` -1 so slot indices stay meaningful.
- `equipment`: map of slot name (`HEAD`, `CAPE`, `AMULET`, `WEAPON`, `BODY`, `SHIELD`, `LEGS`,
  `GLOVES`, `BOOTS`, `RING`, `AMMO`) → item entry; empty slots are omitted.

## quests / achievementDiaries

- A **quest entry** is `{ name, displayName, state }`; `state` is `NOT_STARTED`, `IN_PROGRESS`, or
  `FINISHED`.
- `achievementDiaries` maps a region (`ARDOUGNE`, `DESERT`, `FALADOR`, `FREMENNIK`, `KANDARIN`,
  `KARAMJA`, `KOUREND`, `LUMBRIDGE`, `MORYTANIA`, `VARROCK`, `WESTERN`, `WILDERNESS`) to
  `{ easy, medium, hard, elite }` booleans.

## combatAchievements

`{ tierEasy, tierMedium, tierHard, tierElite, tierMaster, tierGrandmaster }` — each is the raw value
of the corresponding RuneLite combat-achievement tier varbit. A tier's value is non-zero once that
tier is claimed; treat the exact number as opaque and compare `> 0` for "claimed".

## slayer

Present only when a task is assigned:

| Field | Type | Notes |
|---|---|---|
| `creatureId` | int | Assigned-creature game id; map to a name consumer-side. |
| `amount` | int | Remaining kills. |
| `points` | int | Slayer reward points. |
| `streak` | int | Task-completion streak. |

## location

`{ regionId, x, y, plane }` — the character's current world point.

## grandExchange

Array of `{ slot, itemId, name, state, quantitySold, totalQuantity, price, spent }`. `state` is one
of `EMPTY`, `BUYING`, `BOUGHT`, `SELLING`, `SOLD`, `CANCELLED_BUY`, `CANCELLED_SELL` (empty slots
are not exported).

## Versioning

`schemaVersion` is bumped whenever the shape changes. Additive changes (new optional fields) keep the
version where existing fields are unchanged; a removal or a semantic change bumps it. Consumers should
read `schemaVersion` and ignore unknown fields.
