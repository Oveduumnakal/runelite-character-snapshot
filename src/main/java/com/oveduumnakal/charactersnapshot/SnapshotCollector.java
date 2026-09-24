/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BankData;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.CombatAchievementData;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.DiaryRegion;
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
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.coords.WorldPoint;
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
	private static final EquipmentInventorySlot[] EQUIPMENT_SLOTS = {
		EquipmentInventorySlot.HEAD, EquipmentInventorySlot.CAPE, EquipmentInventorySlot.AMULET,
		EquipmentInventorySlot.WEAPON, EquipmentInventorySlot.BODY, EquipmentInventorySlot.SHIELD,
		EquipmentInventorySlot.LEGS, EquipmentInventorySlot.GLOVES, EquipmentInventorySlot.BOOTS,
		EquipmentInventorySlot.RING, EquipmentInventorySlot.AMMO
	};

	private final Client client;

	private final CharacterSnapshotConfig config;

	private final Map<String, SkillEntry> skills = new LinkedHashMap<>();

	private final Map<Integer, GeOffer> geOffers = new TreeMap<>();

	private List<ItemEntry> bankItems = null;

	private int bankTotal = 0;

	private List<InventoryItem> inventory = new ArrayList<>();

	private Map<String, ItemEntry> equipment = new LinkedHashMap<>();

	private List<QuestEntry> quests = null;

	private Integer questPoints = null;

	private Map<String, DiaryRegion> diaries = null;

	private CombatAchievementData combatAchievements = null;

	private SlayerData slayer = null;

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
		bankTotal = 0;
		inventory = new ArrayList<>();
		quests = null;
		questPoints = null;
		diaries = null;
		combatAchievements = null;
		slayer = null;
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
	 * Replaces the cached bank contents with a flat item list.
	 *
	 * @param container the bank item container
	 */
	public void updateBank(ItemContainer container)
	{
		if (container == null)
			return;

		List<ItemEntry> items = new ArrayList<>();
		for (Item item : container.getItems())
		{
			int id = item.getId();
			if (id > 0 && item.getQuantity() > 0)
				items.add(new ItemEntry(id, getItemName(id), item.getQuantity()));
		}

		bankItems = items;
		bankTotal = items.size();
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
	 * Polls achievement-diary completion for every region via named RuneLite varbits.
	 *
	 * @return whether any diary completion changed since the last poll
	 */
	@SuppressWarnings("deprecation")
	public boolean pollDiaries()
	{
		Map<String, DiaryRegion> map = new LinkedHashMap<>();
		map.put("ARDOUGNE", diaryRegion(
			Varbits.DIARY_ARDOUGNE_EASY, Varbits.DIARY_ARDOUGNE_MEDIUM,
			Varbits.DIARY_ARDOUGNE_HARD, Varbits.DIARY_ARDOUGNE_ELITE));
		map.put("DESERT", diaryRegion(
			Varbits.DIARY_DESERT_EASY, Varbits.DIARY_DESERT_MEDIUM,
			Varbits.DIARY_DESERT_HARD, Varbits.DIARY_DESERT_ELITE));
		map.put("FALADOR", diaryRegion(
			Varbits.DIARY_FALADOR_EASY, Varbits.DIARY_FALADOR_MEDIUM,
			Varbits.DIARY_FALADOR_HARD, Varbits.DIARY_FALADOR_ELITE));
		map.put("FREMENNIK", diaryRegion(
			Varbits.DIARY_FREMENNIK_EASY, Varbits.DIARY_FREMENNIK_MEDIUM,
			Varbits.DIARY_FREMENNIK_HARD, Varbits.DIARY_FREMENNIK_ELITE));
		map.put("KANDARIN", diaryRegion(
			Varbits.DIARY_KANDARIN_EASY, Varbits.DIARY_KANDARIN_MEDIUM,
			Varbits.DIARY_KANDARIN_HARD, Varbits.DIARY_KANDARIN_ELITE));
		map.put("KARAMJA", diaryRegion(
			Varbits.DIARY_KARAMJA_EASY, Varbits.DIARY_KARAMJA_MEDIUM,
			Varbits.DIARY_KARAMJA_HARD, Varbits.DIARY_KARAMJA_ELITE));
		map.put("KOUREND", diaryRegion(
			Varbits.DIARY_KOUREND_EASY, Varbits.DIARY_KOUREND_MEDIUM,
			Varbits.DIARY_KOUREND_HARD, Varbits.DIARY_KOUREND_ELITE));
		map.put("LUMBRIDGE", diaryRegion(
			Varbits.DIARY_LUMBRIDGE_EASY, Varbits.DIARY_LUMBRIDGE_MEDIUM,
			Varbits.DIARY_LUMBRIDGE_HARD, Varbits.DIARY_LUMBRIDGE_ELITE));
		map.put("MORYTANIA", diaryRegion(
			Varbits.DIARY_MORYTANIA_EASY, Varbits.DIARY_MORYTANIA_MEDIUM,
			Varbits.DIARY_MORYTANIA_HARD, Varbits.DIARY_MORYTANIA_ELITE));
		map.put("VARROCK", diaryRegion(
			Varbits.DIARY_VARROCK_EASY, Varbits.DIARY_VARROCK_MEDIUM,
			Varbits.DIARY_VARROCK_HARD, Varbits.DIARY_VARROCK_ELITE));
		map.put("WESTERN", diaryRegion(
			Varbits.DIARY_WESTERN_EASY, Varbits.DIARY_WESTERN_MEDIUM,
			Varbits.DIARY_WESTERN_HARD, Varbits.DIARY_WESTERN_ELITE));
		map.put("WILDERNESS", diaryRegion(
			Varbits.DIARY_WILDERNESS_EASY, Varbits.DIARY_WILDERNESS_MEDIUM,
			Varbits.DIARY_WILDERNESS_HARD, Varbits.DIARY_WILDERNESS_ELITE));

		boolean changed = !map.equals(diaries);
		diaries = map;
		return changed;
	}

	/**
	 * Polls the six combat-achievement tier varbits.
	 *
	 * @return whether any tier value changed since the last poll
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
			client.getVarbitValue(Varbits.COMBAT_ACHIEVEMENT_TIER_GRANDMASTER));
		boolean changed = !ca.equals(combatAchievements);
		combatAchievements = ca;
		return changed;
	}

	/**
	 * Polls the current slayer task, clearing it when no task is assigned.
	 *
	 * @return whether the slayer task changed since the last poll
	 */
	@SuppressWarnings("deprecation")
	public boolean pollSlayer()
	{
		int amount = client.getVarpValue(VarPlayer.SLAYER_TASK_SIZE);
		SlayerData task = null;
		if (amount > 0)
		{
			task = new SlayerData(
				client.getVarpValue(VarPlayer.SLAYER_TASK_CREATURE),
				amount,
				client.getVarbitValue(Varbits.SLAYER_POINTS),
				client.getVarbitValue(Varbits.SLAYER_TASK_STREAK));
		}

		boolean changed = !Objects.equals(task, slayer);
		slayer = task;
		return changed;
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
	 * toggles. Owned collections are copied so the writer thread reads a stable graph.
	 *
	 * @return the snapshot, or {@code null} when no character is logged in
	 */
	@SuppressWarnings("deprecation")
	public CharacterSnapshot buildSnapshot()
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null || localPlayer.getName() == null)
			return null;

		CharacterSnapshot data = new CharacterSnapshot();
		data.lastUpdated = Instant.now().toString();

		String accountType = null;
		boolean members = false;
		if (config.syncAccountInfo())
		{
			AccountType type = client.getAccountType();
			accountType = type != null ? type.name() : null;
			members = client.getWorldType().contains(WorldType.MEMBERS);
		}

		data.player = new PlayerInfo(
			localPlayer.getName(),
			localPlayer.getCombatLevel(),
			client.getWorld(),
			accountType,
			members);

		if (config.syncSkills() && !skills.isEmpty())
			data.skills = new LinkedHashMap<>(skills);

		if (config.syncVitals())
			data.vitals = buildVitals();

		if (config.syncBank() && bankItems != null)
			data.bank = new BankData(bankTotal, new ArrayList<>(bankItems));

		if (config.syncInventory())
			data.inventory = new ArrayList<>(inventory);

		if (config.syncEquipment() && !equipment.isEmpty())
			data.equipment = new LinkedHashMap<>(equipment);

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
			data.location = buildLocation(localPlayer);

		if (config.syncGrandExchange() && !geOffers.isEmpty())
			data.grandExchange = new ArrayList<>(geOffers.values());

		return data;
	}

	/**
	 * Reads a diary region's four tiers, each complete when its varbit equals 1.
	 *
	 * @param easy   the easy-tier varbit id
	 * @param medium the medium-tier varbit id
	 * @param hard   the hard-tier varbit id
	 * @param elite  the elite-tier varbit id
	 * @return the region's completion state
	 */
	private DiaryRegion diaryRegion(int easy, int medium, int hard, int elite)
	{
		return new DiaryRegion(
			client.getVarbitValue(easy) == 1,
			client.getVarbitValue(medium) == 1,
			client.getVarbitValue(hard) == 1,
			client.getVarbitValue(elite) == 1);
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
