/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.EqualsAndHashCode;

/**
 * Top-level data model written to the local JSON export file, one file per character. Serialized
 * to JSON via Gson. Fields left {@code null} are omitted or written as {@code null} depending on
 * the configured toggles. See {@code SCHEMA.md} for the field-by-field contract.
 */
public class CharacterSnapshot
{
	/** Schema version of this file; bumped when the shape of the export changes. */
	public int schemaVersion = 2;

	/** ISO-8601 instant at which this snapshot was built. */
	public String lastUpdated;

	/**
	 * ISO-8601 instant each item section ({@code bank}, {@code inventory}, {@code equipment}) was last
	 * captured from the client, keyed by section name. Earlier than {@link #lastUpdated} when the section
	 * was carried over from a previous session because it has not been re-captured yet.
	 */
	public Map<String, String> sectionUpdated;

	/** Identity and account context of the logged-in character. */
	public PlayerInfo player;

	/** Real level and experience per skill, keyed by upper-case skill name, plus {@code OVERALL}. */
	public Map<String, SkillEntry> skills;

	/** Current combat vitals (hitpoints, prayer, run energy). */
	public VitalsData vitals;

	/**
	 * Bank contents as a flat item list, captured when the bank is opened and carried over from the
	 * previous session until then.
	 */
	public BankData bank;

	/** Inventory contents, one entry per slot (empty slots have item id -1). */
	public List<InventoryItem> inventory;

	/** Worn equipment keyed by slot name. */
	public Map<String, ItemEntry> equipment;

	/** Quest completion state, one entry per quest. */
	public List<QuestEntry> quests;

	/** Total quest points. */
	public Integer questPoints;

	/** Achievement diary completion keyed by region name. */
	public Map<String, DiaryRegion> achievementDiaries;

	/** Combat-achievement tier progress. */
	public CombatAchievementData combatAchievements;

	/** Current slayer task, or {@code null} when no task is assigned. */
	public SlayerData slayer;

	/** Current world location of the character. */
	public LocationData location;

	/** Active Grand Exchange offers by slot. */
	public List<GeOffer> grandExchange;

	/**
	 * Identity and account context of the logged-in character.
	 */
	public static class PlayerInfo
	{
		/** In-game display name. */
		public String username;

		/** Combat level. */
		public int combatLevel;

		/** World the character is logged into. */
		public int world;

		/** Account type, e.g. {@code NORMAL}, {@code IRONMAN}, {@code HARDCORE_IRONMAN}. */
		public String accountType;

		/** Whether the current world is a members' world; {@code null} when account info is not exported. */
		public Boolean members;

		/**
		 * @param username    display name
		 * @param combatLevel combat level
		 * @param world       current world
		 * @param accountType account-type name
		 * @param members     whether on a members' world, or {@code null} when not exported
		 */
		public PlayerInfo(String username, int combatLevel, int world, String accountType, Boolean members)
		{
			this.username = username;
			this.combatLevel = combatLevel;
			this.world = world;
			this.accountType = accountType;
			this.members = members;
		}
	}

	/**
	 * Real level and experience for one skill, plus the current boosted level.
	 */
	public static class SkillEntry
	{
		/** Real (unboosted) level. */
		public int level;

		/** Experience points (a {@code long} so the OVERALL total does not overflow). */
		public long xp;

		/** Current boosted level (equals {@code level} when unboosted). */
		public int boostedLevel;

		/**
		 * @param level        real level
		 * @param xp           experience points
		 * @param boostedLevel current boosted level
		 */
		public SkillEntry(int level, long xp, int boostedLevel)
		{
			this.level = level;
			this.xp = xp;
			this.boostedLevel = boostedLevel;
		}
	}

	/**
	 * Current combat vitals: hitpoints, prayer, and run energy.
	 */
	public static class VitalsData
	{
		/** Current (boosted) hitpoints. */
		public int currentHitpoints;

		/** Maximum (real) hitpoints level. */
		public int maxHitpoints;

		/** Current (boosted) prayer points. */
		public int currentPrayer;

		/** Maximum (real) prayer level. */
		public int maxPrayer;

		/** Run energy as a percentage, 0–100. */
		public int runEnergyPercent;

