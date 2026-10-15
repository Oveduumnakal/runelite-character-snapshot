/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.ItemEntry;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.ToolLeprechaunTotals;
import org.junit.Test;

import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.WorldType;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the skilling storages: seed box and tackle box containers, the plank sack and the tool leprechaun.
 */
public class SkillingStorageTest
{
	/**
	 * The seed box and tackle box are storage containers with their own keys.
	 */
	@Test
	public void seedAndTackleBoxesAreStorages()
	{
		assertEquals("seedBox", SnapshotCollector.storageKey(InventoryID.SEED_BOX));
		assertEquals("tackleBox", SnapshotCollector.storageKey(InventoryID.TACKLE_BOX));
		assertEquals("gravestone", SnapshotCollector.storageKey(InventoryID.GRAVESTONE));
		assertEquals("deathsOffice", SnapshotCollector.storageKey(InventoryID.DEATH_PERMANENT));

		Client client = loggedInClient();
		SnapshotCollector collector = new SnapshotCollector(client, config(true));
		ItemContainer box = mock(ItemContainer.class);
		when(box.getItems()).thenReturn(new Item[]{new Item(ItemID.RANARR_SEED, 40)});
		collector.updateStorage("seedBox", box);

		CharacterSnapshot snapshot = collector.buildSnapshot();
		List<ItemEntry> seeds = snapshot.storages.get("seedBox");
		assertEquals(40, seeds.get(0).quantity);
		assertNotNull(snapshot.sectionUpdated.get("storages.seedBox"));
	}

	/**
	 * The kit, quiver, cargo hold and knapsack containers map to their own keys.
	 */
	@Test
	public void remainingContainersAreStorages()
	{
		assertEquals("huntsmansKit", SnapshotCollector.storageKey(InventoryID.HUNTSMANS_KIT));
		assertEquals("forestryKit", SnapshotCollector.storageKey(InventoryID.FORESTRY_KIT));
		assertEquals("dizanasQuiver", SnapshotCollector.storageKey(InventoryID.DIZANAS_QUIVER_AMMO));
		assertEquals("cargoHold1", SnapshotCollector.storageKey(InventoryID.SAILING_BOAT_1_CARGOHOLD));
		assertEquals("cargoHold2", SnapshotCollector.storageKey(InventoryID.SAILING_BOAT_2_CARGOHOLD));
		assertEquals("cargoHold3", SnapshotCollector.storageKey(InventoryID.SAILING_BOAT_3_CARGOHOLD));
		assertEquals("cargoHold4", SnapshotCollector.storageKey(InventoryID.SAILING_BOAT_4_CARGOHOLD));
		assertEquals("cargoHold5", SnapshotCollector.storageKey(InventoryID.SAILING_BOAT_5_CARGOHOLD));
		assertEquals("barbarianKnapsack", SnapshotCollector.storageKey(InventoryID.BARBARIAN_KNAPSACK));
	}

	/**
	 * A cargo hold change lands under its key with a capture time.
	 */
	@Test
	public void cargoHoldContentsAreExported()
	{
		Client client = loggedInClient();
		SnapshotCollector collector = new SnapshotCollector(client, config(true));
		ItemContainer hold = mock(ItemContainer.class);
		when(hold.getItems()).thenReturn(new Item[]{new Item(ItemID.RANARR_SEED, 7)});
		collector.updateStorage(SnapshotCollector.storageKey(InventoryID.SAILING_BOAT_3_CARGOHOLD), hold);

		CharacterSnapshot snapshot = collector.buildSnapshot();
		List<ItemEntry> cargo = snapshot.storages.get("cargoHold3");
		assertEquals(7, cargo.get(0).quantity);
		assertNotNull(snapshot.sectionUpdated.get("storages.cargoHold3"));
	}

	/**
	 * The plank sack lists only planks it holds, with a capture time, and reports a change once.
	 */
	@Test
	public void plankSackFromVarbits()
	{
		Client client = loggedInClient();
		when(client.getVarbitValue(VarbitID.PLANK_SACK_MAHOGANY)).thenReturn(28);
		SnapshotCollector collector = new SnapshotCollector(client, config(true));

		assertTrue(collector.pollSkillingStorages());
		assertFalse(collector.pollSkillingStorages());
		CharacterSnapshot snapshot = collector.buildSnapshot();
		List<ItemEntry> sack = snapshot.storages.get("plankSack");

		assertEquals(1, sack.size());
		assertEquals(ItemID.PLANK_MAHOGANY, sack.get(0).itemId);
		assertEquals(28, sack.get(0).quantity);
		assertNotNull(snapshot.sectionUpdated.get("storages.plankSack"));
	}

	/**
	 * An empty plank sack never seen holding planks is not exported; one emptied later exports as empty.
	 */
	@Test
	public void plankSackOnlyOnceUsed()
	{
		Client client = loggedInClient();
		SnapshotCollector collector = new SnapshotCollector(client, config(true));

		assertFalse(collector.pollSkillingStorages());
		assertNull(collector.buildSnapshot().storages);

		when(client.getVarbitValue(VarbitID.PLANK_SACK_OAK)).thenReturn(5);
		collector.pollSkillingStorages();
		when(client.getVarbitValue(VarbitID.PLANK_SACK_OAK)).thenReturn(0);

		assertTrue(collector.pollSkillingStorages());
		List<ItemEntry> sack = collector.buildSnapshot().storages.get("plankSack");
		assertTrue(sack.isEmpty());
	}

