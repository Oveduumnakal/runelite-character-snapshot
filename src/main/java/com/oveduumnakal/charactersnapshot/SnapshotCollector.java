/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BankData;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BankTab;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BlockedTask;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.CollectionLogData;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.CollectionLogPage;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.CombatAchievementData;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.CombatAchievementTask;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.DiaryRegion;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.DiaryTask;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.GeOffer;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.InventoryItem;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.ItemEntry;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.LocationData;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.PlayerInfo;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.QuestEntry;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.SkillEntry;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.SlayerData;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.TimersData;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.VitalsData;

import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.StructComposition;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.DBTableID;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.vars.AccountType;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;

/**
 * Reads player state from the RuneLite {@link Client} into in-memory caches and assembles the
 * export snapshot. Container updates arrive from events; quests, diaries, combat achievements, and
 * the slayer task are polled. Every read runs on the client thread; {@link #buildSnapshot()} hands
 * a copied, stable object graph to the writer thread. Poll methods return whether the polled data
 * changed, so the plugin can mark the snapshot dirty without a blanket varbit listener.
 */
public class SnapshotCollector
{
	private static final int[] CA_COMPLETED_VARPS = {
		VarPlayerID.CA_TASK_COMPLETED_0, VarPlayerID.CA_TASK_COMPLETED_1, VarPlayerID.CA_TASK_COMPLETED_2,
		VarPlayerID.CA_TASK_COMPLETED_3, VarPlayerID.CA_TASK_COMPLETED_4, VarPlayerID.CA_TASK_COMPLETED_5,
		VarPlayerID.CA_TASK_COMPLETED_6, VarPlayerID.CA_TASK_COMPLETED_7, VarPlayerID.CA_TASK_COMPLETED_8,
		VarPlayerID.CA_TASK_COMPLETED_9, VarPlayerID.CA_TASK_COMPLETED_10, VarPlayerID.CA_TASK_COMPLETED_11,
		VarPlayerID.CA_TASK_COMPLETED_12, VarPlayerID.CA_TASK_COMPLETED_13, VarPlayerID.CA_TASK_COMPLETED_14,
		VarPlayerID.CA_TASK_COMPLETED_15, VarPlayerID.CA_TASK_COMPLETED_16, VarPlayerID.CA_TASK_COMPLETED_17,
		VarPlayerID.CA_TASK_COMPLETED_18, VarPlayerID.CA_TASK_COMPLETED_19, VarPlayerID.CA_TASK_COMPLETED_20
	};

	/** Cache enums listing the task struct ids per tier, easy to grandmaster. Not in the RuneLite API. */
	private static final int[] CA_TIER_ENUMS = {3981, 3982, 3983, 3984, 3985, 3986};

	/** Most reloads attempted after a partial metadata load, so a permanently missing struct is not re-read forever. */
	private static final int CA_META_MAX_RETRIES = 10;

	private static final String[] CA_TIER_NAMES = {"EASY", "MEDIUM", "HARD", "ELITE", "MASTER", "GRANDMASTER"};

	/** Cache enum mapping monster category id to display name. */
	private static final int CA_MONSTER_ENUM = 3971;

	/** Task struct param: task id (the completion bit index). */
	private static final int CA_PARAM_ID = 1306;

	/** Task struct param: task name. */
	private static final int CA_PARAM_NAME = 1308;

	/** Task struct param: monster category id, a key of {@link #CA_MONSTER_ENUM}. */
	private static final int CA_PARAM_MONSTER = 1312;

	private static final int SLAYER_BOSS_TASK_ID = 98;

	/**
	 * Value of the Karamja easy, medium and hard tier varbits once the tier is complete; 1 only means started.
	 * The Karamja elite varbit and every other region's tier varbits are plain 0/1 flags.
	 */
	private static final int KARAMJA_TIER_DONE = 2;

	private static final EquipmentInventorySlot[] EQUIPMENT_SLOTS = {
		EquipmentInventorySlot.HEAD, EquipmentInventorySlot.CAPE, EquipmentInventorySlot.AMULET,
		EquipmentInventorySlot.WEAPON, EquipmentInventorySlot.BODY, EquipmentInventorySlot.SHIELD,
		EquipmentInventorySlot.LEGS, EquipmentInventorySlot.GLOVES, EquipmentInventorySlot.BOOTS,
		EquipmentInventorySlot.RING, EquipmentInventorySlot.AMMO
	};

	private static final int[] BANK_TAB_VARBITS = {
		VarbitID.BANK_TAB_1, VarbitID.BANK_TAB_2, VarbitID.BANK_TAB_3, VarbitID.BANK_TAB_4,
		VarbitID.BANK_TAB_5, VarbitID.BANK_TAB_6, VarbitID.BANK_TAB_7, VarbitID.BANK_TAB_8,
		VarbitID.BANK_TAB_9
	};

	private static final int[] RUNE_POUCH_TYPE_VARBITS = {
		VarbitID.RUNE_POUCH_TYPE_1, VarbitID.RUNE_POUCH_TYPE_2, VarbitID.RUNE_POUCH_TYPE_3,
		VarbitID.RUNE_POUCH_TYPE_4, VarbitID.RUNE_POUCH_TYPE_5, VarbitID.RUNE_POUCH_TYPE_6
	};

	private static final int[] RUNE_POUCH_QUANTITY_VARBITS = {
		VarbitID.RUNE_POUCH_QUANTITY_1, VarbitID.RUNE_POUCH_QUANTITY_2, VarbitID.RUNE_POUCH_QUANTITY_3,
		VarbitID.RUNE_POUCH_QUANTITY_4, VarbitID.RUNE_POUCH_QUANTITY_5, VarbitID.RUNE_POUCH_QUANTITY_6
	};

	/** Every rune pouch variant; the contents varbits are shared by all of them. */
	private static final int[] RUNE_POUCH_ITEMS = {
		ItemID.BH_RUNE_POUCH, ItemID.BH_RUNE_POUCH_TROUVER,
		ItemID.DIVINE_RUNE_POUCH, ItemID.DIVINE_RUNE_POUCH_TROUVER
	};

	private final Client client;

	private final CharacterSnapshotConfig config;

	private final ConfigManager configManager;

	private final Map<String, Integer> killCounts = new TreeMap<>();

	private final Map<String, Integer> varpKillCounts = new TreeMap<>();

	private final Map<String, Integer> activityCounts = new TreeMap<>();

	private final Map<String, Integer> clueCounts = new HashMap<>();

	private final Map<String, SkillEntry> skills = new LinkedHashMap<>();

	private final Map<Integer, GeOffer> geOffers = new TreeMap<>();

	private final Map<Integer, String> slayerTaskNames = new HashMap<>();

	private final Map<Integer, String> slayerBossNames = new HashMap<>();

	private List<ItemEntry> bankItems = null;

	private List<BankTab> bankTabs = null;

	private int bankTotal = 0;

	private List<InventoryItem> inventory = new ArrayList<>();

	private Map<String, ItemEntry> equipment = new LinkedHashMap<>();

	private Instant bankUpdated = null;

	private Instant inventoryUpdated = null;

	private Instant equipmentUpdated = null;

	private List<QuestEntry> quests = null;

	private Integer questPoints = null;

	private Map<String, DiaryRegion> diaries = null;

	private CombatAchievementData combatAchievements = null;

	/** Cached task metadata by task id (completed flag unset); empty until resolved from the cache. */
	private Map<Integer, CombatAchievementTask> caTaskMeta = new TreeMap<>();

