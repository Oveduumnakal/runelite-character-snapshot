/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.DailyTask;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.MiscellaniaData;
import org.junit.Test;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.WorldType;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the daily rewards (unlock and claim conditions) and the Kingdom of Miscellania state.
 */
public class DailiesTest
{
	/**
	 * A diary-gated reward is unlocked by its diary tier and claimed once its claim varbit is set.
	 */
	@Test
	public void diaryRewardUnlockAndClaim()
	{
		Client client = loggedInClient(new WorldPoint(3200, 3200, 0));
		when(client.getVarbitValue(VarbitID.VARROCK_DIARY_EASY_COMPLETE)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.ZAFF_LAST_CLAIMED)).thenReturn(1);

		Map<String, DailyTask> dailies = Dailies.read(client);

		assertTrue(dailies.get("battlestaves").unlocked);
		assertTrue(dailies.get("battlestaves").claimed);
		assertFalse(dailies.get("dynamite").unlocked);
		assertFalse(dailies.get("dynamite").claimed);
	}

	/**
	 * Herb boxes are claimed only once all fifteen are bought, and never unlocked for an ironman.
	 */
	@Test
	public void herbBoxesCountTowardsMax()
	{
		Client client = loggedInClient(new WorldPoint(3200, 3200, 0));
		when(client.getVarbitValue(VarbitID.NZONE_HERBBOXES_PURCHASED)).thenReturn(10);

		DailyTask boxes = Dailies.read(client).get("herbBoxes");
		assertTrue(boxes.unlocked);
		assertFalse(boxes.claimed);
		assertEquals(Integer.valueOf(10), boxes.claimedCount);
		assertEquals(Integer.valueOf(Dailies.HERB_BOX_MAX), boxes.max);

		when(client.getVarbitValue(VarbitID.IRONMAN)).thenReturn(1);
		assertFalse(Dailies.read(client)
			.get("herbBoxes").unlocked);
	}

	/**
	 * Robin's daily maximum grows by thirteen for each Morytania tier from medium to elite.
	 */
	@Test
	public void bonemealMaxFollowsDiaryTiers()
	{
		Client client = loggedInClient(new WorldPoint(3200, 3200, 0));
		when(client.getVarbitValue(VarbitID.MORYTANIA_DIARY_MEDIUM_COMPLETE)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.MORYTANIA_DIARY_HARD_COMPLETE)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.MORYTANIA_SLIME_CLAIMED)).thenReturn(26);

		DailyTask bonemeal = Dailies.read(client).get("bonemeal");

		assertEquals(Integer.valueOf(26), bonemeal.max);
		assertTrue(bonemeal.claimed);
	}

	/**
	 * Bert's sand needs The Hand in the Sand and is never unlocked for an ultimate ironman.
	 */
	@Test
	public void sandNeedsQuestAndNotUltimate()
	{
		Client client = loggedInClient(new WorldPoint(3200, 3200, 0));
		when(client.getVarbitValue(VarbitID.HANDSAND_QUEST)).thenReturn(Dailies.SAND_QUEST_COMPLETE);
		assertTrue(Dailies.read(client)
			.get("sand").unlocked);

		when(client.getVarbitValue(VarbitID.IRONMAN)).thenReturn(Dailies.ULTIMATE_IRONMAN);
		assertFalse(Dailies.read(client)
			.get("sand").unlocked);
	}

	/**
	 * Before Throne of Miscellania there is no kingdom state.
	 */
	@Test
	public void noKingdomBeforeQuest()
	{
		Client client = loggedInClient(new WorldPoint(2500, 3850, 0));

		assertNull(Dailies.readMiscellania(client, mock(ConfigManager.class)));
	}

	/**
	 * Away from the kingdom the values stored by the Kingdom of Miscellania plugin are used.
	 */
	@Test
	public void kingdomFallsBackToStoredValues()
	{
		Client client = loggedInClient(new WorldPoint(3200, 3200, 0));
		when(client.getVarpValue(VarPlayerID.MISC_QUEST)).thenReturn(100);
		ConfigManager cm = mock(ConfigManager.class);
		Instant changed = Instant.parse("2026-10-01T12:00:00Z");
		when(cm.getRSProfileConfiguration(Dailies.KINGDOM_GROUP, "coffer", int.class)).thenReturn(750000);
		when(cm.getRSProfileConfiguration(Dailies.KINGDOM_GROUP, "approval", int.class)).thenReturn(127);
		when(cm.getRSProfileConfiguration(Dailies.KINGDOM_GROUP, "lastChanged", Instant.class)).thenReturn(changed);

		MiscellaniaData kingdom = Dailies.readMiscellania(client, cm);

		assertEquals(100, kingdom.approvalPercent);
		assertEquals(750000, kingdom.coffer);
		assertEquals("2026-10-01T12:00:00Z", kingdom.lastChanged);
	}

	/**
	 * In the kingdom the live varbits win over stored values.
	 */
	@Test
	public void kingdomUsesLiveValuesInRegion()
	{
		Client client = loggedInClient(new WorldPoint(2530, 3860, 0));
		when(client.getVarpValue(VarPlayerID.MISC_QUEST)).thenReturn(100);
		when(client.getVarbitValue(VarbitID.MISC_COFFERS)).thenReturn(500000);
		when(client.getVarbitValue(VarbitID.MISC_APPROVAL)).thenReturn(64);
		ConfigManager cm = mock(ConfigManager.class);
		when(cm.getRSProfileConfiguration(Dailies.KINGDOM_GROUP, "coffer", int.class)).thenReturn(1);
		when(cm.getRSProfileConfiguration(Dailies.KINGDOM_GROUP, "approval", int.class)).thenReturn(1);

		MiscellaniaData kingdom = Dailies.readMiscellania(client, cm);

		assertEquals(50, kingdom.approvalPercent);
		assertEquals(500000, kingdom.coffer);
		assertNull(kingdom.lastChanged);
	}

	/**
	 * The collector exports the dailies under the toggle and reports a change only once.
	 */
	@Test
	public void collectorExportsDailies()
	{
		Client client = loggedInClient(new WorldPoint(3200, 3200, 0));
		CharacterSnapshotConfig config = mock(CharacterSnapshotConfig.class);
		when(config.syncDailies()).thenReturn(true);
		SnapshotCollector collector = new SnapshotCollector(client, config);

		assertTrue(collector.pollDailies());
		assertFalse(collector.pollDailies());
		assertEquals(9, collector.buildSnapshot().dailies.size());

		when(config.syncDailies()).thenReturn(false);
		assertNull(collector.buildSnapshot().dailies);
	}

	/**
	 * Builds a client with a logged-in player at a location.
	 *
	 * @param location the player's location
	 * @return the mock client
	 */
	private static Client loggedInClient(WorldPoint location)
	{
		Client client = mock(Client.class);
		Player player = mock(Player.class);
		when(player.getName()).thenReturn("Zezima");
		when(player.getWorldLocation()).thenReturn(location);
		when(client.getLocalPlayer()).thenReturn(player);
		when(client.getWorldType()).thenReturn(EnumSet.noneOf(WorldType.class));
		return client;
	}
}
