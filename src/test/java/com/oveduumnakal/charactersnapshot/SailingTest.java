/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BoatData;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.SailingData;
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
 * Tests the Sailing state: owned boats, crew, charting count, Barracuda Trials, and the toggle.
 */
public class SailingTest
{
	/**
	 * Only owned boat slots are listed, each with its raw part ids and stored hitpoints.
	 */
	@Test
	public void listsOwnedBoats()
	{
		Client client = loggedInClient();
		when(client.getVarbitValue(VarbitID.SAILING_BOAT_2_OWNED)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.SAILING_BOAT_2_TYPE)).thenReturn(3);
		when(client.getVarbitValue(VarbitID.SAILING_BOAT_2_HULL)).thenReturn(4);
		when(client.getVarbitValue(VarbitID.SAILING_BOAT_2_STORED_HP)).thenReturn(90);
		when(client.getVarbitValue(VarbitID.SAILING_BOAT_2_STORED_MAXHP)).thenReturn(120);

		SailingData sailing = Sailing.read(client);

		assertEquals(1, sailing.boats.size());
		BoatData boat = sailing.boats.get(0);
		assertEquals(2, boat.slot);
		assertEquals(3, boat.type);
		assertEquals(4, boat.hull);
		assertEquals(90, boat.hp);
		assertEquals(120, boat.maxHp);
	}

	/**
	 * Crew, charting bits across varps and trial values are read.
	 */
	@Test
	public void readsCrewChartingAndTrials()
	{
		Client client = loggedInClient();
		when(client.getVarbitValue(VarbitID.SAILING_CREW_GHOST_JENKINS_UNLOCKED)).thenReturn(1);
		when(client.getVarpValue(VarPlayerID.SAILING_CHARTING_COMPLETION_0)).thenReturn(0b1011);
		when(client.getVarpValue(VarPlayerID.SAILING_CHARTING_COMPLETION_11)).thenReturn(-1);
		when(client.getVarpValue(VarPlayerID.SAILING_BT_PERFORMANCE_1)).thenReturn(7);

		SailingData sailing = Sailing.read(client);

		assertEquals(Collections.singletonList("ghostJenkins"), sailing.crew);
		assertEquals(3 + 32, sailing.chartingTasksCompleted);
		assertEquals(Arrays.asList(7, 0, 0, 0, 0), sailing.barracudaTrials);
	}

	/**
	 * The collector reports a change once and honours the toggle.
	 */
	@Test
	public void collectorExportsSailing()
	{
		Client client = loggedInClient();
		CharacterSnapshotConfig config = mock(CharacterSnapshotConfig.class);
		when(config.syncSailing()).thenReturn(true);
		SnapshotCollector collector = new SnapshotCollector(client, config);

		assertTrue(collector.pollSailing());
		assertFalse(collector.pollSailing());
		assertTrue(collector.buildSnapshot().sailing.boats.isEmpty());

		when(config.syncSailing()).thenReturn(false);
		assertNull(collector.buildSnapshot().sailing);
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
