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
import net.runelite.api.gameval.DBTableID;
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
	 * A boat's port id is named from the dock table, and the raw id is kept.
	 */
	@Test
	public void namesBoatPorts()
	{
		Client client = loggedInClient();
		ownBoat(client, VarbitID.SAILING_BOAT_1_OWNED, VarbitID.SAILING_BOAT_1_PORT, 1);
		ownBoat(client, VarbitID.SAILING_BOAT_2_OWNED, VarbitID.SAILING_BOAT_2_PORT, 60);
		ownBoat(client, VarbitID.SAILING_BOAT_3_OWNED, VarbitID.SAILING_BOAT_3_PORT, 9999);
		dockName(client, 1, 101, "The Pandemonium");
		dockName(client, 60, 160, "Wyrmscraig Cavern");

		SailingData sailing = Sailing.read(client);

		assertEquals(1, sailing.boats.get(0).port);
		assertEquals("The Pandemonium", sailing.boats.get(0).portName);
		assertEquals(60, sailing.boats.get(1).port);
		assertEquals("Wyrmscraig Cavern", sailing.boats.get(1).portName);
		assertEquals(9999, sailing.boats.get(2).port);
		assertNull(sailing.boats.get(2).portName);
	}

	/**
	 * A boat type id is named from the boat table; an unknown id has no name.
	 */
	@Test
	public void namesBoatTypes()
	{
		Client client = loggedInClient();
		when(client.getVarbitValue(VarbitID.SAILING_BOAT_1_OWNED)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.SAILING_BOAT_1_TYPE)).thenReturn(2);
		when(client.getDBRowsByValue(DBTableID.SailingBoat.ID, DBTableID.SailingBoat.COL_TYPE_ID, 0, 2))
			.thenReturn(Collections.singletonList(202));
		when(client.getDBTableField(202, DBTableID.SailingBoat.COL_DISPLAYNAME, 0))
			.thenReturn(new Object[]{"Skiff"});

		BoatData boat = Sailing.read(client).boats.get(0);
		assertEquals("Skiff", boat.typeName);

		when(client.getVarbitValue(VarbitID.SAILING_BOAT_1_TYPE)).thenReturn(77);
		boat = Sailing.read(client).boats.get(0);
		assertNull(boat.typeName);
	}

	/**
	 * A blank display name falls back to the boat table's plain name column.
	 */
	@Test
	public void fallsBackToPlainBoatName()
	{
		Client client = loggedInClient();
		when(client.getVarbitValue(VarbitID.SAILING_BOAT_1_OWNED)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.SAILING_BOAT_1_TYPE)).thenReturn(2);
		when(client.getDBRowsByValue(DBTableID.SailingBoat.ID, DBTableID.SailingBoat.COL_TYPE_ID, 0, 2))
			.thenReturn(Collections.singletonList(202));
		when(client.getDBTableField(202, DBTableID.SailingBoat.COL_DISPLAYNAME, 0))
			.thenReturn(new Object[]{""});
		when(client.getDBTableField(202, DBTableID.SailingBoat.COL_NAME, 0))
			.thenReturn(new Object[]{"Sloop"});

		BoatData boat = Sailing.read(client).boats.get(0);
		assertEquals("Sloop", boat.typeName);
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
	 * Marks a boat slot owned and moored at a port.
	 *
	 * @param client the mock client
	 * @param owned  the slot's owned varbit
	 * @param portVarbit the slot's port varbit
	 * @param port   the raw port id
	 */
	private static void ownBoat(Client client, int owned, int portVarbit, int port)
	{
		when(client.getVarbitValue(owned)).thenReturn(1);
		when(client.getVarbitValue(portVarbit)).thenReturn(port);
	}

	/**
	 * Adds a dock row with a name to the mock dock table.
	 *
	 * @param client the mock client
	 * @param port   the raw port id
	 * @param row    the dock row id
	 * @param name   the dock's display name
	 */
	private static void dockName(Client client, int port, int row, String name)
	{
		when(client.getDBRowsByValue(DBTableID.SailingDock.ID, DBTableID.SailingDock.COL_DOCK_ID, 0, port))
			.thenReturn(Collections.singletonList(row));
		when(client.getDBTableField(row, DBTableID.SailingDock.COL_NICE_NAME, 0)).thenReturn(new Object[]{name});
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