	/** Whether the last metadata load skipped a tier enum or task struct the cache did not return. */
	private boolean caMetaPartial = false;

	/** How many times a partial metadata load has been retried. */
	private int caMetaRetries = 0;

	private SlayerData slayer = null;

	private List<ItemEntry> runePouch = null;

	private TimersData timers = null;

	/**
	 * Creates a collector that cannot read other plugins' stored data.
	 *
	 * @param client the RuneLite client
	 * @param config the plugin configuration
	 */
	public SnapshotCollector(Client client, CharacterSnapshotConfig config)
	{
		this(client, config, null);
	}

	/**
	 * @param client        the RuneLite client
	 * @param config        the plugin configuration
	 * @param configManager read-only source of data other built-in plugins store in the RuneScape-profile
	 *                      config, may be {@code null}
	 */
	public SnapshotCollector(Client client, CharacterSnapshotConfig config, ConfigManager configManager)
	{
		this.client = client;
		this.config = config;
		this.configManager = configManager;
	}

	/**
	 * Clears every cache. Called on logout and account change so one character's data can never leak
	 * into another character's export file.
	 */
	public void reset()
	{
		skills.clear();
		geOffers.clear();
		equipment.clear();
		storages.clear();
		storageUpdated.clear();
		bankItems = null;
		bankTabs = null;
		bankTotal = 0;
		inventory = new ArrayList<>();
		bankUpdated = null;
		inventoryUpdated = null;
		equipmentUpdated = null;
		quests = null;
		questPoints = null;
		diaries = null;
		combatAchievements = null;
		slayer = null;
		runePouch = null;
		spellbook = null;
		clogPages.clear();
		clogObtained = 0;
		clogTotal = 0;
		clogUpdated = null;
		lastPlayer = null;
		lastLocation = null;
		polledLocation = null;
		polledEnergy = null;
		killCounts.clear();
		varpKillCounts.clear();
		activityCounts.clear();
		clueCounts.clear();
		timers = null;
	}

	/**
	 * Updates a single skill from a stat-changed event.
	 *
	 * @param skill        the skill
	 * @param level        the real level
	 * @param xp           the experience
	 * @param boostedLevel the current boosted level
	 */
	public void updateSkill(Skill skill, int level, int xp, int boostedLevel)
	{
		if ("OVERALL".equals(skill.name()))
			return;

		skills.put(skill.name(), new SkillEntry(level, xp, boostedLevel));
		refreshOverall();
	}

	/**
	 * Recomputes the {@code OVERALL} entry from the cached skills. Does nothing until every real skill is
	 * cached, so a partial total is never exported.
	 */
	private void refreshOverall()
	{
		int totalLevel = 0;
		long totalXp = 0;
		for (Skill skill : Skill.values())
		{
			if ("OVERALL".equals(skill.name()))
				continue;

			SkillEntry entry = skills.get(skill.name());
			if (entry == null)
				return;

			totalLevel += entry.level;
			totalXp += entry.xp;
		}

		skills.put("OVERALL", new SkillEntry(totalLevel, totalXp, totalLevel));
	}

	/**
	 * Reads every skill from the client, plus a computed {@code OVERALL} total.
	 */
	public void pollAllSkills()
	{
		int totalLevel = 0;
		long totalXp = 0;
		for (Skill skill : Skill.values())
		{
			if ("OVERALL".equals(skill.name()))
				continue;

			int level = client.getRealSkillLevel(skill);
			int xp = client.getSkillExperience(skill);
			int boosted = client.getBoostedSkillLevel(skill);
			skills.put(skill.name(), new SkillEntry(level, xp, boosted));
			totalLevel += level;
			totalXp += xp;
		}

		skills.put("OVERALL", new SkillEntry(totalLevel, totalXp, totalLevel));
	}

	/**
	 * Replaces the cached bank contents with a flat item list and the same items split by bank tab.
	 * The container holds tabs 1 to 9 in order, then the main tab; each numbered tab's slot count is
	 * read from its varbit, and the remaining slots are the main tab. Slicing uses the raw container,
	 * placeholders and empty slots included, so the counts line up; each slice is then filtered like
	 * the flat list.
	 *
	 * @param container the bank item container
	 */
	public void updateBank(ItemContainer container)
	{
		if (container == null)
			return;

		Item[] raw = container.getItems();
		List<ItemEntry> items = new ArrayList<>();
		List<BankTab> tabs = new ArrayList<>();
		int start = 0;
		for (int tab = 1; tab <= BANK_TAB_VARBITS.length; tab++)
		{
			int count = Math.max(0, client.getVarbitValue(BANK_TAB_VARBITS[tab - 1]));
			int end = Math.min(raw.length, start + count);
			if (count > 0)
			{
				BankTab bankTab = new BankTab(tab, filterBankSlice(raw, start, end));
				tabs.add(bankTab);
				items.addAll(bankTab.items);
			}

			start = end;
		}

		BankTab main = new BankTab(0, filterBankSlice(raw, start, raw.length));
		tabs.add(0, main);
		items.addAll(main.items);

		bankItems = items;
		bankTabs = tabs;
		bankTotal = items.size();
		bankUpdated = Instant.now();
	}

	/**
	 * Converts a slice of raw bank slots to item entries, dropping empty slots and placeholders.
	 *
	 * @param raw   the raw bank container items
	 * @param start first slot of the slice, inclusive
	 * @param end   last slot of the slice, exclusive
	 * @return the item entries in the slice
	 */
	private List<ItemEntry> filterBankSlice(Item[] raw, int start, int end)
	{
		List<ItemEntry> slice = new ArrayList<>();
		for (int i = start; i < end; i++)
		{
			int id = raw[i].getId();
			if (id > 0 && raw[i].getQuantity() > 0)
				slice.add(new ItemEntry(id, getItemName(id), raw[i].getQuantity()));
		}

		return slice;
	}

	/**
	 * Copies the cached bank tabs so a built snapshot does not share lists with the cache.
	 *
	 * @return a deep copy of the cached tabs
	 */
	private List<BankTab> copyBankTabs()
	{
		List<BankTab> copy = new ArrayList<>();
		for (BankTab tab : bankTabs)
			copy.add(new BankTab(tab.tabIndex, new ArrayList<>(tab.items)));

		return copy;
	}

	/**
	 * Replaces the cached inventory, keeping empty slots so slot indices stay meaningful.
	 *
	 * @param container the inventory item container
	 */
	public void updateInventory(ItemContainer container)
	{
		if (container == null)
			return;

		List<InventoryItem> inv = new ArrayList<>();
		Item[] items = container.getItems();
		for (int slot = 0; slot < items.length; slot++)
		{
			int id = items[slot].getId();
			String name = id > 0 ? getItemName(id) : null;
			inv.add(new InventoryItem(id, name, items[slot].getQuantity(), slot));
		}

		inventory = inv;
		inventoryUpdated = Instant.now();
	}

	/**
	 * Rebuilds the cached equipment from scratch so an unequipped slot is dropped. The worn container
	 * is not contiguous (it has unused arms, hair and jaw indices), so each slot is read at its own
	 * {@link EquipmentInventorySlot#getSlotIdx()} rather than by position.
	 *
	 * @param container the equipment item container
	 */
	public void updateEquipment(ItemContainer container)
	{
		if (container == null)
			return;

		Map<String, ItemEntry> equip = new LinkedHashMap<>();
		Item[] items = container.getItems();
		for (EquipmentInventorySlot slot : EQUIPMENT_SLOTS)
		{
			int idx = slot.getSlotIdx();
			if (idx >= items.length)
				continue;

			int id = items[idx].getId();
			if (id > 0)
				equip.put(slot.name(), new ItemEntry(id, getItemName(id), items[idx].getQuantity()));
		}

		equipment = equip;
		equipmentUpdated = Instant.now();
	}

