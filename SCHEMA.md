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
| `runePouch` | array | Runes in the rune pouch; present only while one is in the inventory. See below. |
| `storages` | object | Last known looting bag, seed vault and group storage contents. See below. |
| `quests` | array | One entry per quest. |
| `questPoints` | int | Total quest points. |
| `achievementDiaries` | object | Map of region name → tier completion. |
| `combatAchievements` | object | Combat-achievement tier progress. |
| `slayer` | object | Slayer points, streak and current task (task fields are 0/null between tasks). |
| `location` | object | Region and world coordinates. |
| `grandExchange` | array | Active GE offers, by slot. |
| `collectionLog` | object | Collection log captured while the log is open, carried over between sessions. |
| `killCounts` | object | Boss kill counts keyed by lower-case boss name. |
| `clueCounts` | object | Completed treasure trails per tier. |

## player

| Field | Type | Notes |
|---|---|---|
| `username` | string | In-game display name. |
| `combatLevel` | int | |
| `world` | int | |
| `accountType` | string | `NORMAL`, `IRONMAN`, `ULTIMATE_IRONMAN`, `HARDCORE_IRONMAN`, `GROUP_IRONMAN`, `HARDCORE_GROUP_IRONMAN`; null when Account info is off. |
| `members` | boolean or null | Whether the current world is members'; null when Account info is off. |
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
  `GLOVES`, `BOOTS`, `RING`, `AMMO`) → item entry; empty slots are omitted. A character wearing nothing exports `{}`; the field is absent only when equipment is toggled off or not yet captured.

- `runePouch` (additive, optional): array of item entries, one per filled pouch slot (up to 6; the divine pouch has
  more slots than the standard one). Present only while the inventory holds a rune
  pouch of any variant (including divine and trouver-protected); an empty pouch gives `[]`, no pouch
  omits the field. Read live from varbits, so it is as current as the last inventory change or 30-tick poll.
  It is not carried over from a previous session.

- `storages` (additive, optional): `{ lootingBag: [...], seedVault: [...], groupStorage: [...] }`, each an
  array of item entries (empty slots omitted). A storage appears only once it has been captured: the client
  exposes these containers only when the player opens them, so each key is the last known copy and is
  carried over between sessions like the bank. A storage never opened is absent; one opened while empty is `[]`.
  The whole field is absent when none has been captured. Capture times are in `sectionUpdated` as
  `storages.lootingBag`, `storages.seedVault` and `storages.groupStorage`.

### Carried-over sections and `sectionUpdated`

On login the plugin reloads the character's own previous file and keeps its `bank`, `inventory`, and
`equipment` until each is captured again. Inventory and equipment are re-captured almost at once; the
bank is re-captured only when the player opens it. So the bank in a file can be from an earlier session.

`sectionUpdated` maps each present item section (`bank`, `inventory`, `equipment`, and `storages.<name>`) to the ISO-8601
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

`points` (additive, int or null) is the total combat-achievement points. It is read from the game's points
varbit (`VarbitID.CA_POINTS`) when that is non-zero; otherwise it is computed as the sum of completed task
points (easy 1, medium 2, hard 3, elite 4, master 5, grandmaster 6, per
https://oldschool.runescape.wiki/w/Combat_Achievements), and is null if a completed task's tier is unresolved.

`tierCounts` (additive, object or null) maps each of `EASY`, `MEDIUM`, `HARD`, `ELITE`, `MASTER`,
`GRANDMASTER` to `{ completed, total }`, counted from `tasks` (so `total` is only complete once the cache
metadata has fully loaded; the full game has 41 / 64 / 89 / 166 / 173 / 122 tasks). It is null when no task
tier could be resolved.

Cache ids relied on (from the community cache dump tooling, e.g. ReinhardtR/runeprofile
`scripts/ts-scripts/lib/combat-achievements.ts`; not in the RuneLite API and NOT verified in-client):
tier enums 3981 to 3986 (easy to grandmaster; values are task struct ids), monster enum 3971
(monster category id to name), struct params 1306 (task id), 1308 (name), 1312 (monster category id).

## slayer

Present whenever slayer has been read. `points` and `streak` are always exported. When no task is assigned,
`creatureId` is 0, `amount` is 0, and `creatureName` and `bossName` are null.


| Field | Type | Notes |
|---|---|---|
| `creatureId` | int | Assigned-creature game id (98 is the generic boss task). |
| `creatureName` | string or null | Assigned-creature name, e.g. `Abyssal demons`. Null when it could not be resolved. |
| `bossName` | string or null | Specific boss for boss tasks. Null for every other task or when unresolved. |
| `amount` | int | Remaining kills. |
| `points` | int | Slayer reward points. |
| `streak` | int | Task-completion streak. |
| `masterId` | int | Current slayer master id (1 Turael, 2 Mazchna, 3 Vannaka, 4 Chaeldar, 5 Duradel, 6 Nieve, 7 Krystilia, 8 Konar, 9 Spria, 10 Mortimer); 0 when none. Absent in older files. |
| `masterName` | string or null | Master name for `masterId`; null when 0 or unknown. Absent in older files. |
| `blockedTasks` | array | Blocked tasks, `{ taskId, name, master }`. `name` is null when unresolved. `master` is the master whose block list holds the task, or null for the legacy shared list (used only when no per-master slot is filled). Empty when nothing is blocked. Absent in older files. |
| `unlockedTasks` | array of string | Purchased task-assignment unlocks, e.g. `BOSSES`, `RED_DRAGONS`, `SUPERIOR_MONSTERS`. Absent in older files. |
| `extendedTasks` | array of string | Tasks with a purchased extension, e.g. `ANKOU`, `BLACK_DRAGONS`. Absent in older files. |

## location

`{ regionId, x, y, plane, instanced }` — the character's current world point. Inside an instance (raids,
boss rooms, player-owned house) `x`, `y`, `plane` and `regionId` are mapped back to the template area the
instance was built from, and `instanced` is `true`. `instanced` is `false` (or absent in older files) in the
normal world. The location and `vitals.runEnergyPercent` are refreshed whenever the position or run energy
changes, at the usual save interval.

