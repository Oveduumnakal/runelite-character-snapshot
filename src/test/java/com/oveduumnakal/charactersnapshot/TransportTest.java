/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.TransportData;
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
 * Tests the transport unlocks: fairy ring access and travel log, quetzal sites, and the toggle.
 */
public class TransportTest
{
	/**
	 * Logged fairy ring codes and available quetzal sites are listed in table order.
	 */
	@Test
	public void listsUnlocks()
	{
		Client client = loggedInClient();
		when(client.getVarbitValue(VarbitID.FAIRYRING_PERMISSION)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.FAIRYRINGS_LOG_AJR)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.FAIRYRINGS_LOG_CKS)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.QUETZAL_ALDARIN)).thenReturn(1);

		TransportData transport = Transport.read(client);

		assertTrue(transport.fairyRings);
		assertEquals(Arrays.asList("AJR", "CKS"), transport.fairyRingLog);
		assertEquals(Collections.singletonList("Aldarin"), transport.quetzalSites);
	}

	/**
	 * The fairy ring table holds every travel-log code, alphabetical.
	 */
	@Test
	public void fairyRingTableIsComplete()
	{
		assertEquals(55, Transport.FAIRY_RING_LOG.size());
		assertEquals("AIQ", Transport.FAIRY_RING_LOG.keySet()
			.iterator()
			.next());
	}

	/**
	 * The collector reports a change once and honours the toggle.
	 */
	@Test
	public void collectorExportsTransport()
	{
		Client client = loggedInClient();
		CharacterSnapshotConfig config = mock(CharacterSnapshotConfig.class);
		when(config.syncTransport()).thenReturn(true);
		SnapshotCollector collector = new SnapshotCollector(client, config);

		assertTrue(collector.pollTransport());
		assertFalse(collector.pollTransport());
		assertFalse(collector.buildSnapshot().transport.fairyRings);

		when(config.syncTransport()).thenReturn(false);
		assertNull(collector.buildSnapshot().transport);
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