	/**
	 * Seeds the bank, inventory, equipment, and storage caches from this character's previous export, so a
	 * section not yet re-captured this session keeps its last known contents instead of vanishing. Only
	 * sections not captured since the last {@link #reset()} are filled, so live data is never replaced,
	 * and each restored section keeps its original capture time. A snapshot belonging to a different
	 * character, or a section with no readable capture time, is ignored.
	 *
	 * @param previous the previous export, may be {@code null}
	 */
	public void restore(CharacterSnapshot previous)
	{
		Player localPlayer = client.getLocalPlayer();
		if (previous == null || previous.player == null || localPlayer == null
				|| !Objects.equals(previous.player.username, localPlayer.getName()))
			return;

		Instant fallback = parseInstant(previous.lastUpdated);
		Map<String, String> times = previous.sectionUpdated != null
				? previous.sectionUpdated
				: Collections.emptyMap();

		Instant bankTime = sectionTime(times, "bank", fallback);
		if (bankUpdated == null && bankTime != null && previous.bank != null && previous.bank.items != null)
		{
			bankItems = new ArrayList<>(previous.bank.items);
			bankTabs = previous.bank.tabs != null && !previous.bank.tabs.isEmpty()
					? new ArrayList<>(previous.bank.tabs)
					: new ArrayList<>(Collections.singletonList(new BankTab(0, bankItems)));
			bankTotal = bankItems.size();
			bankUpdated = bankTime;
		}

		Instant inventoryTime = sectionTime(times, "inventory", fallback);
		if (inventoryUpdated == null && inventoryTime != null && previous.inventory != null)
		{
			inventory = new ArrayList<>(previous.inventory);
			inventoryUpdated = inventoryTime;
		}

		if (previous.killCounts != null)
		{
			for (Map.Entry<String, Integer> entry : previous.killCounts.entrySet())
			{
				if (entry.getKey() != null && entry.getValue() != null)
					countsFor(entry.getKey()).merge(entry.getKey(), entry.getValue(), Math::max);
			}
		}

		if (previous.activityCounts != null)
		{
			for (Map.Entry<String, Integer> entry : previous.activityCounts.entrySet())
			{
				if (entry.getKey() != null && entry.getValue() != null)
					activityCounts.merge(entry.getKey(), entry.getValue(), Math::max);
			}
		}

		if (previous.clueCounts != null)
		{
			for (Map.Entry<String, Integer> entry : previous.clueCounts.entrySet())
			{
				if (ClueChat.TIERS.contains(entry.getKey()) && entry.getValue() != null)
					clueCounts.merge(entry.getKey(), entry.getValue(), Math::max);
			}
		}

		Instant equipmentTime = sectionTime(times, "equipment", fallback);
		if (equipmentUpdated == null && equipmentTime != null && previous.equipment != null)
		{
			equipment = new LinkedHashMap<>(previous.equipment);
			equipmentUpdated = equipmentTime;
		}

		if (previous.storages != null)
		{
			for (String key : STORAGE_KEYS.values())
			{
				List<ItemEntry> old = previous.storages.get(key);
				Instant time = sectionTime(times, "storages." + key, fallback);
				if (!storageUpdated.containsKey(key) && old != null && time != null)
				{
					storages.put(key, new ArrayList<>(old));
					storageUpdated.put(key, time);
				}
			}
		}

		Instant clogTime = sectionTime(times, "collectionLog", fallback);
		if (clogUpdated == null && clogTime != null && previous.collectionLog != null
				&& previous.collectionLog.pages != null)
		{
			clogPages.clear();
			clogPages.putAll(previous.collectionLog.pages);
			clogObtained = previous.collectionLog.obtained;
			clogTotal = previous.collectionLog.total;
			clogUpdated = clogTime;
		}
	}

	/**
	 * Polls the rune pouch contents. The pouch is present only while the inventory holds one of its variants
	 * (the same check RuneLite's rune pouch plugin makes); otherwise the cached contents are cleared. Each
	 * of the six slots pairs a rune-type varbit with a quantity varbit, and the rune type maps to an item id
	 * through the {@link EnumID#RUNEPOUCH_RUNE} cache enum. Empty slots and unresolvable runes are skipped.
	 *
	 * @return whether the pouch contents (or its presence) changed since the last poll
	 */
	public boolean pollRunePouch()
	{
		List<ItemEntry> contents = hasRunePouch() ? readRunePouch() : null;
		boolean changed = !Objects.equals(contents, runePouch);
		runePouch = contents;
		return changed;
	}

	/**
	 * @return whether the live inventory holds any rune pouch variant
	 */
	private boolean hasRunePouch()
	{
		ItemContainer inv = client.getItemContainer(InventoryID.INV);
		if (inv == null)
			return false;

		for (Item item : inv.getItems())
		{
			for (int pouch : RUNE_POUCH_ITEMS)
			{
				if (item.getId() == pouch)
					return true;
			}
		}

		return false;
	}

	/**
	 * Reads the rune pouch slots from the client varbits.
	 *
	 * @return the runes held, possibly empty
	 */
	private List<ItemEntry> readRunePouch()
	{
		List<ItemEntry> runes = new ArrayList<>();
		EnumComposition runeEnum = client.getEnum(EnumID.RUNEPOUCH_RUNE);
		if (runeEnum == null)
			return runes;

		for (int slot = 0; slot < RUNE_POUCH_TYPE_VARBITS.length; slot++)
		{
			int type = client.getVarbitValue(RUNE_POUCH_TYPE_VARBITS[slot]);
			int amount = client.getVarbitValue(RUNE_POUCH_QUANTITY_VARBITS[slot]);
			if (type <= 0 || amount <= 0)
				continue;

			int itemId = runeEnum.getIntValue(type);
			if (itemId > 0)
				runes.add(new ItemEntry(itemId, getItemName(itemId), amount));
		}

		return runes;
	}

	/**
	 * Reads every boss kill-count varp (authoritative, exact and current), then merges the counts the
	 * Chat Commands plugin stored in the RuneScape-profile config (group {@code killcount}, key = lower-cased
	 * name) as a fallback for bosses without a varp. Non-boss counts such as agility laps go to
	 * {@code activityCounts}. A stored count never lowers a cached one and never overrides a varp value.
	 *
	 * @return whether any cached count changed
	 */
	public boolean pollKillCounts()
	{
		boolean changed = false;
		for (Map.Entry<Integer, String> boss : KillCountVarps.BOSSES.entrySet())
		{
			int value = client.getVarpValue(boss.getKey());
			if (value > 0 && !Integer.valueOf(value).equals(varpKillCounts.put(boss.getValue(), value)))
				changed = true;
		}

		if (configManager == null)
			return changed;

		String profile = configManager.getRSProfileKey();
		for (String key : configManager.getRSProfileConfigurationKeys(ChatCounts.KILLCOUNT_GROUP, profile, ""))
		{
			Integer stored = configManager.getRSProfileConfiguration(ChatCounts.KILLCOUNT_GROUP, key, int.class);
			if (stored == null || stored <= 0)
				continue;

			Map<String, Integer> target = countsFor(key);
			Integer cached = target.get(key);
			if (cached == null || stored > cached)
			{
				target.put(key, stored);
				changed |= !isVarpBoss(key);
			}
		}

		return changed;
	}

