# Stockpile — JavaDoc Reference

<!-- GENERATED FILE — DO NOT EDIT BY HAND.
     Run `./gradlew generateJavaDocs` and commit the result. -->

## Contents

- [com.oveduumnakal.dataexport.PlayerDataCollector](#comoveduumnakaldataexportplayerdatacollector)
- [com.oveduumnakal.dataexport.PlayerDataExportConfig](#comoveduumnakaldataexportplayerdataexportconfig)
- [com.oveduumnakal.dataexport.PlayerDataExportPlugin](#comoveduumnakaldataexportplayerdataexportplugin)
- [com.oveduumnakal.dataexport.PlayerDataWriter](#comoveduumnakaldataexportplayerdatawriter)
- [com.oveduumnakal.dataexport.model.PlayerSyncData](#comoveduumnakaldataexportmodelplayersyncdata)
- [com.oveduumnakal.dataexport.model.PlayerSyncData.BankData](#comoveduumnakaldataexportmodelplayersyncdatabankdata)
- [com.oveduumnakal.dataexport.model.PlayerSyncData.CombatAchievementData](#comoveduumnakaldataexportmodelplayersyncdatacombatachievementdata)
- [com.oveduumnakal.dataexport.model.PlayerSyncData.DiaryRegion](#comoveduumnakaldataexportmodelplayersyncdatadiaryregion)
- [com.oveduumnakal.dataexport.model.PlayerSyncData.GeOffer](#comoveduumnakaldataexportmodelplayersyncdatageoffer)
- [com.oveduumnakal.dataexport.model.PlayerSyncData.InventoryItem](#comoveduumnakaldataexportmodelplayersyncdatainventoryitem)
- [com.oveduumnakal.dataexport.model.PlayerSyncData.ItemEntry](#comoveduumnakaldataexportmodelplayersyncdataitementry)
- [com.oveduumnakal.dataexport.model.PlayerSyncData.LocationData](#comoveduumnakaldataexportmodelplayersyncdatalocationdata)
- [com.oveduumnakal.dataexport.model.PlayerSyncData.PlayerInfo](#comoveduumnakaldataexportmodelplayersyncdataplayerinfo)
- [com.oveduumnakal.dataexport.model.PlayerSyncData.QuestEntry](#comoveduumnakaldataexportmodelplayersyncdataquestentry)
- [com.oveduumnakal.dataexport.model.PlayerSyncData.SkillEntry](#comoveduumnakaldataexportmodelplayersyncdataskillentry)
- [com.oveduumnakal.dataexport.model.PlayerSyncData.SlayerData](#comoveduumnakaldataexportmodelplayersyncdataslayerdata)
- [com.oveduumnakal.dataexport.model.PlayerSyncData.VitalsData](#comoveduumnakaldataexportmodelplayersyncdatavitalsdata)

---

## com.oveduumnakal.dataexport.PlayerDataCollector

_class_

`public class PlayerDataCollector`

Reads player state from the RuneLite `Client` into in-memory caches and assembles the
export snapshot. Container updates arrive from events; quests, diaries, combat achievements, and
the slayer task are polled. Every read runs on the client thread; `#buildSnapshot()` hands
a copied, stable object graph to the writer thread. Poll methods return whether the polled data
changed, so the plugin can mark the snapshot dirty without a blanket varbit listener.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `private static final String[]` | `EQUIPMENT_SLOTS` |  |
| `private List<ItemEntry>` | `bankItems` |  |
| `private int` | `bankTotal` |  |
| `private final Client` | `client` |  |
| `private CombatAchievementData` | `combatAchievements` |  |
| `private final PlayerDataExportConfig` | `config` |  |
| `private Map<String,DiaryRegion>` | `diaries` |  |
| `private Map<String,ItemEntry>` | `equipment` |  |
| `private final Map<Integer,GeOffer>` | `geOffers` |  |
| `private List<InventoryItem>` | `inventory` |  |
| `private Integer` | `questPoints` |  |
| `private List<QuestEntry>` | `quests` |  |
| `private final Map<String,SkillEntry>` | `skills` |  |
| `private SlayerData` | `slayer` |  |

### Constructor Summary

| Constructor | Description |
|---|---|
| `PlayerDataCollector(Client client, PlayerDataExportConfig config)` |  |

### Method Summary

| Modifier and Type | Method | Description |
|---|---|---|
| `private LocationData` | `buildLocation(Player localPlayer)` | Reads the character's world location. |
| `public PlayerSyncData` | `buildSnapshot()` | Assembles the export snapshot from the caches and cheap client reads, honouring the config toggles. |
| `private VitalsData` | `buildVitals()` | Reads current combat vitals from boosted and real skill levels and run energy. |
| `private DiaryRegion` | `diaryRegion(int easy, int medium, int hard, int elite)` | Reads a diary region's four tiers, each complete when its varbit equals 1. |
| `private String` | `getItemName(int itemId)` | Resolves an item id to its display name. |
| `public void` | `pollAllGeOffers()` | Rebuilds the Grand Exchange cache from the client's current offer array. |
| `public void` | `pollAllSkills()` | Reads every skill from the client, plus a computed `OVERALL` total. |
| `public boolean` | `pollCombatAchievements()` | Polls the six combat-achievement tier varbits. |
| `public boolean` | `pollDiaries()` | Polls achievement-diary completion for every region via named RuneLite varbits. |
| `public boolean` | `pollQuests()` | Polls quest completion state and total quest points. |
| `public boolean` | `pollSlayer()` | Polls the current slayer task, clearing it when no task is assigned. |
| `public void` | `reset()` | Clears every cache. |
| `private static String` | `stateName(QuestState state)` | Maps a quest state to its exported string form. |
| `private GeOffer` | `toGeOffer(int slot, GrandExchangeOffer offer)` | Converts a client GE offer into the exported model. |
| `public void` | `updateBank(ItemContainer container)` | Replaces the cached bank contents with a flat item list. |
| `public void` | `updateEquipment(ItemContainer container)` | Rebuilds the cached equipment from scratch so an unequipped slot is dropped. |
| `public void` | `updateGeOffer(int slot, GrandExchangeOffer offer)` | Adds, replaces, or removes a cached Grand Exchange offer for one slot. |
| `public void` | `updateInventory(ItemContainer container)` | Replaces the cached inventory, keeping empty slots so slot indices stay meaningful. |
| `public void` | `updateSkill(Skill skill, int level, int xp, int boostedLevel)` | Updates a single skill from a stat-changed event. |

### Field Detail

#### EQUIPMENT_SLOTS

`private static final String[] EQUIPMENT_SLOTS`

#### bankItems

`private List<ItemEntry> bankItems`

#### bankTotal

`private int bankTotal`

#### client

`private final Client client`

#### combatAchievements

`private CombatAchievementData combatAchievements`

#### config

`private final PlayerDataExportConfig config`

#### diaries

`private Map<String,DiaryRegion> diaries`

#### equipment

`private Map<String,ItemEntry> equipment`

#### geOffers

`private final Map<Integer,GeOffer> geOffers`

#### inventory

`private List<InventoryItem> inventory`

#### questPoints

`private Integer questPoints`

#### quests

`private List<QuestEntry> quests`

#### skills

`private final Map<String,SkillEntry> skills`

#### slayer

`private SlayerData slayer`

### Constructor Detail

#### PlayerDataCollector

`public PlayerDataCollector(Client client, PlayerDataExportConfig config)`

- **Parameter** `client` — the RuneLite client
- **Parameter** `config` — the plugin configuration

### Method Detail

#### buildLocation

`private LocationData buildLocation(Player localPlayer)`

Reads the character's world location.

- **Parameter** `localPlayer` — the local player
- **Returns:** the location, or `null` when it is unavailable

#### buildSnapshot

`public PlayerSyncData buildSnapshot()`

Assembles the export snapshot from the caches and cheap client reads, honouring the config
toggles. Owned collections are copied so the writer thread reads a stable graph.

- **Returns:** the snapshot, or `null` when no character is logged in

#### buildVitals

`private VitalsData buildVitals()`

Reads current combat vitals from boosted and real skill levels and run energy.

- **Returns:** the vitals snapshot

#### diaryRegion

`private DiaryRegion diaryRegion(int easy, int medium, int hard, int elite)`

Reads a diary region's four tiers, each complete when its varbit equals 1.

- **Parameter** `easy` — the easy-tier varbit id
- **Parameter** `medium` — the medium-tier varbit id
- **Parameter** `hard` — the hard-tier varbit id
- **Parameter** `elite` — the elite-tier varbit id
- **Returns:** the region's completion state

#### getItemName

`private String getItemName(int itemId)`

Resolves an item id to its display name.

- **Parameter** `itemId` — the item id
- **Returns:** the item name, or `null` when it cannot be resolved

#### pollAllGeOffers

`public void pollAllGeOffers()`

Rebuilds the Grand Exchange cache from the client's current offer array.

#### pollAllSkills

`public void pollAllSkills()`

Reads every skill from the client, plus a computed `OVERALL` total.

#### pollCombatAchievements

`public boolean pollCombatAchievements()`

Polls the six combat-achievement tier varbits.

- **Returns:** whether any tier value changed since the last poll

#### pollDiaries

`public boolean pollDiaries()`

Polls achievement-diary completion for every region via named RuneLite varbits.

- **Returns:** whether any diary completion changed since the last poll

#### pollQuests

`public boolean pollQuests()`

Polls quest completion state and total quest points.

- **Returns:** whether either the quest states or the quest-point total changed since the last poll

#### pollSlayer

`public boolean pollSlayer()`

Polls the current slayer task, clearing it when no task is assigned.

- **Returns:** whether the slayer task changed since the last poll

#### reset

`public void reset()`

Clears every cache. Called on logout and account change so one character's data can never leak
into another character's export file.

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

Replaces the cached bank contents with a flat item list.

- **Parameter** `container` — the bank item container

#### updateEquipment

`public void updateEquipment(ItemContainer container)`

Rebuilds the cached equipment from scratch so an unequipped slot is dropped.

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

## com.oveduumnakal.dataexport.PlayerDataExportConfig

_interface_

`public interface PlayerDataExportConfig`

Configuration for the Player Data Export plugin: the save location and interval, and a toggle
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

## com.oveduumnakal.dataexport.PlayerDataExportPlugin

_class_

`public class PlayerDataExportPlugin`

Exports the logged-in character's data to a local JSON file for use by external tools. Data is
collected on the client thread from events and periodic polling, and written off-thread. The
plugin makes no network calls of any kind.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `private static final int` | `INITIAL_DELAY_TICKS` |  |
| `private static final int` | `POLL_INTERVAL_TICKS` |  |
| `private Client` | `client` |  |
| `private PlayerDataCollector` | `collector` |  |
| `private PlayerDataExportConfig` | `config` |  |
| `private volatile boolean` | `dirty` |  |
| `private ScheduledExecutorService` | `executor` |  |
| `private Gson` | `gson` |  |
| `private boolean` | `initialCollectionDone` |  |
| `private String` | `lastPlayerName` |  |
| `private int` | `syncTickThreshold` |  |
| `private int` | `tickCounter` |  |
| `private PlayerDataWriter` | `writer` |  |

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
| `PlayerDataExportConfig` | `provideConfig(ConfigManager configManager)` | Provides the plugin configuration. |
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

`private PlayerDataCollector collector`

#### config

`private PlayerDataExportConfig config`

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

`private PlayerDataWriter writer`

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
character's data cannot leak into another's file.

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

`PlayerDataExportConfig provideConfig(ConfigManager configManager)`

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

## com.oveduumnakal.dataexport.PlayerDataWriter

_class_

`public class PlayerDataWriter`

Serializes a `PlayerSyncData` snapshot to a per-character JSON file. The write is atomic:
the JSON is written to a temporary file in the target directory and then renamed over the
destination, so a second process polling the file never observes a half-written document.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `private final Gson` | `gson` |  |

### Constructor Summary

| Constructor | Description |
|---|---|
| `PlayerDataWriter(Gson gson)` |  |

### Method Summary

| Modifier and Type | Method | Description |
|---|---|---|
| `private static void` | `deleteQuietly(Path path)` | Deletes a temporary file, ignoring any failure. |
| `static String` | `filenameFor(String username)` | Builds the JSON filename for a username, lower-casing it and replacing every character outside `[a-z0-9_-]` with an underscore so the name cannot escape the target directory. |
| `public boolean` | `write(PlayerSyncData data, File syncDir)` | Writes the snapshot to ` / .json` atomically. |

### Field Detail

#### gson

`private final Gson gson`

### Constructor Detail

#### PlayerDataWriter

`public PlayerDataWriter(Gson gson)`

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

#### write

`public boolean write(PlayerSyncData data, File syncDir)`

Writes the snapshot to `<syncDir>/<sanitized-username>.json` atomically.

- **Parameter** `data` — the snapshot to write
- **Parameter** `syncDir` — the directory to write into (created if absent)
- **Returns:** `true` if the file was written, `false` on missing data or I/O error

---

## com.oveduumnakal.dataexport.model.PlayerSyncData

_class_

`public class PlayerSyncData`

Top-level data model written to the local JSON export file, one file per character. Serialized
to JSON via Gson. Fields left `null` are omitted or written as `null` depending on
the configured toggles. See `SCHEMA.md` for the field-by-field contract.

### Nested Type Summary

| Type | Description |
|---|---|
| _class_ [`BankData`](#comoveduumnakaldataexportmodelplayersyncdatabankdata) | Bank contents: total distinct item count and a flat list of the stacks it holds. |
| _class_ [`CombatAchievementData`](#comoveduumnakaldataexportmodelplayersyncdatacombatachievementdata) | Combat-achievement tier progress. |
| _class_ [`DiaryRegion`](#comoveduumnakaldataexportmodelplayersyncdatadiaryregion) | Completion of the four achievement-diary tiers for one region. |
| _class_ [`GeOffer`](#comoveduumnakaldataexportmodelplayersyncdatageoffer) | One active Grand Exchange offer. |
| _class_ [`InventoryItem`](#comoveduumnakaldataexportmodelplayersyncdatainventoryitem) | An inventory item, carrying its slot index in addition to the item stack. |
| _class_ [`ItemEntry`](#comoveduumnakaldataexportmodelplayersyncdataitementry) | One stack of items: id, resolved name, and quantity. |
| _class_ [`LocationData`](#comoveduumnakaldataexportmodelplayersyncdatalocationdata) | World location of the character. |
| _class_ [`PlayerInfo`](#comoveduumnakaldataexportmodelplayersyncdataplayerinfo) | Identity and account context of the logged-in character. |
| _class_ [`QuestEntry`](#comoveduumnakaldataexportmodelplayersyncdataquestentry) | Completion state of one quest. |
| _class_ [`SkillEntry`](#comoveduumnakaldataexportmodelplayersyncdataskillentry) | Real level and experience for one skill, plus the current boosted level. |
| _class_ [`SlayerData`](#comoveduumnakaldataexportmodelplayersyncdataslayerdata) | The current slayer task. |
| _class_ [`VitalsData`](#comoveduumnakaldataexportmodelplayersyncdatavitalsdata) | Current combat vitals: hitpoints, prayer, and run energy. |

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public Map<String,DiaryRegion>` | `achievementDiaries` | Achievement diary completion keyed by region name. |
| `public BankData` | `bank` | Bank contents as a flat item list, captured when the bank is opened. |
| `public CombatAchievementData` | `combatAchievements` | Combat-achievement tier progress. |
| `public Map<String,ItemEntry>` | `equipment` | Worn equipment keyed by slot name. |
| `public List<GeOffer>` | `grandExchange` | Active Grand Exchange offers by slot. |
| `public List<InventoryItem>` | `inventory` | Inventory contents, one entry per slot (empty slots have item id -1). |
| `public String` | `lastUpdated` | ISO-8601 instant at which this snapshot was built. |
| `public LocationData` | `location` | Current world location of the character. |
| `public PlayerInfo` | `player` | Identity and account context of the logged-in character. |
| `public Integer` | `questPoints` | Total quest points. |
| `public List<QuestEntry>` | `quests` | Quest completion state, one entry per quest. |
| `public int` | `schemaVersion` | Schema version of this file; bumped when the shape of the export changes. |
| `public Map<String,SkillEntry>` | `skills` | Real level and experience per skill, keyed by upper-case skill name, plus `OVERALL`. |
| `public SlayerData` | `slayer` | Current slayer task, or `null` when no task is assigned. |
| `public VitalsData` | `vitals` | Current combat vitals (hitpoints, prayer, run energy). |

### Field Detail

#### achievementDiaries

`public Map<String,DiaryRegion> achievementDiaries`

Achievement diary completion keyed by region name.

#### bank

`public BankData bank`

Bank contents as a flat item list, captured when the bank is opened.

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

#### schemaVersion

`public int schemaVersion`

Schema version of this file; bumped when the shape of the export changes.

#### skills

`public Map<String,SkillEntry> skills`

Real level and experience per skill, keyed by upper-case skill name, plus `OVERALL`.

#### slayer

`public SlayerData slayer`

Current slayer task, or `null` when no task is assigned.

#### vitals

`public VitalsData vitals`

Current combat vitals (hitpoints, prayer, run energy).

---

## com.oveduumnakal.dataexport.model.PlayerSyncData.BankData

_class_

`public static class BankData`

Bank contents: total distinct item count and a flat list of the stacks it holds.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public List<ItemEntry>` | `items` | Bank contents as a flat item list. |
| `public int` | `totalItems` | Number of distinct item stacks in the bank. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `BankData(int totalItems, List<ItemEntry> items)` |  |

### Field Detail

#### items

`public List<ItemEntry> items`

Bank contents as a flat item list.

#### totalItems

`public int totalItems`

Number of distinct item stacks in the bank.

### Constructor Detail

#### BankData

`public BankData(int totalItems, List<ItemEntry> items)`

- **Parameter** `totalItems` — distinct stack count
- **Parameter** `items` — flat item list

---

## com.oveduumnakal.dataexport.model.PlayerSyncData.CombatAchievementData

_class_

`public static class CombatAchievementData`

Combat-achievement tier progress. Each field is the raw value of the corresponding RuneLite
`Varbits.COMBAT_ACHIEVEMENT_TIER_*` varbit; see `SCHEMA.md` for its meaning.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
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

### Field Detail

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

---

## com.oveduumnakal.dataexport.model.PlayerSyncData.DiaryRegion

_class_

`public static class DiaryRegion`

Completion of the four achievement-diary tiers for one region.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public boolean` | `easy` | Whether the easy tier is complete. |
| `public boolean` | `elite` | Whether the elite tier is complete. |
| `public boolean` | `hard` | Whether the hard tier is complete. |
| `public boolean` | `medium` | Whether the medium tier is complete. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `DiaryRegion(boolean easy, boolean medium, boolean hard, boolean elite)` |  |

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

### Constructor Detail

#### DiaryRegion

`public DiaryRegion(boolean easy, boolean medium, boolean hard, boolean elite)`

- **Parameter** `easy` — easy complete
- **Parameter** `medium` — medium complete
- **Parameter** `hard` — hard complete
- **Parameter** `elite` — elite complete

---

## com.oveduumnakal.dataexport.model.PlayerSyncData.GeOffer

_class_

`public static class GeOffer`

One active Grand Exchange offer.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public int` | `itemId` | Item id being traded. |
| `public String` | `name` | Resolved item name. |
| `public int` | `price` | Per-item price of the offer. |
| `public int` | `quantitySold` | Quantity bought or sold so far. |
| `public int` | `slot` | Slot index, 0–7. |
| `public int` | `spent` | Coins spent or gained so far. |
| `public String` | `state` | Offer state, e.g. |
| `public int` | `totalQuantity` | Total quantity of the offer. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `GeOffer(int slot, int itemId, String name, String state, int quantitySold, int totalQuantity, int price, int spent)` |  |

### Field Detail

#### itemId

`public int itemId`

Item id being traded.

#### name

`public String name`

Resolved item name.

#### price

`public int price`

Per-item price of the offer.

#### quantitySold

`public int quantitySold`

Quantity bought or sold so far.

#### slot

`public int slot`

Slot index, 0–7.

#### spent

`public int spent`

Coins spent or gained so far.

#### state

`public String state`

Offer state, e.g. `BUYING`, `SOLD`, `CANCELLED_BUY`.

#### totalQuantity

`public int totalQuantity`

Total quantity of the offer.

### Constructor Detail

#### GeOffer

`public GeOffer(int slot, int itemId, String name, String state, int quantitySold, int totalQuantity, int price, int spent)`

- **Parameter** `slot` — slot index
- **Parameter** `itemId` — item id
- **Parameter** `name` — resolved name
- **Parameter** `state` — offer state
- **Parameter** `quantitySold` — quantity transacted
- **Parameter** `totalQuantity` — total quantity
- **Parameter** `price` — per-item price
- **Parameter** `spent` — coins moved

---

## com.oveduumnakal.dataexport.model.PlayerSyncData.InventoryItem

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

## com.oveduumnakal.dataexport.model.PlayerSyncData.ItemEntry

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

## com.oveduumnakal.dataexport.model.PlayerSyncData.LocationData

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

## com.oveduumnakal.dataexport.model.PlayerSyncData.PlayerInfo

_class_

`public static class PlayerInfo`

Identity and account context of the logged-in character.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public String` | `accountType` | Account type, e.g. |
| `public int` | `combatLevel` | Combat level. |
| `public boolean` | `members` | Whether the current world is a members' world. |
| `public String` | `username` | In-game display name. |
| `public int` | `world` | World the character is logged into. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `PlayerInfo(String username, int combatLevel, int world, String accountType, boolean members)` |  |

### Field Detail

#### accountType

`public String accountType`

Account type, e.g. `NORMAL`, `IRONMAN`, `HARDCORE_IRONMAN`.

#### combatLevel

`public int combatLevel`

Combat level.

#### members

`public boolean members`

Whether the current world is a members' world.

#### username

`public String username`

In-game display name.

#### world

`public int world`

World the character is logged into.

### Constructor Detail

#### PlayerInfo

`public PlayerInfo(String username, int combatLevel, int world, String accountType, boolean members)`

- **Parameter** `username` — display name
- **Parameter** `combatLevel` — combat level
- **Parameter** `world` — current world
- **Parameter** `accountType` — account-type name
- **Parameter** `members` — whether on a members' world

---

## com.oveduumnakal.dataexport.model.PlayerSyncData.QuestEntry

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

## com.oveduumnakal.dataexport.model.PlayerSyncData.SkillEntry

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

## com.oveduumnakal.dataexport.model.PlayerSyncData.SlayerData

_class_

`public static class SlayerData`

The current slayer task. The creature is exported as its game id; consumers map it to a name.

### Field Summary

| Modifier and Type | Field | Description |
|---|---|---|
| `public int` | `amount` | Remaining kill count. |
| `public int` | `creatureId` | Game id of the assigned creature. |
| `public int` | `points` | Slayer reward points. |
| `public int` | `streak` | Current task-completion streak. |

### Constructor Summary

| Constructor | Description |
|---|---|
| `SlayerData(int creatureId, int amount, int points, int streak)` |  |

### Field Detail

#### amount

`public int amount`

Remaining kill count.

#### creatureId

`public int creatureId`

Game id of the assigned creature.

#### points

`public int points`

Slayer reward points.

#### streak

`public int streak`

Current task-completion streak.

### Constructor Detail

#### SlayerData

`public SlayerData(int creatureId, int amount, int points, int streak)`

- **Parameter** `creatureId` — assigned-creature id
- **Parameter** `amount` — remaining kills
- **Parameter** `points` — reward points
- **Parameter** `streak` — completion streak

---

## com.oveduumnakal.dataexport.model.PlayerSyncData.VitalsData

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