		/**
		 * @param currentHitpoints current hitpoints
		 * @param maxHitpoints     maximum hitpoints
		 * @param currentPrayer    current prayer
		 * @param maxPrayer        maximum prayer
		 * @param runEnergyPercent run energy percent
		 */
		public VitalsData(int currentHitpoints, int maxHitpoints, int currentPrayer, int maxPrayer,
			int runEnergyPercent)
		{
			this.currentHitpoints = currentHitpoints;
			this.maxHitpoints = maxHitpoints;
			this.currentPrayer = currentPrayer;
			this.maxPrayer = maxPrayer;
			this.runEnergyPercent = runEnergyPercent;
		}
	}

	/**
	 * One stack of items: id, resolved name, and quantity.
	 */
	public static class ItemEntry
	{
		/** Item id. */
		public int itemId;

		/** Resolved item name, or {@code null} for an empty slot. */
		public String name;

		/** Stack quantity. */
		public int quantity;

		/**
		 * @param itemId   item id
		 * @param name     resolved name
		 * @param quantity stack quantity
		 */
		public ItemEntry(int itemId, String name, int quantity)
		{
			this.itemId = itemId;
			this.name = name;
			this.quantity = quantity;
		}
	}

	/**
	 * An inventory item, carrying its slot index in addition to the item stack.
	 */
	public static class InventoryItem extends ItemEntry
	{
		/** Zero-based inventory slot. */
		public int slot;

		/**
		 * @param itemId   item id
		 * @param name     resolved name
		 * @param quantity stack quantity
		 * @param slot     inventory slot index
		 */
		public InventoryItem(int itemId, String name, int quantity, int slot)
		{
			super(itemId, name, quantity);
			this.slot = slot;
		}
	}

	/**
	 * Bank contents: total distinct item count and a flat list of the stacks it holds.
	 */
	public static class BankData
	{
		/** Number of distinct item stacks in the bank. */
		public int totalItems;

		/** Bank contents as a flat item list. */
		public List<ItemEntry> items;

		/**
		 * The bank split into its real tabs: the main tab is {@code tabIndex} 0 and the numbered tabs
		 * are 1 to 9. Never null when the bank is present; an export from before tabs were modelled
		 * holds a single tab 0 wrapping the flat list.
		 */
		public List<BankTab> tabs;

		/**
		 * Creates bank data whose only tab is the main tab holding every item.
		 *
		 * @param totalItems distinct stack count
		 * @param items      flat item list
		 */
		public BankData(int totalItems, List<ItemEntry> items)
		{
			this(totalItems, items, Collections.singletonList(new BankTab(0, items)));
		}

		/**
		 * @param totalItems distinct stack count
		 * @param items      flat item list
		 * @param tabs       the real bank tabs
		 */
		public BankData(int totalItems, List<ItemEntry> items, List<BankTab> tabs)
		{
			this.totalItems = totalItems;
			this.items = items;
			this.tabs = tabs;
		}
	}

	/**
	 * A bank tab: an index (0 for the main tab, 1 to 9 for the numbered tabs) and the items it holds.
	 */
	public static class BankTab
	{
		/** Tab index: 0 is the main tab, 1 to 9 are the numbered tabs. */
		public int tabIndex;

		/** Items in this tab. */
		public List<ItemEntry> items;

		/**
		 * @param tabIndex the tab index
		 * @param items    the items in the tab
		 */
		public BankTab(int tabIndex, List<ItemEntry> items)
		{
			this.tabIndex = tabIndex;
			this.items = items;
		}
	}

	/**
	 * Completion state of one quest.
	 */
	@EqualsAndHashCode
	public static class QuestEntry
	{
		/** Enum name of the quest. */
		public String name;

		/** Human-readable quest name. */
		public String displayName;

		/** One of {@code NOT_STARTED}, {@code IN_PROGRESS}, {@code FINISHED}. */
		public String state;

		/**
		 * @param name        enum name
		 * @param displayName readable name
		 * @param state       completion state
		 */
		public QuestEntry(String name, String displayName, String state)
		{
			this.name = name;
			this.displayName = displayName;
			this.state = state;
		}
	}

	/**
	 * Completion of the four achievement-diary tiers for one region, plus the individual tasks where known.
	 */
	@EqualsAndHashCode
	public static class DiaryRegion
	{
		/** Whether the easy tier is complete. */
		public boolean easy;

		/** Whether the medium tier is complete. */
		public boolean medium;

		/** Whether the hard tier is complete. */
		public boolean hard;

