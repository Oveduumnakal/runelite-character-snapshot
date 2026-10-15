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
| `storages` | object | Last known looting bag, seed vault, group storage, seed box, tackle box, plank sack, gravestone, Death's office, hunter/forestry kits, Dizana's quiver, boat cargo holds and barbarian knapsack contents. See below. |
| `toolLeprechaun` | object | Tool leprechaun storage, raw varbit values. See below. |
| `toolLeprechaunTotals` | object | Tool leprechaun contents decoded into Store-screen totals. See below. |
| `quests` | array | One entry per quest. |
| `questPoints` | int | Total quest points. |
| `achievementDiaries` | object | Map of region name → tier completion. |
| `combatAchievements` | object | Combat-achievement tier progress. |
| `slayer` | object | Slayer points, streak and current task (task fields are 0/null between tasks). |
| `location` | object | Region and world coordinates. |
| `grandExchange` | array | Active GE offers, by slot. |
| `collectionLog` | object | Collection log captured while the log is open, carried over between sessions. |
| `killCounts` | object | Boss kill counts keyed by lower-case boss name. |
| `activityCounts` | object | Optional. Agility laps and other non-boss running totals keyed by lower-case name. |
| `clueCounts` | object | Completed treasure trails per tier. |
| `timers` | object | Farming patch and birdhouse timers. |
| `dailies` | object | Daily rewards: unlocked and claimed today. See below. |
| `miscellania` | object | Kingdom of Miscellania approval and coffer. See below. |
| `minigamePoints` | object | Minigame reward-shop balances. See below. |
| `transport` | object | Fairy ring access and travel log, quetzal landing sites. See below. |
| `house` | object | Player-owned house fields, raw game values. See below. |
| `sailing` | object | Sailing boats, crew, charting and Barracuda Trials. See below. |

## player

| Field | Type | Notes |
|---|---|---|
| `username` | string | In-game display name. |
| `combatLevel` | int | |
| `world` | int | |
| `accountType` | string | `NORMAL`, `IRONMAN`, `ULTIMATE_IRONMAN`, `HARDCORE_IRONMAN`, `GROUP_IRONMAN`, `HARDCORE_GROUP_IRONMAN`; null when Account info is off. |
| `members` | boolean or null | Whether the current world is members'; null when Account info is off. |
| `spellbook` | string | Active spellbook: `STANDARD`, `ANCIENT`, `LUNAR`, `ARCEUUS`; null when unknown or Account info is off. |
| `unlockedPrayers` | string[] | Unlocked reward prayers: `RIGOUR`, `AUGURY`, `PRESERVE`, `DEADEYE`, `MYSTIC_VIGOUR`; `[]` when none; null when Account info is off. |
| `quickPrayers` | string[] | Prayers selected as quick prayers, by RuneLite `Prayer` name (e.g. `PROTECT_FROM_MELEE`, `PIETY`); ruinous prayers are not covered; null when Account info is off. |
| `autocastSpell` | string or null | Name of the autocast spell (e.g. `Fire Surge`); null when nothing is autocast or Account info is off. |
| `autocastDefensive` | boolean or null | Whether the autocast is defensive; null when nothing is autocast. |

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

