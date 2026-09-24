/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot.model;

import java.util.Collections;
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

		/** Whether the current world is a members' world. */
		public boolean members;

		/**
		 * @param username    display name
		 * @param combatLevel combat level
		 * @param world       current world
		 * @param accountType account-type name
		 * @param members     whether on a members' world
		 */
		public PlayerInfo(String username, int combatLevel, int world, String accountType, boolean members)
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
		 * Compatibility view: the flat list wrapped in a single tab, for consumers that expect the
		 * tabbed shape (e.g. the osrs-companion MCP server). Individual tabs are not modelled.
		 */
		public List<BankTab> tabs;

		/**
		 * @param totalItems distinct stack count
		 * @param items      flat item list
		 */
		public BankData(int totalItems, List<ItemEntry> items)
		{
			this.totalItems = totalItems;
			this.items = items;
			this.tabs = Collections.singletonList(new BankTab(0, items));
		}
	}

	/**
	 * A bank tab: an index and the items it holds. The exporter emits a single tab wrapping the flat
	 * item list; it is a compatibility view, not a faithful per-tab breakdown.
	 */
	public static class BankTab
	{
		/** Zero-based tab index. */
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
	 * Completion of the four achievement-diary tiers for one region.
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
		 * @param easy   easy complete
		 * @param medium medium complete
		 * @param hard   hard complete
		 * @param elite  elite complete
		 */
		public DiaryRegion(boolean easy, boolean medium, boolean hard, boolean elite)
		{
			this.easy = easy;
			this.medium = medium;
			this.hard = hard;
			this.elite = elite;
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

		/** Compatibility field: always empty; individual tasks are not enumerated. */
		public List<String> completedTasks;

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
		}
	}

	/**
	 * The current slayer task. The creature is exported as its game id; consumers map it to a name.
	 */
	@EqualsAndHashCode
	public static class SlayerData
	{
		/** Game id of the assigned creature. */
		public int creatureId;

		/** Remaining kill count. */
		public int amount;

		/** Slayer reward points. */
		public int points;

		/** Current task-completion streak. */
		public int streak;

		/**
		 * @param creatureId assigned-creature id
		 * @param amount     remaining kills
		 * @param points     reward points
		 * @param streak     completion streak
		 */
		public SlayerData(int creatureId, int amount, int points, int streak)
		{
			this.creatureId = creatureId;
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
		public int price;

		/** Coins spent or gained so far. */
		public int spent;

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
			int totalQuantity, int price, int spent)
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
