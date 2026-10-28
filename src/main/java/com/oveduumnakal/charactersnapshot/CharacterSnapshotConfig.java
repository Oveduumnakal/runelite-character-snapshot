/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

/**
 * Configuration for the Character Snapshot plugin: the save location and interval, and a toggle
 * for each category of data written to the export file.
 */
@ConfigGroup(CharacterSnapshotConfig.GROUP)
public interface CharacterSnapshotConfig extends Config
{
	/** Config group key, shared with {@code ConfigChanged} handling. */
	String GROUP = "charactersnapshot";

	/** Storage section: where and how often data is written. */
	@ConfigSection(
		name = "Storage",
		description = "Where and how often data is written to disk",
		position = 0
	)
	String storageSection = "storage";

	/** Data section: which categories of data to include. */
	@ConfigSection(
		name = "Data",
		description = "Choose what data to include in the export",
		position = 1
	)
	String dataSection = "data";

	/**
	 * @return how often, in seconds, updated data is written to disk (minimum 30)
	 */
	@ConfigItem(
		keyName = "syncIntervalSeconds",
		name = "Save interval",
		description = "How often to save updated data to disk",
		section = storageSection,
		position = 0
	)
	@Range(min = 30, max = 3600)
	@Units(Units.SECONDS)
	default int syncIntervalSeconds()
	{
		return 60;
	}

	/**
	 * @return a custom directory to write export files into, or blank to use the default
	 *         ({@code .runelite/osrs-companion})
	 */
	@ConfigItem(
		keyName = "saveDirectory",
		name = "Save folder",
		description = "Folder to write export files into. Leave blank for the default "
			+ "(.runelite/osrs-companion). An invalid folder falls back to the default.",
		section = storageSection,
		position = 1
	)
	default String saveDirectory()
	{
		return "";
	}

	/**
	 * @return whether to include skill levels, experience, and boosted levels
	 */
	@ConfigItem(
		keyName = "syncSkills",
		name = "Skills",
		description = "Include skill levels, experience, and boosted levels",
		section = dataSection,
		position = 0
	)
	default boolean syncSkills()
	{
		return true;
	}

	/**
	 * @return whether to include current hitpoints, prayer, and run energy
	 */
	@ConfigItem(
		keyName = "syncVitals",
		name = "Vitals",
		description = "Include current hitpoints, prayer, and run energy",
		section = dataSection,
		position = 1
	)
	default boolean syncVitals()
	{
		return true;
	}

	/**
	 * @return whether to include bank contents (captured when the bank is opened)
	 */
	@ConfigItem(
		keyName = "syncBank",
		name = "Bank",
		description = "Include bank contents (captured when the bank is opened)",
		section = dataSection,
		position = 2
	)
	default boolean syncBank()
	{
		return true;
	}

	/**
	 * @return whether to include the current inventory contents
	 */
	@ConfigItem(
		keyName = "syncInventory",
		name = "Inventory",
		description = "Include current inventory contents",
		section = dataSection,
		position = 3
	)
	default boolean syncInventory()
	{
		return true;
	}

	/**
	 * @return whether to include currently worn equipment
	 */
	@ConfigItem(
		keyName = "syncEquipment",
		name = "Equipment",
		description = "Include currently worn equipment",
		section = dataSection,
		position = 4
	)
	default boolean syncEquipment()
	{
		return true;
	}

	/**
	 * @return whether to include quest completion state and total quest points
	 */
	@ConfigItem(
		keyName = "syncQuests",
		name = "Quests",
		description = "Include quest completion state and total quest points",
		section = dataSection,
		position = 5
	)
	default boolean syncQuests()
	{
		return true;
	}

	/**
	 * @return whether to include achievement diary completion
	 */
	@ConfigItem(
		keyName = "syncDiaries",
		name = "Achievement diaries",
		description = "Include achievement diary completion",
		section = dataSection,
		position = 6
	)
	default boolean syncDiaries()
	{
		return true;
	}

	/**
	 * @return whether to include combat-achievement tier progress
	 */
	@ConfigItem(
		keyName = "syncCombatAchievements",
		name = "Combat achievements",
		description = "Include combat-achievement tier progress",
		section = dataSection,
		position = 7
	)
	default boolean syncCombatAchievements()
	{
		return true;
	}

	/**
	 * @return whether to include the current slayer task
	 */
	@ConfigItem(
		keyName = "syncSlayer",
		name = "Slayer task",
		description = "Include the current slayer task (creature, amount, points, streak)",
		section = dataSection,
		position = 8
	)
	default boolean syncSlayer()
	{
		return true;
	}

	/**
	 * @return whether to include the character's world location
	 */
	@ConfigItem(
		keyName = "syncLocation",
		name = "Location",
		description = "Include the character's world location (region and coordinates)",
		section = dataSection,
		position = 9
	)
	default boolean syncLocation()
	{
		return true;
	}