- `storages` (additive, optional): `{ lootingBag, seedVault, groupStorage, seedBox, tackleBox, plankSack, gravestone, deathsOffice,
  huntsmansKit, forestryKit, dizanasQuiver, cargoHold1..cargoHold5, barbarianKnapsack }`,
  each an array of item entries (empty slots omitted). A storage appears only once it has been captured: the
  client exposes the container storages only when the player opens or checks them, so each key is the last
  known copy and is carried over between sessions like the bank. A storage never opened is absent; one opened
  while empty is `[]`. `plankSack` is read from the game's plank-sack varbits (plain, oak, teak, mahogany,
  camphor, ironwood, rosewood planks) and appears once the sack has held planks. `gravestone` holds the
  items in your gravestone and `deathsOffice` the items Death is keeping for you; both are captured whenever the
  game sends them (on death, at the grave, in Death's office) and kept until it sends them again, so an emptied
  grave reads `[]` only after the game reports it — check `sectionUpdated`. The gravestone timer is not
  exported. `huntsmansKit`, `forestryKit`, `dizanasQuiver` and `barbarianKnapsack` are plain item containers
  captured when opened. `cargoHold1` to `cargoHold5` are the cargo holds of the player's sailing boats, one flat
  key per boat slot (not a nested map). `barbarianKnapsack` is unverified: it is the game's
  `BARBARIAN_KNAPSACK` inventory (id 1012) and what it holds is not confirmed. The whole field is absent when
  none has been captured. Capture times are in `sectionUpdated` as `storages.<name>`.

- `toolLeprechaun` (additive, optional): map of tool → raw varbit value, every key present once anything is
  stored: `rake`, `extraRakes`, `seedDibber`, `extraSeedDibbers`, `spade`, `extraSpades`, `secateurs`,
  `magicSecateurs`, `extraSecateurs`, `wateringCan`, `trowel`, `extraTrowels`, `buckets`, `extraBuckets`,
  `extra2Buckets`, `compost`, `extraCompost`, `supercompost`, `extraSupercompost`, `ultracompost`,
  `plantCure`, `bottomlessBucketType`, `bottomlessBucketQuantity`. Values are the game's raw varbit values and
  are not decoded: 0/1 for single tools, a type index for `wateringCan` and `bottomlessBucketType`, counts
  otherwise, where an `extra*` key holds the higher part of a count that overflows its base key. Read live;
  absent when nothing is stored. Under the Storages toggle.

- `toolLeprechaunTotals` (additive, optional): the same contents decoded into the totals the leprechaun's Store
  screen shows, present exactly when `toolLeprechaun` is: `rakes`, `seedDibbers`, `spades`, `secateurs`,
  `trowels` (each `base + 2 × extra`), `magicSecateurs` (boolean: the stored secateurs are magic), `buckets`
  (`base + 32 × extra`), `compost` and `supercompost` (`base + 256 × extra`), `ultracompost` (base only),
  `plantCure` (raw count), `wateringCanCharges` (`value − 1`; absent when no can is stored) and
  `bottomlessBucket` (`{ type, uses }`, raw varbit values, absent when both are 0). Not decoded: the
  `extra2Buckets` weight (ignored, it has only ever been 0), the Gricoller's can value, and what the
  bottomless bucket `type` means. Example: `{ "rakes": 2, "spades": 3, "buckets": 77, "supercompost": 801,
  "wateringCanCharges": 7, ... }`. The raw `toolLeprechaun` map is unchanged. Under the Storages toggle.

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
plugin keeps the last known data (also across sessions, with `sectionUpdated.collectionLog` giving the
last capture time). The section is absent until a page has been viewed or a Search pass has run.

Two capture paths feed it:

- Page views: each page the player opens is stored under `pages` and its obtained items merged into `items`.
- Search pass: when the player clicks the log's Search, the game runs one script per obtained item (script
  4100). The plugin merges `{ itemId, quantity }` for each into `items`, so `items` is complete after one
  click even for pages never viewed. `pages` is not filled by this pass.

- `obtained` / `total`: the game's overall counters (varps) as of the last capture; 0 if unreadable.
- `items`: every obtained item seen so far, one item entry `{ itemId, name, quantity }` per item id.
  Complete once a Search pass has run; before that it holds only the items on viewed pages.
- `pages`: map of page title (e.g. `Abyssal Sire`) to `{ total, obtained, items[] }`, where `total` is
  the number of item slots on the page and `items` its obtained items. Only pages the player has opened
  are present, regardless of Search passes.

## killCounts

`{ "<boss>": count }` — a JSON object of integers, sorted by key. The key is the boss name as RuneLite's
Chat Commands plugin stores it: lower-cased, colons removed, `Barrows chest` renamed to `barrows chests`
(e.g. `zulrah`, `vorkath`, `chambers of xeric`, `theatre of blood`). Raid keys hold completion counts.
The section is omitted until at least one count is known, and when the "Boss kill counts" toggle is off.

Sources, in priority order:

1. The game's own boss kill-count varps (`VarPlayerID.TOTAL_*_KILLS`, e.g. Brutus, Tempoross, Zulrah,
   Vorkath, the god wars bosses, Nex, the Moons-era bosses). These are exact and current, need no chat or
   other plugin, and are exported only when above 0. A varp value always wins for its boss. Keys are the
   hiscores boss names, lower-cased (e.g. `brutus`, `brutus (hard mode)`, `tempoross`,
   `leviathan (awakened)`, `guardians of the rift`).
2. Fallback for bosses and raids with no varp (e.g. `chambers of xeric`, `theatre of blood`,
   `tombs of amascut`): the counts the built-in Chat Commands plugin keeps in the RuneScape-profile config
   (group `killcount`), and kill-count lines in the game chat ("Your Zulrah kill count is: N"). A chat line
   replaces the cached fallback value; neither ever overrides a varp value.

