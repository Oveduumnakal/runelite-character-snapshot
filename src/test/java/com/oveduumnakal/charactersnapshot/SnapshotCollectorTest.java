/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BankData;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BankTab;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.InventoryItem;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.ItemEntry;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.PlayerInfo;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.SlayerData;
import org.junit.Test;

import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.WorldType;
import net.runelite.api.gameval.DBTableID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the collector's client-facing behaviour with a mocked {@link Client}: the flat bank list and tabs,
 * equipment slot mapping, cross-account {@link SnapshotCollector#reset()}, restoring last known
 * items from a previous export, and that a built snapshot is isolated from later cache mutation.
 */
public class SnapshotCollectorTest
{
	private static final String OLD_BANK_TIME = "2026-09-20T10:00:00Z";

	private static final String OLD_BUILD_TIME = "2026-09-21T12:00:00Z";

	/**
	 * Bank contents are exported as a flat item list.
	 */
	@Test
	public void bankIsFlatList()
	{
		Client client = loggedInClient("Zezima");
		ItemComposition claws = named("Dragon claws");
		when(client.getItemDefinition(1)).thenReturn(claws);
		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());

		collector.updateBank(container(item(1, 5)));
		CharacterSnapshot data = collector.buildSnapshot();

		assertEquals(1, data.bank.totalItems);
		assertEquals(1, data.bank.items.size());
		assertEquals("Dragon claws", data.bank.items.get(0).name);
		assertEquals(5, data.bank.items.get(0).quantity);
	}

	/**
	 * The raw container is sliced by the tab-count varbits: tabs 1 to 9 first, the remainder is the main tab.
	 * Placeholders and empty slots count toward the slice sizes but are not exported.
	 */
	@Test
	public void bankSplitsIntoRealTabs()
	{
		Client client = loggedInClient("Zezima");
		when(client.getVarbitValue(VarbitID.BANK_TAB_1)).thenReturn(2);
		when(client.getVarbitValue(VarbitID.BANK_TAB_3)).thenReturn(3);
		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());

		collector.updateBank(container(
			item(10, 1), item(11, 0),
			item(30, 4), item(-1, 0), item(31, 2),
			item(1, 5), item(2, 6)));
		CharacterSnapshot data = collector.buildSnapshot();

		assertEquals(3, data.bank.tabs.size());
		assertEquals(0, data.bank.tabs.get(0).tabIndex);
		assertEquals(Arrays.asList(1, 2), ids(data.bank.tabs.get(0).items));
		assertEquals(1, data.bank.tabs.get(1).tabIndex);
		assertEquals(Arrays.asList(10), ids(data.bank.tabs.get(1).items));
		assertEquals(3, data.bank.tabs.get(2).tabIndex);
		assertEquals(Arrays.asList(30, 31), ids(data.bank.tabs.get(2).items));
		assertEquals(Arrays.asList(10, 30, 31, 1, 2), ids(data.bank.items));
		assertEquals(5, data.bank.totalItems);
	}

	/**
	 * A numbered tab holding only placeholders still exists, and an empty bank still has its main tab.
	 */
	@Test
	public void emptyTabsAreHandled()
	{
		Client client = loggedInClient("Zezima");
		when(client.getVarbitValue(VarbitID.BANK_TAB_2)).thenReturn(1);
		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());

		collector.updateBank(container(item(5, 0)));
		CharacterSnapshot data = collector.buildSnapshot();

		assertEquals(2, data.bank.tabs.size());
		assertEquals(0, data.bank.tabs.get(0).tabIndex);
		assertTrue(data.bank.tabs.get(0).items.isEmpty());
		assertEquals(2, data.bank.tabs.get(1).tabIndex);
		assertTrue(data.bank.tabs.get(1).items.isEmpty());
		assertEquals(0, data.bank.totalItems);
	}

	/**
	 * A tab count larger than the container cannot read past its end.
	 */
	@Test
	public void oversizedTabCountIsClamped()
	{
		Client client = loggedInClient("Zezima");
		when(client.getVarbitValue(VarbitID.BANK_TAB_1)).thenReturn(50);
		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());

		collector.updateBank(container(item(1, 1)));
		CharacterSnapshot data = collector.buildSnapshot();

		assertEquals(Arrays.asList(1), ids(data.bank.tabs.get(1).items));
		assertTrue(data.bank.tabs.get(0).items.isEmpty());
	}

	/**
	 * A bank restored from an older export keeps its tabs when present.
	 */
	@Test
	public void restoreKeepsPreviousTabs()
	{
		Client client = loggedInClient("Zezima");
		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());
		CharacterSnapshot previous = previous("Zezima", "Dragon claws");
		List<ItemEntry> tabItems = Collections.singletonList(new ItemEntry(1, "Dragon claws", 5));
		previous.bank = new BankData(1, tabItems, Collections.singletonList(new BankTab(4, tabItems)));

		collector.restore(previous);
		CharacterSnapshot data = collector.buildSnapshot();

		assertEquals(1, data.bank.tabs.size());
		assertEquals(4, data.bank.tabs.get(0).tabIndex);
	}

	/**
	 * A bank restored from an older export with no tabs field gets a single tab 0 derived from the items.
	 */
	@Test
	public void restoreWithoutTabsDerivesMainTab()
	{
		Client client = loggedInClient("Zezima");
		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());
		CharacterSnapshot previous = previous("Zezima", "Dragon claws");
		previous.bank.tabs = null;

		collector.restore(previous);
		CharacterSnapshot data = collector.buildSnapshot();

		assertEquals(1, data.bank.tabs.size());
		assertEquals(0, data.bank.tabs.get(0).tabIndex);
		ItemEntry main = data.bank.tabs.get(0).items.get(0);
		assertEquals("Dragon claws", main.name);
	}

	/**
	 * A bank restored from an older single-tab export loads as that single tab 0.
	 */
	@Test
	public void restoreFromSingleTabFile()
	{
		Client client = loggedInClient("Zezima");
		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());

		collector.restore(previous("Zezima", "Dragon claws"));
		CharacterSnapshot data = collector.buildSnapshot();

		assertEquals(1, data.bank.tabs.size());
		assertEquals(0, data.bank.tabs.get(0).tabIndex);
		assertEquals(1, data.bank.tabs.get(0).items.size());
	}

	/**
	 * Lists the item ids of the given entries.
	 *
	 * @param entries the entries
	 * @return their ids, in order
	 */
	private static List<Integer> ids(List<ItemEntry> entries)
	{
		List<Integer> ids = new ArrayList<>();
		for (ItemEntry entry : entries)
			ids.add(entry.itemId);

		return ids;
	}

	/**
	 * After a reset, the bank cache is empty so the next character's file cannot inherit it.
	 */
	@Test
	public void resetClearsBank()
	{
		Client client = loggedInClient("Zezima");
		ItemComposition claws = named("Dragon claws");
		when(client.getItemDefinition(1)).thenReturn(claws);
		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());

		collector.updateBank(container(item(1, 5)));
		collector.reset();

		assertNull(collector.buildSnapshot().bank);
	}

	/**
	 * A built snapshot keeps its own copy of the bank, unaffected by a later bank update.
	 */
	@Test
	public void snapshotIsIsolatedFromLaterMutation()
	{
		Client client = loggedInClient("Zezima");
		ItemComposition claws = named("Dragon claws");
		ItemComposition whip = named("Abyssal whip");
		when(client.getItemDefinition(1)).thenReturn(claws);
		when(client.getItemDefinition(2)).thenReturn(whip);
		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());

		collector.updateBank(container(item(1, 5)));
		CharacterSnapshot first = collector.buildSnapshot();
		collector.updateBank(container(item(2, 1)));

		assertEquals("Dragon claws", first.bank.items.get(0).name);
	}

	/**
	 * Each worn item is exported under its own slot name, including the ring and ammo slots that sit
	 * past the container's unused arms, hair and jaw indices.
	 */
	@Test
	public void equipmentUsesRealSlotIndices()
	{
		Client client = loggedInClient("Zezima");
		EquipmentInventorySlot[] slots = EquipmentInventorySlot.values();
		Item[] worn = new Item[slots.length];
		for (EquipmentInventorySlot slot : slots)
		{
			int idx = slot.getSlotIdx();
			worn[idx] = item(idx + 100, 1);
			ItemComposition def = named(slot.name() + " item");
			when(client.getItemDefinition(idx + 100)).thenReturn(def);
		}

		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());
		collector.updateEquipment(container(worn));
		Map<String, ItemEntry> equipment = collector.buildSnapshot().equipment;

		List<String> exported = Arrays.asList(
			"HEAD", "CAPE", "AMULET", "WEAPON", "BODY", "SHIELD", "LEGS", "GLOVES", "BOOTS", "RING", "AMMO");
		assertEquals(exported, new ArrayList<>(equipment.keySet()));
		for (String name : exported)
		{
			EquipmentInventorySlot slot = EquipmentInventorySlot.valueOf(name);
			assertEquals(slot.getSlotIdx() + 100, equipment.get(name).itemId);
			assertEquals(name + " item", equipment.get(name).name);
		}
	}

	/**
	 * An equipment container shorter than the ammo index drops the slots it does not cover rather
	 * than throwing.
	 */
	@Test
	public void shortEquipmentContainerIsTolerated()
	{
		Client client = loggedInClient("Zezima");
		ItemComposition helm = named("Helm");
		when(client.getItemDefinition(1)).thenReturn(helm);
		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());

		collector.updateEquipment(container(item(1, 1)));
		Map<String, ItemEntry> equipment = collector.buildSnapshot().equipment;

		assertEquals(1, equipment.size());
		assertEquals("Helm", equipment.get("HEAD").name);
	}

	/**
	 * A restored bank survives into the next snapshot, with its original capture time, when the bank
	 * has not been opened this session.
	 */
	@Test
	public void restoredBankKeptUntilReopened()
	{
		Client client = loggedInClient("Zezima");
		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());

		collector.restore(previous("Zezima", "Dragon claws"));
		CharacterSnapshot data = collector.buildSnapshot();

		assertEquals("Dragon claws", data.bank.items.get(0).name);
		assertEquals(OLD_BANK_TIME, data.sectionUpdated.get("bank"));
		assertEquals("Old helm", data.equipment.get("HEAD").name);
		assertEquals(OLD_BUILD_TIME, data.sectionUpdated.get("equipment"));
		assertEquals("Old rune", data.inventory.get(0).name);
	}

	/**
	 * A live bank update replaces the restored bank and its capture time.
	 */
	@Test
	public void liveBankReplacesRestored()
	{
		Client client = loggedInClient("Zezima");
		ItemComposition whip = named("Abyssal whip");
		when(client.getItemDefinition(2)).thenReturn(whip);
		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());

		collector.restore(previous("Zezima", "Dragon claws"));
		collector.updateBank(container(item(2, 1)));
		CharacterSnapshot data = collector.buildSnapshot();

		assertEquals(1, data.bank.items.size());
		assertEquals("Abyssal whip", data.bank.items.get(0).name);
		assertTrue(!OLD_BANK_TIME.equals(data.sectionUpdated.get("bank")));
	}

	/**
	 * Restoring never overwrites a section already captured live this session.
	 */
	@Test
	public void restoreDoesNotReplaceLiveData()
	{
		Client client = loggedInClient("Zezima");
		ItemComposition whip = named("Abyssal whip");
		when(client.getItemDefinition(2)).thenReturn(whip);
		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());

		collector.updateBank(container(item(2, 1)));
		collector.restore(previous("Zezima", "Dragon claws"));
		CharacterSnapshot data = collector.buildSnapshot();

		assertEquals("Abyssal whip", data.bank.items.get(0).name);
	}

	/**
	 * A previous export belonging to another character is ignored.
	 */
	@Test
	public void restoreIgnoresOtherCharacter()
	{
		Client client = loggedInClient("Zezima");
		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());

		collector.restore(previous("Zezima_", "Dragon claws"));

		assertNull(collector.buildSnapshot().bank);
	}

	/**
	 * A restored bank is still left out of the export while the bank toggle is off.
	 */
	@Test
	public void restoredBankRespectsToggle()
	{
		Client client = loggedInClient("Zezima");
		CharacterSnapshotConfig config = allEnabled();
		when(config.syncBank()).thenReturn(false);
		SnapshotCollector collector = new SnapshotCollector(client, config);

		collector.restore(previous("Zezima", "Dragon claws"));
		CharacterSnapshot data = collector.buildSnapshot();

		assertNull(data.bank);
		assertTrue(!data.sectionUpdated.containsKey("bank"));
	}

	/**
	 * No snapshot is produced when no character is logged in.
	 */
	@Test
	public void snapshotNullWhenLoggedOut()
	{
		Client client = mock(Client.class);
		when(client.getLocalPlayer()).thenReturn(null);
		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());

		assertTrue(collector.buildSnapshot() == null);
	}

	/**
	 * Builds a previous export with a bank (own capture time), an equipped helm and one inventory item
	 * (both falling back to the build time).
	 *
	 * @param name     the player name the export belongs to
	 * @param bankItem the single banked item's name
	 * @return the previous export
	 */
	private static CharacterSnapshot previous(String name, String bankItem)
	{
		CharacterSnapshot data = new CharacterSnapshot();
		data.lastUpdated = OLD_BUILD_TIME;
		data.player = new PlayerInfo(name, 126, 302, "NORMAL", true);
		data.bank = new BankData(1, Collections.singletonList(new ItemEntry(1, bankItem, 5)));
		data.inventory = Collections.singletonList(new InventoryItem(3, "Old rune", 10, 0));
		data.equipment = new LinkedHashMap<>();
		data.equipment.put("HEAD", new ItemEntry(4, "Old helm", 1));
		Map<String, String> times = new LinkedHashMap<>();
		times.put("bank", OLD_BANK_TIME);
		data.sectionUpdated = times;
		return data;
	}

	/**
	 * Builds a mock client with a logged-in player and an empty world type.
	 *
	 * @param name the player name
	 * @return the mock client
	 */
	private static Client loggedInClient(String name)
	{
		Client client = mock(Client.class);
		Player player = mock(Player.class);
		when(player.getName()).thenReturn(name);
		when(player.getWorldLocation()).thenReturn(null);
		when(client.getLocalPlayer()).thenReturn(player);
		when(client.getWorldType()).thenReturn(EnumSet.noneOf(WorldType.class));
		return client;
	}

	/**
	 * A regular slayer task resolves its creature name from the slayer task table.
	 */
	@Test
	public void slayerTaskNameIsResolved()
	{
		Client client = loggedInClient("Zezima");
		stubSlayerTask(client, 40, 7, "Abyssal demons");
		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());

		assertTrue(collector.pollSlayer());
		SlayerData slayer = collector.buildSnapshot().slayer;

		assertNotNull(slayer);
		assertEquals(7, slayer.creatureId);
		assertEquals("Abyssal demons", slayer.creatureName);
		assertNull(slayer.bossName);
		assertEquals(40, slayer.amount);
	}

	/**
	 * A boss task resolves both the generic task name and the specific boss.
	 */
	@Test
	public void slayerBossTaskResolvesBossName()
	{
		Client client = loggedInClient("Zezima");
		stubSlayerTask(client, 3, 98, "Bosses");
		when(client.getVarbitValue(VarbitID.SLAYER_TARGET_BOSSID)).thenReturn(12);
		when(client.getDBRowsByValue(DBTableID.SlayerTaskSublist.ID, DBTableID.SlayerTaskSublist.COL_TASK_SUBTABLE_ID,
			0, 12)).thenReturn(Collections.singletonList(500));
		when(client.getDBTableField(500, DBTableID.SlayerTaskSublist.COL_TASK, 0)).thenReturn(new Object[] {901});
		when(client.getDBTableField(901, DBTableID.SlayerTask.COL_NAME_UPPERCASE, 0))
			.thenReturn(new Object[] {"Vorkath"});
		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());

		collector.pollSlayer();
		SlayerData slayer = collector.buildSnapshot().slayer;

		assertEquals(98, slayer.creatureId);
		assertEquals("Bosses", slayer.creatureName);
		assertEquals("Vorkath", slayer.bossName);
	}

	/**
	 * An id missing from the slayer task table exports a null name, never a guess.
	 */
	@Test
	public void slayerUnresolvableIdGivesNullName()
	{
		Client client = loggedInClient("Zezima");
		when(client.getVarpValue(VarPlayerID.SLAYER_COUNT)).thenReturn(10);
		when(client.getVarpValue(VarPlayerID.SLAYER_TARGET)).thenReturn(9999);
		when(client.getDBRowsByValue(eq(DBTableID.SlayerTask.ID), eq(DBTableID.SlayerTask.COL_ID), eq(0), eq(9999)))
			.thenReturn(Collections.emptyList());
		SnapshotCollector collector = new SnapshotCollector(client, allEnabled());

		collector.pollSlayer();
		SlayerData slayer = collector.buildSnapshot().slayer;

		assertEquals(9999, slayer.creatureId);
		assertNull(slayer.creatureName);
		assertNull(slayer.bossName);
	}

	/**
	 * Stubs a slayer task and its name row on a mock client.
	 *
	 * @param client the mock client
	 * @param amount the remaining kill count
	 * @param taskId the slayer task id
	 * @param name   the task name stored in the table
	 */
	private static void stubSlayerTask(Client client, int amount, int taskId, String name)
	{
		int row = 800 + taskId;
		when(client.getVarpValue(VarPlayerID.SLAYER_COUNT)).thenReturn(amount);
		when(client.getVarpValue(VarPlayerID.SLAYER_TARGET)).thenReturn(taskId);
		when(client.getDBRowsByValue(DBTableID.SlayerTask.ID, DBTableID.SlayerTask.COL_ID, 0, taskId))
			.thenReturn(Collections.singletonList(row));
		when(client.getDBTableField(row, DBTableID.SlayerTask.COL_NAME_UPPERCASE, 0))
			.thenReturn(new Object[] {name});
	}

	/**
	 * Builds a config with every data category enabled.
	 *
	 * @return the mock config
	 */
	private static CharacterSnapshotConfig allEnabled()
	{
		CharacterSnapshotConfig config = mock(CharacterSnapshotConfig.class);
		when(config.syncSkills()).thenReturn(true);
		when(config.syncVitals()).thenReturn(true);
		when(config.syncBank()).thenReturn(true);
		when(config.syncInventory()).thenReturn(true);
		when(config.syncEquipment()).thenReturn(true);
		when(config.syncQuests()).thenReturn(true);
		when(config.syncDiaries()).thenReturn(true);
		when(config.syncCombatAchievements()).thenReturn(true);
		when(config.syncSlayer()).thenReturn(true);
		when(config.syncLocation()).thenReturn(true);
		when(config.syncAccountInfo()).thenReturn(true);
		when(config.syncGrandExchange()).thenReturn(true);
		return config;
	}

	/**
	 * Builds an item container over the given items.
	 *
	 * @param items the items it holds
	 * @return the mock container
	 */
	private static ItemContainer container(Item... items)
	{
		ItemContainer c = mock(ItemContainer.class);
		when(c.getItems()).thenReturn(items);
		return c;
	}

	/**
	 * Builds a mock item.
	 *
	 * @param id  the item id
	 * @param qty the quantity
	 * @return the mock item
	 */
	private static Item item(int id, int qty)
	{
		Item item = mock(Item.class);
		when(item.getId()).thenReturn(id);
		when(item.getQuantity()).thenReturn(qty);
		return item;
	}

	/**
	 * Builds a mock item composition with the given name.
	 *
	 * @param name the item name
	 * @return the mock composition
	 */
	private static ItemComposition named(String name)
	{
		ItemComposition comp = mock(ItemComposition.class);
		when(comp.getName()).thenReturn(name);
		return comp;
	}
}