	/**
	 * Reads a kill-count line from chat. A chat count is current by definition, so it replaces the cached
	 * value even when lower, but it never overrides a varp-backed boss count.
	 *
	 * @param message the raw chat message
	 * @return whether the exported counts changed
	 */
	public boolean onChatMessage(String message)
	{
		Map.Entry<String, Integer> parsed = ChatCounts.parseKillCount(message);
		if (parsed == null)
			return false;

		Integer previous = countsFor(parsed.getKey()).put(parsed.getKey(), parsed.getValue());
		return !parsed.getValue().equals(previous) && !isVarpBoss(parsed.getKey());
	}

	private Map<String, Integer> countsFor(String key)
	{
		return ChatCounts.isActivityKey(key) ? activityCounts : killCounts;
	}

	private boolean isVarpBoss(String key)
	{
		return varpKillCounts.containsKey(key);
	}

	private Map<String, Integer> mergedKillCounts()
	{
		Map<String, Integer> merged = new TreeMap<>(killCounts);
		merged.putAll(varpKillCounts);
		return merged;
	}

	/**
	 * Reads a treasure-trail completion line from chat. The game prints the new running total, so a parsed
	 * value replaces the cached one.
	 *
	 * @param message the raw chat message
	 * @return whether the cached clue counts changed
	 */
	public boolean onClueMessage(String message)
	{
		Map.Entry<String, Integer> parsed = ClueChat.parse(message);
		if (parsed == null)
			return false;

		Integer previous = clueCounts.put(parsed.getKey(), parsed.getValue());
		return !parsed.getValue().equals(previous);
	}

	/**
	 * Re-reads the farming and birdhouse timers from the Time Tracking plugin's stored data.
	 *
	 * @return whether the timers changed since the last poll
	 */
	public boolean pollTimers()
	{
		return pollTimers(Instant.now().getEpochSecond());
	}

	/**
	 * Re-reads the farming and birdhouse timers as of a given time.
	 *
	 * @param nowSeconds the current time, Unix seconds
	 * @return whether the timers changed since the last poll
	 */
	boolean pollTimers(long nowSeconds)
	{
		if (configManager == null)
			return false;

		EnumSet<WorldType> worldTypes = client.getWorldType();
		boolean leagues = worldTypes != null && worldTypes.contains(WorldType.SEASONAL)
			&& !worldTypes.contains(WorldType.DEADMAN);
		TimersData fresh = new TimersData(
			FarmingTimers.readFarming(configManager, nowSeconds, leagues),
			FarmingTimers.readBirdhouses(configManager, nowSeconds));
		if (fresh.farming.isEmpty() && fresh.birdhouses.isEmpty())
			fresh = null;

		boolean changed = !Objects.equals(timers, fresh);
		timers = fresh;
		return changed;
	}

	/**
	 * Polls quest completion state and total quest points.
	 *
	 * @return whether either the quest states or the quest-point total changed since the last poll
	 */
	@SuppressWarnings("deprecation")
	public boolean pollQuests()
	{
		List<QuestEntry> questList = new ArrayList<>();
		for (Quest quest : Quest.values())
		{
			try
			{
				QuestState state = quest.getState(client);
				questList.add(new QuestEntry(quest.name(), quest.getName(), stateName(state)));
			}
			catch (RuntimeException e)
			{
				// some quests are not queryable in every client state; skip them
			}
		}

		int qp = client.getVarpValue(VarPlayer.QUEST_POINTS);
		boolean changed = !questList.equals(quests) || !Objects.equals(questPoints, qp);
		quests = questList;
		questPoints = qp;
		return changed;
	}

	/**
	 * Polls achievement-diary completion for every region via named RuneLite varbits, and each mapped
	 * task via {@link DiaryTasks}.
	 *
	 * @return whether any diary completion changed since the last poll
	 */
	@SuppressWarnings("deprecation")
	public boolean pollDiaries()
	{
		Map<String, DiaryRegion> map = new LinkedHashMap<>();
		map.put("ARDOUGNE", diaryRegion("ARDOUGNE",
			Varbits.DIARY_ARDOUGNE_EASY, Varbits.DIARY_ARDOUGNE_MEDIUM,
			Varbits.DIARY_ARDOUGNE_HARD, Varbits.DIARY_ARDOUGNE_ELITE));
		map.put("DESERT", diaryRegion("DESERT",
			Varbits.DIARY_DESERT_EASY, Varbits.DIARY_DESERT_MEDIUM,
			Varbits.DIARY_DESERT_HARD, Varbits.DIARY_DESERT_ELITE));
		map.put("FALADOR", diaryRegion("FALADOR",
			Varbits.DIARY_FALADOR_EASY, Varbits.DIARY_FALADOR_MEDIUM,
			Varbits.DIARY_FALADOR_HARD, Varbits.DIARY_FALADOR_ELITE));
		map.put("FREMENNIK", diaryRegion("FREMENNIK",
			Varbits.DIARY_FREMENNIK_EASY, Varbits.DIARY_FREMENNIK_MEDIUM,
			Varbits.DIARY_FREMENNIK_HARD, Varbits.DIARY_FREMENNIK_ELITE));
		map.put("KANDARIN", diaryRegion("KANDARIN",
			Varbits.DIARY_KANDARIN_EASY, Varbits.DIARY_KANDARIN_MEDIUM,
			Varbits.DIARY_KANDARIN_HARD, Varbits.DIARY_KANDARIN_ELITE));
		map.put("KARAMJA", diaryRegion("KARAMJA",
			Varbits.DIARY_KARAMJA_EASY, Varbits.DIARY_KARAMJA_MEDIUM,
			Varbits.DIARY_KARAMJA_HARD, Varbits.DIARY_KARAMJA_ELITE, KARAMJA_TIER_DONE));
		map.put("KOUREND", diaryRegion("KOUREND",
			Varbits.DIARY_KOUREND_EASY, Varbits.DIARY_KOUREND_MEDIUM,
			Varbits.DIARY_KOUREND_HARD, Varbits.DIARY_KOUREND_ELITE));
		map.put("LUMBRIDGE", diaryRegion("LUMBRIDGE",
			Varbits.DIARY_LUMBRIDGE_EASY, Varbits.DIARY_LUMBRIDGE_MEDIUM,
			Varbits.DIARY_LUMBRIDGE_HARD, Varbits.DIARY_LUMBRIDGE_ELITE));
		map.put("MORYTANIA", diaryRegion("MORYTANIA",
			Varbits.DIARY_MORYTANIA_EASY, Varbits.DIARY_MORYTANIA_MEDIUM,
			Varbits.DIARY_MORYTANIA_HARD, Varbits.DIARY_MORYTANIA_ELITE));
		map.put("VARROCK", diaryRegion("VARROCK",
			Varbits.DIARY_VARROCK_EASY, Varbits.DIARY_VARROCK_MEDIUM,
			Varbits.DIARY_VARROCK_HARD, Varbits.DIARY_VARROCK_ELITE));
		map.put("WESTERN", diaryRegion("WESTERN",
			Varbits.DIARY_WESTERN_EASY, Varbits.DIARY_WESTERN_MEDIUM,
			Varbits.DIARY_WESTERN_HARD, Varbits.DIARY_WESTERN_ELITE));
		map.put("WILDERNESS", diaryRegion("WILDERNESS",
			Varbits.DIARY_WILDERNESS_EASY, Varbits.DIARY_WILDERNESS_MEDIUM,
			Varbits.DIARY_WILDERNESS_HARD, Varbits.DIARY_WILDERNESS_ELITE));

		boolean changed = !map.equals(diaries);
		diaries = map;
		return changed;
	}

