/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

import com.oveduumnakal.charactersnapshot.FarmingTimers.Decoded;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BirdhouseTimer;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.FarmingTimer;
import org.junit.Test;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.WorldType;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.config.ConfigManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the farming and birdhouse timers: stored-value parsing, the tick projection, birdhouse states, and the
 * collector wiring.
 */
public class FarmingTimersTest
{
	private static final String CATHERBY_HERB = "11062.4774";

	private static final long NOW = 1_800_000_000L;

	/**
	 * Stored text parses to a value and time; malformed text and non-positive times are rejected.
	 */
	@Test
	public void parsesStoredValues()
	{
		assertEquals(24L, FarmingTimers.parseStored("24:1700000000")[0]);
		assertEquals(1700000000L, FarmingTimers.parseStored("24:1700000000")[1]);
		assertNull(FarmingTimers.parseStored(null));
		assertNull(FarmingTimers.parseStored("24"));
		assertNull(FarmingTimers.parseStored("a:b"));
		assertNull(FarmingTimers.parseStored("24:0"));
		assertNull(FarmingTimers.parseStored("1:2:3"));
	}

	/**
	 * Ranarr herbs planted just now are growing and ready one tick-run later; the same patch checked long
	 * enough afterwards is harvestable.
	 */
	@Test
	public void herbGrowsThenIsHarvestable()
	{
		int growing = ranarrValue("GROWING", 0);
		ConfigManager cm = config(CATHERBY_HERB, growing + ":" + NOW);

		FarmingTimer fresh = find(FarmingTimers.readFarming(cm, NOW, false), "Catherby herb");
		assertEquals("Ranarr", fresh.crop);
		assertEquals("GROWING", fresh.state);
		assertEquals(0, fresh.stage);
		assertEquals(5, fresh.stages);
		assertNotNull(fresh.readyAt);
		assertEquals(Instant.ofEpochSecond(NOW).toString(), fresh.lastChecked);
		long wait = Instant.parse(fresh.readyAt).getEpochSecond() - NOW;
		assertTrue(wait > 3 * 20 * 60 && wait <= 4 * 20 * 60);

		FarmingTimer later = find(FarmingTimers.readFarming(cm, NOW + 5 * 3600, false), "Catherby herb");
		assertEquals("HARVESTABLE", later.state);
		assertEquals(4, later.stage);
		assertEquals(fresh.readyAt, later.readyAt);
	}

	/**
	 * Seasonal worlds tick five times faster, so the same herb finishes sooner.
	 */
	@Test
	public void leaguesWorldTicksFaster()
	{
		ConfigManager cm = config(CATHERBY_HERB, ranarrValue("GROWING", 0) + ":" + NOW);

		long normal = Instant.parse(find(FarmingTimers.readFarming(cm, NOW, false), "Catherby herb").readyAt)
			.getEpochSecond();
		long fast = Instant.parse(find(FarmingTimers.readFarming(cm, NOW, true), "Catherby herb").readyAt)
			.getEpochSecond();

		assertTrue(fast < normal);
		assertTrue(fast - NOW <= 4 * 4 * 60);
	}

	/**
	 * Weeds read as an empty patch with no crop timer, and a diseased crop has no ready time.
	 */
	@Test
	public void weedsAndDeadCrops()
	{
		ConfigManager cm = config(CATHERBY_HERB, "3:" + NOW);
		FarmingTimer weeds = find(FarmingTimers.readFarming(cm, NOW, false), "Catherby herb");
		assertEquals("Weeds", weeds.crop);
		assertEquals("EMPTY", weeds.state);
		assertNull(weeds.readyAt);

		cm = config(CATHERBY_HERB, ranarrValue("DISEASED", -1) + ":" + NOW);
		FarmingTimer sick = find(FarmingTimers.readFarming(cm, NOW, false), "Catherby herb");
		assertEquals("DISEASED", sick.state);
		assertNull(sick.readyAt);
	}

	/**
	 * Patches with nothing stored, or with an undecodable value, are not listed.
	 */
	@Test
	public void unknownPatchesAreOmitted()
	{
		assertTrue(FarmingTimers.readFarming(mock(ConfigManager.class), NOW, false).isEmpty());
		assertTrue(FarmingTimers.readFarming(config(CATHERBY_HERB, "99999:" + NOW), NOW, false).isEmpty());
	}

