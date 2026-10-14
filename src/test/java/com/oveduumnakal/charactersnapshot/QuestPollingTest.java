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
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests that quests are polled once at login and afterwards only when a var change marked them stale.
 */
public class QuestPollingTest
{
	private static final int TICKS_PER_POLL_WINDOW = 30;

	/** Scratch export folder. */
	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	private CharacterSnapshotPlugin plugin;

	private SnapshotCollector collector;

	/**
	 * Builds a started plugin with a mocked collector.
	 *
	 * @throws Exception if the temporary folder cannot be created
	 */
	@Before
	public void setUp() throws Exception
	{
		File dir = temp.newFolder("exports");
		Client client = mock(Client.class);
		Player player = mock(Player.class);
		when(player.getName()).thenReturn("Zezima");
		when(client.getLocalPlayer()).thenReturn(player);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		CharacterSnapshotConfig config = mock(CharacterSnapshotConfig.class);
		when(config.saveDirectory()).thenReturn(dir.getPath());
		when(config.syncQuests()).thenReturn(true);

		plugin = new CharacterSnapshotPlugin();
		plugin.client = client;
		plugin.config = config;
		plugin.executor = mock(ScheduledExecutorService.class);
		plugin.gson = new Gson();
		plugin.startUp();
		collector = mock(SnapshotCollector.class);
		plugin.collector = collector;
	}

	/**
	 * Runs game ticks.
	 *
	 * @param count how many ticks
	 */
	private void tick(int count)
	{
		for (int i = 0; i < count; i++)
			plugin.onGameTick(new GameTick());
	}

	/**
	 * Without any var change the quests are polled only by the initial collection.
	 */
	@Test
	public void noRepollWithoutVarChange()
	{
		tick(TICKS_PER_POLL_WINDOW * 4);

		verify(collector, times(1)).pollQuests();
	}

	/**
	 * A var change makes the next periodic poll re-read the quests exactly once.
	 */
	@Test
	public void varChangeTriggersOneRepoll()
	{
		tick(1);
		plugin.onVarbitChanged(new VarbitChanged());
		tick(TICKS_PER_POLL_WINDOW * 3);

		verify(collector, times(2)).pollQuests();
	}

	/**
	 * Var changes during the login delay do not cause a second poll after the initial collection.
	 */
	@Test
	public void varChangesBeforeInitialCollectionAreCovered()
	{
		plugin.onVarbitChanged(new VarbitChanged());
		tick(TICKS_PER_POLL_WINDOW * 2);

		verify(collector, times(1)).pollQuests();
	}
}