## grandExchange

Array of `{ slot, itemId, name, state, quantitySold, totalQuantity, price, spent }`. `state` is one
of `EMPTY`, `BUYING`, `BOUGHT`, `SELLING`, `SOLD`, `CANCELLED_BUY`, `CANCELLED_SELL` (empty slots
are not exported).

## collectionLog

`{ obtained, total, items[], pages }`. The game only exposes the log while its interface is open, so the
plugin captures each page the player views and keeps the last known data (also across sessions, with
`sectionUpdated.collectionLog` giving the last capture time). The section is absent until a page has
been viewed.

- `obtained` / `total`: the game's overall counters (varps) as of the last capture; 0 if unreadable.
- `items`: every obtained item seen so far, one item entry `{ itemId, name, quantity }` per item id.
- `pages`: map of page title (e.g. `Abyssal Sire`) to `{ total, obtained, items[] }`, where `total` is
  the number of item slots on the page and `items` its obtained items. Only pages the player has opened
  are present, so `items` can hold fewer entries than `obtained` until every page has been viewed.
## killCounts

`{ "<boss>": count }` — a JSON object of integers, sorted by key. The key is the boss name as RuneLite's
Chat Commands plugin stores it: lower-cased, colons removed, `Barrows chest` renamed to `barrows chests`
(e.g. `zulrah`, `vorkath`, `chambers of xeric`, `theatre of blood`). Raid keys hold completion counts.
The section is omitted until at least one count is known, and when the "Boss kill counts" toggle is off.

Sources, merged per boss:

1. The counts the built-in Chat Commands plugin keeps in the RuneScape-profile config (group `killcount`),
   read through `ConfigManager`. If that plugin is disabled its stored counts stop updating and go stale.
2. Kill-count lines in the game chat ("Your Zulrah kill count is: N"), parsed with the same pattern. A chat
   line always replaces the cached value, so counts stay current even when Chat Commands is disabled.
3. The previous export of the same character, loaded on login.

When merging a stored or previous value with the cache the larger wins (kill counts only grow). Only
bosses whose count the game has printed in chat since the account first ran either plugin appear; a boss
never killed (or never checked) is simply absent.
## clueCounts

`{ beginner, easy, medium, hard, elite, master }` — integers, completed treasure trails per tier, in that
order. Only tiers whose count is known are present; a tier absent from the object means "not seen yet", never
zero. The section is omitted when no tier is known, and when the "Clue scroll counts" toggle is off.

RuneLite's built-in plugins keep no clue completion count offline (Chat Commands queries the hiscores), so
the plugin reads the game's own line "You have completed N <tier> Treasure Trails." when a clue is handed
in. That line carries the new running total and replaces the cached value. Counts are carried over from the
previous export of the same character on login (the larger value wins), so a tier first appears after its
next completion on an account that has never been exported; clues completed before the plugin first ran stay
unknown until then.

## Versioning

`schemaVersion` is bumped whenever the shape changes. Additive changes (new optional fields) keep the
version where existing fields are unchanged; a removal or a semantic change bumps it. Consumers should
read `schemaVersion` and ignore unknown fields.
