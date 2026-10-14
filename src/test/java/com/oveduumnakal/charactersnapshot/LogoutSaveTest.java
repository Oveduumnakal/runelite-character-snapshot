/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

import com.google.gson.Gson;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that the final save of a session still happens when the local player is already gone at the
 * login screen, and that nothing is written when no player was ever seen.
 */
public class LogoutSaveTest
{
	/** Scratch export folder. */
	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	private Client client;

	private CharacterSnapshotPlugin plugin;

	private File dir;

	private SnapshotWriter reader;

	/**
	 * Builds a started plugin whose executor runs tasks inline.
	 *
	 * @throws Exception if the temporary folder cannot be created
	 */
	@Before
	public void setUp() throws Exception
	{
		dir = temp.newFolder("exports");
		client = mock(Client.class);
		CharacterSnapshotConfig config = mock(CharacterSnapshotConfig.class);
		when(config.saveDirectory()).thenReturn(dir.getPath());
		when(config.syncSkills()).thenReturn(true);
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
		reader = new SnapshotWriter(new Gson());
	}

	/**
	 * Changes made after the last interval save are written on the logout transition even though the
	 * local player is already null, using the identity cached during the session.
	 */
	@Test
	public void logoutWritesWithCachedIdentityWhenPlayerIsGone()
	{
		Player player = mock(Player.class);
		when(player.getName()).thenReturn("Zezima");
		when(player.getCombatLevel()).thenReturn(126);
		when(client.getLocalPlayer()).thenReturn(player);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		plugin.onGameTick(new GameTick());
		assertNotNull(reader.read("Zezima", dir));

		plugin.onStatChanged(new StatChanged(Skill.ATTACK, 13034431, 99, 99));

		when(client.getLocalPlayer()).thenReturn(null);
		when(client.getGameState()).thenReturn(GameState.LOGIN_SCREEN);
		GameStateChanged event = new GameStateChanged();
		event.setGameState(GameState.LOGIN_SCREEN);
		plugin.onGameStateChanged(event);

		CharacterSnapshot written = reader.read("Zezima", dir);
		assertEquals("Zezima", written.player.username);
		assertEquals(126, written.player.combatLevel);
		assertEquals(13034431, written.skills.get("ATTACK").xp);
	}

	/**
	 * With no player ever seen this session, the logout transition writes nothing.
	 */
	@Test
	public void logoutWithoutAnyPlayerWritesNothing()
	{
		when(client.getLocalPlayer()).thenReturn(null);
		plugin.onStatChanged(new StatChanged(Skill.ATTACK, 100, 2, 2));

		GameStateChanged event = new GameStateChanged();
		event.setGameState(GameState.LOGIN_SCREEN);
		plugin.onGameStateChanged(event);

		assertEquals(0, dir.list().length);
	}
}
