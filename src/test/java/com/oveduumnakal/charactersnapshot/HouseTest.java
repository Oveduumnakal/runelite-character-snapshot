/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.Arrays;
import java.util.EnumSet;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.HouseData;
import org.junit.Test;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.WorldType;
import net.runelite.api.gameval.VarbitID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the player-owned house fields and their toggle.
 */
public class HouseTest
{
	/**
	 * Without a house location there is no house.
	 */
	@Test
	public void noHouseWithoutLocation()
	{
		assertNull(House.read(loggedInClient()));
	}

	/**
	 * The raw fields are read and set nexus slots are listed by slot number.
	 */
	@Test
	public void readsRawFields()
	{
		Client client = loggedInClient();
		when(client.getVarbitValue(VarbitID.POH_HOUSE_LOCATION)).thenReturn(2);
		when(client.getVarbitValue(VarbitID.POH_SERVANT_TYPE)).thenReturn(5);
		when(client.getVarbitValue(VarbitID.POH_JEWELLERYBOX_MULTI)).thenReturn(3);
		when(client.getVarbitValue(VarbitID.POH_NEXUS_TELE_1)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.POH_NEXUS_TELE_45)).thenReturn(1);

		HouseData house = House.read(client);

		assertEquals(2, house.location);
		assertEquals(5, house.servant);
		assertEquals(3, house.jewelleryBox);
		assertEquals(Arrays.asList(1, 45), house.nexusTeleports);
		assertEquals(45, House.NEXUS_TELEPORTS.length);
	}

	/**
	 * The collector reports a change once and honours the toggle.
	 */
	@Test
	public void collectorExportsHouse()
	{
		Client client = loggedInClient();
		when(client.getVarbitValue(VarbitID.POH_HOUSE_LOCATION)).thenReturn(1);
		CharacterSnapshotConfig config = mock(CharacterSnapshotConfig.class);
		when(config.syncHouse()).thenReturn(true);
		SnapshotCollector collector = new SnapshotCollector(client, config);

		assertTrue(collector.pollHouse());
		assertFalse(collector.pollHouse());
		assertEquals(1, collector.buildSnapshot().house.location);

		when(config.syncHouse()).thenReturn(false);
		assertNull(collector.buildSnapshot().house);
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
