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
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.InventoryItem;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.ItemEntry;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.PlayerInfo;
import org.junit.Test;

import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.WorldType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the collector's client-facing behaviour with a mocked {@link Client}: the flat bank list,
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