	/**
	 * A seeded birdhouse is ready 50 minutes after it was last seen seeded; built-but-unseeded and empty
	 * spaces carry no ready time.
	 */
	@Test
	public void birdhouseStates()
	{
		ConfigManager cm = mock(ConfigManager.class);
		stub(cm, "birdhouse." + VarPlayerID.BIRDHOUSE_TRANSMIT_A, "6:" + NOW);
		stub(cm, "birdhouse." + VarPlayerID.BIRDHOUSE_TRANSMIT_B, "7:" + NOW);
		stub(cm, "birdhouse." + VarPlayerID.BIRDHOUSE_TRANSMIT_C, "0:" + NOW);

		List<BirdhouseTimer> seeded = FarmingTimers.readBirdhouses(cm, NOW + 60);
		assertEquals(3, seeded.size());
		assertEquals("Mushroom Meadow (North)", seeded.get(0).space);
		assertEquals("Oak Bird House", seeded.get(0).house);
		assertEquals("SEEDED", seeded.get(0).state);
		assertEquals(Instant.ofEpochSecond(NOW + 3000).toString(), seeded.get(0).readyAt);
		assertEquals("Willow Bird House", seeded.get(1).house);
		assertEquals("BUILT", seeded.get(1).state);
		assertNull(seeded.get(1).readyAt);
		assertEquals("EMPTY", seeded.get(2).state);
		assertNull(seeded.get(2).house);

		assertEquals("READY", FarmingTimers.readBirdhouses(cm, NOW + 3000).get(0).state);
	}

	/**
	 * The collector reports a change only when the projected timers differ, honours the toggle, and drops
	 * timers on reset.
	 */
	@Test
	public void collectorWiring()
	{
		ConfigManager cm = config(CATHERBY_HERB, ranarrValue("GROWING", 0) + ":" + NOW);
		SnapshotCollector collector = collector(cm, true);

		assertTrue(collector.pollTimers(NOW));
		assertFalse(collector.pollTimers(NOW));
		assertTrue(collector.pollTimers(NOW + 5 * 3600));

		CharacterSnapshot snapshot = collector.buildSnapshot();
		assertEquals(1, snapshot.timers.farming.size());
		assertTrue(snapshot.timers.birdhouses.isEmpty());
		assertEquals("HARVESTABLE", snapshot.timers.farming.get(0).state);

		collector.reset();
		assertNull(collector.buildSnapshot().timers);

		SnapshotCollector off = collector(cm, false);
		off.pollTimers(NOW);
		assertNull(off.buildSnapshot().timers);
		assertFalse(new SnapshotCollector(client(), enabled(true)).pollTimers(NOW));
	}

	/**
	 * @param state the crop state name
	 * @param stage the stage
	 * @return a Ranarr herb varbit value in that state and stage
	 */
	private static int ranarrValue(String state, int stage)
	{
		for (int value = 0; value < 256; value++)
		{
			Decoded d = FarmingTimers.decode("HERB", value);
			if (d != null && "RANARR".equals(d.cropKey) && d.state.equals(state) && (stage < 0 || d.stage == stage))
				return value;
		}

		throw new AssertionError("no ranarr " + state + " " + stage);
	}

	/**
	 * @param timers the timers
	 * @param patch  the patch label
	 * @return the matching timer
	 */
	private static FarmingTimer find(List<FarmingTimer> timers, String patch)
	{
		for (FarmingTimer timer : timers)
		{
			if (timer.patch.equals(patch))
				return timer;
		}

		throw new AssertionError("no timer for " + patch + " in " + timers.size());
	}

	/**
	 * @param key   the Time Tracking config key
	 * @param value the stored value
	 * @return a mock config manager holding it
	 */
	private static ConfigManager config(String key, String value)
	{
		ConfigManager cm = mock(ConfigManager.class);
		stub(cm, key, value);
		return cm;
	}

	/**
	 * @param cm    the config manager mock
	 * @param key   the Time Tracking config key
	 * @param value the stored value
	 */
	private static void stub(ConfigManager cm, String key, String value)
	{
		when(cm.getRSProfileConfiguration("timetracking", key)).thenReturn(value);
	}

	/**
	 * @param cm      the config manager
	 * @param enabled the timers toggle
	 * @return a collector for a logged-in player
	 */
	private static SnapshotCollector collector(ConfigManager cm, boolean enabled)
	{
		return new SnapshotCollector(client(), enabled(enabled), cm);
	}

	/**
	 * @return a mock client with a logged-in player
	 */
	private static Client client()
	{
		Client client = mock(Client.class);
		Player player = mock(Player.class);
		when(player.getName()).thenReturn("Zezima");
		when(client.getLocalPlayer()).thenReturn(player);
		when(client.getWorldType()).thenReturn(EnumSet.noneOf(WorldType.class));
		return client;
	}

	/**
	 * @param enabled the timers toggle
	 * @return a mock config
	 */
	private static CharacterSnapshotConfig enabled(boolean enabled)
	{
		CharacterSnapshotConfig config = mock(CharacterSnapshotConfig.class);
		when(config.syncTimers()).thenReturn(enabled);
		return config;
	}
}