		/** Whether the elite tier is complete. */
		public boolean elite;

		/**
		 * Individual task completion keyed by tier ({@code easy}, {@code medium}, {@code hard},
		 * {@code elite}). Absent when no task of the region is mapped; a tier is absent when none of
		 * its tasks are mapped.
		 */
		public Map<String, List<DiaryTask>> tasks;

		/**
		 * @param easy   easy complete
		 * @param medium medium complete
		 * @param hard   hard complete
		 * @param elite  elite complete
		 */
		public DiaryRegion(boolean easy, boolean medium, boolean hard, boolean elite)
		{
			this(easy, medium, hard, elite, null);
		}

		/**
		 * @param easy   easy complete
		 * @param medium medium complete
		 * @param hard   hard complete
		 * @param elite  elite complete
		 * @param tasks  per-tier task completion, or {@code null} when none is mapped
		 */
		public DiaryRegion(boolean easy, boolean medium, boolean hard, boolean elite,
			Map<String, List<DiaryTask>> tasks)
		{
			this.easy = easy;
			this.medium = medium;
			this.hard = hard;
			this.elite = elite;
			this.tasks = tasks;
		}
	}

	/**
	 * One achievement-diary task and whether it is complete.
	 */
	@EqualsAndHashCode
	public static class DiaryTask
	{
		/** Task text. */
		public String name;

		/** Whether the task is complete. */
		public boolean completed;

		/**
		 * @param name      task text
		 * @param completed whether the task is complete
		 */
		public DiaryTask(String name, boolean completed)
		{
			this.name = name;
			this.completed = completed;
		}
	}

	/**
	 * Combat-achievement tier progress. Each field is the raw value of the corresponding RuneLite
	 * {@code Varbits.COMBAT_ACHIEVEMENT_TIER_*} varbit; see {@code SCHEMA.md} for its meaning.
	 */
	@EqualsAndHashCode
	public static class CombatAchievementData
	{
		/** Raw easy-tier varbit value. */
		public int tierEasy;

		/** Raw medium-tier varbit value. */
		public int tierMedium;

		/** Raw hard-tier varbit value. */
		public int tierHard;

		/** Raw elite-tier varbit value. */
		public int tierElite;

		/** Raw master-tier varbit value. */
		public int tierMaster;

		/** Raw grandmaster-tier varbit value. */
		public int tierGrandmaster;

		/** Compatibility flag: whether the easy tier has any progress ({@code tierEasy > 0}). */
		public boolean easyComplete;

		/** Compatibility flag: whether the medium tier has any progress. */
		public boolean mediumComplete;

		/** Compatibility flag: whether the hard tier has any progress. */
		public boolean hardComplete;

		/** Compatibility flag: whether the elite tier has any progress. */
		public boolean eliteComplete;

		/**
		 * Compatibility field: names of completed tasks whose name resolved from the game cache. Always a
		 * list of strings; external consumers depend on that.
		 */
		public List<String> completedTasks;

		/**
		 * Every combat-achievement task with its completion flag. When the cache metadata cannot be
		 * resolved, holds only the completed task ids with null name, tier, and monster.
		 */
		public List<CombatAchievementTask> tasks;

		/**
		 * Total combat-achievement points. Read from the game's points varbit when it is non-zero; otherwise
		 * the sum of completed task points, or null when a completed task's tier is unresolved.
		 */
		public Integer points;

		/**
		 * Completed and total task counts per tier, keyed {@code EASY} to {@code GRANDMASTER}. Null when no
		 * task tier could be resolved from the game cache.
		 */
		public Map<String, TierCount> tierCounts;

		/**
		 * @param tierEasy        easy varbit value
		 * @param tierMedium      medium varbit value
		 * @param tierHard        hard varbit value
		 * @param tierElite       elite varbit value
		 * @param tierMaster      master varbit value
		 * @param tierGrandmaster grandmaster varbit value
		 */
		public CombatAchievementData(int tierEasy, int tierMedium, int tierHard, int tierElite,
			int tierMaster, int tierGrandmaster)
		{
			this.tierEasy = tierEasy;
			this.tierMedium = tierMedium;
			this.tierHard = tierHard;
			this.tierElite = tierElite;
			this.tierMaster = tierMaster;
			this.tierGrandmaster = tierGrandmaster;
			this.easyComplete = tierEasy > 0;
			this.mediumComplete = tierMedium > 0;
			this.hardComplete = tierHard > 0;
			this.eliteComplete = tierElite > 0;
			this.completedTasks = Collections.emptyList();
			this.tasks = Collections.emptyList();
		}