	/**
	 * Polls the six combat-achievement tier varbits and the per-task completion bits.
	 *
	 * @return whether any tier value or task completion changed since the last poll
	 */
	@SuppressWarnings("deprecation")
	public boolean pollCombatAchievements()
	{
		CombatAchievementData ca = new CombatAchievementData(
			client.getVarbitValue(Varbits.COMBAT_ACHIEVEMENT_TIER_EASY),
			client.getVarbitValue(Varbits.COMBAT_ACHIEVEMENT_TIER_MEDIUM),
			client.getVarbitValue(Varbits.COMBAT_ACHIEVEMENT_TIER_HARD),
			client.getVarbitValue(Varbits.COMBAT_ACHIEVEMENT_TIER_ELITE),
			client.getVarbitValue(Varbits.COMBAT_ACHIEVEMENT_TIER_MASTER),
			client.getVarbitValue(Varbits.COMBAT_ACHIEVEMENT_TIER_GRANDMASTER),
			buildCombatAchievementTasks(),
			client.getVarbitValue(VarbitID.CA_POINTS));
		boolean changed = !ca.equals(combatAchievements);
		combatAchievements = ca;
		return changed;
	}

	/**
	 * Combines the completion varps with the cached task metadata. Task id {@code N} is bit
	 * {@code N % 32} of the {@code N / 32}th entry of {@link #CA_COMPLETED_VARPS}.
	 *
	 * @return every known task with its completion flag, plus any completed id missing from the metadata
	 */
	private List<CombatAchievementTask> buildCombatAchievementTasks()
	{
		reloadCombatAchievementMetaIfNeeded();

		Map<Integer, CombatAchievementTask> byId = new TreeMap<>();
		for (CombatAchievementTask meta : caTaskMeta.values())
			byId.put(meta.id, meta);

		for (int block = 0; block < CA_COMPLETED_VARPS.length; block++)
		{
			int value = client.getVarpValue(CA_COMPLETED_VARPS[block]);
			for (int bit = 0; bit < 32; bit++)
			{
				int id = block * 32 + bit;
				boolean done = (value & (1 << bit)) != 0;
				CombatAchievementTask meta = byId.get(id);
				if (meta != null)
					byId.put(id, new CombatAchievementTask(id, meta.name, meta.tier, meta.monster, done));
				else if (done)
					byId.put(id, new CombatAchievementTask(id, null, null, null, true));
			}
		}

		return new ArrayList<>(byId.values());
	}

	/**
	 * Loads the task metadata when none is cached, and retries (at most {@link #CA_META_MAX_RETRIES} times)
	 * while the last load was partial, i.e. some tier enum or task struct was not returned by the cache.
	 * A retry never replaces the cached metadata with a smaller set.
	 */
	private void reloadCombatAchievementMetaIfNeeded()
	{
		boolean empty = caTaskMeta.isEmpty();
		if (!empty && (!caMetaPartial || caMetaRetries >= CA_META_MAX_RETRIES))
			return;

		if (!empty)
			caMetaRetries++;

		Map<Integer, CombatAchievementTask> loaded = loadCombatAchievementMeta();
		if (loaded.size() >= caTaskMeta.size())
			caTaskMeta = loaded;
	}

	/**
	 * Reads task names, tiers, and monsters from the game cache: each tier enum lists task structs,
	 * and each struct carries the task id, name, and monster category as params.
	 *
	 * @return the metadata by task id, or empty if the cache is not readable yet; also sets
	 *         {@link #caMetaPartial}
	 */
	private Map<Integer, CombatAchievementTask> loadCombatAchievementMeta()
	{
		Map<Integer, CombatAchievementTask> out = new TreeMap<>();
		boolean partial = false;
		EnumComposition monsterEnum = client.getEnum(CA_MONSTER_ENUM);
		Map<Integer, String> monsters = new LinkedHashMap<>();
		if (monsterEnum != null && monsterEnum.getKeys() != null && monsterEnum.getStringVals() != null)
		{
			int[] keys = monsterEnum.getKeys();
			String[] vals = monsterEnum.getStringVals();
			for (int i = 0; i < keys.length && i < vals.length; i++)
				monsters.put(keys[i], vals[i]);
		}

		for (int t = 0; t < CA_TIER_ENUMS.length; t++)
		{
			EnumComposition tierEnum = client.getEnum(CA_TIER_ENUMS[t]);
			if (tierEnum == null || tierEnum.getIntVals() == null)
			{
				partial = true;
				continue;
			}

			for (int structId : tierEnum.getIntVals())
			{
				StructComposition struct = client.getStructComposition(structId);
				if (struct == null)
				{
					partial = true;
					continue;
				}

				String name = struct.getStringValue(CA_PARAM_NAME);
				if (name == null || name.isEmpty() || "null".equals(name))
				{
					partial = true;
					continue;
				}

				int id = struct.getIntValue(CA_PARAM_ID);
				String monster = monsters.get(struct.getIntValue(CA_PARAM_MONSTER));
				out.put(id, new CombatAchievementTask(id, name, CA_TIER_NAMES[t], monster, false));
			}
		}

		caMetaPartial = partial;
		return out;
	}

	/**
	 * Polls the slayer state. Points and streak are always exported; the task fields are null/0 when no task is
	 * assigned.
	 *
	 * @return whether the slayer state changed since the last poll
	 */
	@SuppressWarnings("deprecation")
	public boolean pollSlayer()
	{
		int amount = Math.max(0, client.getVarpValue(VarPlayerID.SLAYER_COUNT));
		int creatureId = 0;
		String creatureName = null;
		String bossName = null;
		if (amount > 0)
		{
			creatureId = client.getVarpValue(VarPlayerID.SLAYER_TARGET);
			creatureName = resolveTaskName(creatureId);
			if (creatureId == SLAYER_BOSS_TASK_ID)
				bossName = resolveBossName(client.getVarbitValue(VarbitID.SLAYER_TARGET_BOSSID));
		}

		SlayerData task = new SlayerData(
			creatureId,
			creatureName,
			bossName,
			amount,
			client.getVarbitValue(Varbits.SLAYER_POINTS),
			client.getVarbitValue(Varbits.SLAYER_TASK_STREAK));
		readSlayerDetails(task);

		boolean changed = !Objects.equals(task, slayer);
		slayer = task;
		return changed;
	}

	/**
	 * Resolves a slayer task id to its display name from the cache's slayer task table.
	 *
	 * @param taskId the slayer task id
	 * @return the task name, or null when it cannot be resolved
	 */
	private String resolveTaskName(int taskId)
	{
		String cached = slayerTaskNames.get(taskId);
		if (cached != null)
			return cached;

		List<Integer> rows = client.getDBRowsByValue(
			DBTableID.SlayerTask.ID, DBTableID.SlayerTask.COL_ID, 0, taskId);
		if (rows == null || rows.isEmpty())
			return null;

		String name = readTaskRowName(rows.get(0));
		if (name != null)
			slayerTaskNames.put(taskId, name);

		return name;
	}