## activityCounts

`{ "<activity>": count }` — optional object of integers, sorted by key, from the same Chat Commands and
chat sources as the fallback above, holding what is not a boss kill count: agility course laps
(`canifis rooftop`, `agility pyramid`, ...), Hallowed Sepulchre floors, chests, hunter rumours, TzHaar
challenges. Older exports kept these inside `killCounts`; on load they are moved here. Omitted when empty
and when the "Boss kill counts" toggle is off.

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
## timers

`{ farming: [...], birdhouses: [...] }`. **Only as fresh as the player's last visit.** The game sends a
patch's state only while the player is near it, so the plugin cannot see a patch it has not been near. The
data is RuneLite's built-in Time Tracking plugin's: it records each patch (and birdhouse space) when you
walk past, and this plugin decodes that record read-only through `ConfigManager` (group `timetracking`,
keys `<regionId>.<varbit>` and `birdhouse.<varp>`, values `<value>:<unixSeconds>`) and projects the growth
forward to now with the same tick maths. Consequences a consumer must respect:

- If Time Tracking is disabled, or has never seen a patch, that patch is simply absent (no data), and a
  disabled plugin stops updating the rest. When nothing is known `timers` is omitted.
- Every entry carries `lastChecked`, the instant its state was last recorded. A crop planted, harvested or
  killed by disease after that is not reflected until the player is next near the patch.
- Harvestable is a prediction for a growing crop: `state` becomes `HARVESTABLE` once `readyAt` has passed,
  even though the game has not shown it yet. Disease can still strike a crop between visits.

`farming[]` entries (one per patch with stored data, including compost bins, in a fixed table order):

| Field | Type | Notes |
|---|---|---|
| `patch` | string | e.g. `Catherby herb`, `Ardougne allotment (North)`. |
| `region` | string | e.g. `Catherby`. |
| `type` | string | `herb`, `allotment`, `flower`, `bush`, `fruit tree`, `tree`, `hardwood tree`, `hops`, `cactus`, `compost`, ... |
| `crop` | string | e.g. `Ranarr`; `Weeds` for an empty or overgrown patch. |
| `state` | string | `GROWING`, `HARVESTABLE`, `DISEASED`, `DEAD`, `EMPTY`, `FILLING` (compost bin filling). Weeds read `EMPTY`. |
| `stage` / `stages` | int | Projected growth stage and the stage count of the current phase. |
| `readyAt` | string or absent | ISO-8601 instant the crop finishes (or finished) growing; present only for a growing crop. |
| `lastChecked` | string | ISO-8601 instant the state was last recorded. |

`birdhouses[]` entries (one per space with stored data):

| Field | Type | Notes |
|---|---|---|
| `space` | string | `Mushroom Meadow (North)`, `Mushroom Meadow (South)`, `Verdant Valley (Northeast)`, `Verdant Valley (Southwest)`. |
| `house` | string or absent | Birdhouse type, e.g. `Oak Bird House`; absent when the space is empty. |
| `state` | string | `EMPTY`, `BUILT` (built, not seeded), `SEEDED`, `READY` (seeded and 50 minutes elapsed), `UNKNOWN`. |
| `readyAt` | string or absent | ISO-8601 instant a seeded birdhouse finishes (50 minutes after it was recorded seeded). |
| `lastChecked` | string | ISO-8601 instant the state was last recorded. |

Decoding tables (patch list, varbit value to crop/state/stage, growth timings) are copied from the Time
Tracking plugin as of RuneLite 1.13.1 (BSD 2-Clause, headers in `FarmingData.java`); a RuneLite update that
adds crops or patches leaves those patches out of `farming` until the tables are refreshed. The tables were
verified identical to the 1.13.1 client's own (patches, crop timings, and every varbit value 0 to 1023).

## dailies / miscellania

`dailies` maps a reward key to `{ unlocked, claimed, claimedCount?, max? }`, in this order: `herbBoxes`
(NMZ, 15 a day, never for ironmen), `battlestaves` (Zaff, Varrock easy), `pureEssence` (Wizard Cromperty,
Ardougne medium), `randomRunes` (Lundail, Wilderness easy), `sand` (Bert, The Hand in the Sand, not for
ultimate ironmen), `bowstrings` (Flax keeper, Kandarin easy), `ogreArrows` (Rantz, Western easy), `bonemeal`
(Robin, 13 per Morytania tier from medium), `dynamite` (Thirus, Kourend medium). `claimedCount` and `max` are
set only for herb boxes and bonemeal. The game clears claims at 00:00 UTC, so a `claimed: true` read before
the reset is stale after it — compare with `lastUpdated`. Conditions mirror RuneLite's Daily Task
Indicators plugin. Absent when the Daily rewards toggle is off.

