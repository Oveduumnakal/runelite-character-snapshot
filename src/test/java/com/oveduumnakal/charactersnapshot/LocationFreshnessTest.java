/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that walking alone, with no other event, gets the new location written at the next save interval.
 */
public class LocationFreshnessTest
{
	private static final int TICKS_PAST_INTERVAL = 60;

	/** Scratch export folder. */
	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	private Player player;

	private CharacterSnapshotPlugin plugin;

	private File dir;

	/**
	 * Builds a started plugin whose executor runs tasks inline.
	 *
	 * @throws Exception if the temporary folder cannot be created
	 */
	@Before
	public void setUp() throws Exception
	{
		dir = temp.newFolder("exports");
		Client client = mock(Client.class);
		player = mock(Player.class);
		when(player.getName()).thenReturn("Zezima");
		when(player.getWorldLocation()).thenReturn(new WorldPoint(3200, 3200, 0));
		when(client.getLocalPlayer()).thenReturn(player);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		CharacterSnapshotConfig config = mock(CharacterSnapshotConfig.class);
		when(config.saveDirectory()).thenReturn(dir.getPath());
		when(config.syncLocation()).thenReturn(true);
		ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
		when(executor.submit(any(Runnable.class))).thenAnswer(invocation ->
		{
			((Runnable) invocation.getArgument(0)).run();
			return null;
		});

		plugin = new CharacterSnapshotPlugin();
		plugin.client = client;
		plugin.config = config;
		plugin.executor = executor;
		plugin.gson = new Gson();
		plugin.startUp();
	}

	/**
	 * A move with no item or stat events is saved once the interval has passed.
	 */
	@Test
	public void movingAloneIsSavedAtTheNextInterval()
	{
		SnapshotWriter reader = new SnapshotWriter(new Gson());
		plugin.onGameTick(new GameTick());
		assertEquals(3200, reader.read("Zezima", dir).location.x);

		when(player.getWorldLocation()).thenReturn(new WorldPoint(3210, 3200, 0));
		for (int i = 0; i < TICKS_PAST_INTERVAL; i++)
			plugin.onGameTick(new GameTick());

		assertEquals(3210, reader.read("Zezima", dir).location.x);
	}
}