	/**
	 * Resolves a boss-task boss id to the boss name via the slayer task sublist table.
	 *
	 * @param bossId the value of the slayer target boss varbit
	 * @return the boss name, or null when it cannot be resolved
	 */
	private String resolveBossName(int bossId)
	{
		String cached = slayerBossNames.get(bossId);
		if (cached != null)
			return cached;

		List<Integer> rows = client.getDBRowsByValue(
			DBTableID.SlayerTaskSublist.ID, DBTableID.SlayerTaskSublist.COL_TASK_SUBTABLE_ID, 0, bossId);
		if (rows == null || rows.isEmpty())
			return null;

		Object[] taskRow = client.getDBTableField(rows.get(0), DBTableID.SlayerTaskSublist.COL_TASK, 0);
		if (taskRow == null || taskRow.length == 0 || !(taskRow[0] instanceof Integer))
			return null;

		String name = readTaskRowName((Integer) taskRow[0]);
		if (name != null)
			slayerBossNames.put(bossId, name);

		return name;
	}

	/**
	 * Reads the display name column of one slayer task table row.
	 *
	 * @param row the slayer task table row id
	 * @return the name, or null when absent
	 */
	private String readTaskRowName(int row)
	{
		Object[] values = client.getDBTableField(row, DBTableID.SlayerTask.COL_NAME_UPPERCASE, 0);
		if (values == null || values.length == 0 || !(values[0] instanceof String))
			return null;

		return (String) values[0];
	}

	/**
	 * Adds, replaces, or removes a cached Grand Exchange offer for one slot.
	 *
	 * @param slot  the slot index
	 * @param offer the offer, or an {@code EMPTY}/null offer to clear the slot
	 */
	public void updateGeOffer(int slot, GrandExchangeOffer offer)
	{
		if (offer == null || offer.getState() == GrandExchangeOfferState.EMPTY)
		{
			geOffers.remove(slot);
			return;
		}

		geOffers.put(slot, toGeOffer(slot, offer));
	}

	/**
	 * Rebuilds the Grand Exchange cache from the client's current offer array.
	 */
	public void pollAllGeOffers()
	{
		geOffers.clear();
		GrandExchangeOffer[] offers = client.getGrandExchangeOffers();
		if (offers == null)
			return;

		for (int slot = 0; slot < offers.length; slot++)
		{
			GrandExchangeOffer offer = offers[slot];
			if (offer != null && offer.getState() != GrandExchangeOfferState.EMPTY)
				geOffers.put(slot, toGeOffer(slot, offer));
		}
	}

	/**
	 * Assembles the export snapshot from the caches and cheap client reads, honouring the config
	 * toggles. Owned collections are copied so the writer thread reads a stable graph. The local
	 * player may already be gone when the client reaches the login screen, so identity and location
	 * are remembered from the last live build and used for the final write of a session.
	 *
	 * @return the snapshot, or {@code null} when no character is logged in and none was seen this session
	 */
	@SuppressWarnings("deprecation")
	public CharacterSnapshot buildSnapshot()
	{
		Player localPlayer = client.getLocalPlayer();
		boolean live = localPlayer != null && localPlayer.getName() != null;
		if (!live && lastPlayer == null)
			return null;

		CharacterSnapshot data = new CharacterSnapshot();
		data.lastUpdated = Instant.now().toString();

		if (live)
			lastPlayer = readPlayerInfo(localPlayer);

		data.player = new PlayerInfo(
			lastPlayer.username,
			lastPlayer.combatLevel,
			lastPlayer.world,
			lastPlayer.accountType,
			lastPlayer.members,
			lastPlayer.spellbook);

		if (config.syncSkills() && !skills.isEmpty())
			data.skills = new LinkedHashMap<>(skills);

		if (config.syncVitals())
			data.vitals = buildVitals();

		if (config.syncBank() && bankItems != null)
			data.bank = new BankData(bankTotal, new ArrayList<>(bankItems), copyBankTabs());

		if (config.syncInventory())
			data.inventory = new ArrayList<>(inventory);

		if (config.syncEquipment() && equipmentUpdated != null)
			data.equipment = new LinkedHashMap<>(equipment);

		if (config.syncStorages() && !storages.isEmpty())
		{
			data.storages = new LinkedHashMap<>();
			for (Map.Entry<String, List<ItemEntry>> entry : storages.entrySet())
				data.storages.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}

		if (config.syncCollectionLog())
			data.collectionLog = buildCollectionLog();

		data.sectionUpdated = buildSectionUpdated(data);

		if (config.syncRunePouch() && runePouch != null)
			data.runePouch = new ArrayList<>(runePouch);

		if (config.syncKillCounts())
		{
			Map<String, Integer> bosses = mergedKillCounts();
			if (!bosses.isEmpty())
				data.killCounts = bosses;

			if (!activityCounts.isEmpty())
				data.activityCounts = new TreeMap<>(activityCounts);
		}

		if (config.syncClueCounts() && !clueCounts.isEmpty())
			data.clueCounts = orderedClueCounts();

		if (config.syncTimers() && timers != null)
			data.timers = new TimersData(new ArrayList<>(timers.farming), new ArrayList<>(timers.birdhouses));

		if (config.syncQuests() && quests != null)
		{
			data.quests = new ArrayList<>(quests);
			data.questPoints = questPoints;
		}

		if (config.syncDiaries() && diaries != null)
			data.achievementDiaries = new LinkedHashMap<>(diaries);

		if (config.syncCombatAchievements())
			data.combatAchievements = combatAchievements;

		if (config.syncSlayer())
			data.slayer = slayer;

		if (config.syncLocation())
		{
			if (live)
				lastLocation = buildLocation(localPlayer);

			data.location = lastLocation;
		}

		if (config.syncGrandExchange() && !geOffers.isEmpty())
			data.grandExchange = new ArrayList<>(geOffers.values());

		return data;
	}

	/**
	 * Copies the known clue counts in tier order, beginner first.
	 *
	 * @return tier name to completed count, only for tiers seen
	 */
	private Map<String, Integer> orderedClueCounts()
	{
		Map<String, Integer> ordered = new LinkedHashMap<>();
		for (String tier : ClueChat.TIERS)
		{
			if (clueCounts.containsKey(tier))
				ordered.put(tier, clueCounts.get(tier));
		}

		return ordered;
	}

	/**
	 * Collects the capture time of each item section present in the snapshot.
	 *
	 * @param data the snapshot being built, with its item sections already set
	 * @return section name to ISO-8601 capture time, or {@code null} when no item section is present
	 */
	private Map<String, String> buildSectionUpdated(CharacterSnapshot data)
	{
		Map<String, String> times = new LinkedHashMap<>();
		if (data.bank != null && bankUpdated != null)
			times.put("bank", bankUpdated.toString());

		if (data.inventory != null && inventoryUpdated != null)
			times.put("inventory", inventoryUpdated.toString());

		if (data.equipment != null && equipmentUpdated != null)
			times.put("equipment", equipmentUpdated.toString());

		if (data.storages != null)
		{
			for (String key : data.storages.keySet())
			{
				Instant time = storageUpdated.get(key);
				if (time != null)
					times.put("storages." + key, time.toString());
			}
		}

		if (data.collectionLog != null && clogUpdated != null)
			times.put("collectionLog", clogUpdated.toString());

		return times.isEmpty() ? null : times;
	}

	/**
	 * Resolves a restored section's capture time from the previous export.
	 *
	 * @param times    the previous export's per-section times
	 * @param section  the section name
	 * @param fallback the previous export's overall build time, may be {@code null}
	 * @return the section's own time if readable, else {@code fallback}
	 */
	private static Instant sectionTime(Map<String, String> times, String section, Instant fallback)
	{
		Instant own = parseInstant(times.get(section));
		return own != null ? own : fallback;
	}

