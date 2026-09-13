/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.dataexport;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

/**
 * Configuration for the Player Data Export plugin: the save location and interval, and a toggle
 * for each category of data written to the export file.
 */
@ConfigGroup(PlayerDataExportConfig.GROUP)
public interface PlayerDataExportConfig extends Config
{
	/** Config group key, shared with {@code ConfigChanged} handling. */
	String GROUP = "playerdataexport";

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
	 * @return whether to include account type, membership, and world
	 */
	@ConfigItem(
		keyName = "syncAccountInfo",
		name = "Account info",
		description = "Include account type and membership status",
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
}
