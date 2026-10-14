/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.EnumSet;
import java.util.Map;

import org.junit.Test;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.WorldType;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
