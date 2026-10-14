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

	/**
	 * Runes held in the rune pouch, present only while the inventory holds a rune pouch (any variant).
	 * Empty when the pouch holds nothing.
	 */
	public List<ItemEntry> runePouch;

	/** Quest completion state, one entry per quest. */
	public List<QuestEntry> quests;

	/** Total quest points. */
	public Integer questPoints;

	/** Achievement diary completion keyed by region name. */
	public Map<String, DiaryRegion> achievementDiaries;

	/** Combat-achievement tier progress. */
	public CombatAchievementData combatAchievements;

	/** Slayer state; points and streak always set once polled, task fields null/0 when no task. */
	public SlayerData slayer;

	/** Current world location of the character. */
	public LocationData location;

	/** Active Grand Exchange offers by slot. */
	public List<GeOffer> grandExchange;

	/** Collection log contents captured while the log was open, carried over between sessions. */
	public CollectionLogData collectionLog;

	/**
	 * Boss kill counts keyed by lower-case boss name, sorted by name. Absent when the toggle is off or no
	 * count is known yet.
	 */
	public Map<String, Integer> killCounts;

	/**
	 * Agility laps and other non-boss running totals (chests, rumours, challenges) keyed by lower-case
	 * activity name, sorted by name. Absent when the toggle is off or none is known yet.
	 */
	public Map<String, Integer> activityCounts;

	/**
	 * Completed treasure trails per tier ({@code beginner} to {@code master}), in tier order. Only tiers whose
	 * count has been seen are present. Absent when the toggle is off or no tier is known yet.
	 */
	public Map<String, Integer> clueCounts;

	/**
	 * Farming patch and birdhouse timers decoded from RuneLite's Time Tracking data. Only as fresh as the
	 * player's last visit to each patch. Absent when the toggle is off or Time Tracking has no data.
	 */
	public TimersData timers;

	/**
	 * Claimable daily rewards keyed by name ({@code herbBoxes}, {@code battlestaves}, ...), whether each is
	 * unlocked and whether today's is already claimed. The game resets claims at 00:00 UTC; compare with
	 * {@link #lastUpdated}. Absent when the toggle is off.
	 */
	public Map<String, DailyTask> dailies;

	/** Kingdom of Miscellania approval and coffer; absent before Throne of Miscellania or when the toggle is off. */
	public MiscellaniaData miscellania;

	/**
	 * Minigame reward-shop balances keyed by currency ({@code nightmareZone}, {@code titheFarm}, ...), in a fixed
	 * order. Absent when the toggle is off.
	 */
	public Map<String, Integer> minigamePoints;

	/**
	 * Tool leprechaun storage as raw varbit values keyed by tool ({@code rake}, {@code buckets}, ...); see
	 * {@code SCHEMA.md}. Absent when nothing is stored or the storages toggle is off.
	 */
	public Map<String, Integer> toolLeprechaun;

	/** Transport unlocks kept in game variables (fairy rings, quetzal sites); absent when the toggle is off. */
	public TransportData transport;

	/** Player-owned house fields kept in game variables, raw values; absent with no house or the toggle off. */
	public HouseData house;

	/** Sailing boats, crew, charting and Barracuda Trials; absent when the toggle is off. */
	public SailingData sailing;

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

		/** Active spellbook: {@code STANDARD}, {@code ANCIENT}, {@code LUNAR} or {@code ARCEUUS}; null if unknown. */
		public String spellbook;

		/**
		 * Unlocked reward prayers: {@code RIGOUR}, {@code AUGURY}, {@code PRESERVE}, {@code DEADEYE},
		 * {@code MYSTIC_VIGOUR}. Empty when none; null when account info is not exported.
		 */
		public List<String> unlockedPrayers;

		/** Prayers selected as quick prayers, by RuneLite prayer name; null when account info is not exported. */
		public List<String> quickPrayers;

		/** Name of the autocast spell; null when nothing is autocast or account info is not exported. */
		public String autocastSpell;

		/** Whether the autocast is defensive; null when nothing is autocast or account info is not exported. */
		public Boolean autocastDefensive;

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

		/**
		 * @param username    display name
		 * @param combatLevel combat level
		 * @param world       current world
		 * @param accountType account-type name
		 * @param members     whether on a members' world
		 * @param spellbook   active spellbook name, may be {@code null}
		 */
		public PlayerInfo(String username, int combatLevel, int world, String accountType, Boolean members,
			String spellbook)
		{
			this(username, combatLevel, world, accountType, members);
			this.spellbook = spellbook;
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
	@EqualsAndHashCode
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
	@EqualsAndHashCode(callSuper = true)
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
	 * Collection log: the account-wide obtained and total counts, the flat list of obtained items, and
	 * the obtained items grouped by the log page they were seen on.
	 */
	public static class CollectionLogData
	{
		/** Obtained-item count shown by the game; 0 when it was not readable. */
		public int obtained;

		/** Total collectable-item count shown by the game; 0 when it was not readable. */
		public int total;

		/** Every obtained item seen so far, one entry per item id. */
		public List<ItemEntry> items;

		/** Per-page captures keyed by page title; only pages the player has opened are present. */
		public Map<String, CollectionLogPage> pages;

		/**
		 * @param obtained obtained-item count
		 * @param total    total item count
		 * @param items    obtained items
		 * @param pages    captured pages by title
		 */
		public CollectionLogData(int obtained, int total, List<ItemEntry> items,
			Map<String, CollectionLogPage> pages)
		{
			this.obtained = obtained;
			this.total = total;
			this.items = items;
			this.pages = pages;
		}
	}

	/**
	 * One collection log page as last seen: how many of its slots there are and which are obtained.
	 */
	public static class CollectionLogPage
	{
		/** Number of item slots on the page, obtained or not. */
		public int total;

		/** Number of obtained items on the page. */
		public int obtained;

		/** The obtained items on the page. */
		public List<ItemEntry> items;

		/**
		 * @param total    slot count
		 * @param obtained obtained count
		 * @param items    obtained items
		 */
		public CollectionLogPage(int total, int obtained, List<ItemEntry> items)
		{
			this.total = total;
			this.obtained = obtained;
			this.items = items;
		}

		/**
		 * Compares by counts and by item id, name and quantity in order.
		 *
		 * @param o the other object
		 * @return whether the pages hold the same capture
		 */
		@Override
		public boolean equals(Object o)
		{
			if (!(o instanceof CollectionLogPage))
				return false;

			CollectionLogPage other = (CollectionLogPage) o;
			if (total != other.total || obtained != other.obtained || items.size() != other.items.size())
				return false;

			for (int i = 0; i < items.size(); i++)
			{
				ItemEntry a = items.get(i);
				ItemEntry c = other.items.get(i);
				if (a.itemId != c.itemId || a.quantity != c.quantity)
					return false;
			}

			return true;
		}

		/**
		 * @return a hash consistent with {@link #equals(Object)}
		 */
		@Override
		public int hashCode()
		{
			return 31 * total + obtained;
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
	 * The slayer state. With no task assigned, the creature id and amount are 0 and the names are null, while
	 * points and streak stay. The creature is exported as its game id plus, when resolvable, its name.
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

		/** Id of the current slayer master, 0 when unknown. Null in files written before this field existed. */
		public Integer masterId;

		/** Name of the current slayer master, or null when unresolved. */
		public String masterName;

		/** Blocked tasks across all masters, or null in files written before this field existed. */
		public List<BlockedTask> blockedTasks;

		/** Keys of purchased task-assignment unlocks, or null in files written before this field existed. */
		public List<String> unlockedTasks;

		/** Keys of tasks whose extension is purchased, or null in files written before this field existed. */
		public List<String> extendedTasks;

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
	 * One blocked slayer task slot.
	 */
	@EqualsAndHashCode
	public static class BlockedTask
	{
		/** Slayer task id (same id space as the slayer creature id). */
		public int taskId;

		/** Task name, or null when it could not be resolved. */
		public String name;

		/** Name of the slayer master whose block list holds this task, or null for the legacy shared list. */
		public String master;

		/**
		 * @param taskId slayer task id
		 * @param name   task name, or null
		 * @param master master name, or null
		 */
		public BlockedTask(int taskId, String name, String master)
		{
			this.taskId = taskId;
			this.name = name;
			this.master = master;
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

		/** Whether the character is in an instanced region; x and y are then the template area's coordinates. */
		public boolean instanced;

		/**
		 * @param regionId map region id
		 * @param x        world x
		 * @param y        world y
		 * @param plane    plane
		 */
		public LocationData(int regionId, int x, int y, int plane)
		{
			this(regionId, x, y, plane, false);
		}

		/**
		 * @param regionId  map region id
		 * @param x         world x
		 * @param y         world y
		 * @param plane     plane
		 * @param instanced whether the character is in an instanced region
		 */
		public LocationData(int regionId, int x, int y, int plane, boolean instanced)
		{
			this.regionId = regionId;
			this.x = x;
			this.y = y;
			this.plane = plane;
			this.instanced = instanced;
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

	/**
	 * Last known contents of storage containers that are only readable when opened, keyed by storage
	 * ({@code lootingBag}, {@code seedVault}, {@code groupStorage}); a storage never opened is absent.
	 */
	public Map<String, List<ItemEntry>> storages;

	/**
	 * Farming and birdhouse timers.
	 */
	@EqualsAndHashCode
	public static class TimersData
	{
		/** Farming patches with stored data; patches never visited are not listed. */
		public List<FarmingTimer> farming;

		/** Birdhouse spaces with stored data. */
		public List<BirdhouseTimer> birdhouses;

		/**
		 * @param farming    farming patch timers
		 * @param birdhouses birdhouse timers
		 */
		public TimersData(List<FarmingTimer> farming, List<BirdhouseTimer> birdhouses)
		{
			this.farming = farming;
			this.birdhouses = birdhouses;
		}
	}

	/**
	 * The projected state of one farming patch or compost bin.
	 */
	@EqualsAndHashCode
	public static class FarmingTimer
	{
		/** Human-readable patch, e.g. {@code Catherby herb}. */
		public String patch;

		/** Region the patch is in, e.g. {@code Catherby}. */
		public String region;

		/** Patch type, e.g. {@code herb}, {@code fruit tree}, {@code compost}. */
		public String type;

		/** Crop name, e.g. {@code Ranarr}; {@code Weeds} for an empty patch. */
		public String crop;

		/** One of GROWING, HARVESTABLE, DISEASED, DEAD, EMPTY, FILLING. */
		public String state;

		/** Projected growth stage. */
		public int stage;

		/** Number of stages in the current phase. */
		public int stages;

		/** ISO-8601 instant the crop finishes (or finished) growing; null when it is not growing. */
		public String readyAt;

		/** ISO-8601 instant of the last time the patch state was stored, i.e. the last visit. */
		public String lastChecked;

		/**
		 * @param patch       patch label
		 * @param region      region name
		 * @param type        patch type
		 * @param crop        crop name
		 * @param state       projected state
		 * @param stage       projected stage
		 * @param stages      stage count
		 * @param readyAt     completion instant, or null
		 * @param lastChecked last-visit instant
		 */
		public FarmingTimer(String patch, String region, String type, String crop, String state, int stage,
			int stages, String readyAt, String lastChecked)
		{
			this.patch = patch;
			this.region = region;
			this.type = type;
			this.crop = crop;
			this.state = state;
			this.stage = stage;
			this.stages = stages;
			this.readyAt = readyAt;
			this.lastChecked = lastChecked;
		}
	}

	/**
	 * The projected state of one birdhouse space.
	 */
	@EqualsAndHashCode
	public static class BirdhouseTimer
	{
		/** Space name, e.g. {@code Mushroom Meadow (North)}. */
		public String space;

		/** Birdhouse type, e.g. {@code Oak Bird House}; null when the space is empty. */
		public String house;

		/** One of EMPTY, BUILT, SEEDED, READY, UNKNOWN. */
		public String state;

		/** ISO-8601 instant the birdhouse finishes (or finished); null unless seeded. */
		public String readyAt;

		/** ISO-8601 instant of the last time the state was stored, i.e. the last visit. */
		public String lastChecked;

		/**
		 * @param space       space name
		 * @param house       birdhouse type, or null
		 * @param state       projected state
		 * @param readyAt     completion instant, or null
		 * @param lastChecked last-visit instant
		 */
		public BirdhouseTimer(String space, String house, String state, String readyAt, String lastChecked)
		{
			this.space = space;
			this.house = house;
			this.state = state;
			this.readyAt = readyAt;
			this.lastChecked = lastChecked;
		}
	}

	/**
	 * One claimable daily reward.
	 */
	@EqualsAndHashCode
	public static class DailyTask
	{
		/** Whether the reward is unlocked (diary tier, quest or account type allows it). */
		public boolean unlocked;

		/** Whether today's reward has been claimed in full. */
		public boolean claimed;

		/** Amount claimed today, for rewards claimed in parts (herb boxes, bonemeal); null otherwise. */
		public Integer claimedCount;

		/** Daily maximum for rewards claimed in parts; null otherwise. */
		public Integer max;

		/**
		 * @param unlocked     whether the reward is unlocked
		 * @param claimed      whether today's reward is claimed
		 * @param claimedCount amount claimed today, or null
		 * @param max          daily maximum, or null
		 */
		public DailyTask(boolean unlocked, boolean claimed, Integer claimedCount, Integer max)
		{
			this.unlocked = unlocked;
			this.claimed = claimed;
			this.claimedCount = claimedCount;
			this.max = max;
		}
	}

	/**
	 * Kingdom of Miscellania state. The game only sends the values while the player is in the kingdom, so they
	 * are as fresh as {@link #lastChanged}; RuneLite's Kingdom of Miscellania plugin data fills the gap.
	 */
	@EqualsAndHashCode
	public static class MiscellaniaData
	{
		/** Approval, 0 to 100 percent. */
		public int approvalPercent;

		/** Coins in the coffer. */
		public int coffer;

		/** ISO-8601 instant the values were last seen changing; null when unknown. */
		public String lastChanged;

		/**
		 * @param approvalPercent approval percentage
		 * @param coffer          coins in the coffer
		 * @param lastChanged     last-change instant, or null
		 */
		public MiscellaniaData(int approvalPercent, int coffer, String lastChanged)
		{
			this.approvalPercent = approvalPercent;
			this.coffer = coffer;
			this.lastChanged = lastChanged;
		}
	}

	/**
	 * Transport unlocks kept in game variables.
	 */
	@EqualsAndHashCode
	public static class TransportData
	{
		/** Whether the player may use fairy rings. */
		public boolean fairyRings;

		/** Fairy ring codes in the travel log, e.g. {@code AJR}, alphabetical. */
		public List<String> fairyRingLog;

		/** Quetzal landing sites available, by name. */
		public List<String> quetzalSites;

		/**
		 * @param fairyRings   whether fairy rings may be used
		 * @param fairyRingLog codes in the travel log
		 * @param quetzalSites available quetzal sites
		 */
		public TransportData(boolean fairyRings, List<String> fairyRingLog, List<String> quetzalSites)
		{
			this.fairyRings = fairyRings;
			this.fairyRingLog = fairyRingLog;
			this.quetzalSites = quetzalSites;
		}
	}

	/**
	 * Player-owned house fields kept in game variables. All values are the game's raw varbit values.
	 */
	@EqualsAndHashCode
	public static class HouseData
	{
		/** House location id (raw {@code POH_HOUSE_LOCATION}). */
		public int location;

		/** House style id (raw {@code POH_HOUSE_STYLE}). */
		public int style;

		/** Raw {@code POH_HOUSE_SIZE}. */
		public int size;

		/** Servant id, 0 for none (raw {@code POH_SERVANT_TYPE}). */
		public int servant;

		/** Jewellery box state, 0 when none is built (raw {@code POH_JEWELLERYBOX_MULTI}). */
		public int jewelleryBox;

		/** Portal nexus teleport slots (1 to 45) whose varbit is set. */
		public List<Integer> nexusTeleports;

		/**
		 * @param location       house location id
		 * @param style          house style id
		 * @param size           raw house size
		 * @param servant        servant id
		 * @param jewelleryBox   jewellery box state
		 * @param nexusTeleports set nexus teleport slots
		 */
		public HouseData(int location, int style, int size, int servant, int jewelleryBox,
			List<Integer> nexusTeleports)
		{
			this.location = location;
			this.style = style;
			this.size = size;
			this.servant = servant;
			this.jewelleryBox = jewelleryBox;
			this.nexusTeleports = nexusTeleports;
		}
	}

	/**
	 * Sailing state read from its named game variables.
	 */
	@EqualsAndHashCode
	public static class SailingData
	{
		/** Owned boats, by boat slot. */
		public List<BoatData> boats;

		/** Unlocked crew members by key, e.g. {@code werewolf}, {@code ghostJenkins}. */
		public List<String> crew;

		/** Completed charting tasks: set bits across the charting completion varps. */
		public int chartingTasksCompleted;

		/** Raw {@code SAILING_CHARTING_TOTAL_ENTRIES}. */
		public int chartingEntries;

		/** Raw Barracuda Trials performance values, trial slots 1 to 5. */
		public List<Integer> barracudaTrials;

		/**
		 * @param boats                  owned boats
		 * @param crew                   unlocked crew
		 * @param chartingTasksCompleted completed charting tasks
		 * @param chartingEntries        raw charting entries
		 * @param barracudaTrials        raw trial performance values
		 */
		public SailingData(List<BoatData> boats, List<String> crew, int chartingTasksCompleted, int chartingEntries,
			List<Integer> barracudaTrials)
		{
			this.boats = boats;
			this.crew = crew;
			this.chartingTasksCompleted = chartingTasksCompleted;
			this.chartingEntries = chartingEntries;
			this.barracudaTrials = barracudaTrials;
		}
	}

	/**
	 * One owned boat. Type, port and part values are the game's raw ids.
	 */
	@EqualsAndHashCode
	public static class BoatData
	{
		/** Boat slot, 1 to 5. */
		public int slot;

		/** Raw boat type id. */
		public int type;

		/** Raw id of the port the boat is moored at. */
		public int port;

		/** Raw hull tier id. */
		public int hull;

		/** Raw keel tier id. */
		public int keel;

		/** Raw sail tier id. */
		public int sail;

		/** Raw steering (helm) tier id. */
		public int steering;

		/** Stored hitpoints. */
		public int hp;

		/** Stored maximum hitpoints. */
		public int maxHp;

		/**
		 * @param slot     boat slot
		 * @param type     raw type id
		 * @param port     raw port id
		 * @param hull     raw hull id
		 * @param keel     raw keel id
		 * @param sail     raw sail id
		 * @param steering raw steering id
		 * @param hp       stored hitpoints
		 * @param maxHp    stored maximum hitpoints
		 */
		public BoatData(int slot, int type, int port, int hull, int keel, int sail, int steering, int hp, int maxHp)
		{
			this.slot = slot;
			this.type = type;
			this.port = port;
			this.hull = hull;
			this.keel = keel;
			this.sail = sail;
			this.steering = steering;
			this.hp = hp;
			this.maxHp = maxHp;
		}
	}
}
