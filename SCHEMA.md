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
| `sectionUpdated` | object | When each item section was last captured. See below. |
| `player` | object | Always present while logged in. See below. |
| `skills` | object | Map of skill name → skill entry. Includes `OVERALL`. |
| `vitals` | object | Current hitpoints, prayer, run energy. |
| `bank` | object | Flat bank contents; captured when the bank is opened, carried over from the previous session until then. |
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
| `spellbook` | string | Active spellbook: `STANDARD`, `ANCIENT`, `LUNAR`, `ARCEUUS`; null when unknown or Account info is off. |

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

- `bank`: `{ totalItems, items[], tabs[] }` — `items` is the canonical flat list. `tabs` holds the
  real bank tabs, `[{ tabIndex, items: [...] }]`: `tabIndex` 0 is the main tab (always present), 1 to 9
  are the numbered tabs (present only if the tab exists). The flat `items` keeps game container order
  (tabs 1 to 9, then the main tab). A bank restored from an older file has a single tab 0 until the bank is opened again.
- `inventory`: array of item entries with an extra `slot` (0–27); empty slots are included with
  `itemId` -1 so slot indices stay meaningful.
- `equipment`: map of slot name (`HEAD`, `CAPE`, `AMULET`, `WEAPON`, `BODY`, `SHIELD`, `LEGS`,
  `GLOVES`, `BOOTS`, `RING`, `AMMO`) → item entry; empty slots are omitted.

### Carried-over sections and `sectionUpdated`

On login the plugin reloads the character's own previous file and keeps its `bank`, `inventory`, and
`equipment` until each is captured again. Inventory and equipment are re-captured almost at once; the
bank is re-captured only when the player opens it. So the bank in a file can be from an earlier session.

`sectionUpdated` maps each present item section (`bank`, `inventory`, `equipment`) to the ISO-8601
instant it was last captured from the client. A value earlier than `lastUpdated` means that section was
carried over. A consumer can show it as, e.g., "bank as of 2 days ago". The field is additive: files
written before it existed simply lack it, and a section missing from the map has an unknown capture
time.

## quests / achievementDiaries

- A **quest entry** is `{ name, displayName, state }`; `state` is `NOT_STARTED`, `IN_PROGRESS`, or
  `FINISHED`.
- `achievementDiaries` maps a region (`ARDOUGNE`, `DESERT`, `FALADOR`, `FREMENNIK`, `KANDARIN`,
  `KARAMJA`, `KOUREND`, `LUMBRIDGE`, `MORYTANIA`, `VARROCK`, `WESTERN`, `WILDERNESS`) to
  `{ easy, medium, hard, elite, tasks }`. `easy`/`medium`/`hard`/`elite` are the tier-complete booleans.
- `tasks` (additive, optional) lists the individual diary tasks and whether each is done:
  `{ easy: [{ name, completed }], medium: [...], hard: [...], elite: [...] }`. `completed` is a boolean;
  `name` is the task text. All 492 tasks of the twelve diaries are listed. Order within a tier follows the
  game's storage order, not the in-game journal order. Task names are short labels and may be reworded
  between releases; do not key on them across versions. A tier or the whole `tasks` field is absent for
  a region with no mapped task. Task state comes from client varps and varbits and is as current as the
  last poll; a tier's boolean may lag the tasks until the in-game reward is claimed.

## combatAchievements

`{ tierEasy, tierMedium, tierHard, tierElite, tierMaster, tierGrandmaster }` — each is the raw value
of the corresponding RuneLite combat-achievement tier varbit. A tier's value is non-zero once that
tier is claimed; treat the exact number as opaque and compare `> 0` for "claimed".

Compatibility fields (derived, for the osrs-companion MCP server): `easyComplete`, `mediumComplete`,
`hardComplete`, `eliteComplete` — booleans equal to `tier* > 0` — and `completedTasks`, an array of
strings: the names of completed tasks (only tasks whose name resolved; never invented). The `tier*`
integers are the source of truth.

`tasks` is an array of `{ id, name, tier, monster, completed }` covering every task, completed or not.
`id` is the task id (bit `id % 32` of the `id / 32`th `VarPlayerID.CA_TASK_COMPLETED_n` varp, n = 0..20).
`name`, `tier` (`EASY` to `GRANDMASTER`) and `monster` are read from the game cache at runtime and are
`null` when unresolved. If the cache metadata cannot be read, `tasks` holds only the completed ids with
null `name`/`tier`/`monster`.

Cache ids relied on (from the community cache dump tooling, e.g. ReinhardtR/runeprofile
`scripts/ts-scripts/lib/combat-achievements.ts`; not in the RuneLite API and NOT verified in-client):
tier enums 3981 to 3986 (easy to grandmaster; values are task struct ids), monster enum 3971
(monster category id to name), struct params 1306 (task id), 1308 (name), 1312 (monster category id).

## slayer

Present only when a task is assigned:

| Field | Type | Notes |
|---|---|---|
| `creatureId` | int | Assigned-creature game id (98 is the generic boss task). |
| `creatureName` | string or null | Assigned-creature name, e.g. `Abyssal demons`. Null when it could not be resolved. |
| `bossName` | string or null | Specific boss for boss tasks. Null for every other task or when unresolved. |
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