	/**
	 * The tool leprechaun is exported as raw values once anything is stored, and honours the toggle.
	 */
	@Test
	public void toolLeprechaunRawValues()
	{
		Client client = loggedInClient();
		CharacterSnapshotConfig config = config(true);
		SnapshotCollector collector = new SnapshotCollector(client, config);
		collector.pollSkillingStorages();
		assertNull(collector.buildSnapshot().toolLeprechaun);

		when(client.getVarbitValue(VarbitID.FARMING_TOOLS_RAKE)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.FARMING_TOOLS_ULTRACOMPOST)).thenReturn(150);
		assertTrue(collector.pollSkillingStorages());
		CharacterSnapshot snapshot = collector.buildSnapshot();

		assertEquals(Integer.valueOf(1), snapshot.toolLeprechaun.get("rake"));
		assertEquals(Integer.valueOf(150), snapshot.toolLeprechaun.get("ultracompost"));
		assertEquals(SkillingStorage.TOOL_LEPRECHAUN.size(), snapshot.toolLeprechaun.size());

		when(config.syncStorages()).thenReturn(false);
		assertNull(collector.buildSnapshot().toolLeprechaun);
	}

	/**
	 * Decodes a tool leprechaun state from the given raw key/value pairs, all others zero.
	 *
	 * @param pairs alternating raw key and value
	 * @return the decoded totals
	 */
	private static ToolLeprechaunTotals decode(Object... pairs)
	{
		Map<String, Integer> raw = new LinkedHashMap<>();
		for (String key : SkillingStorage.TOOL_LEPRECHAUN.keySet())
			raw.put(key, 0);

		for (int i = 0; i < pairs.length; i += 2)
			raw.put((String) pairs[i], (Integer) pairs[i + 1]);

		return SkillingStorage.decodeToolTotals(raw);
	}

	/**
	 * Rakes, spades, trowels and seed dibbers decode as base plus two per extra.
	 */
	@Test
	public void toolTotalsSingleTools()
	{
		assertEquals(2, decode("rake", 0, "extraRakes", 1).rakes);
		assertEquals(3, decode("spade", 1, "extraSpades", 1).spades);
		assertEquals(2, decode("trowel", 0, "extraTrowels", 1).trowels);
		assertEquals(1, decode("seedDibber", 1, "extraSeedDibbers", 0).seedDibbers);
		assertEquals(1, decode("secateurs", 1, "extraSecateurs", 0).secateurs);
	}

	/**
	 * Magic secateurs are a flag.
	 */
	@Test
	public void toolTotalsMagicSecateursFlag()
	{
		assertTrue(decode("magicSecateurs", 1).magicSecateurs);
		assertFalse(decode().magicSecateurs);
	}

	/**
	 * Buckets decode as base plus 32 per extra; the extra2 part is ignored.
	 */
	@Test
	public void toolTotalsBuckets()
	{
		assertEquals(77, decode("buckets", 13, "extraBuckets", 2, "extra2Buckets", 0).buckets);
	}

	/**
	 * Compost and supercompost decode as base plus 256 per extra; ultracompost is the base alone.
	 */
	@Test
	public void toolTotalsCompost()
	{
		assertEquals(801, decode("supercompost", 33, "extraSupercompost", 3).supercompost);
		assertEquals(16, decode("compost", 16, "extraCompost", 0).compost);
		assertEquals(150, decode("ultracompost", 150).ultracompost);
	}

	/**
	 * The watering can reports its value minus one as charges, and none when empty.
	 */
	@Test
	public void toolTotalsWateringCan()
	{
		assertEquals(Integer.valueOf(7), decode("wateringCan", 8).wateringCanCharges);
		assertNull(decode("wateringCan", 0).wateringCanCharges);
	}

	/**
	 * Plant cure is a raw count and the bottomless bucket keeps its raw type and uses.
	 */
	@Test
	public void toolTotalsPlantCureAndBottomlessBucket()
	{
		ToolLeprechaunTotals totals = decode("plantCure", 4, "bottomlessBucketType", 4,
			"bottomlessBucketQuantity", 598);

		assertEquals(4, totals.plantCure);
		assertEquals(4, totals.bottomlessBucket.type);
		assertEquals(598, totals.bottomlessBucket.uses);
		assertNull(decode().bottomlessBucket);
	}

	/**
	 * The totals ride along with the raw map in the snapshot and follow the storages toggle.
	 */
	@Test
	public void toolTotalsInSnapshot()
	{
		Client client = loggedInClient();
		CharacterSnapshotConfig config = config(true);
		SnapshotCollector collector = new SnapshotCollector(client, config);
		when(client.getVarbitValue(VarbitID.FARMING_TOOLS_SPADE)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.FARMING_TOOLS_EXTRASPADES)).thenReturn(1);
		collector.pollSkillingStorages();

		assertEquals(3, collector.buildSnapshot().toolLeprechaunTotals.spades);

		when(config.syncStorages()).thenReturn(false);
		assertNull(collector.buildSnapshot().toolLeprechaunTotals);
	}

	/**
	 * Builds a client with a logged-in player and item names for any id.
	 *
	 * @return the mock client
	 */
	private static Client loggedInClient()
	{
		Client client = mock(Client.class);
		Player player = mock(Player.class);
		when(player.getName()).thenReturn("Zezima");
		when(client.getLocalPlayer()).thenReturn(player);
		when(client.getWorldType()).thenReturn(EnumSet.noneOf(WorldType.class));
		ItemComposition item = mock(ItemComposition.class);
		when(item.getName()).thenReturn("Item");
		when(client.getItemDefinition(anyInt())).thenReturn(item);
		return client;
	}

	/**
	 * Builds a config with only the storages toggle set.
	 *
	 * @param storages the storages toggle
	 * @return the mock config
	 */
	private static CharacterSnapshotConfig config(boolean storages)
	{
		CharacterSnapshotConfig config = mock(CharacterSnapshotConfig.class);
		when(config.syncStorages()).thenReturn(storages);
		return config;
	}
}