	/**
	 * @return whether to include account type, membership, spellbook and prayer/magic setup
	 */
	@ConfigItem(
		keyName = "syncAccountInfo",
		name = "Account info",
		description = "Include account type, membership status, spellbook, unlocked prayers, quick prayers and "
			+ "autocast spell",
		section = dataSection,
		position = 10
	)
	default boolean syncAccountInfo()
	{
		return true;
	}

	/**
	 * @return whether to include active Grand Exchange offers
	 */
	@ConfigItem(
		keyName = "syncGrandExchange",
		name = "Grand Exchange offers",
		description = "Include active Grand Exchange offers",
		section = dataSection,
		position = 11
	)
	default boolean syncGrandExchange()
	{
		return true;
	}

	/**
	 * @return whether to include the rune pouch contents
	 */
	@ConfigItem(
		keyName = "syncRunePouch",
		name = "Rune pouch",
		description = "Include the runes held in your rune pouch (only while you carry one)",
		section = dataSection,
		position = 12
	)
	default boolean syncRunePouch()
	{
		return true;
	}

	/**
	 * @return whether to include looting bag, seed vault and group storage contents
	 */
	@ConfigItem(
		keyName = "syncStorages",
		name = "Storage containers",
		description = "Include the last known contents of the looting bag, seed vault and group storage "
			+ "(captured when opened)",
		section = dataSection,
		position = 13
	)
	default boolean syncStorages()
	{
		return true;
	}

	/**
	 * @return whether to include the collection log
	 */
	@ConfigItem(
		keyName = "syncCollectionLog",
		name = "Collection log",
		description = "Include the collection log, captured as pages are viewed in the log interface",
		section = dataSection,
		position = 14
	)
	default boolean syncCollectionLog()
	{
		return true;
	}

	/**
	 * @return whether to include boss kill counts
	 */
	@ConfigItem(
		keyName = "syncKillCounts",
		name = "Boss kill counts",
		description = "Include boss kill counts (read from the Chat Commands plugin's stored counts and from "
			+ "kill-count chat messages)",
		section = dataSection,
		position = 15
	)
	default boolean syncKillCounts()
	{
		return true;
	}

	/**
	 * @return whether to include completed clue scroll counts
	 */
	@ConfigItem(
		keyName = "syncClueCounts",
		name = "Clue scroll counts",
		description = "Include completed clue scroll counts per tier (read from completion messages in chat)",
		section = dataSection,
		position = 16
	)
	default boolean syncClueCounts()
	{
		return true;
	}

	/**
	 * @return whether to include farming patch and birdhouse timers
	 */
	@ConfigItem(
		keyName = "syncTimers",
		name = "Farming and birdhouse timers",
		description = "Include farming patch and birdhouse timers read from the Time Tracking plugin's data "
			+ "(only as fresh as your last visit to each patch)",
		section = dataSection,
		position = 17
	)
	default boolean syncTimers()
	{
		return true;
	}

	/**
	 * @return whether to include daily reward claim status and the Kingdom of Miscellania
	 */
	@ConfigItem(
		keyName = "syncDailies",
		name = "Daily rewards",
		description = "Include which daily rewards (herb boxes, battlestaves, sand...) are unlocked and "
			+ "claimed today, and Kingdom of Miscellania approval and coffer",
		section = dataSection,
		position = 18
	)
	default boolean syncDailies()
	{
		return true;
	}

	/**
	 * @return whether to include minigame reward-shop points
	 */
	@ConfigItem(
		keyName = "syncMinigamePoints",
		name = "Minigame points",
		description = "Include minigame reward points (Nightmare Zone, Tithe Farm, Giants' Foundry, Mixology, "
			+ "Tempoross, Soul Wars, kudos...) and the Mixology hopper paste",
		section = dataSection,
		position = 19
	)
	default boolean syncMinigamePoints()
	{
		return true;
	}

	/**
	 * @return whether to include transport unlocks (fairy rings, quetzal sites)
	 */
	@ConfigItem(
		keyName = "syncTransport",
		name = "Transport unlocks",
		description = "Include fairy ring access and travel log, and available quetzal landing sites",
		section = dataSection,
		position = 20
	)
	default boolean syncTransport()
	{
		return true;
	}

	/**
	 * @return whether to include player-owned house fields
	 */
	@ConfigItem(
		keyName = "syncHouse",
		name = "Player-owned house",
		description = "Include house location, style, servant, jewellery box and portal nexus teleports "
			+ "(raw game values)",
		section = dataSection,
		position = 21
	)
	default boolean syncHouse()
	{
		return true;
	}

	/**
	 * @return whether to include Sailing boats, crew, charting and Barracuda Trials
	 */
	@ConfigItem(
		keyName = "syncSailing",
		name = "Sailing",
		description = "Include owned boats and their parts, unlocked crew, charting progress and Barracuda Trials",
		section = dataSection,
		position = 22
	)
	default boolean syncSailing()
	{
		return true;
	}
}