`miscellania` is `{ approvalPercent, coffer, lastChanged }`, present once Throne of Miscellania is done. The
game only sends the values inside the kingdom, so away from it the values RuneLite's Kingdom of Miscellania
plugin stored on the last visit are used, with that visit's `lastChanged`; `lastChanged` is null when the
values were read live and the plugin has no matching record. Approval and coffer decay daily after
`lastChanged`.

## minigamePoints

Map of currency → balance, every key always present (0 when none), in this order: `nightmareZone`,
`giantsFoundry`, `mixologyMox`, `mixologyAga`, `mixologyLye`, `soulWarsZeal`, `valeTotemsResearch`,
`titheFarm`, `temporossPermits`, `museumKudos` (Varrock Museum), `pvpArena`. Each is one named game variable
read live. Pest Control, Mahogany Homes, Last Man Standing, Guardians of the Rift and Barbarian Assault are
not exported: their currency has no clearly named variable. Absent when the Minigame points toggle is off.

## transport

`{ fairyRings, fairyRingLog, quetzalSites }`. `fairyRings` (boolean) is whether the player may use fairy
rings (the game's fairy ring permission varbit). `fairyRingLog` lists the codes recorded in the fairy ring
travel log (e.g. `AJR`, `CKS`), alphabetical, out of the 55 codes the log tracks. `quetzalSites` lists the
quetzal landing sites whose varbit is set (Civitas illa Fortis, The Teomat, Sunset Coast, Hunter Guild, Cam
Torum, Colossal Wyrm Remains, Outer Fortis, Fortis Colosseum, Aldarin, Quetzacalli Gorge, Salvager Overlook,
Tal Teklan, Auburn Valley, Kastori). Transports gated only by quests or diaries (spirit trees, gnome gliders,
charter ships) are not repeated here; read them from `quests` and `achievementDiaries`. Sailing ports are under
`sailing`. Absent when the Transport unlocks toggle is off.

## house

`{ location, style, size, servant, jewelleryBox, nexusTeleports }`, present once the player owns a house
(`POH_HOUSE_LOCATION` > 0). **All values are the game's raw varbit values and are not decoded yet**:
`location` (`POH_HOUSE_LOCATION`), `style` (`POH_HOUSE_STYLE`), `size` (`POH_HOUSE_SIZE`), `servant`
(`POH_SERVANT_TYPE`, 0 = none), `jewelleryBox` (`POH_JEWELLERYBOX_MULTI`, 0 = none built; higher values are
expected to be basic, fancy, ornate), and `nexusTeleports`, the portal nexus teleport slots 1 to 45 whose
`POH_NEXUS_TELE_<n>` varbit is set. Room contents (portal chamber, pool, altar, spirit tree) have no varbits
and are not exported. Absent when the Player-owned house toggle is off.

## sailing

`{ boats, crew, chartingTasksCompleted, chartingEntries, barracudaTrials }`, read live from Sailing's named
game variables.

- `boats[]`: one entry per owned boat slot (`SAILING_BOAT_<n>_OWNED` set): `{ slot, type, port, hull, keel,
  sail, steering, hp, maxHp }`. `slot` is 1 to 5; `hp`/`maxHp` are the stored hitpoints. **`type`, `port` and
  the part fields are raw game ids, not decoded yet.**
- `crew[]`: unlocked crew members: `generic1` to `generic5`, `werewolf`, `fremennik`, `spiritAngler`,
  `captainSiad`, `ghostJenkins`.
- `chartingTasksCompleted`: set bits across the twelve `SAILING_CHARTING_COMPLETION_<n>` varps, i.e. charting
  tasks completed. `chartingEntries` is raw `SAILING_CHARTING_TOTAL_ENTRIES`.
- `barracudaTrials`: raw `SAILING_BT_PERFORMANCE_1..5` values (trial performance; encoding not decoded yet).
  Barracuda Trials completion counts are in `activityCounts` (`tempor tantrum`, `jubbly jive`, `gwenith glide`).

Absent when the Sailing toggle is off.

## Versioning

`schemaVersion` is bumped whenever the shape changes. Additive changes (new optional fields) keep the
version where existing fields are unchanged; a removal or a semantic change bumps it. Consumers should
read `schemaVersion` and ignore unknown fields.
