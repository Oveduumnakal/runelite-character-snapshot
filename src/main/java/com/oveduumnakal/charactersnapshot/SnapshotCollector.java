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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BankData;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BankTab;
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
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.VitalsData;

import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
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
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.DBTableID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.vars.AccountType;

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

	private final Client client;

	private final CharacterSnapshotConfig config;

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

	private SlayerData slayer = null;

	/** Identity captured by the last snapshot built with a live local player; cleared by {@link #reset()}. */
	private PlayerInfo lastPlayer = null;

	/** Location captured by the last snapshot built with a live local player; cleared by {@link #reset()}. */
	private LocationData lastLocation = null;

	/**
	 * @param client the RuneLite client
	 * @param config the plugin configuration
	 */
	public SnapshotCollector(Client client, CharacterSnapshotConfig config)
	{
		this.client = client;
		this.config = config;
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
		lastPlayer = null;
		lastLocation = null;
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
		skills.put(skill.name(), new SkillEntry(level, xp, boostedLevel));
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
	 * Seeds the bank, inventory, and equipment caches from this character's previous export, so a
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

		Instant equipmentTime = sectionTime(times, "equipment", fallback);
		if (equipmentUpdated == null && equipmentTime != null && previous.equipment != null)
		{
			equipment = new LinkedHashMap<>(previous.equipment);
			equipmentUpdated = equipmentTime;
		}
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
			buildCombatAchievementTasks());
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
		if (caTaskMeta.isEmpty())
			caTaskMeta = loadCombatAchievementMeta();

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
	 * Reads task names, tiers, and monsters from the game cache: each tier enum lists task structs,
	 * and each struct carries the task id, name, and monster category as params.
	 *
	 * @return the metadata by task id, or empty if the cache is not readable yet
	 */
	private Map<Integer, CombatAchievementTask> loadCombatAchievementMeta()
	{
		Map<Integer, CombatAchievementTask> out = new TreeMap<>();
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
				continue;

			for (int structId : tierEnum.getIntVals())
			{
				StructComposition struct = client.getStructComposition(structId);
				if (struct == null)
					continue;

				String name = struct.getStringValue(CA_PARAM_NAME);
				if (name == null || name.isEmpty() || "null".equals(name))
					continue;

				int id = struct.getIntValue(CA_PARAM_ID);
				String monster = monsters.get(struct.getIntValue(CA_PARAM_MONSTER));
				out.put(id, new CombatAchievementTask(id, name, CA_TIER_NAMES[t], monster, false));
			}
		}

		return out;
	}

	/**
	 * Polls the current slayer task, clearing it when no task is assigned.
	 *
	 * @return whether the slayer task changed since the last poll
	 */
	@SuppressWarnings("deprecation")
	public boolean pollSlayer()
	{
		int amount = client.getVarpValue(VarPlayerID.SLAYER_COUNT);
		SlayerData task = null;
		if (amount > 0)
		{
			int creatureId = client.getVarpValue(VarPlayerID.SLAYER_TARGET);
			String bossName = null;
			if (creatureId == SLAYER_BOSS_TASK_ID)
				bossName = resolveBossName(client.getVarbitValue(VarbitID.SLAYER_TARGET_BOSSID));

			task = new SlayerData(
				creatureId,
				resolveTaskName(creatureId),
				bossName,
				amount,
				client.getVarbitValue(Varbits.SLAYER_POINTS),
				client.getVarbitValue(Varbits.SLAYER_TASK_STREAK));
		}

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
			lastPlayer.members);

		if (config.syncSkills() && !skills.isEmpty())
			data.skills = new LinkedHashMap<>(skills);

		if (config.syncVitals())
			data.vitals = buildVitals();

		if (config.syncBank() && bankItems != null)
			data.bank = new BankData(bankTotal, new ArrayList<>(bankItems), copyBankTabs());

		if (config.syncInventory())
			data.inventory = new ArrayList<>(inventory);

		if (config.syncEquipment() && !equipment.isEmpty())
			data.equipment = new LinkedHashMap<>(equipment);

		data.sectionUpdated = buildSectionUpdated(data);

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
	 * Reads the live player's identity and account context.
	 *
	 * @param localPlayer the local player
	 * @return the player info
	 */
	@SuppressWarnings("deprecation")
	private PlayerInfo readPlayerInfo(Player localPlayer)
	{
		String accountType = null;
		boolean members = false;
		if (config.syncAccountInfo())
		{
			AccountType type = client.getAccountType();
			accountType = type != null ? type.name() : null;
			members = client.getWorldType().contains(WorldType.MEMBERS);
		}

		return new PlayerInfo(
			localPlayer.getName(),
			localPlayer.getCombatLevel(),
			client.getWorld(),
			accountType,
			members);
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
	private LocationData buildLocation(Player localPlayer)
	{
		WorldPoint wp = localPlayer.getWorldLocation();
		if (wp == null)
			return null;

		return new LocationData(wp.getRegionID(), wp.getX(), wp.getY(), wp.getPlane());
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
}