		/**
		 * @param tierEasy        easy varbit value
		 * @param tierMedium      medium varbit value
		 * @param tierHard        hard varbit value
		 * @param tierElite       elite varbit value
		 * @param tierMaster      master varbit value
		 * @param tierGrandmaster grandmaster varbit value
		 * @param tasks           per-task detail; {@code completedTasks} is derived from it
		 */
		public CombatAchievementData(int tierEasy, int tierMedium, int tierHard, int tierElite,
			int tierMaster, int tierGrandmaster, List<CombatAchievementTask> tasks)
		{
			this(tierEasy, tierMedium, tierHard, tierElite, tierMaster, tierGrandmaster, tasks, 0);
		}

		/**
		 * @param tierEasy        easy varbit value
		 * @param tierMedium      medium varbit value
		 * @param tierHard        hard varbit value
		 * @param tierElite       elite varbit value
		 * @param tierMaster      master varbit value
		 * @param tierGrandmaster grandmaster varbit value
		 * @param tasks           per-task detail; {@code completedTasks}, {@code tierCounts} and the fallback
		 *                        {@code points} are derived from it
		 * @param pointsVarbit    the game's combat-achievement points varbit; used when non-zero
		 */
		public CombatAchievementData(int tierEasy, int tierMedium, int tierHard, int tierElite,
			int tierMaster, int tierGrandmaster, List<CombatAchievementTask> tasks, int pointsVarbit)
		{
			this(tierEasy, tierMedium, tierHard, tierElite, tierMaster, tierGrandmaster);
			this.tasks = tasks;
			List<String> names = new ArrayList<>();
			for (CombatAchievementTask t : tasks)
				if (t.completed && t.name != null)
					names.add(t.name);

			this.completedTasks = names;
			this.tierCounts = countTiers(tasks);
			this.points = pointsVarbit > 0 ? Integer.valueOf(pointsVarbit) : computePoints(tasks);
		}

		/**
		 * Points one completed task of the given tier is worth: easy 1, medium 2, hard 3, elite 4, master 5,
		 * grandmaster 6. Source: https://oldschool.runescape.wiki/w/Combat_Achievements (points per task
		 * table, checked 2026-10-14).
		 *
		 * @param tier the tier name, {@code EASY} to {@code GRANDMASTER}
		 * @return the points, or 0 for an unknown tier
		 */
		static int pointsPerTask(String tier)
		{
			switch (tier)
			{
				case "EASY":
					return 1;
				case "MEDIUM":
					return 2;
				case "HARD":
					return 3;
				case "ELITE":
					return 4;
				case "MASTER":
					return 5;
				case "GRANDMASTER":
					return 6;
				default:
					return 0;
			}
		}

		/**
		 * Sums the points of completed tasks.
		 *
		 * @param tasks the tasks
		 * @return the total, or null when a completed task has no resolved tier
		 */
		private static Integer computePoints(List<CombatAchievementTask> tasks)
		{
			int total = 0;
			for (CombatAchievementTask t : tasks)
			{
				if (!t.completed)
					continue;

				if (t.tier == null)
					return null;

				total += pointsPerTask(t.tier);
			}

			return total;
		}

		/**
		 * Counts completed and total tasks per tier, ignoring tasks with no resolved tier.
		 *
		 * @param tasks the tasks
		 * @return the counts for all six tiers in order, or null when no task has a tier
		 */
		private static Map<String, TierCount> countTiers(List<CombatAchievementTask> tasks)
		{
			Map<String, TierCount> counts = new LinkedHashMap<>();
			for (String tier : new String[] {"EASY", "MEDIUM", "HARD", "ELITE", "MASTER", "GRANDMASTER"})
				counts.put(tier, new TierCount(0, 0));

			boolean any = false;
			for (CombatAchievementTask t : tasks)
			{
				TierCount count = t.tier != null ? counts.get(t.tier) : null;
				if (count == null)
					continue;

				any = true;
				count.total++;
				if (t.completed)
					count.completed++;
			}

			return any ? counts : null;
		}
	}

