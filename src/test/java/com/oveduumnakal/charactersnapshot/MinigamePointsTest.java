/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.MixologyHopper;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.PlayerInfo;
import org.junit.Test;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.WorldType;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the minigame reward-shop balances and their toggle.
 */
public class MinigamePointsTest
{
	/**
	 * Varp and varbit currencies are read into one map, and every known currency is present.
	 */
	@Test
	public void readsVarpAndVarbitCurrencies()
	{
		Client client = loggedInClient();
		when(client.getVarpValue(VarPlayerID.NZONE_REWARDPOINTS)).thenReturn(123456);
		when(client.getVarbitValue(VarbitID.HOSIDIUS_TITHE_REWARDPOINTS)).thenReturn(80);

		Map<String, Integer> points = MinigamePoints.read(client);

		assertEquals(Integer.valueOf(123456), points.get("nightmareZone"));
		assertEquals(Integer.valueOf(80), points.get("titheFarm"));
		assertEquals(Integer.valueOf(0), points.get("soulWarsZeal"));
		assertEquals(MinigamePoints.VARPS.size() + MinigamePoints.VARBITS.size(), points.size());
	}

	/**
	 * A negative varp (an unset value) reads as zero.
	 */
	@Test
	public void negativeVarpIsZero()
	{
		Client client = loggedInClient();
		when(client.getVarpValue(VarPlayerID.SOUL_WARS_ZEAL)).thenReturn(-1);

		assertEquals(Integer.valueOf(0), MinigamePoints.read(client)
			.get("soulWarsZeal"));
	}

	/**
	 * The collector reports a change once and honours the toggle.
	 */
	@Test
	public void collectorExportsPoints()
	{
		Client client = loggedInClient();
		CharacterSnapshotConfig config = mock(CharacterSnapshotConfig.class);
		when(config.syncMinigamePoints()).thenReturn(true);
		SnapshotCollector collector = new SnapshotCollector(client, config);

		assertTrue(collector.pollMinigamePoints());
		assertFalse(collector.pollMinigamePoints());
		when(client.getVarbitValue(VarbitID.VM_KUDOS)).thenReturn(153);
		assertTrue(collector.pollMinigamePoints());
		assertEquals(Integer.valueOf(153), collector.buildSnapshot().minigamePoints.get("museumKudos"));

		when(config.syncMinigamePoints()).thenReturn(false);
		assertNull(collector.buildSnapshot().minigamePoints);
	}

	/**
	 * The hopper is read only in the lab region on its ground plane.
	 */
	@Test
	public void readsHopperOnlyInLab()
	{
		Client client = loggedInClient();
		when(client.getVarbitValue(VarbitID.MM_AVAILABLE_MOX)).thenReturn(2500);
		when(client.getVarbitValue(VarbitID.MM_AVAILABLE_AGA)).thenReturn(1200);
		when(client.getVarbitValue(VarbitID.MM_AVAILABLE_LYE)).thenReturn(3000);

		placePlayer(client, new WorldPoint(1394, 9322, 0));
		assertEquals(new MixologyHopper(2500, 1200, 3000), MinigamePoints.readHopper(client));

		placePlayer(client, new WorldPoint(1394, 9322, 1));
		assertNull(MinigamePoints.readHopper(client));

		placePlayer(client, new WorldPoint(3222, 3218, 0));
		assertNull(MinigamePoints.readHopper(client));
	}

	/**
	 * Leaving the lab keeps the last hopper reading and its capture time, and the toggle hides it.
	 */
	@Test
	public void collectorKeepsHopperOutsideLab()
	{
		Client client = loggedInClient();
		CharacterSnapshotConfig config = mock(CharacterSnapshotConfig.class);
		when(config.syncMinigamePoints()).thenReturn(true);
		SnapshotCollector collector = new SnapshotCollector(client, config);

		placePlayer(client, new WorldPoint(3222, 3218, 0));
		collector.pollMinigamePoints();
		assertNull(collector.buildSnapshot().mixologyHopper);

		when(client.getVarbitValue(VarbitID.MM_AVAILABLE_LYE)).thenReturn(900);
		placePlayer(client, new WorldPoint(1394, 9322, 0));
		assertTrue(collector.pollMinigamePoints());
		assertFalse(collector.pollMinigamePoints());

		when(client.getVarbitValue(VarbitID.MM_AVAILABLE_LYE)).thenReturn(0);
		placePlayer(client, new WorldPoint(3222, 3218, 0));
		assertFalse(collector.pollMinigamePoints());
		CharacterSnapshot snapshot = collector.buildSnapshot();
		assertEquals(new MixologyHopper(0, 0, 900), snapshot.mixologyHopper);
		assertNotNull(snapshot.sectionUpdated.get("mixologyHopper"));

		when(config.syncMinigamePoints()).thenReturn(false);
		assertNull(collector.buildSnapshot().mixologyHopper);
	}

	/**
	 * The previous export's hopper survives a new session until the lab is visited, then a reset clears it.
	 */
	@Test
	public void restoresHopper()
	{
		Client client = loggedInClient();
		CharacterSnapshotConfig config = mock(CharacterSnapshotConfig.class);
		when(config.syncMinigamePoints()).thenReturn(true);
		SnapshotCollector collector = new SnapshotCollector(client, config);
		CharacterSnapshot previous = new CharacterSnapshot();
		previous.lastUpdated = "2026-10-27T12:00:00Z";
		previous.player = new PlayerInfo("Zezima", 126, 302, "NORMAL", true);
		previous.mixologyHopper = new MixologyHopper(10, 20, 30);
		previous.sectionUpdated = new HashMap<>();
		previous.sectionUpdated.put("mixologyHopper", "2026-10-26T08:00:00Z");

		collector.restore(previous);
		CharacterSnapshot snapshot = collector.buildSnapshot();
		assertEquals(new MixologyHopper(10, 20, 30), snapshot.mixologyHopper);
		assertEquals("2026-10-26T08:00:00Z", snapshot.sectionUpdated.get("mixologyHopper"));

		collector.reset();
		assertNull(collector.buildSnapshot().mixologyHopper);
	}

	/**
	 * Places the client's local player at a world point.
	 *
	 * @param client the mock client
	 * @param point  the player's location
	 */
	private static void placePlayer(Client client, WorldPoint point)
	{
		Player player = client.getLocalPlayer();
		when(player.getWorldLocation()).thenReturn(point);
	}

	/**
	 * Builds a client with a logged-in player.
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
		return client;
	}
}
