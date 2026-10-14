/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.PlayerInfo;
import org.junit.Test;

import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.VarbitComposition;
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
 * Tests the prayer and magic setup: reward prayer unlocks, quick prayers decoded through the varbit
 * definitions, the autocast spell name, and the account-info toggle.
 */
public class PrayerMagicTest
{
	/**
	 * Only reward prayers whose unlock varbit is set are listed, in a fixed order.
	 */
	@Test
	public void listsUnlockedPrayers()
	{
		Client client = loggedInClient();
		when(client.getVarbitValue(VarbitID.PRAYER_AUGURY_UNLOCKED)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.PRAYER_RIGOUR_UNLOCKED)).thenReturn(1);

		assertEquals(Arrays.asList("RIGOUR", "AUGURY"), PrayerMagic.unlockedPrayers(client));
	}

	/**
	 * A quick-prayer bit is matched to the prayer whose active varbit sits at the same offset in the
	 * active-prayer varp; prayers stored in another varp are ignored.
	 */
	@Test
	public void decodesQuickPrayers()
	{
		Client client = loggedInClient();
		stubBit(client, VarbitID.PRAYER_ALLACTIVE, 83, 0);
		stubBit(client, Prayer.THICK_SKIN.getVarbit(), 83, 0);
		stubBit(client, Prayer.PROTECT_FROM_MELEE.getVarbit(), 83, 18);
		stubBit(client, Prayer.PIETY.getVarbit(), 83, 25);
		stubBit(client, Prayer.RP_REJUVENATION.getVarbit(), 99, 18);
		when(client.getVarbitValue(VarbitID.QUICKPRAYER_SELECTED)).thenReturn((1 << 18) | (1 << 25));

		assertEquals(Arrays.asList("PROTECT_FROM_MELEE", "PIETY"), PrayerMagic.quickPrayers(client));
	}

	/**
	 * Without the active-prayer varbit definition no quick prayer is guessed.
	 */
	@Test
	public void quickPrayersEmptyWithoutDefinitions()
	{
		Client client = loggedInClient();
		when(client.getVarbitValue(VarbitID.QUICKPRAYER_SELECTED)).thenReturn(-1);

		assertEquals(Collections.emptyList(), PrayerMagic.quickPrayers(client));
	}

	/**
	 * The autocast spell is named from its spell object; defensive mode is reported alongside it.
	 */
	@Test
	public void namesAutocastSpell()
	{
		Client client = loggedInClient();
		ItemComposition spell = mock(ItemComposition.class);
		when(spell.getName()).thenReturn("Fire Surge");
		when(client.getVarpValue(VarPlayerID.AUTOCAST_SPELL_OBJ)).thenReturn(20200);
		when(client.getItemDefinition(20200)).thenReturn(spell);
		when(client.getVarbitValue(VarbitID.AUTOCAST_DEFMODE)).thenReturn(1);
		SnapshotCollector collector = new SnapshotCollector(client, config(true));

		assertTrue(collector.pollPrayerMagic());
		PlayerInfo player = collector.buildSnapshot().player;

		assertEquals("Fire Surge", player.autocastSpell);
		assertEquals(Boolean.TRUE, player.autocastDefensive);
		assertFalse(collector.pollPrayerMagic());
	}

	/**
	 * With nothing autocast both autocast fields are null, while the prayer lists are present.
	 */
	@Test
	public void noAutocastIsNull()
	{
		Client client = loggedInClient();
		SnapshotCollector collector = new SnapshotCollector(client, config(true));

		collector.pollPrayerMagic();
		PlayerInfo player = collector.buildSnapshot().player;

		assertNull(player.autocastSpell);
		assertNull(player.autocastDefensive);
		assertEquals(Collections.emptyList(), player.unlockedPrayers);
	}

	/**
	 * With account info off, none of the prayer or magic fields are exported.
	 */
	@Test
	public void toggleOffOmitsFields()
	{
		Client client = loggedInClient();
		when(client.getVarbitValue(VarbitID.PRAYER_RIGOUR_UNLOCKED)).thenReturn(1);
		SnapshotCollector collector = new SnapshotCollector(client, config(false));

		collector.pollPrayerMagic();
		PlayerInfo player = collector.buildSnapshot().player;

		assertNull(player.unlockedPrayers);
		assertNull(player.quickPrayers);
	}

	/**
	 * Stubs a varbit definition.
	 *
	 * @param client the mock client
	 * @param varbit the varbit id
	 * @param varp   the varp it lives in
	 * @param lsb    its least significant bit
	 */
	private static void stubBit(Client client, int varbit, int varp, int lsb)
	{
		VarbitComposition def = mock(VarbitComposition.class);
		when(def.getIndex()).thenReturn(varp);
		when(def.getLeastSignificantBit()).thenReturn(lsb);
		when(client.getVarbit(varbit)).thenReturn(def);
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

	/**
	 * Builds a config with only the account-info toggle set.
	 *
	 * @param accountInfo the account-info toggle
	 * @return the mock config
	 */
	private static CharacterSnapshotConfig config(boolean accountInfo)
	{
		CharacterSnapshotConfig config = mock(CharacterSnapshotConfig.class);
		when(config.syncAccountInfo()).thenReturn(accountInfo);
		return config;
	}
}