	/**
	 * Completed and total task counts for one combat-achievement tier.
	 */
	@EqualsAndHashCode
	public static class TierCount
	{
		/** Number of completed tasks in the tier. */
		public int completed;

		/** Number of tasks in the tier. */
		public int total;

		/**
		 * @param completed completed task count
		 * @param total     total task count
		 */
		public TierCount(int completed, int total)
		{
			this.completed = completed;
			this.total = total;
		}
	}

	/**
	 * A single combat-achievement task and whether it is complete.
	 */
	@EqualsAndHashCode
	public static class CombatAchievementTask
	{
		/** Task id; completion is bit {@code id % 32} of the {@code id / 32}th completion varp. */
		public int id;

		/** Task name from the game cache, or null if unresolved. */
		public String name;

		/** Tier name (EASY to GRANDMASTER), or null if unresolved. */
		public String tier;

		/** Monster or category name from the game cache, or null if unresolved. */
		public String monster;

		/** Whether the task is complete. */
		public boolean completed;

		/**
		 * @param id        task id
		 * @param name      task name, or null
		 * @param tier      tier name, or null
		 * @param monster   monster name, or null
		 * @param completed whether the task is complete
		 */
		public CombatAchievementTask(int id, String name, String tier, String monster, boolean completed)
		{
			this.id = id;
			this.name = name;
			this.tier = tier;
			this.monster = monster;
			this.completed = completed;
		}
	}

	/**
	 * The current slayer task. The creature is exported as its game id plus, when resolvable, its name.
	 */
	@EqualsAndHashCode
	public static class SlayerData
	{
		/** Game id of the assigned creature. */
		public int creatureId;

		/** Display name of the assigned creature, or null when it could not be resolved. */
		public String creatureName;

		/** Specific boss name for boss tasks, or null for any other task. */
		public String bossName;

		/** Remaining kill count. */
		public int amount;

		/** Slayer reward points. */
		public int points;

		/** Current task-completion streak. */
		public int streak;

		/**
		 * @param creatureId   assigned-creature id
		 * @param creatureName assigned-creature name, or null
		 * @param bossName     boss name for boss tasks, or null
		 * @param amount       remaining kills
		 * @param points       reward points
		 * @param streak       completion streak
		 */
		public SlayerData(int creatureId, String creatureName, String bossName, int amount, int points, int streak)
		{
			this.creatureId = creatureId;
			this.creatureName = creatureName;
			this.bossName = bossName;
			this.amount = amount;
			this.points = points;
			this.streak = streak;
		}
	}

	/**
	 * World location of the character.
	 */
	public static class LocationData
	{
		/** Map region id. */
		public int regionId;

		/** World x coordinate. */
		public int x;

		/** World y coordinate. */
		public int y;

		/** Plane (height level), 0–3. */
		public int plane;

		/**
		 * @param regionId map region id
		 * @param x        world x
		 * @param y        world y
		 * @param plane    plane
		 */
		public LocationData(int regionId, int x, int y, int plane)
		{
			this.regionId = regionId;
			this.x = x;
			this.y = y;
			this.plane = plane;
		}
	}

	/**
	 * One active Grand Exchange offer.
	 */
	@EqualsAndHashCode
	public static class GeOffer
	{
		/** Slot index, 0–7. */
		public int slot;

		/** Item id being traded. */
		public int itemId;

		/** Resolved item name. */
		public String name;

		/** Offer state, e.g. {@code BUYING}, {@code SOLD}, {@code CANCELLED_BUY}. */
		public String state;

		/** Quantity bought or sold so far. */
		public int quantitySold;

		/** Total quantity of the offer. */
		public int totalQuantity;

		/** Per-item price of the offer. */
		public long price;

		/** Coins spent or gained so far. */
		public long spent;

		/**
		 * @param slot          slot index
		 * @param itemId        item id
		 * @param name          resolved name
		 * @param state         offer state
		 * @param quantitySold  quantity transacted
		 * @param totalQuantity total quantity
		 * @param price         per-item price
		 * @param spent         coins moved
		 */
		public GeOffer(int slot, int itemId, String name, String state, int quantitySold,
			int totalQuantity, long price, long spent)
		{
			this.slot = slot;
			this.itemId = itemId;
			this.name = name;
			this.state = state;
			this.quantitySold = quantitySold;
			this.totalQuantity = totalQuantity;
			this.price = price;
			this.spent = spent;
		}
	}
}
