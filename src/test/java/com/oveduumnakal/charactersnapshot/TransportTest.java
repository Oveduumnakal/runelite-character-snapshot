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
 * Tests the transport unlocks: fairy ring access, travel log, favourites and last destination, quetzal sites,
 * and the toggle.
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
	 * Favourites decode each dial to its letter, skip slots whose set flag is zero, and keep slot order.
	 */
	@Test
	public void decodesFavourites()
	{
		Client client = loggedInClient();
		when(client.getVarbitValue(VarbitID.FAIRYRING_FAVE_SET_1)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.FAIRYRING_FAVE_1_1)).thenReturn(2);
		when(client.getVarbitValue(VarbitID.FAIRYRING_FAVE_1_2)).thenReturn(2);
		when(client.getVarbitValue(VarbitID.FAIRYRING_FAVE_1_3)).thenReturn(2);
		when(client.getVarbitValue(VarbitID.FAIRYRING_FAVE_SET_2)).thenReturn(0);
		when(client.getVarbitValue(VarbitID.FAIRYRING_FAVE_2_1)).thenReturn(3);
		when(client.getVarbitValue(VarbitID.FAIRYRING_FAVE_SET_10)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.FAIRYRING_FAVE_10_1)).thenReturn(3);
		when(client.getVarbitValue(VarbitID.FAIRYRING_FAVE_10_2)).thenReturn(2);
		when(client.getVarbitValue(VarbitID.FAIRYRING_FAVE_10_3)).thenReturn(1);

		assertEquals(Arrays.asList("CKR", "BKS"), Transport.read(client).fairyRingFavourites);
	}

	/**
	 * With no favourites saved the list is empty, and a set slot with an out-of-range dial is skipped.
	 */
	@Test
	public void emptyAndInvalidFavouritesAreSkipped()
	{
		Client client = loggedInClient();
		assertEquals(Collections.emptyList(), Transport.read(client).fairyRingFavourites);

		when(client.getVarbitValue(VarbitID.FAIRYRING_FAVE_SET_3)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.FAIRYRING_FAVE_3_1)).thenReturn(7);
		assertEquals(Collections.emptyList(), Transport.read(client).fairyRingFavourites);
	}

	/**
	 * The last destination unpacks dial 1 from bits 4-5, dial 2 from bits 2-3 and dial 3 from bits 0-1.
	 */
	@Test
	public void decodesLastDestination()
	{
		Client client = loggedInClient();
		when(client.getVarbitValue(VarbitID.FAIRYRING_LASTLOC_SET)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.FAIRYRING_LASTLOC)).thenReturn((3 << 4) | (2 << 2) | 1);
		assertEquals("BKS", Transport.read(client).fairyRingLast);

		when(client.getVarbitValue(VarbitID.FAIRYRING_LASTLOC)).thenReturn(0);
		assertEquals("AIP", Transport.read(client).fairyRingLast);
	}

	/**
	 * With the set flag zero there is no last destination, whatever the packed value holds.
	 */
	@Test
	public void lastDestinationIsNullWhenUnset()
	{
		Client client = loggedInClient();
		when(client.getVarbitValue(VarbitID.FAIRYRING_LASTLOC)).thenReturn(42);
		assertNull(Transport.read(client).fairyRingLast);
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
