# Stockpile — JavaDoc Reference

<!-- GENERATED FILE — DO NOT EDIT BY HAND.
     Run `./gradlew generateJavaDocs` and commit the result. -->

## Contents

- [com.oveduumnakal.charactersnapshot.CharacterSnapshotConfig](#comoveduumnakalcharactersnapshotcharactersnapshotconfig)
- [com.oveduumnakal.charactersnapshot.CharacterSnapshotPlugin](#comoveduumnakalcharactersnapshotcharactersnapshotplugin)
- [com.oveduumnakal.charactersnapshot.DiaryTasks](#comoveduumnakalcharactersnapshotdiarytasks)
- [com.oveduumnakal.charactersnapshot.DiaryTasks.Task](#comoveduumnakalcharactersnapshotdiarytaskstask)
- [com.oveduumnakal.charactersnapshot.SlayerInfo](#comoveduumnakalcharactersnapshotslayerinfo)
- [com.oveduumnakal.charactersnapshot.SnapshotCollector](#comoveduumnakalcharactersnapshotsnapshotcollector)
- [com.oveduumnakal.charactersnapshot.SnapshotWriter](#comoveduumnakalcharactersnapshotsnapshotwriter)
- [com.oveduumnakal.charactersnapshot.model.CharacterSnapshot](#comoveduumnakalcharactersnapshotmodelcharactersnapshot)
- [com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BankData](#comoveduumnakalcharactersnapshotmodelcharactersnapshotbankdata)
- [com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BankTab](#comoveduumnakalcharactersnapshotmodelcharactersnapshotbanktab)
- [com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BlockedTask](#comoveduumnakalcharactersnapshotmodelcharactersnapshotblockedtask)
- [com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.CombatAchievementData](#comoveduumnakalcharactersnapshotmodelcharactersnapshotcombatachievementdata)
- [com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.CombatAchievementTask](#comoveduumnakalcharactersnapshotmodelcharactersnapshotcombatachievementtask)
- [com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.DiaryRegion](#comoveduumnakalcharactersnapshotmodelcharactersnapshotdiaryregion)
- [com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.DiaryTask](#comoveduumnakalcharactersnapshotmodelcharactersnapshotdiarytask)
- [com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.GeOffer](#comoveduumnakalcharactersnapshotmodelcharactersnapshotgeoffer)
- [com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.InventoryItem](#comoveduumnakalcharactersnapshotmodelcharactersnapshotinventoryitem)
- [com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.ItemEntry](#comoveduumnakalcharactersnapshotmodelcharactersnapshotitementry)
- [com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.LocationData](#comoveduumnakalcharactersnapshotmodelcharactersnapshotlocationdata)
- [com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.PlayerInfo](#comoveduumnakalcharactersnapshotmodelcharactersnapshotplayerinfo)
- [com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.QuestEntry](#comoveduumnakalcharactersnapshotmodelcharactersnapshotquestentry)
- [com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.SkillEntry](#comoveduumnakalcharactersnapshotmodelcharactersnapshotskillentry)
- [com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.SlayerData](#comoveduumnakalcharactersnapshotmodelcharactersnapshotslayerdata)
- [com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.TierCount](#comoveduumnakalcharactersnapshotmodelcharactersnapshottiercount)
- [com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.VitalsData](#comoveduumnakalcharactersnapshotmodelcharactersnapshotvitalsdata)

---

## com.oveduumnakal.charactersnapshot.CharacterSnapshotConfig

_interface_

`public interface CharacterSnapshotConfig`

Configuration for the Character Snapshot plugin: the save location and interval, and a toggle
for each category of data written to the export file.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `String` | `GROUP` | Config group key, shared with `ConfigChanged` handling. |
| `String` | `dataSection` | Data section: which categories of data to include. |
| `String` | `storageSection` | Storage section: where and how often data is written. |

### Method Summary

| Modifier and Type | Method | Description |
|---|---|---|
| `default String` | `saveDirectory()` |  |
| `default boolean` | `syncAccountInfo()` |  |
| `default boolean` | `syncBank()` |  |
| `default boolean` | `syncCombatAchievements()` |  |
| `default boolean` | `syncDiaries()` |  |
| `default boolean` | `syncEquipment()` |  |
| `default boolean` | `syncGrandExchange()` |  |
| `default int` | `syncIntervalSeconds()` |  |
| `default boolean` | `syncInventory()` |  |
| `default boolean` | `syncLocation()` |  |
| `default boolean` | `syncQuests()` |  |
| `default boolean` | `syncRunePouch()` |  |
| `default boolean` | `syncSkills()` |  |
| `default boolean` | `syncSlayer()` |  |
| `default boolean` | `syncVitals()` |  |

### Field Detail

#### GROUP

`String GROUP`

Config group key, shared with `ConfigChanged` handling.

#### dataSection

`String dataSection`

Data section: which categories of data to include.

#### storageSection

`String storageSection`

Storage section: where and how often data is written.

### Method Detail

#### saveDirectory

`default String saveDirectory()`

- **Returns:** a custom directory to write export files into, or blank to use the default
        (`.runelite/osrs-companion`)

#### syncAccountInfo

`default boolean syncAccountInfo()`

- **Returns:** whether to include account type, membership, and world

#### syncBank

`default boolean syncBank()`

- **Returns:** whether to include bank contents (captured when the bank is opened)

#### syncCombatAchievements

`default boolean syncCombatAchievements()`

- **Returns:** whether to include combat-achievement tier progress

#### syncDiaries

`default boolean syncDiaries()`

- **Returns:** whether to include achievement diary completion

#### syncEquipment

`default boolean syncEquipment()`

- **Returns:** whether to include currently worn equipment

#### syncGrandExchange

`default boolean syncGrandExchange()`

- **Returns:** whether to include active Grand Exchange offers

#### syncIntervalSeconds

`default int syncIntervalSeconds()`

- **Returns:** how often, in seconds, updated data is written to disk (minimum 30)

#### syncInventory

`default boolean syncInventory()`

- **Returns:** whether to include the current inventory contents

#### syncLocation

`default boolean syncLocation()`

- **Returns:** whether to include the character's world location

#### syncQuests

`default boolean syncQuests()`

- **Returns:** whether to include quest completion state and total quest points

#### syncRunePouch

`default boolean syncRunePouch()`

- **Returns:** whether to include the rune pouch contents

#### syncSkills

`default boolean syncSkills()`

- **Returns:** whether to include skill levels, experience, and boosted levels

#### syncSlayer

`default boolean syncSlayer()`

- **Returns:** whether to include the current slayer task

#### syncVitals

`default boolean syncVitals()`

- **Returns:** whether to include current hitpoints, prayer, and run energy

---

## com.oveduumnakal.charactersnapshot.CharacterSnapshotPlugin

_class_

`public class CharacterSnapshotPlugin`

Writes a live snapshot of the logged-in character to a local JSON file for external tools. State is
collected on the client thread from events and periodic polling, and written off-thread. The plugin
makes no network calls of any kind.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `private static final int` | `INITIAL_DELAY_TICKS` |  |
| `private static final int` | `POLL_INTERVAL_TICKS` |  |
| `private Client` | `client` |  |
| `private SnapshotCollector` | `collector` |  |
| `private CharacterSnapshotConfig` | `config` |  |
| `private volatile boolean` | `dirty` |  |
| `private ScheduledExecutorService` | `executor` |  |
| `private Gson` | `gson` |  |
| `private boolean` | `initialCollectionDone` |  |
| `private String` | `lastPlayerName` |  |
| `private int` | `syncTickThreshold` |  |
| `private int` | `tickCounter` |  |
| `private SnapshotWriter` | `writer` |  |

### Method Summary

| Modifier and Type | Method | Description |
|---|---|---|
| `private void` | `doFullCollection()` | Runs a one-off full collection of every enabled data source after login. |
| `private void` | `doSave()` | Builds a snapshot on the client thread and writes it off-thread. |
| `public void` | `onConfigChanged(ConfigChanged event)` | Recomputes the save interval when this plugin's configuration changes. |
| `public void` | `onGameStateChanged(GameStateChanged event)` | Schedules the initial collection on login and clears state on logout or world hop so one character's data cannot leak into another's file. |
| `public void` | `onGameTick(GameTick event)` | Drives the initial collection, periodic polling, and interval-gated saves. |
| `public void` | `onGrandExchangeOfferChanged(GrandExchangeOfferChanged event)` | Caches a changed Grand Exchange offer. |
| `public void` | `onItemContainerChanged(ItemContainerChanged event)` | Caches bank, inventory, or equipment changes. |
| `public void` | `onStatChanged(StatChanged event)` | Caches a changed skill (real, experience, and boosted levels). |
| `private void` | `pollPeriodic()` | Polls the data sources that are not event-driven, marking the snapshot dirty on any change. |
| `CharacterSnapshotConfig` | `provideConfig(ConfigManager configManager)` | Provides the plugin configuration. |
| `private void` | `recalcSyncThreshold()` | Recomputes the save threshold in game ticks (one tick is about 0.6 seconds). |
| `private File` | `resolveSyncDir()` | Resolves the export directory: the configured folder if set, else the default under the RuneLite directory. |
| `protected void` | `shutDown()` | Writes a final snapshot when still logged in, then releases resources. |
| `protected void` | `startUp()` | Initializes the collector and writer and computes the save interval. |

### Field Detail

#### INITIAL_DELAY_TICKS

`private static final int INITIAL_DELAY_TICKS`

#### POLL_INTERVAL_TICKS

`private static final int POLL_INTERVAL_TICKS`

#### client

`private Client client`

#### collector

`private SnapshotCollector collector`

#### config

`private CharacterSnapshotConfig config`

#### dirty

`private volatile boolean dirty`

#### executor

`private ScheduledExecutorService executor`

#### gson

`private Gson gson`

#### initialCollectionDone

`private boolean initialCollectionDone`

#### lastPlayerName

`private String lastPlayerName`

#### syncTickThreshold

`private int syncTickThreshold`

#### tickCounter

`private int tickCounter`

#### writer

`private SnapshotWriter writer`

### Method Detail

#### doFullCollection

`private void doFullCollection()`

Runs a one-off full collection of every enabled data source after login.

#### doSave

`private void doSave()`

Builds a snapshot on the client thread and writes it off-thread. Clears the dirty flag
optimistically and re-sets it if the write fails, so a failed write is retried.

#### onConfigChanged

`public void onConfigChanged(ConfigChanged event)`

Recomputes the save interval when this plugin's configuration changes.

- **Parameter** `event` — the config-changed event

#### onGameStateChanged

`public void onGameStateChanged(GameStateChanged event)`

Schedules the initial collection on login and clears state on logout or world hop so one
character's data cannot leak into another's file. The initial collection restores the
character's own last known items from its previous export.

- **Parameter** `event` — the game-state event

#### onGameTick

`public void onGameTick(GameTick event)`

Drives the initial collection, periodic polling, and interval-gated saves.

- **Parameter** `event` — the game-tick event

#### onGrandExchangeOfferChanged

`public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged event)`

Caches a changed Grand Exchange offer.

- **Parameter** `event` — the offer-changed event

#### onItemContainerChanged

`public void onItemContainerChanged(ItemContainerChanged event)`

Caches bank, inventory, or equipment changes.

- **Parameter** `event` — the container-changed event

#### onStatChanged

`public void onStatChanged(StatChanged event)`

Caches a changed skill (real, experience, and boosted levels).

- **Parameter** `event` — the stat-changed event

#### pollPeriodic

`private void pollPeriodic()`

Polls the data sources that are not event-driven, marking the snapshot dirty on any change.

#### provideConfig

`CharacterSnapshotConfig provideConfig(ConfigManager configManager)`

Provides the plugin configuration.

- **Parameter** `configManager` — the config manager
- **Returns:** the bound configuration

#### recalcSyncThreshold

`private void recalcSyncThreshold()`

Recomputes the save threshold in game ticks (one tick is about 0.6 seconds).

#### resolveSyncDir

`private File resolveSyncDir()`

Resolves the export directory: the configured folder if set, else the default under the
RuneLite directory.

- **Returns:** the directory to write export files into

#### shutDown

`protected void shutDown()`

Writes a final snapshot when still logged in, then releases resources.

#### startUp

`protected void startUp()`

Initializes the collector and writer and computes the save interval.

---

## com.oveduumnakal.charactersnapshot.DiaryTasks

_class_

`public final class DiaryTasks`

Table of the individual achievement-diary tasks and where the game stores whether each is done.

<p>RuneLite exposes no per-task API, so each task is read straight from client state in one of two
ways: a single bit of a diary varp (`Task#isComplete(Client)` tests `(varp >> bit) & 1`),
or a Karamja varbit that counts up from zero and is complete once it reaches a minimum value.
A task is only listed when its storage location is known from a source; unsourced tasks are left
out rather than guessed.

<p><b>Source.</b> The task names, varps, bit positions, and Karamja varbits come from the Quest Helper
RuneLite plugin (https://github.com/Zoinkwiz/quest-helper, BSD 2-Clause licence), package
`com.questhelper.helpers.achievementdiaries`, where each task's "not done" requirement is a
`VarplayerRequirement(VarPlayerID.*_ACHIEVEMENT_DIARY[2], false, bit)` or a
`VarbitRequirement(VarbitID.ATJUN_*, ...)` and the task is titled by its panel. Quest Helper's
panel titles are terse; the Falador task names were rewritten in plain English from that plugin's
step text. Quest Helper's copyright notice and BSD 2-Clause licence terms apply to this derived table.

<p>The table holds 492 tasks, matching the 492 tasks the wiki lists across the twelve diaries. Within a
tier, varp tasks are ordered by varp then bit, which is not the in-game journal order.

### Nested Type Summary

| Type | Description |
|---|---|
| _class_ [`Task`](#comoveduumnakalcharactersnapshotdiarytaskstask) | One diary task and the client state that says whether it is complete. |

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `private static final int` | `ARDOUGNE_1` | Varp `ARDOUNGE_ACHIEVEMENT_DIARY`. |
| `private static final int` | `ARDOUGNE_2` | Varp `ARDOUNGE_ACHIEVEMENT_DIARY2`. |
| `private static final int` | `DESERT_1` | Varp `DESERT_ACHIEVEMENT_DIARY`. |
| `private static final int` | `DESERT_2` | Varp `DESERT_ACHIEVEMENT_DIARY2`. |
| `private static final int` | `FALADOR_1` | Varp `FALADOR_ACHIEVEMENT_DIARY`. |
| `private static final int` | `FALADOR_2` | Varp `FALADOR_ACHIEVEMENT_DIARY2`. |
| `private static final int` | `FREMENNIK_1` | Varp `FREMENNIK_ACHIEVEMENT_DIARY`. |
| `private static final int` | `FREMENNIK_2` | Varp `FREMENNIK_ACHIEVEMENT_DIARY2`. |
| `private static final int` | `KANDARIN_1` | Varp `KANDARIN_ACHIEVEMENT_DIARY`. |
| `private static final int` | `KANDARIN_2` | Varp `KANDARIN_ACHIEVEMENT_DIARY2`. |
| `private static final int` | `KARAMJA_4` | Varp `ATJUN_TASKS_4`. |
| `private static final int` | `KOUREND_1` | Varp `KOUREND_ACHIEVEMENT_DIARY`. |
| `private static final int` | `KOUREND_2` | Varp `KOUREND_ACHIEVEMENT_DIARY2`. |
| `private static final int` | `LUMBRIDGE_1` | Varp `LUMB_DRAY_ACHIEVEMENT_DIARY`. |
| `private static final int` | `LUMBRIDGE_2` | Varp `LUMB_DRAY_ACHIEVEMENT_DIARY2`. |
| `private static final int` | `MORYTANIA_1` | Varp `MORYTANIA_ACHIEVEMENT_DIARY`. |
| `private static final int` | `MORYTANIA_2` | Varp `MORYTANIA_ACHIEVEMENT_DIARY2`. |
| `private static final Map<String,Map<String,List<Task>>>` | `TABLE` | Region name to tier to tasks, in the same region order as the tier booleans. |
| `public static final List<String>` | `TIERS` | Tier keys in easy to elite order. |
| `private static final int` | `VARROCK_1` | Varp `VARROCK_ACHIEVEMENT_DIARY`. |
| `private static final int` | `VARROCK_2` | Varp `VARROCK_ACHIEVEMENT_DIARY2`. |
| `private static final int` | `WESTERN_1` | Varp `WESTERN_ACHIEVEMENT_DIARY`. |
| `private static final int` | `WESTERN_2` | Varp `WESTERN_ACHIEVEMENT_DIARY2`. |
| `private static final int` | `WILDERNESS_1` | Varp `WILDERNESS_ACHIEVEMENT_DIARY`. |
| `private static final int` | `WILDERNESS_2` | Varp `WILDERNESS_ACHIEVEMENT_DIARY2`. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `DiaryTasks()` |  |

### Method Summary

| Modifier and Type | Method | Description |
|---|---|---|
| `private static Map<String,List<Task>>` | `ardougne()` | Tasks of the Ardougne diary, by tier. |
| `private static Map<String,Map<String,List<Task>>>` | `buildTable()` |  |
| `private static Map<String,List<Task>>` | `desert()` | Tasks of the Desert diary, by tier. |
| `private static Map<String,List<Task>>` | `falador()` | Tasks of the Falador diary, by tier. |
| `private static Map<String,List<Task>>` | `fremennik()` | Tasks of the Fremennik diary, by tier. |
| `private static Map<String,List<Task>>` | `kandarin()` | Tasks of the Kandarin diary, by tier. |
| `private static Map<String,List<Task>>` | `karamja()` | Tasks of the Karamja diary, by tier. |
| `private static Map<String,List<Task>>` | `kourend()` | Tasks of the Kourend diary, by tier. |
| `private static Map<String,List<Task>>` | `lumbridge()` | Tasks of the Lumbridge diary, by tier. |
| `private static Map<String,List<Task>>` | `morytania()` | Tasks of the Morytania diary, by tier. |
| `public static Map<String,List<Task>>` | `tasks(String region)` | Returns the known tasks of a region. |
| `private static Task` | `varbit(String name, int varbit, int minValue)` |  |
| `private static Task` | `varp(String name, int varp, int bit)` |  |
| `private static Map<String,List<Task>>` | `varrock()` | Tasks of the Varrock diary, by tier. |
| `private static Map<String,List<Task>>` | `western()` | Tasks of the Western diary, by tier. |
| `private static Map<String,List<Task>>` | `wilderness()` | Tasks of the Wilderness diary, by tier. |

### Field Detail

#### ARDOUGNE_1

`private static final int ARDOUGNE_1`

Varp `ARDOUNGE_ACHIEVEMENT_DIARY`.

#### ARDOUGNE_2

`private static final int ARDOUGNE_2`

Varp `ARDOUNGE_ACHIEVEMENT_DIARY2`.

#### DESERT_1

`private static final int DESERT_1`

Varp `DESERT_ACHIEVEMENT_DIARY`.

#### DESERT_2

`private static final int DESERT_2`

Varp `DESERT_ACHIEVEMENT_DIARY2`.

#### FALADOR_1

`private static final int FALADOR_1`

Varp `FALADOR_ACHIEVEMENT_DIARY`.

#### FALADOR_2

`private static final int FALADOR_2`

Varp `FALADOR_ACHIEVEMENT_DIARY2`.

#### FREMENNIK_1

`private static final int FREMENNIK_1`

Varp `FREMENNIK_ACHIEVEMENT_DIARY`.

#### FREMENNIK_2

`private static final int FREMENNIK_2`

Varp `FREMENNIK_ACHIEVEMENT_DIARY2`.

#### KANDARIN_1

`private static final int KANDARIN_1`

Varp `KANDARIN_ACHIEVEMENT_DIARY`.

#### KANDARIN_2

`private static final int KANDARIN_2`

Varp `KANDARIN_ACHIEVEMENT_DIARY2`.

#### KARAMJA_4

`private static final int KARAMJA_4`

Varp `ATJUN_TASKS_4`.

#### KOUREND_1

`private static final int KOUREND_1`

Varp `KOUREND_ACHIEVEMENT_DIARY`.

#### KOUREND_2

`private static final int KOUREND_2`

Varp `KOUREND_ACHIEVEMENT_DIARY2`.

#### LUMBRIDGE_1

`private static final int LUMBRIDGE_1`

Varp `LUMB_DRAY_ACHIEVEMENT_DIARY`.

#### LUMBRIDGE_2

`private static final int LUMBRIDGE_2`

Varp `LUMB_DRAY_ACHIEVEMENT_DIARY2`.

#### MORYTANIA_1

`private static final int MORYTANIA_1`

Varp `MORYTANIA_ACHIEVEMENT_DIARY`.

#### MORYTANIA_2

`private static final int MORYTANIA_2`

Varp `MORYTANIA_ACHIEVEMENT_DIARY2`.

#### TABLE

`private static final Map<String,Map<String,List<Task>>> TABLE`

Region name to tier to tasks, in the same region order as the tier booleans.

#### TIERS

`public static final List<String> TIERS`

Tier keys in easy to elite order.

#### VARROCK_1

`private static final int VARROCK_1`

Varp `VARROCK_ACHIEVEMENT_DIARY`.

#### VARROCK_2

`private static final int VARROCK_2`

Varp `VARROCK_ACHIEVEMENT_DIARY2`.

#### WESTERN_1

`private static final int WESTERN_1`

Varp `WESTERN_ACHIEVEMENT_DIARY`.

#### WESTERN_2

`private static final int WESTERN_2`

Varp `WESTERN_ACHIEVEMENT_DIARY2`.

#### WILDERNESS_1

`private static final int WILDERNESS_1`

Varp `WILDERNESS_ACHIEVEMENT_DIARY`.

#### WILDERNESS_2

`private static final int WILDERNESS_2`

Varp `WILDERNESS_ACHIEVEMENT_DIARY2`.

### Constructor Detail

#### DiaryTasks

`private DiaryTasks()`

### Method Detail

#### ardougne

`private static Map<String,List<Task>> ardougne()`

Tasks of the Ardougne diary, by tier.

- **Returns:** the tiers in easy to elite order

#### buildTable

`private static Map<String,Map<String,List<Task>>> buildTable()`

#### desert

`private static Map<String,List<Task>> desert()`

Tasks of the Desert diary, by tier.

- **Returns:** the tiers in easy to elite order

#### falador

`private static Map<String,List<Task>> falador()`

Tasks of the Falador diary, by tier.

- **Returns:** the tiers in easy to elite order

#### fremennik

`private static Map<String,List<Task>> fremennik()`

Tasks of the Fremennik diary, by tier.

- **Returns:** the tiers in easy to elite order

#### kandarin

`private static Map<String,List<Task>> kandarin()`

Tasks of the Kandarin diary, by tier.

- **Returns:** the tiers in easy to elite order

#### karamja

`private static Map<String,List<Task>> karamja()`

Tasks of the Karamja diary, by tier.

- **Returns:** the tiers in easy to elite order

#### kourend

`private static Map<String,List<Task>> kourend()`

Tasks of the Kourend diary, by tier.

- **Returns:** the tiers in easy to elite order

#### lumbridge

`private static Map<String,List<Task>> lumbridge()`

Tasks of the Lumbridge diary, by tier.

- **Returns:** the tiers in easy to elite order

#### morytania

`private static Map<String,List<Task>> morytania()`

Tasks of the Morytania diary, by tier.

- **Returns:** the tiers in easy to elite order

#### tasks

`public static Map<String,List<Task>> tasks(String region)`

Returns the known tasks of a region.

- **Parameter** `region` — region key such as `VARROCK`
- **Returns:** tier key (`easy`, `medium`, `hard`, `elite`) to its tasks, or an
        empty map when no task of the region is mapped

#### varbit

`private static Task varbit(String name, int varbit, int minValue)`

#### varp

`private static Task varp(String name, int varp, int bit)`

#### varrock

`private static Map<String,List<Task>> varrock()`

Tasks of the Varrock diary, by tier.

- **Returns:** the tiers in easy to elite order

#### western

`private static Map<String,List<Task>> western()`

Tasks of the Western diary, by tier.

- **Returns:** the tiers in easy to elite order

#### wilderness

`private static Map<String,List<Task>> wilderness()`

Tasks of the Wilderness diary, by tier.

- **Returns:** the tiers in easy to elite order

---

## com.oveduumnakal.charactersnapshot.DiaryTasks.Task

_class_

`public static final class Task`

One diary task and the client state that says whether it is complete.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `private final int` | `arg` |  |
| `private final int` | `id` |  |
| `public final String` | `name` | Task text shown in the export. |
| `private final boolean` | `varbit` |  |

### Constructor Summary

| Constructor | Description |
|---|---|
| `Task(String name, boolean varbit, int id, int arg)` |  |

### Method Summary

| Modifier and Type | Method | Description |
|---|---|---|
| `public boolean` | `isComplete(Client client)` | Reads this task's completion from the client. |

### Field Detail

#### arg

`private final int arg`

#### id

`private final int id`

#### name

`public final String name`

Task text shown in the export.

#### varbit

`private final boolean varbit`

### Constructor Detail

#### Task

`private Task(String name, boolean varbit, int id, int arg)`

### Method Detail

#### isComplete

`public boolean isComplete(Client client)`

Reads this task's completion from the client.

- **Parameter** `client` — the game client
- **Returns:** whether the task is complete

---

## com.oveduumnakal.charactersnapshot.SlayerInfo

_class_

`final class SlayerInfo`

Static tables describing where the game stores the slayer master, per-master blocked task slots and the
task-assignment reward unlocks. Master ids follow `VarbitID.SLAYER_MASTER`; ids 1 to 10 were matched
against the slayer master NPC constants in the API (`NpcID.SLAYER_MASTER_n`).

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `static final Map<String,int[]>` | `BLOCK_VARBITS` | Master name to the varbits holding that master's blocked task ids (0 = empty slot). |
| `static final Map<String,Integer>` | `EXTEND_VARBITS` | Task key to the varbit that is non-zero once that task's extension is purchased. |
| `static final int[]` | `LEGACY_BLOCK_VARBITS` | Legacy shared block-list varbits, used only when no per-master slot is filled. |
| `static final String[]` | `MASTER_NAMES` | Master id to display name. |
| `static final Map<String,Integer>` | `UNLOCK_VARBITS` | Unlock key to the varbit that is non-zero once the task-assignment unlock is purchased. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `SlayerInfo()` |  |

### Method Summary

| Modifier and Type | Method | Description |
|---|---|---|
| `static Map<String,int[]>` | `blockVarbits()` |  |
| `static String` | `masterName(int masterId)` |  |

### Field Detail

#### BLOCK_VARBITS

`static final Map<String,int[]> BLOCK_VARBITS`

Master name to the varbits holding that master's blocked task ids (0 = empty slot).

#### EXTEND_VARBITS

`static final Map<String,Integer> EXTEND_VARBITS`

Task key to the varbit that is non-zero once that task's extension is purchased.

#### LEGACY_BLOCK_VARBITS

`static final int[] LEGACY_BLOCK_VARBITS`

Legacy shared block-list varbits, used only when no per-master slot is filled.

#### MASTER_NAMES

`static final String[] MASTER_NAMES`

Master id to display name. Index is the master id; index 0 is unused.

#### UNLOCK_VARBITS

`static final Map<String,Integer> UNLOCK_VARBITS`

Unlock key to the varbit that is non-zero once the task-assignment unlock is purchased.

### Constructor Detail

#### SlayerInfo

`private SlayerInfo()`

### Method Detail

#### blockVarbits

`static Map<String,int[]> blockVarbits()`

- **Returns:** an unmodifiable view of the per-master block varbits

#### masterName

`static String masterName(int masterId)`

- **Parameter** `masterId` — the value of the slayer master varbit
- **Returns:** the master's name, or null when the id is 0 or unknown

---

## com.oveduumnakal.charactersnapshot.SnapshotCollector

_class_

`public class SnapshotCollector`

Reads player state from the RuneLite `Client` into in-memory caches and assembles the
export snapshot. Container updates arrive from events; quests, diaries, combat achievements, and
the slayer task are polled. Every read runs on the client thread; `#buildSnapshot()` hands
a copied, stable object graph to the writer thread. Poll methods return whether the polled data
changed, so the plugin can mark the snapshot dirty without a blanket varbit listener.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `private static final int[]` | `BANK_TAB_VARBITS` |  |
| `private static final int[]` | `CA_COMPLETED_VARPS` |  |
| `private static final int` | `CA_META_MAX_RETRIES` | Most reloads attempted after a partial metadata load, so a permanently missing struct is not re-read forever. |
| `private static final int` | `CA_MONSTER_ENUM` | Cache enum mapping monster category id to display name. |
| `private static final int` | `CA_PARAM_ID` | Task struct param: task id (the completion bit index). |
| `private static final int` | `CA_PARAM_MONSTER` | Task struct param: monster category id, a key of `#CA_MONSTER_ENUM`. |
| `private static final int` | `CA_PARAM_NAME` | Task struct param: task name. |
| `private static final int[]` | `CA_TIER_ENUMS` | Cache enums listing the task struct ids per tier, easy to grandmaster. |
| `private static final String[]` | `CA_TIER_NAMES` |  |
| `private static final EquipmentInventorySlot[]` | `EQUIPMENT_SLOTS` |  |
| `private static final int` | `KARAMJA_TIER_DONE` | Value of the Karamja easy, medium and hard tier varbits once the tier is complete; 1 only means started. |
| `private static final int[]` | `RUNE_POUCH_ITEMS` | Every rune pouch variant; the contents varbits are shared by all of them. |
| `private static final int[]` | `RUNE_POUCH_QUANTITY_VARBITS` |  |
| `private static final int[]` | `RUNE_POUCH_TYPE_VARBITS` |  |
| `private static final int` | `SLAYER_BOSS_TASK_ID` |  |
| `private List<ItemEntry>` | `bankItems` |  |
| `private List<BankTab>` | `bankTabs` |  |
| `private int` | `bankTotal` |  |
| `private Instant` | `bankUpdated` |  |
| `private boolean` | `caMetaPartial` | Whether the last metadata load skipped a tier enum or task struct the cache did not return. |
| `private int` | `caMetaRetries` | How many times a partial metadata load has been retried. |
| `private Map<Integer,CombatAchievementTask>` | `caTaskMeta` | Cached task metadata by task id (completed flag unset); empty until resolved from the cache. |
| `private final Client` | `client` |  |
| `private CombatAchievementData` | `combatAchievements` |  |
| `private final CharacterSnapshotConfig` | `config` |  |
| `private Map<String,DiaryRegion>` | `diaries` |  |
| `private Map<String,ItemEntry>` | `equipment` |  |
| `private Instant` | `equipmentUpdated` |  |
| `private final Map<Integer,GeOffer>` | `geOffers` |  |
| `private List<InventoryItem>` | `inventory` |  |
| `private Instant` | `inventoryUpdated` |  |
| `private Integer` | `questPoints` |  |
| `private List<QuestEntry>` | `quests` |  |
| `private List<ItemEntry>` | `runePouch` |  |
| `private final Map<String,SkillEntry>` | `skills` |  |
| `private SlayerData` | `slayer` |  |
| `private final Map<Integer,String>` | `slayerBossNames` |  |
| `private final Map<Integer,String>` | `slayerTaskNames` |  |

### Constructor Summary

| Constructor | Description |
|---|---|
| `SnapshotCollector(Client client, CharacterSnapshotConfig config)` |  |

### Method Summary

| Modifier and Type | Method | Description |
|---|---|---|
| `private List<CombatAchievementTask>` | `buildCombatAchievementTasks()` | Combines the completion varps with the cached task metadata. |
| `private LocationData` | `buildLocation(Player localPlayer)` | Reads the character's world location. |
| `private Map<String,String>` | `buildSectionUpdated(CharacterSnapshot data)` | Collects the capture time of each item section present in the snapshot. |
| `public CharacterSnapshot` | `buildSnapshot()` | Assembles the export snapshot from the caches and cheap client reads, honouring the config toggles. |
| `private VitalsData` | `buildVitals()` | Reads current combat vitals from boosted and real skill levels and run energy. |
| `private List<BankTab>` | `copyBankTabs()` | Copies the cached bank tabs so a built snapshot does not share lists with the cache. |
| `private DiaryRegion` | `diaryRegion(String region, int easy, int medium, int hard, int elite)` | Reads a diary region whose four tier varbits are 0/1 completion flags. |
| `private DiaryRegion` | `diaryRegion(String region, int easy, int medium, int hard, int elite, int doneValue)` | Reads a diary region's four tiers and its mapped tasks. |
| `private List<ItemEntry>` | `filterBankSlice(Item[] raw, int start, int end)` | Converts a slice of raw bank slots to item entries, dropping empty slots and placeholders. |
| `private String` | `getItemName(int itemId)` | Resolves an item id to its display name. |
| `private boolean` | `hasRunePouch()` |  |
| `private Map<Integer,CombatAchievementTask>` | `loadCombatAchievementMeta()` | Reads task names, tiers, and monsters from the game cache: each tier enum lists task structs, and each struct carries the task id, name, and monster category as params. |
| `private static Instant` | `parseInstant(String value)` | Parses an ISO-8601 instant, tolerating a missing or malformed value. |
| `public void` | `pollAllGeOffers()` | Rebuilds the Grand Exchange cache from the client's current offer array. |
| `public void` | `pollAllSkills()` | Reads every skill from the client, plus a computed `OVERALL` total. |
| `public boolean` | `pollCombatAchievements()` | Polls the six combat-achievement tier varbits and the per-task completion bits. |
| `public boolean` | `pollDiaries()` | Polls achievement-diary completion for every region via named RuneLite varbits, and each mapped task via `DiaryTasks`. |
| `public boolean` | `pollQuests()` | Polls quest completion state and total quest points. |
| `public boolean` | `pollRunePouch()` | Polls the rune pouch contents. |
| `public boolean` | `pollSlayer()` | Polls the slayer state. |
| `private List<BlockedTask>` | `readBlockedTasks()` | Reads every master's blocked task slots, falling back to the legacy shared block list when no per-master slot is filled. |
| `private List<String>` | `readFlags(Map<String,Integer> varbits)` | Lists the keys whose varbit is non-zero. |
| `private List<ItemEntry>` | `readRunePouch()` | Reads the rune pouch slots from the client varbits. |
| `private void` | `readSlayerDetails(SlayerData task)` | Fills in the slayer master, blocked tasks and reward unlocks of a task. |
| `private String` | `readTaskRowName(int row)` | Reads the display name column of one slayer task table row. |
| `private void` | `refreshOverall()` | Recomputes the `OVERALL` entry from the cached skills. |
| `private void` | `reloadCombatAchievementMetaIfNeeded()` | Loads the task metadata when none is cached, and retries (at most `#CA_META_MAX_RETRIES` times) while the last load was partial, i.e. |
| `public void` | `reset()` | Clears every cache. |
| `private String` | `resolveBossName(int bossId)` | Resolves a boss-task boss id to the boss name via the slayer task sublist table. |
| `private String` | `resolveTaskName(int taskId)` | Resolves a slayer task id to its display name from the cache's slayer task table. |
| `public void` | `restore(CharacterSnapshot previous)` | Seeds the bank, inventory, and equipment caches from this character's previous export, so a section not yet re-captured this session keeps its last known contents instead of vanishing. |
| `private static Instant` | `sectionTime(Map<String,String> times, String section, Instant fallback)` | Resolves a restored section's capture time from the previous export. |
| `private static String` | `stateName(QuestState state)` | Maps a quest state to its exported string form. |
| `private GeOffer` | `toGeOffer(int slot, GrandExchangeOffer offer)` | Converts a client GE offer into the exported model. |
| `public void` | `updateBank(ItemContainer container)` | Replaces the cached bank contents with a flat item list and the same items split by bank tab. |
| `public void` | `updateEquipment(ItemContainer container)` | Rebuilds the cached equipment from scratch so an unequipped slot is dropped. |
| `public void` | `updateGeOffer(int slot, GrandExchangeOffer offer)` | Adds, replaces, or removes a cached Grand Exchange offer for one slot. |
| `public void` | `updateInventory(ItemContainer container)` | Replaces the cached inventory, keeping empty slots so slot indices stay meaningful. |
| `public void` | `updateSkill(Skill skill, int level, int xp, int boostedLevel)` | Updates a single skill from a stat-changed event. |

### Field Detail

#### BANK_TAB_VARBITS

`private static final int[] BANK_TAB_VARBITS`

#### CA_COMPLETED_VARPS

`private static final int[] CA_COMPLETED_VARPS`

#### CA_META_MAX_RETRIES

`private static final int CA_META_MAX_RETRIES`

Most reloads attempted after a partial metadata load, so a permanently missing struct is not re-read forever.

#### CA_MONSTER_ENUM

`private static final int CA_MONSTER_ENUM`

Cache enum mapping monster category id to display name.

#### CA_PARAM_ID

`private static final int CA_PARAM_ID`

Task struct param: task id (the completion bit index).

#### CA_PARAM_MONSTER

`private static final int CA_PARAM_MONSTER`

Task struct param: monster category id, a key of `#CA_MONSTER_ENUM`.

#### CA_PARAM_NAME

`private static final int CA_PARAM_NAME`

Task struct param: task name.

#### CA_TIER_ENUMS

`private static final int[] CA_TIER_ENUMS`

Cache enums listing the task struct ids per tier, easy to grandmaster. Not in the RuneLite API.

#### CA_TIER_NAMES

`private static final String[] CA_TIER_NAMES`

#### EQUIPMENT_SLOTS

`private static final EquipmentInventorySlot[] EQUIPMENT_SLOTS`

#### KARAMJA_TIER_DONE

`private static final int KARAMJA_TIER_DONE`

Value of the Karamja easy, medium and hard tier varbits once the tier is complete; 1 only means started.
The Karamja elite varbit and every other region's tier varbits are plain 0/1 flags.

#### RUNE_POUCH_ITEMS

`private static final int[] RUNE_POUCH_ITEMS`

Every rune pouch variant; the contents varbits are shared by all of them.

#### RUNE_POUCH_QUANTITY_VARBITS

`private static final int[] RUNE_POUCH_QUANTITY_VARBITS`

#### RUNE_POUCH_TYPE_VARBITS

`private static final int[] RUNE_POUCH_TYPE_VARBITS`

#### SLAYER_BOSS_TASK_ID

`private static final int SLAYER_BOSS_TASK_ID`

#### bankItems

`private List<ItemEntry> bankItems`

#### bankTabs

`private List<BankTab> bankTabs`

#### bankTotal

`private int bankTotal`

#### bankUpdated

`private Instant bankUpdated`

#### caMetaPartial

`private boolean caMetaPartial`

Whether the last metadata load skipped a tier enum or task struct the cache did not return.

#### caMetaRetries

`private int caMetaRetries`

How many times a partial metadata load has been retried.

#### caTaskMeta

`private Map<Integer,CombatAchievementTask> caTaskMeta`

Cached task metadata by task id (completed flag unset); empty until resolved from the cache.

#### client

`private final Client client`

#### combatAchievements

`private CombatAchievementData combatAchievements`

#### config

`private final CharacterSnapshotConfig config`

#### diaries

`private Map<String,DiaryRegion> diaries`

#### equipment

`private Map<String,ItemEntry> equipment`

#### equipmentUpdated

`private Instant equipmentUpdated`

#### geOffers

`private final Map<Integer,GeOffer> geOffers`

#### inventory

`private List<InventoryItem> inventory`

#### inventoryUpdated

`private Instant inventoryUpdated`

#### questPoints

`private Integer questPoints`

#### quests

`private List<QuestEntry> quests`

#### runePouch

`private List<ItemEntry> runePouch`

#### skills

`private final Map<String,SkillEntry> skills`

#### slayer

`private SlayerData slayer`

#### slayerBossNames

`private final Map<Integer,String> slayerBossNames`

#### slayerTaskNames

`private final Map<Integer,String> slayerTaskNames`

### Constructor Detail

#### SnapshotCollector

`public SnapshotCollector(Client client, CharacterSnapshotConfig config)`

- **Parameter** `client` — the RuneLite client
- **Parameter** `config` — the plugin configuration

### Method Detail

#### buildCombatAchievementTasks

`private List<CombatAchievementTask> buildCombatAchievementTasks()`

Combines the completion varps with the cached task metadata. Task id `N` is bit
`N % 32` of the `N / 32`th entry of `#CA_COMPLETED_VARPS`.

- **Returns:** every known task with its completion flag, plus any completed id missing from the metadata

#### buildLocation

`private LocationData buildLocation(Player localPlayer)`

Reads the character's world location.

- **Parameter** `localPlayer` — the local player
- **Returns:** the location, or `null` when it is unavailable

#### buildSectionUpdated

`private Map<String,String> buildSectionUpdated(CharacterSnapshot data)`

Collects the capture time of each item section present in the snapshot.

- **Parameter** `data` — the snapshot being built, with its item sections already set
- **Returns:** section name to ISO-8601 capture time, or `null` when no item section is present

#### buildSnapshot

`public CharacterSnapshot buildSnapshot()`

Assembles the export snapshot from the caches and cheap client reads, honouring the config
toggles. Owned collections are copied so the writer thread reads a stable graph.

- **Returns:** the snapshot, or `null` when no character is logged in

#### buildVitals

`private VitalsData buildVitals()`

Reads current combat vitals from boosted and real skill levels and run energy.

- **Returns:** the vitals snapshot

#### copyBankTabs

`private List<BankTab> copyBankTabs()`

Copies the cached bank tabs so a built snapshot does not share lists with the cache.

- **Returns:** a deep copy of the cached tabs

#### diaryRegion

`private DiaryRegion diaryRegion(String region, int easy, int medium, int hard, int elite)`

Reads a diary region whose four tier varbits are 0/1 completion flags.

- **Parameter** `region` — the region key
- **Parameter** `easy` — the easy-tier varbit id
- **Parameter** `medium` — the medium-tier varbit id
- **Parameter** `hard` — the hard-tier varbit id
- **Parameter** `elite` — the elite-tier varbit id
- **Returns:** the region's completion state

#### diaryRegion

`private DiaryRegion diaryRegion(String region, int easy, int medium, int hard, int elite, int doneValue)`

Reads a diary region's four tiers and its mapped tasks. The elite tier is complete when its varbit is 1;
the easy, medium and hard tiers are complete once their varbit reaches `doneValue`.

- **Parameter** `region` — the region key
- **Parameter** `easy` — the easy-tier varbit id
- **Parameter** `medium` — the medium-tier varbit id
- **Parameter** `hard` — the hard-tier varbit id
- **Parameter** `elite` — the elite-tier varbit id
- **Parameter** `doneValue` — the easy, medium and hard varbit value that marks the tier complete
- **Returns:** the region's completion state

#### filterBankSlice

`private List<ItemEntry> filterBankSlice(Item[] raw, int start, int end)`

Converts a slice of raw bank slots to item entries, dropping empty slots and placeholders.

- **Parameter** `raw` — the raw bank container items
- **Parameter** `start` — first slot of the slice, inclusive
- **Parameter** `end` — last slot of the slice, exclusive
- **Returns:** the item entries in the slice

#### getItemName

`private String getItemName(int itemId)`

Resolves an item id to its display name.

- **Parameter** `itemId` — the item id
- **Returns:** the item name, or `null` when it cannot be resolved

#### hasRunePouch

`private boolean hasRunePouch()`

- **Returns:** whether the live inventory holds any rune pouch variant

#### loadCombatAchievementMeta

`private Map<Integer,CombatAchievementTask> loadCombatAchievementMeta()`

Reads task names, tiers, and monsters from the game cache: each tier enum lists task structs,
and each struct carries the task id, name, and monster category as params.

- **Returns:** the metadata by task id, or empty if the cache is not readable yet; also sets
        `#caMetaPartial`

#### parseInstant

`private static Instant parseInstant(String value)`

Parses an ISO-8601 instant, tolerating a missing or malformed value.

- **Parameter** `value` — the text to parse, may be `null`
- **Returns:** the instant, or `null` if absent or unparseable

#### pollAllGeOffers

`public void pollAllGeOffers()`

Rebuilds the Grand Exchange cache from the client's current offer array.

#### pollAllSkills

`public void pollAllSkills()`

Reads every skill from the client, plus a computed `OVERALL` total.

#### pollCombatAchievements

`public boolean pollCombatAchievements()`

Polls the six combat-achievement tier varbits and the per-task completion bits.

- **Returns:** whether any tier value or task completion changed since the last poll

#### pollDiaries

`public boolean pollDiaries()`

Polls achievement-diary completion for every region via named RuneLite varbits, and each mapped
task via `DiaryTasks`.

- **Returns:** whether any diary completion changed since the last poll

#### pollQuests

`public boolean pollQuests()`

Polls quest completion state and total quest points.

- **Returns:** whether either the quest states or the quest-point total changed since the last poll

#### pollRunePouch

`public boolean pollRunePouch()`

Polls the rune pouch contents. The pouch is present only while the inventory holds one of its variants
(the same check RuneLite's rune pouch plugin makes); otherwise the cached contents are cleared. Each
of the six slots pairs a rune-type varbit with a quantity varbit, and the rune type maps to an item id
through the `EnumID#RUNEPOUCH_RUNE` cache enum. Empty slots and unresolvable runes are skipped.

- **Returns:** whether the pouch contents (or its presence) changed since the last poll

#### pollSlayer

`public boolean pollSlayer()`

Polls the slayer state. Points and streak are always exported; the task fields are null/0 when no task is
assigned.

- **Returns:** whether the slayer state changed since the last poll

#### readBlockedTasks

`private List<BlockedTask> readBlockedTasks()`

Reads every master's blocked task slots, falling back to the legacy shared block list when no
per-master slot is filled.

- **Returns:** the blocked tasks, never null

#### readFlags

`private List<String> readFlags(Map<String,Integer> varbits)`

Lists the keys whose varbit is non-zero.

- **Parameter** `varbits` — key to varbit id
- **Returns:** the keys of the set flags, in table order

#### readRunePouch

`private List<ItemEntry> readRunePouch()`

Reads the rune pouch slots from the client varbits.

- **Returns:** the runes held, possibly empty

#### readSlayerDetails

`private void readSlayerDetails(SlayerData task)`

Fills in the slayer master, blocked tasks and reward unlocks of a task.

- **Parameter** `task` — the slayer data to complete

#### readTaskRowName

`private String readTaskRowName(int row)`

Reads the display name column of one slayer task table row.

- **Parameter** `row` — the slayer task table row id
- **Returns:** the name, or null when absent

#### refreshOverall

`private void refreshOverall()`

Recomputes the `OVERALL` entry from the cached skills. Does nothing until every real skill is
cached, so a partial total is never exported.

#### reloadCombatAchievementMetaIfNeeded

`private void reloadCombatAchievementMetaIfNeeded()`

Loads the task metadata when none is cached, and retries (at most `#CA_META_MAX_RETRIES` times)
while the last load was partial, i.e. some tier enum or task struct was not returned by the cache.
A retry never replaces the cached metadata with a smaller set.

#### reset

`public void reset()`

Clears every cache. Called on logout and account change so one character's data can never leak
into another character's export file.

#### resolveBossName

`private String resolveBossName(int bossId)`

Resolves a boss-task boss id to the boss name via the slayer task sublist table.

- **Parameter** `bossId` — the value of the slayer target boss varbit
- **Returns:** the boss name, or null when it cannot be resolved

#### resolveTaskName

`private String resolveTaskName(int taskId)`

Resolves a slayer task id to its display name from the cache's slayer task table.

- **Parameter** `taskId` — the slayer task id
- **Returns:** the task name, or null when it cannot be resolved

#### restore

`public void restore(CharacterSnapshot previous)`

Seeds the bank, inventory, and equipment caches from this character's previous export, so a
section not yet re-captured this session keeps its last known contents instead of vanishing. Only
sections not captured since the last `#reset()` are filled, so live data is never replaced,
and each restored section keeps its original capture time. A snapshot belonging to a different
character, or a section with no readable capture time, is ignored.

- **Parameter** `previous` — the previous export, may be `null`

#### sectionTime

`private static Instant sectionTime(Map<String,String> times, String section, Instant fallback)`

Resolves a restored section's capture time from the previous export.

- **Parameter** `times` — the previous export's per-section times
- **Parameter** `section` — the section name
- **Parameter** `fallback` — the previous export's overall build time, may be `null`
- **Returns:** the section's own time if readable, else `fallback`

#### stateName

`private static String stateName(QuestState state)`

Maps a quest state to its exported string form.

- **Parameter** `state` — the quest state
- **Returns:** `FINISHED`, `IN_PROGRESS`, or `NOT_STARTED`

#### toGeOffer

`private GeOffer toGeOffer(int slot, GrandExchangeOffer offer)`

Converts a client GE offer into the exported model.

- **Parameter** `slot` — the slot index
- **Parameter** `offer` — the client offer
- **Returns:** the export model offer

#### updateBank

`public void updateBank(ItemContainer container)`

Replaces the cached bank contents with a flat item list and the same items split by bank tab.
The container holds tabs 1 to 9 in order, then the main tab; each numbered tab's slot count is
read from its varbit, and the remaining slots are the main tab. Slicing uses the raw container,
placeholders and empty slots included, so the counts line up; each slice is then filtered like
the flat list.

- **Parameter** `container` — the bank item container

#### updateEquipment

`public void updateEquipment(ItemContainer container)`

Rebuilds the cached equipment from scratch so an unequipped slot is dropped. The worn container
is not contiguous (it has unused arms, hair and jaw indices), so each slot is read at its own
`EquipmentInventorySlot#getSlotIdx()` rather than by position.

- **Parameter** `container` — the equipment item container

#### updateGeOffer

`public void updateGeOffer(int slot, GrandExchangeOffer offer)`

Adds, replaces, or removes a cached Grand Exchange offer for one slot.

- **Parameter** `slot` — the slot index
- **Parameter** `offer` — the offer, or an `EMPTY`/null offer to clear the slot

#### updateInventory

`public void updateInventory(ItemContainer container)`

Replaces the cached inventory, keeping empty slots so slot indices stay meaningful.

- **Parameter** `container` — the inventory item container

#### updateSkill

`public void updateSkill(Skill skill, int level, int xp, int boostedLevel)`

Updates a single skill from a stat-changed event.

- **Parameter** `skill` — the skill
- **Parameter** `level` — the real level
- **Parameter** `xp` — the experience
- **Parameter** `boostedLevel` — the current boosted level

---

## com.oveduumnakal.charactersnapshot.SnapshotWriter

_class_

`public class SnapshotWriter`

Serializes a `CharacterSnapshot` snapshot to a per-character JSON file. The write is atomic:
the JSON is written to a temporary file in the target directory and then renamed over the
destination, so a second process polling the file never observes a half-written document. It can
also read a character's previous export back, so its last known contents survive a new session.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `private final Gson` | `gson` |  |

### Constructor Summary

| Constructor | Description |
|---|---|
| `SnapshotWriter(Gson gson)` |  |

### Method Summary

| Modifier and Type | Method | Description |
|---|---|---|
| `private static void` | `deleteQuietly(Path path)` | Deletes a temporary file, ignoring any failure. |
| `static String` | `filenameFor(String username)` | Builds the JSON filename for a username, lower-casing it and replacing every character outside `[a-z0-9_-]` with an underscore so the name cannot escape the target directory. |
| `public CharacterSnapshot` | `read(String username, File syncDir)` | Reads a character's previous export from ` / .json`. |
| `public boolean` | `write(CharacterSnapshot data, File syncDir)` | Writes the snapshot to ` / .json` atomically. |

### Field Detail

#### gson

`private final Gson gson`

### Constructor Detail

#### SnapshotWriter

`public SnapshotWriter(Gson gson)`

- **Parameter** `gson` — the injected Gson instance; a pretty-printing copy is derived from it

### Method Detail

#### deleteQuietly

`private static void deleteQuietly(Path path)`

Deletes a temporary file, ignoring any failure.

- **Parameter** `path` — the file to remove, may be `null`

#### filenameFor

`static String filenameFor(String username)`

Builds the JSON filename for a username, lower-casing it and replacing every character outside
`[a-z0-9_-]` with an underscore so the name cannot escape the target directory.

- **Parameter** `username` — the raw in-game name
- **Returns:** the sanitized `<name>.json` filename, or `null` for a null/blank name

#### read

`public CharacterSnapshot read(String username, File syncDir)`

Reads a character's previous export from `<syncDir>/<sanitized-username>.json`.

- **Parameter** `username` — the raw in-game name
- **Parameter** `syncDir` — the directory the export was written into
- **Returns:** the parsed snapshot, or `null` if the name is blank or the file is missing or unreadable

#### write

`public boolean write(CharacterSnapshot data, File syncDir)`

Writes the snapshot to `<syncDir>/<sanitized-username>.json` atomically.

- **Parameter** `data` — the snapshot to write
- **Parameter** `syncDir` — the directory to write into (created if absent)
- **Returns:** `true` if the file was written, `false` on missing data or I/O error

---

## com.oveduumnakal.charactersnapshot.model.CharacterSnapshot

_class_

`public class CharacterSnapshot`

Top-level data model written to the local JSON export file, one file per character. Serialized
to JSON via Gson. Fields left `null` are omitted or written as `null` depending on
the configured toggles. See `SCHEMA.md` for the field-by-field contract.

### Nested Type Summary

| Type | Description |
|---|---|
| _class_ [`BankData`](#comoveduumnakalcharactersnapshotmodelcharactersnapshotbankdata) | Bank contents: total distinct item count and a flat list of the stacks it holds. |
| _class_ [`BankTab`](#comoveduumnakalcharactersnapshotmodelcharactersnapshotbanktab) | A bank tab: an index (0 for the main tab, 1 to 9 for the numbered tabs) and the items it holds. |
| _class_ [`BlockedTask`](#comoveduumnakalcharactersnapshotmodelcharactersnapshotblockedtask) | One blocked slayer task slot. |
| _class_ [`CombatAchievementData`](#comoveduumnakalcharactersnapshotmodelcharactersnapshotcombatachievementdata) | Combat-achievement tier progress. |
| _class_ [`CombatAchievementTask`](#comoveduumnakalcharactersnapshotmodelcharactersnapshotcombatachievementtask) | A single combat-achievement task and whether it is complete. |
| _class_ [`DiaryRegion`](#comoveduumnakalcharactersnapshotmodelcharactersnapshotdiaryregion) | Completion of the four achievement-diary tiers for one region, plus the individual tasks where known. |
| _class_ [`DiaryTask`](#comoveduumnakalcharactersnapshotmodelcharactersnapshotdiarytask) | One achievement-diary task and whether it is complete. |
| _class_ [`GeOffer`](#comoveduumnakalcharactersnapshotmodelcharactersnapshotgeoffer) | One active Grand Exchange offer. |
| _class_ [`InventoryItem`](#comoveduumnakalcharactersnapshotmodelcharactersnapshotinventoryitem) | An inventory item, carrying its slot index in addition to the item stack. |
| _class_ [`ItemEntry`](#comoveduumnakalcharactersnapshotmodelcharactersnapshotitementry) | One stack of items: id, resolved name, and quantity. |
| _class_ [`LocationData`](#comoveduumnakalcharactersnapshotmodelcharactersnapshotlocationdata) | World location of the character. |
| _class_ [`PlayerInfo`](#comoveduumnakalcharactersnapshotmodelcharactersnapshotplayerinfo) | Identity and account context of the logged-in character. |
| _class_ [`QuestEntry`](#comoveduumnakalcharactersnapshotmodelcharactersnapshotquestentry) | Completion state of one quest. |
| _class_ [`SkillEntry`](#comoveduumnakalcharactersnapshotmodelcharactersnapshotskillentry) | Real level and experience for one skill, plus the current boosted level. |
| _class_ [`SlayerData`](#comoveduumnakalcharactersnapshotmodelcharactersnapshotslayerdata) | The slayer state. |
| _class_ [`TierCount`](#comoveduumnakalcharactersnapshotmodelcharactersnapshottiercount) | Completed and total task counts for one combat-achievement tier. |
| _class_ [`VitalsData`](#comoveduumnakalcharactersnapshotmodelcharactersnapshotvitalsdata) | Current combat vitals: hitpoints, prayer, and run energy. |

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public Map<String,DiaryRegion>` | `achievementDiaries` | Achievement diary completion keyed by region name. |
| `public BankData` | `bank` | Bank contents as a flat item list, captured when the bank is opened and carried over from the previous session until then. |
| `public CombatAchievementData` | `combatAchievements` | Combat-achievement tier progress. |
| `public Map<String,ItemEntry>` | `equipment` | Worn equipment keyed by slot name. |
| `public List<GeOffer>` | `grandExchange` | Active Grand Exchange offers by slot. |
| `public List<InventoryItem>` | `inventory` | Inventory contents, one entry per slot (empty slots have item id -1). |
| `public String` | `lastUpdated` | ISO-8601 instant at which this snapshot was built. |
| `public LocationData` | `location` | Current world location of the character. |
| `public PlayerInfo` | `player` | Identity and account context of the logged-in character. |
| `public Integer` | `questPoints` | Total quest points. |
| `public List<QuestEntry>` | `quests` | Quest completion state, one entry per quest. |
| `public List<ItemEntry>` | `runePouch` | Runes held in the rune pouch, present only while the inventory holds a rune pouch (any variant). |
| `public int` | `schemaVersion` | Schema version of this file; bumped when the shape of the export changes. |
| `public Map<String,String>` | `sectionUpdated` | ISO-8601 instant each item section (`bank`, `inventory`, `equipment`) was last captured from the client, keyed by section name. |
| `public Map<String,SkillEntry>` | `skills` | Real level and experience per skill, keyed by upper-case skill name, plus `OVERALL`. |
| `public SlayerData` | `slayer` | Slayer state; points and streak always set once polled, task fields null/0 when no task. |
| `public VitalsData` | `vitals` | Current combat vitals (hitpoints, prayer, run energy). |

### Field Detail

#### achievementDiaries

`public Map<String,DiaryRegion> achievementDiaries`

Achievement diary completion keyed by region name.

#### bank

`public BankData bank`

Bank contents as a flat item list, captured when the bank is opened and carried over from the
previous session until then.

#### combatAchievements

`public CombatAchievementData combatAchievements`

Combat-achievement tier progress.

#### equipment

`public Map<String,ItemEntry> equipment`

Worn equipment keyed by slot name.

#### grandExchange

`public List<GeOffer> grandExchange`

Active Grand Exchange offers by slot.

#### inventory

`public List<InventoryItem> inventory`

Inventory contents, one entry per slot (empty slots have item id -1).

#### lastUpdated

`public String lastUpdated`

ISO-8601 instant at which this snapshot was built.

#### location

`public LocationData location`

Current world location of the character.

#### player

`public PlayerInfo player`

Identity and account context of the logged-in character.

#### questPoints

`public Integer questPoints`

Total quest points.

#### quests

`public List<QuestEntry> quests`

Quest completion state, one entry per quest.

#### runePouch

`public List<ItemEntry> runePouch`

Runes held in the rune pouch, present only while the inventory holds a rune pouch (any variant).
Empty when the pouch holds nothing.

#### schemaVersion

`public int schemaVersion`

Schema version of this file; bumped when the shape of the export changes.

#### sectionUpdated

`public Map<String,String> sectionUpdated`

ISO-8601 instant each item section (`bank`, `inventory`, `equipment`) was last
captured from the client, keyed by section name. Earlier than `#lastUpdated` when the section
was carried over from a previous session because it has not been re-captured yet.

#### skills

`public Map<String,SkillEntry> skills`

Real level and experience per skill, keyed by upper-case skill name, plus `OVERALL`.

#### slayer

`public SlayerData slayer`

Slayer state; points and streak always set once polled, task fields null/0 when no task.

#### vitals

`public VitalsData vitals`

Current combat vitals (hitpoints, prayer, run energy).

---

## com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BankData

_class_

`public static class BankData`

Bank contents: total distinct item count and a flat list of the stacks it holds.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public List<ItemEntry>` | `items` | Bank contents as a flat item list. |
| `public List<BankTab>` | `tabs` | The bank split into its real tabs: the main tab is `tabIndex` 0 and the numbered tabs are 1 to 9. |
| `public int` | `totalItems` | Number of distinct item stacks in the bank. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `BankData(int totalItems, List<ItemEntry> items)` | Creates bank data whose only tab is the main tab holding every item. |
| `BankData(int totalItems, List<ItemEntry> items, List<BankTab> tabs)` |  |

### Field Detail

#### items

`public List<ItemEntry> items`

Bank contents as a flat item list.

#### tabs

`public List<BankTab> tabs`

The bank split into its real tabs: the main tab is `tabIndex` 0 and the numbered tabs
are 1 to 9. Never null when the bank is present; an export from before tabs were modelled
holds a single tab 0 wrapping the flat list.

#### totalItems

`public int totalItems`

Number of distinct item stacks in the bank.

### Constructor Detail

#### BankData

`public BankData(int totalItems, List<ItemEntry> items)`

Creates bank data whose only tab is the main tab holding every item.

- **Parameter** `totalItems` — distinct stack count
- **Parameter** `items` — flat item list

#### BankData

`public BankData(int totalItems, List<ItemEntry> items, List<BankTab> tabs)`

- **Parameter** `totalItems` — distinct stack count
- **Parameter** `items` — flat item list
- **Parameter** `tabs` — the real bank tabs

---

## com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BankTab

_class_

`public static class BankTab`

A bank tab: an index (0 for the main tab, 1 to 9 for the numbered tabs) and the items it holds.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public List<ItemEntry>` | `items` | Items in this tab. |
| `public int` | `tabIndex` | Tab index: 0 is the main tab, 1 to 9 are the numbered tabs. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `BankTab(int tabIndex, List<ItemEntry> items)` |  |

### Field Detail

#### items

`public List<ItemEntry> items`

Items in this tab.

#### tabIndex

`public int tabIndex`

Tab index: 0 is the main tab, 1 to 9 are the numbered tabs.

### Constructor Detail

#### BankTab

`public BankTab(int tabIndex, List<ItemEntry> items)`

- **Parameter** `tabIndex` — the tab index
- **Parameter** `items` — the items in the tab

---

## com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BlockedTask

_class_

`public static class BlockedTask`

One blocked slayer task slot.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public String` | `master` | Name of the slayer master whose block list holds this task, or null for the legacy shared list. |
| `public String` | `name` | Task name, or null when it could not be resolved. |
| `public int` | `taskId` | Slayer task id (same id space as the slayer creature id). |

### Constructor Summary

| Constructor | Description |
|---|---|
| `BlockedTask(int taskId, String name, String master)` |  |

### Field Detail

#### master

`public String master`

Name of the slayer master whose block list holds this task, or null for the legacy shared list.

#### name

`public String name`

Task name, or null when it could not be resolved.

#### taskId

`public int taskId`

Slayer task id (same id space as the slayer creature id).

### Constructor Detail

#### BlockedTask

`public BlockedTask(int taskId, String name, String master)`

- **Parameter** `taskId` — slayer task id
- **Parameter** `name` — task name, or null
- **Parameter** `master` — master name, or null

---

## com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.CombatAchievementData

_class_

`public static class CombatAchievementData`

Combat-achievement tier progress. Each field is the raw value of the corresponding RuneLite
`Varbits.COMBAT_ACHIEVEMENT_TIER_*` varbit; see `SCHEMA.md` for its meaning.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public List<String>` | `completedTasks` | Compatibility field: names of completed tasks whose name resolved from the game cache. |
| `public boolean` | `easyComplete` | Compatibility flag: whether the easy tier has any progress (`tierEasy > 0`). |
| `public boolean` | `eliteComplete` | Compatibility flag: whether the elite tier has any progress. |
| `public boolean` | `hardComplete` | Compatibility flag: whether the hard tier has any progress. |
| `public boolean` | `mediumComplete` | Compatibility flag: whether the medium tier has any progress. |
| `public Integer` | `points` | Total combat-achievement points. |
| `public List<CombatAchievementTask>` | `tasks` | Every combat-achievement task with its completion flag. |
| `public Map<String,TierCount>` | `tierCounts` | Completed and total task counts per tier, keyed `EASY` to `GRANDMASTER`. |
| `public int` | `tierEasy` | Raw easy-tier varbit value. |
| `public int` | `tierElite` | Raw elite-tier varbit value. |
| `public int` | `tierGrandmaster` | Raw grandmaster-tier varbit value. |
| `public int` | `tierHard` | Raw hard-tier varbit value. |
| `public int` | `tierMaster` | Raw master-tier varbit value. |
| `public int` | `tierMedium` | Raw medium-tier varbit value. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `CombatAchievementData(int tierEasy, int tierMedium, int tierHard, int tierElite, int tierMaster, int tierGrandmaster)` |  |
| `CombatAchievementData(int tierEasy, int tierMedium, int tierHard, int tierElite, int tierMaster, int tierGrandmaster, List<CombatAchievementTask> tasks)` |  |
| `CombatAchievementData(int tierEasy, int tierMedium, int tierHard, int tierElite, int tierMaster, int tierGrandmaster, List<CombatAchievementTask> tasks, int pointsVarbit)` |  |

### Method Summary

| Modifier and Type | Method | Description |
|---|---|---|
| `private static Integer` | `computePoints(List<CombatAchievementTask> tasks)` | Sums the points of completed tasks. |
| `private static Map<String,TierCount>` | `countTiers(List<CombatAchievementTask> tasks)` | Counts completed and total tasks per tier, ignoring tasks with no resolved tier. |
| `static int` | `pointsPerTask(String tier)` | Points one completed task of the given tier is worth: easy 1, medium 2, hard 3, elite 4, master 5, grandmaster 6. |

### Field Detail

#### completedTasks

`public List<String> completedTasks`

Compatibility field: names of completed tasks whose name resolved from the game cache. Always a
list of strings; external consumers depend on that.

#### easyComplete

`public boolean easyComplete`

Compatibility flag: whether the easy tier has any progress (`tierEasy > 0`).

#### eliteComplete

`public boolean eliteComplete`

Compatibility flag: whether the elite tier has any progress.

#### hardComplete

`public boolean hardComplete`

Compatibility flag: whether the hard tier has any progress.

#### mediumComplete

`public boolean mediumComplete`

Compatibility flag: whether the medium tier has any progress.

#### points

`public Integer points`

Total combat-achievement points. Read from the game's points varbit when it is non-zero; otherwise
the sum of completed task points, or null when a completed task's tier is unresolved.

#### tasks

`public List<CombatAchievementTask> tasks`

Every combat-achievement task with its completion flag. When the cache metadata cannot be
resolved, holds only the completed task ids with null name, tier, and monster.

#### tierCounts

`public Map<String,TierCount> tierCounts`

Completed and total task counts per tier, keyed `EASY` to `GRANDMASTER`. Null when no
task tier could be resolved from the game cache.

#### tierEasy

`public int tierEasy`

Raw easy-tier varbit value.

#### tierElite

`public int tierElite`

Raw elite-tier varbit value.

#### tierGrandmaster

`public int tierGrandmaster`

Raw grandmaster-tier varbit value.

#### tierHard

`public int tierHard`

Raw hard-tier varbit value.

#### tierMaster

`public int tierMaster`

Raw master-tier varbit value.

#### tierMedium

`public int tierMedium`

Raw medium-tier varbit value.

### Constructor Detail

#### CombatAchievementData

`public CombatAchievementData(int tierEasy, int tierMedium, int tierHard, int tierElite, int tierMaster, int tierGrandmaster)`

- **Parameter** `tierEasy` — easy varbit value
- **Parameter** `tierMedium` — medium varbit value
- **Parameter** `tierHard` — hard varbit value
- **Parameter** `tierElite` — elite varbit value
- **Parameter** `tierMaster` — master varbit value
- **Parameter** `tierGrandmaster` — grandmaster varbit value

#### CombatAchievementData

`public CombatAchievementData(int tierEasy, int tierMedium, int tierHard, int tierElite, int tierMaster, int tierGrandmaster, List<CombatAchievementTask> tasks)`

- **Parameter** `tierEasy` — easy varbit value
- **Parameter** `tierMedium` — medium varbit value
- **Parameter** `tierHard` — hard varbit value
- **Parameter** `tierElite` — elite varbit value
- **Parameter** `tierMaster` — master varbit value
- **Parameter** `tierGrandmaster` — grandmaster varbit value
- **Parameter** `tasks` — per-task detail; `completedTasks` is derived from it

#### CombatAchievementData

`public CombatAchievementData(int tierEasy, int tierMedium, int tierHard, int tierElite, int tierMaster, int tierGrandmaster, List<CombatAchievementTask> tasks, int pointsVarbit)`

- **Parameter** `tierEasy` — easy varbit value
- **Parameter** `tierMedium` — medium varbit value
- **Parameter** `tierHard` — hard varbit value
- **Parameter** `tierElite` — elite varbit value
- **Parameter** `tierMaster` — master varbit value
- **Parameter** `tierGrandmaster` — grandmaster varbit value
- **Parameter** `tasks` — per-task detail; `completedTasks`, `tierCounts` and the fallback
                       `points` are derived from it
- **Parameter** `pointsVarbit` — the game's combat-achievement points varbit; used when non-zero

### Method Detail

#### computePoints

`private static Integer computePoints(List<CombatAchievementTask> tasks)`

Sums the points of completed tasks.

- **Parameter** `tasks` — the tasks
- **Returns:** the total, or null when a completed task has no resolved tier

#### countTiers

`private static Map<String,TierCount> countTiers(List<CombatAchievementTask> tasks)`

Counts completed and total tasks per tier, ignoring tasks with no resolved tier.

- **Parameter** `tasks` — the tasks
- **Returns:** the counts for all six tiers in order, or null when no task has a tier

#### pointsPerTask

`static int pointsPerTask(String tier)`

Points one completed task of the given tier is worth: easy 1, medium 2, hard 3, elite 4, master 5,
grandmaster 6. Source: https://oldschool.runescape.wiki/w/Combat_Achievements (points per task
table, checked 2026-10-14).

- **Parameter** `tier` — the tier name, `EASY` to `GRANDMASTER`
- **Returns:** the points, or 0 for an unknown tier

---

## com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.CombatAchievementTask

_class_

`public static class CombatAchievementTask`

A single combat-achievement task and whether it is complete.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public boolean` | `completed` | Whether the task is complete. |
| `public int` | `id` | Task id; completion is bit `id % 32` of the `id / 32`th completion varp. |
| `public String` | `monster` | Monster or category name from the game cache, or null if unresolved. |
| `public String` | `name` | Task name from the game cache, or null if unresolved. |
| `public String` | `tier` | Tier name (EASY to GRANDMASTER), or null if unresolved. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `CombatAchievementTask(int id, String name, String tier, String monster, boolean completed)` |  |

### Field Detail

#### completed

`public boolean completed`

Whether the task is complete.

#### id

`public int id`

Task id; completion is bit `id % 32` of the `id / 32`th completion varp.

#### monster

`public String monster`

Monster or category name from the game cache, or null if unresolved.

#### name

`public String name`

Task name from the game cache, or null if unresolved.

#### tier

`public String tier`

Tier name (EASY to GRANDMASTER), or null if unresolved.

### Constructor Detail

#### CombatAchievementTask

`public CombatAchievementTask(int id, String name, String tier, String monster, boolean completed)`

- **Parameter** `id` — task id
- **Parameter** `name` — task name, or null
- **Parameter** `tier` — tier name, or null
- **Parameter** `monster` — monster name, or null
- **Parameter** `completed` — whether the task is complete

---

## com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.DiaryRegion

_class_

`public static class DiaryRegion`

Completion of the four achievement-diary tiers for one region, plus the individual tasks where known.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public boolean` | `easy` | Whether the easy tier is complete. |
| `public boolean` | `elite` | Whether the elite tier is complete. |
| `public boolean` | `hard` | Whether the hard tier is complete. |
| `public boolean` | `medium` | Whether the medium tier is complete. |
| `public Map<String,List<DiaryTask>>` | `tasks` | Individual task completion keyed by tier (`easy`, `medium`, `hard`, `elite`). |

### Constructor Summary

| Constructor | Description |
|---|---|
| `DiaryRegion(boolean easy, boolean medium, boolean hard, boolean elite)` |  |
| `DiaryRegion(boolean easy, boolean medium, boolean hard, boolean elite, Map<String,List<DiaryTask>> tasks)` |  |

### Field Detail

#### easy

`public boolean easy`

Whether the easy tier is complete.

#### elite

`public boolean elite`

Whether the elite tier is complete.

#### hard

`public boolean hard`

Whether the hard tier is complete.

#### medium

`public boolean medium`

Whether the medium tier is complete.

#### tasks

`public Map<String,List<DiaryTask>> tasks`

Individual task completion keyed by tier (`easy`, `medium`, `hard`,
`elite`). Absent when no task of the region is mapped; a tier is absent when none of
its tasks are mapped.

### Constructor Detail

#### DiaryRegion

`public DiaryRegion(boolean easy, boolean medium, boolean hard, boolean elite)`

- **Parameter** `easy` — easy complete
- **Parameter** `medium` — medium complete
- **Parameter** `hard` — hard complete
- **Parameter** `elite` — elite complete

#### DiaryRegion

`public DiaryRegion(boolean easy, boolean medium, boolean hard, boolean elite, Map<String,List<DiaryTask>> tasks)`

- **Parameter** `easy` — easy complete
- **Parameter** `medium` — medium complete
- **Parameter** `hard` — hard complete
- **Parameter** `elite` — elite complete
- **Parameter** `tasks` — per-tier task completion, or `null` when none is mapped

---

## com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.DiaryTask

_class_

`public static class DiaryTask`

One achievement-diary task and whether it is complete.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public boolean` | `completed` | Whether the task is complete. |
| `public String` | `name` | Task text. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `DiaryTask(String name, boolean completed)` |  |

### Field Detail

#### completed

`public boolean completed`

Whether the task is complete.

#### name

`public String name`

Task text.

### Constructor Detail

#### DiaryTask

`public DiaryTask(String name, boolean completed)`

- **Parameter** `name` — task text
- **Parameter** `completed` — whether the task is complete

---

## com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.GeOffer

_class_

`public static class GeOffer`

One active Grand Exchange offer.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public int` | `itemId` | Item id being traded. |
| `public String` | `name` | Resolved item name. |
| `public long` | `price` | Per-item price of the offer. |
| `public int` | `quantitySold` | Quantity bought or sold so far. |
| `public int` | `slot` | Slot index, 0–7. |
| `public long` | `spent` | Coins spent or gained so far. |
| `public String` | `state` | Offer state, e.g. |
| `public int` | `totalQuantity` | Total quantity of the offer. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `GeOffer(int slot, int itemId, String name, String state, int quantitySold, int totalQuantity, long price, long spent)` |  |

### Field Detail

#### itemId

`public int itemId`

Item id being traded.

#### name

`public String name`

Resolved item name.

#### price

`public long price`

Per-item price of the offer.

#### quantitySold

`public int quantitySold`

Quantity bought or sold so far.

#### slot

`public int slot`

Slot index, 0–7.

#### spent

`public long spent`

Coins spent or gained so far.

#### state

`public String state`

Offer state, e.g. `BUYING`, `SOLD`, `CANCELLED_BUY`.

#### totalQuantity

`public int totalQuantity`

Total quantity of the offer.

### Constructor Detail

#### GeOffer

`public GeOffer(int slot, int itemId, String name, String state, int quantitySold, int totalQuantity, long price, long spent)`

- **Parameter** `slot` — slot index
- **Parameter** `itemId` — item id
- **Parameter** `name` — resolved name
- **Parameter** `state` — offer state
- **Parameter** `quantitySold` — quantity transacted
- **Parameter** `totalQuantity` — total quantity
- **Parameter** `price` — per-item price
- **Parameter** `spent` — coins moved

---

## com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.InventoryItem

_class_

`public static class InventoryItem`

An inventory item, carrying its slot index in addition to the item stack.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public int` | `slot` | Zero-based inventory slot. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `InventoryItem(int itemId, String name, int quantity, int slot)` |  |

### Field Detail

#### slot

`public int slot`

Zero-based inventory slot.

### Constructor Detail

#### InventoryItem

`public InventoryItem(int itemId, String name, int quantity, int slot)`

- **Parameter** `itemId` — item id
- **Parameter** `name` — resolved name
- **Parameter** `quantity` — stack quantity
- **Parameter** `slot` — inventory slot index

---

## com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.ItemEntry

_class_

`public static class ItemEntry`

One stack of items: id, resolved name, and quantity.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public int` | `itemId` | Item id. |
| `public String` | `name` | Resolved item name, or `null` for an empty slot. |
| `public int` | `quantity` | Stack quantity. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `ItemEntry(int itemId, String name, int quantity)` |  |

### Field Detail

#### itemId

`public int itemId`

Item id.

#### name

`public String name`

Resolved item name, or `null` for an empty slot.

#### quantity

`public int quantity`

Stack quantity.

### Constructor Detail

#### ItemEntry

`public ItemEntry(int itemId, String name, int quantity)`

- **Parameter** `itemId` — item id
- **Parameter** `name` — resolved name
- **Parameter** `quantity` — stack quantity

---

## com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.LocationData

_class_

`public static class LocationData`

World location of the character.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public int` | `plane` | Plane (height level), 0–3. |
| `public int` | `regionId` | Map region id. |
| `public int` | `x` | World x coordinate. |
| `public int` | `y` | World y coordinate. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `LocationData(int regionId, int x, int y, int plane)` |  |

### Field Detail

#### plane

`public int plane`

Plane (height level), 0–3.

#### regionId

`public int regionId`

Map region id.

#### x

`public int x`

World x coordinate.

#### y

`public int y`

World y coordinate.

### Constructor Detail

#### LocationData

`public LocationData(int regionId, int x, int y, int plane)`

- **Parameter** `regionId` — map region id
- **Parameter** `x` — world x
- **Parameter** `y` — world y
- **Parameter** `plane` — plane

---

## com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.PlayerInfo

_class_

`public static class PlayerInfo`

Identity and account context of the logged-in character.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public String` | `accountType` | Account type, e.g. |
| `public int` | `combatLevel` | Combat level. |
| `public Boolean` | `members` | Whether the current world is a members' world; `null` when account info is not exported. |
| `public String` | `username` | In-game display name. |
| `public int` | `world` | World the character is logged into. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `PlayerInfo(String username, int combatLevel, int world, String accountType, Boolean members)` |  |

### Field Detail

#### accountType

`public String accountType`

Account type, e.g. `NORMAL`, `IRONMAN`, `HARDCORE_IRONMAN`.

#### combatLevel

`public int combatLevel`

Combat level.

#### members

`public Boolean members`

Whether the current world is a members' world; `null` when account info is not exported.

#### username

`public String username`

In-game display name.

#### world

`public int world`

World the character is logged into.

### Constructor Detail

#### PlayerInfo

`public PlayerInfo(String username, int combatLevel, int world, String accountType, Boolean members)`

- **Parameter** `username` — display name
- **Parameter** `combatLevel` — combat level
- **Parameter** `world` — current world
- **Parameter** `accountType` — account-type name
- **Parameter** `members` — whether on a members' world, or `null` when not exported

---

## com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.QuestEntry

_class_

`public static class QuestEntry`

Completion state of one quest.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public String` | `displayName` | Human-readable quest name. |
| `public String` | `name` | Enum name of the quest. |
| `public String` | `state` | One of `NOT_STARTED`, `IN_PROGRESS`, `FINISHED`. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `QuestEntry(String name, String displayName, String state)` |  |

### Field Detail

#### displayName

`public String displayName`

Human-readable quest name.

#### name

`public String name`

Enum name of the quest.

#### state

`public String state`

One of `NOT_STARTED`, `IN_PROGRESS`, `FINISHED`.

### Constructor Detail

#### QuestEntry

`public QuestEntry(String name, String displayName, String state)`

- **Parameter** `name` — enum name
- **Parameter** `displayName` — readable name
- **Parameter** `state` — completion state

---

## com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.SkillEntry

_class_

`public static class SkillEntry`

Real level and experience for one skill, plus the current boosted level.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public int` | `boostedLevel` | Current boosted level (equals `level` when unboosted). |
| `public int` | `level` | Real (unboosted) level. |
| `public long` | `xp` | Experience points (a `long` so the OVERALL total does not overflow). |

### Constructor Summary

| Constructor | Description |
|---|---|
| `SkillEntry(int level, long xp, int boostedLevel)` |  |

### Field Detail

#### boostedLevel

`public int boostedLevel`

Current boosted level (equals `level` when unboosted).

#### level

`public int level`

Real (unboosted) level.

#### xp

`public long xp`

Experience points (a `long` so the OVERALL total does not overflow).

### Constructor Detail

#### SkillEntry

`public SkillEntry(int level, long xp, int boostedLevel)`

- **Parameter** `level` — real level
- **Parameter** `xp` — experience points
- **Parameter** `boostedLevel` — current boosted level

---

## com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.SlayerData

_class_

`public static class SlayerData`

The slayer state. With no task assigned, the creature id and amount are 0 and the names are null, while
points and streak stay. The creature is exported as its game id plus, when resolvable, its name.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public int` | `amount` | Remaining kill count. |
| `public List<BlockedTask>` | `blockedTasks` | Blocked tasks across all masters, or null in files written before this field existed. |
| `public String` | `bossName` | Specific boss name for boss tasks, or null for any other task. |
| `public int` | `creatureId` | Game id of the assigned creature. |
| `public String` | `creatureName` | Display name of the assigned creature, or null when it could not be resolved. |
| `public List<String>` | `extendedTasks` | Keys of tasks whose extension is purchased, or null in files written before this field existed. |
| `public Integer` | `masterId` | Id of the current slayer master, 0 when unknown. |
| `public String` | `masterName` | Name of the current slayer master, or null when unresolved. |
| `public int` | `points` | Slayer reward points. |
| `public int` | `streak` | Current task-completion streak. |
| `public List<String>` | `unlockedTasks` | Keys of purchased task-assignment unlocks, or null in files written before this field existed. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `SlayerData(int creatureId, String creatureName, String bossName, int amount, int points, int streak)` |  |

### Field Detail

#### amount

`public int amount`

Remaining kill count.

#### blockedTasks

`public List<BlockedTask> blockedTasks`

Blocked tasks across all masters, or null in files written before this field existed.

#### bossName

`public String bossName`

Specific boss name for boss tasks, or null for any other task.

#### creatureId

`public int creatureId`

Game id of the assigned creature.

#### creatureName

`public String creatureName`

Display name of the assigned creature, or null when it could not be resolved.

#### extendedTasks

`public List<String> extendedTasks`

Keys of tasks whose extension is purchased, or null in files written before this field existed.

#### masterId

`public Integer masterId`

Id of the current slayer master, 0 when unknown. Null in files written before this field existed.

#### masterName

`public String masterName`

Name of the current slayer master, or null when unresolved.

#### points

`public int points`

Slayer reward points.

#### streak

`public int streak`

Current task-completion streak.

#### unlockedTasks

`public List<String> unlockedTasks`

Keys of purchased task-assignment unlocks, or null in files written before this field existed.

### Constructor Detail

#### SlayerData

`public SlayerData(int creatureId, String creatureName, String bossName, int amount, int points, int streak)`

- **Parameter** `creatureId` — assigned-creature id
- **Parameter** `creatureName` — assigned-creature name, or null
- **Parameter** `bossName` — boss name for boss tasks, or null
- **Parameter** `amount` — remaining kills
- **Parameter** `points` — reward points
- **Parameter** `streak` — completion streak

---

## com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.TierCount

_class_

`public static class TierCount`

Completed and total task counts for one combat-achievement tier.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public int` | `completed` | Number of completed tasks in the tier. |
| `public int` | `total` | Number of tasks in the tier. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `TierCount(int completed, int total)` |  |

### Field Detail

#### completed

`public int completed`

Number of completed tasks in the tier.

#### total

`public int total`

Number of tasks in the tier.

### Constructor Detail

#### TierCount

`public TierCount(int completed, int total)`

- **Parameter** `completed` — completed task count
- **Parameter** `total` — total task count

---

## com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.VitalsData

_class_

`public static class VitalsData`

Current combat vitals: hitpoints, prayer, and run energy.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public int` | `currentHitpoints` | Current (boosted) hitpoints. |
| `public int` | `currentPrayer` | Current (boosted) prayer points. |
| `public int` | `maxHitpoints` | Maximum (real) hitpoints level. |
| `public int` | `maxPrayer` | Maximum (real) prayer level. |
| `public int` | `runEnergyPercent` | Run energy as a percentage, 0–100. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `VitalsData(int currentHitpoints, int maxHitpoints, int currentPrayer, int maxPrayer, int runEnergyPercent)` |  |

### Field Detail

#### currentHitpoints

`public int currentHitpoints`

Current (boosted) hitpoints.

#### currentPrayer

`public int currentPrayer`

Current (boosted) prayer points.

#### maxHitpoints

`public int maxHitpoints`

Maximum (real) hitpoints level.

#### maxPrayer

`public int maxPrayer`

Maximum (real) prayer level.

#### runEnergyPercent

`public int runEnergyPercent`

Run energy as a percentage, 0–100.

### Constructor Detail

#### VitalsData

`public VitalsData(int currentHitpoints, int maxHitpoints, int currentPrayer, int maxPrayer, int runEnergyPercent)`

- **Parameter** `currentHitpoints` — current hitpoints
- **Parameter** `maxHitpoints` — maximum hitpoints
- **Parameter** `currentPrayer` — current prayer
- **Parameter** `maxPrayer` — maximum prayer
- **Parameter** `runEnergyPercent` — run energy percent