	/**
	 * Parses an ISO-8601 instant, tolerating a missing or malformed value.
	 *
	 * @param value the text to parse, may be {@code null}
	 * @return the instant, or {@code null} if absent or unparseable
	 */
	private static Instant parseInstant(String value)
	{
		if (value == null)
			return null;

		try
		{
			return Instant.parse(value);
		}
		catch (DateTimeParseException e)
		{
			return null;
		}
	}

	/**
	 * Reads a diary region whose four tier varbits are 0/1 completion flags.
	 *
	 * @param region the region key
	 * @param easy   the easy-tier varbit id
	 * @param medium the medium-tier varbit id
	 * @param hard   the hard-tier varbit id
	 * @param elite  the elite-tier varbit id
	 * @return the region's completion state
	 */
	private DiaryRegion diaryRegion(String region, int easy, int medium, int hard, int elite)
	{
		return diaryRegion(region, easy, medium, hard, elite, 1);
	}

	/**
	 * Reads a diary region's four tiers and its mapped tasks. The elite tier is complete when its varbit is 1;
	 * the easy, medium and hard tiers are complete once their varbit reaches {@code doneValue}.
	 *
	 * @param region    the region key
	 * @param easy      the easy-tier varbit id
	 * @param medium    the medium-tier varbit id
	 * @param hard      the hard-tier varbit id
	 * @param elite     the elite-tier varbit id
	 * @param doneValue the easy, medium and hard varbit value that marks the tier complete
	 * @return the region's completion state
	 */
	private DiaryRegion diaryRegion(String region, int easy, int medium, int hard, int elite, int doneValue)
	{
		Map<String, List<DiaryTask>> tasks = new LinkedHashMap<>();
		for (Map.Entry<String, List<DiaryTasks.Task>> tier : DiaryTasks.tasks(region).entrySet())
		{
			List<DiaryTask> list = new ArrayList<>();
			for (DiaryTasks.Task task : tier.getValue())
				list.add(new DiaryTask(task.name, task.isComplete(client)));

			tasks.put(tier.getKey(), list);
		}

		return new DiaryRegion(
			client.getVarbitValue(easy) >= doneValue,
			client.getVarbitValue(medium) >= doneValue,
			client.getVarbitValue(hard) >= doneValue,
			client.getVarbitValue(elite) == 1,
			tasks.isEmpty() ? null : tasks);
	}

	/**
	 * Reads current combat vitals from boosted and real skill levels and run energy.
	 *
	 * @return the vitals snapshot
	 */
	private VitalsData buildVitals()
	{
		return new VitalsData(
			client.getBoostedSkillLevel(Skill.HITPOINTS),
			client.getRealSkillLevel(Skill.HITPOINTS),
			client.getBoostedSkillLevel(Skill.PRAYER),
			client.getRealSkillLevel(Skill.PRAYER),
			client.getEnergy() / 100);
	}

	/**
	 * Reads the character's world location.
	 *
	 * @param localPlayer the local player
	 * @return the location, or {@code null} when it is unavailable
	 */
	@SuppressWarnings("deprecation")
	private LocationData buildLocation(Player localPlayer)
	{
		WorldPoint wp = resolveLocation(localPlayer);
		if (wp == null)
			return null;

		return new LocationData(wp.getRegionID(), wp.getX(), wp.getY(), wp.getPlane(), client.isInInstancedRegion());
	}

	/**
	 * Converts a client GE offer into the exported model.
	 *
	 * @param slot  the slot index
	 * @param offer the client offer
	 * @return the export model offer
	 */
	private GeOffer toGeOffer(int slot, GrandExchangeOffer offer)
	{
		return new GeOffer(
			slot,
			offer.getItemId(),
			getItemName(offer.getItemId()),
			offer.getState().name(),
			offer.getQuantitySold(),
			offer.getTotalQuantity(),
			offer.getPrice(),
			offer.getSpent());
	}

	/**
	 * Maps a quest state to its exported string form.
	 *
	 * @param state the quest state
	 * @return {@code FINISHED}, {@code IN_PROGRESS}, or {@code NOT_STARTED}
	 */
	private static String stateName(QuestState state)
	{
		switch (state)
		{
			case FINISHED:
				return "FINISHED";
			case IN_PROGRESS:
				return "IN_PROGRESS";
			default:
				return "NOT_STARTED";
		}
	}

	/**
	 * Resolves an item id to its display name.
	 *
	 * @param itemId the item id
	 * @return the item name, or {@code null} when it cannot be resolved
	 */
	private String getItemName(int itemId)
	{
		ItemComposition def = client.getItemDefinition(itemId);
		return def != null ? def.getName() : null;
	}

	/**
	 * Fills in the slayer master, blocked tasks and reward unlocks of a task.
	 *
	 * @param task the slayer data to complete
	 */
	private void readSlayerDetails(SlayerData task)
	{
		int masterId = client.getVarbitValue(VarbitID.SLAYER_MASTER);
		task.masterId = masterId;
		task.masterName = SlayerInfo.masterName(masterId);
		task.blockedTasks = readBlockedTasks();
		task.unlockedTasks = readFlags(SlayerInfo.UNLOCK_VARBITS);
		task.extendedTasks = readFlags(SlayerInfo.EXTEND_VARBITS);
	}

	/**
	 * Reads every master's blocked task slots, falling back to the legacy shared block list when no
	 * per-master slot is filled.
	 *
	 * @return the blocked tasks, never null
	 */
	private List<BlockedTask> readBlockedTasks()
	{
		List<BlockedTask> blocked = new ArrayList<>();
		for (Map.Entry<String, int[]> entry : SlayerInfo.blockVarbits().entrySet())
		{
			for (int varbit : entry.getValue())
			{
				int taskId = client.getVarbitValue(varbit);
				if (taskId > 0)
					blocked.add(new BlockedTask(taskId, resolveTaskName(taskId), entry.getKey()));
			}
		}

		if (!blocked.isEmpty())
			return blocked;

		for (int varbit : SlayerInfo.LEGACY_BLOCK_VARBITS)
		{
			int taskId = client.getVarbitValue(varbit);
			if (taskId > 0)
				blocked.add(new BlockedTask(taskId, resolveTaskName(taskId), null));
		}

		return blocked;
	}

	/**
	 * Lists the keys whose varbit is non-zero.
	 *
	 * @param varbits key to varbit id
	 * @return the keys of the set flags, in table order
	 */
	private List<String> readFlags(Map<String, Integer> varbits)
	{
		List<String> keys = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : varbits.entrySet())
		{
			if (client.getVarbitValue(entry.getValue()) != 0)
				keys.add(entry.getKey());
		}

		return keys;
	}

	/**
	 * Storage containers captured when opened, keyed by container id to export key. The group storage id is the
	 * one the legacy {@code InventoryID.GROUP_STORAGE} constant held; the current API names it
	 * {@code INV_GROUP_TEMP}.
	 */
	private static final Map<Integer, String> STORAGE_KEYS = new LinkedHashMap<>();

	static
	{
		STORAGE_KEYS.put(InventoryID.LOOTING_BAG, "lootingBag");
		STORAGE_KEYS.put(InventoryID.SEED_VAULT, "seedVault");
		STORAGE_KEYS.put(InventoryID.INV_GROUP_TEMP, "groupStorage");
	}

	private final Map<String, List<ItemEntry>> storages = new LinkedHashMap<>();

	private final Map<String, Instant> storageUpdated = new HashMap<>();

	/**
	 * Resolves the export key of a storage container.
	 *
	 * @param containerId the item container id
	 * @return {@code lootingBag}, {@code seedVault} or {@code groupStorage}, or {@code null} for any other container
	 */
	public static String storageKey(int containerId)
	{
		return STORAGE_KEYS.get(containerId);
	}

	/**
	 * @return the item container ids of every captured storage, in export order
	 */
	public static Iterable<Integer> storageContainerIds()
	{
		return STORAGE_KEYS.keySet();
	}

	/**
	 * Replaces the cached contents of one storage container, dropping empty slots and placeholders.
	 *
	 * @param key       the storage export key, see {@link #storageKey(int)}
	 * @param container the storage item container
	 */
	public void updateStorage(String key, ItemContainer container)
	{
		if (key == null || container == null)
			return;

		List<ItemEntry> items = new ArrayList<>();
		for (Item item : container.getItems())
		{
			if (item.getId() > 0 && item.getQuantity() > 0)
				items.add(new ItemEntry(item.getId(), getItemName(item.getId()), item.getQuantity()));
		}

		storages.put(key, items);
		storageUpdated.put(key, Instant.now());
	}

	/** Spellbook varbit values 0 to 3, in order; any other value is reported as unknown. */
	private static final String[] SPELLBOOK_NAMES = {"STANDARD", "ANCIENT", "LUNAR", "ARCEUUS"};

	private String spellbook = null;

	/**
	 * Polls the active spellbook from the spellbook varbit (0 standard, 1 ancient, 2 lunar, 3 arceuus).
	 *
	 * @return whether the spellbook changed since the last poll
	 */
	public boolean pollSpellbook()
	{
		int value = client.getVarbitValue(VarbitID.SPELLBOOK);
		String book = value >= 0 && value < SPELLBOOK_NAMES.length ? SPELLBOOK_NAMES[value] : null;
		boolean changed = !Objects.equals(book, spellbook);
		spellbook = book;
		return changed;
	}

	/** Index of the page title text among the children of the collection log header widget. */
	private static final int CLOG_HEADER_TITLE_INDEX = 0;

	private final Map<String, CollectionLogPage> clogPages = new LinkedHashMap<>();

	private int clogObtained = 0;

	private int clogTotal = 0;

	private Instant clogUpdated = null;

	/**
	 * Captures the collection log page currently drawn. Called when the log's draw-list script has
	 * run, so it only does work as the player opens or switches pages. Each item widget under the
	 * items container is an obtained item when its opacity is 0, and a greyed-out missing item
	 * otherwise. The page is stored under its title, replacing any earlier capture of that page, and
	 * the account-wide counts are read from the collection log varps.
	 *
	 * @return whether the captured data changed
	 */
	public boolean captureCollectionLog()
	{
		Widget header = client.getWidget(InterfaceID.Collection.HEADER_TEXT);
		Widget content = client.getWidget(InterfaceID.Collection.ITEMS_CONTENTS);
		if (header == null || content == null || header.getChildren() == null || content.getChildren() == null)
			return false;

		Widget title = header.getChild(CLOG_HEADER_TITLE_INDEX);
		if (title == null || title.getText() == null || title.getText().isEmpty())
			return false;

		int total = 0;
		List<ItemEntry> obtainedItems = new ArrayList<>();
		for (Widget child : content.getChildren())
		{
			if (child == null || child.getItemId() <= 0)
				continue;

			total++;
			if (child.getOpacity() == 0)
				obtainedItems.add(new ItemEntry(child.getItemId(), getItemName(child.getItemId()),
					Math.max(1, child.getItemQuantity())));
		}

		CollectionLogPage page = new CollectionLogPage(total, obtainedItems.size(), obtainedItems);
		boolean changed = !page.equals(clogPages.get(title.getText()));
		clogPages.put(title.getText(), page);

		int obtained = client.getVarpValue(VarPlayerID.COLLECTION_COUNT);
		int max = client.getVarpValue(VarPlayerID.COLLECTION_COUNT_MAX);
		changed |= obtained != clogObtained || max != clogTotal;
		clogObtained = obtained;
		clogTotal = max;
		if (changed || clogUpdated == null)
			clogUpdated = Instant.now();

		return changed;
	}

	/**
	 * Builds the exported collection log from the stored pages. The flat item list holds each obtained
	 * item id once, taking the quantity from the first page it appears on.
	 *
	 * @return the collection log, or {@code null} when no page has been captured
	 */
	private CollectionLogData buildCollectionLog()
	{
		if (clogPages.isEmpty())
			return null;

		Map<Integer, ItemEntry> flat = new LinkedHashMap<>();
		Map<String, CollectionLogPage> pages = new LinkedHashMap<>();
		for (Map.Entry<String, CollectionLogPage> e : clogPages.entrySet())
		{
			CollectionLogPage page = e.getValue();
			pages.put(e.getKey(), new CollectionLogPage(page.total, page.obtained, new ArrayList<>(page.items)));
			for (ItemEntry item : page.items)
				flat.putIfAbsent(item.itemId, item);
		}

		return new CollectionLogData(clogObtained, clogTotal, new ArrayList<>(flat.values()), pages);
	}

	/** Identity captured by the last snapshot built with a live local player; cleared by {@link #reset()}. */
	private PlayerInfo lastPlayer = null;

	/** Location captured by the last snapshot built with a live local player; cleared by {@link #reset()}. */
	private LocationData lastLocation = null;

	/**
	 * Reads the live player's identity and account context.
	 *
	 * @param localPlayer the local player
	 * @return the player info
	 */
	@SuppressWarnings("deprecation")
	private PlayerInfo readPlayerInfo(Player localPlayer)
	{
		String accountType = null;
		Boolean members = null;
		String book = null;
		if (config.syncAccountInfo())
		{
			AccountType type = client.getAccountType();
			accountType = type != null ? type.name() : null;
			members = client.getWorldType().contains(WorldType.MEMBERS);
			book = spellbook;
		}

		return new PlayerInfo(
			localPlayer.getName(),
			localPlayer.getCombatLevel(),
			client.getWorld(),
			accountType,
			members,
			book);
	}

	private WorldPoint polledLocation = null;

	private Integer polledEnergy = null;

	/**
	 * Compares the character's real-world location and run energy against the previous call. Meant to
	 * run every game tick so walking or regenerating run energy marks the snapshot dirty.
	 *
	 * @return whether an enabled value (location or vitals) changed since the last call
	 */
	public boolean pollMovement()
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
			return false;

		WorldPoint location = resolveLocation(localPlayer);
		int energy = client.getEnergy() / 100;
		boolean changed = config.syncLocation() && !Objects.equals(location, polledLocation);
		changed |= config.syncVitals() && !Objects.equals(energy, polledEnergy);
		polledLocation = location;
		polledEnergy = energy;
		return changed;
	}

	/**
	 * Resolves the character's real-world point. Inside an instance (raids, boss rooms, player-owned
	 * house) the world location is a copy-area coordinate, so the local point is mapped back to the
	 * template area it was built from.
	 *
	 * @param localPlayer the local player
	 * @return the world point, or {@code null} when it is unavailable
	 */
	private WorldPoint resolveLocation(Player localPlayer)
	{
		LocalPoint local = localPlayer.getLocalLocation();
		WorldPoint mapped = local != null ? WorldPoint.fromLocalInstance(client, local) : null;
		return mapped != null ? mapped : localPlayer.getWorldLocation();
	}
}
