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
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.PlayerInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InOrder;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the plugin lifecycle against the current behaviour: initial collection after the login delay,
 * saves gated on dirty and the save interval, reset-and-save on hop and logout, account switches, and
 * retry after a failed write. The collector and writer are mocks and submitted tasks run inline.
 */
public class PluginLifecycleTest
{
	private static final int LOGIN_DELAY_TICKS = 10;

	private static final int SAVE_THRESHOLD_TICKS = 50;

	/** Scratch export folder. */
	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	private Client client;

	private Player player;

	private CharacterSnapshotConfig config;

	private SnapshotCollector collector;

	private SnapshotWriter writer;

	private CharacterSnapshotPlugin plugin;

	private File dir;

	/**
	 * Builds a started plugin with mock collaborators and an inline executor.
	 *
	 * @throws Exception if the temporary folder cannot be created
	 */
	@Before
	public void setUp() throws Exception
	{
		dir = temp.newFolder("exports");
		client = mock(Client.class);
		player = mock(Player.class);
		when(player.getName()).thenReturn("Zezima");
		when(client.getLocalPlayer()).thenReturn(player);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		config = mock(CharacterSnapshotConfig.class);
		when(config.saveDirectory()).thenReturn(dir.getPath());
		when(config.syncIntervalSeconds()).thenReturn(30);
		when(config.syncSkills()).thenReturn(true);
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

		collector = mock(SnapshotCollector.class);
		CharacterSnapshot snapshot = new CharacterSnapshot();
		snapshot.player = new PlayerInfo("Zezima", 3, 301, null, false);
		when(collector.buildSnapshot()).thenReturn(snapshot);
		writer = mock(SnapshotWriter.class);
		when(writer.write(any(), any())).thenReturn(true);
		plugin.collector = collector;
		plugin.writer = writer;
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
	 * Sends a game-state change.
	 *
	 * @param state the new state
	 */
	private void gameState(GameState state)
	{
		GameStateChanged event = new GameStateChanged();
		event.setGameState(state);
		plugin.onGameStateChanged(event);
	}

	/**
	 * Marks the snapshot dirty through a stat change.
	 */
	private void makeDirty()
	{
		plugin.onStatChanged(new StatChanged(Skill.ATTACK, 100, 2, 2));
	}

	/**
	 * Logs in and runs through the login delay and the initial collection.
	 */
	private void loginAndCollect()
	{
		gameState(GameState.LOGGED_IN);
		tick(LOGIN_DELAY_TICKS);
	}

	/**
	 * Nothing is collected during the login delay; the initial collection restores the previous export,
	 * polls every enabled source and writes once on the tick the delay ends.
	 */
	@Test
	public void initialCollectionRunsAfterLoginDelay()
	{
		gameState(GameState.LOGGED_IN);
		tick(LOGIN_DELAY_TICKS - 1);

		verify(collector, never()).restore(any());
		verify(collector, never()).pollAllSkills();
		verify(writer, never()).write(any(), any());

		tick(1);

		verify(writer).read("Zezima", dir);
		verify(collector).restore(any());
		verify(collector).pollAllSkills();
		verify(writer, times(1)).write(any(), eq(dir));
	}

	/**
	 * With no player there is nothing to collect, however many ticks pass.
	 */
	@Test
	public void ticksWithoutLocalPlayerDoNothing()
	{
		when(client.getLocalPlayer()).thenReturn(null);

		gameState(GameState.LOGGED_IN);
		tick(LOGIN_DELAY_TICKS * 3);

		verify(collector, never()).restore(any());
		verify(writer, never()).write(any(), any());
	}

	/**
	 * A clean snapshot is never rewritten, however much time passes.
	 */
	@Test
	public void noSaveWhenNotDirty()
	{
		loginAndCollect();
		tick(SAVE_THRESHOLD_TICKS * 3);

		verify(writer, times(1)).write(any(), any());
	}

	/**
	 * A dirty snapshot is written only once the save interval has passed, then the interval restarts.
	 */
	@Test
	public void dirtySnapshotIsSavedOnlyPastTheInterval()
	{
		loginAndCollect();
		verify(writer, times(1)).write(any(), any());
		makeDirty();

		tick(SAVE_THRESHOLD_TICKS - 1);
		verify(writer, times(1)).write(any(), any());

		tick(1);
		verify(writer, times(2)).write(any(), any());

		makeDirty();
		tick(SAVE_THRESHOLD_TICKS - 1);
		verify(writer, times(2)).write(any(), any());

		tick(1);
		verify(writer, times(3)).write(any(), any());
	}

	/**
	 * A world hop saves the pending changes first, then clears the collector, and runs a fresh initial
	 * collection once logged back in.
	 */
	@Test
	public void hopSavesThenResetsThenCollectsAgain()
	{
		loginAndCollect();
		makeDirty();

		gameState(GameState.HOPPING);

		InOrder order = inOrder(collector, writer);
		order.verify(collector).buildSnapshot();
		order.verify(writer, times(2)).write(any(), any());
		order.verify(collector).reset();

		gameState(GameState.LOGGED_IN);
		tick(LOGIN_DELAY_TICKS);

		verify(collector, times(2)).restore(any());
	}

	/**
	 * Logging out saves pending changes before the collector is cleared.
	 */
	@Test
	public void logoutSavesThenResets()
	{
		loginAndCollect();
		makeDirty();

		gameState(GameState.LOGIN_SCREEN);

		InOrder order = inOrder(collector);
		order.verify(collector, times(2)).buildSnapshot();
		order.verify(collector).reset();
		verify(writer, times(2)).write(any(), any());
	}

	/**
	 * Logging out with nothing pending clears the collector without writing.
	 */
	@Test
	public void logoutWhenCleanOnlyResets()
	{
		loginAndCollect();

		gameState(GameState.LOGIN_SCREEN);

		verify(collector).reset();
		verify(writer, times(1)).write(any(), any());
	}

	/**
	 * A different account name on a later tick clears the old caches and restores the new character.
	 */
	@Test
	public void accountSwitchResetsAndRestoresTheNewCharacter()
	{
		loginAndCollect();
		verify(collector, never()).reset();

		when(player.getName()).thenReturn("Other");
		tick(1);

		verify(collector).reset();
		verify(writer).read("Other", dir);
		verify(collector, times(2)).restore(any());
	}

	/**
	 * The same name on later ticks never resets.
	 */
	@Test
	public void sameAccountDoesNotReset()
	{
		loginAndCollect();
		tick(SAVE_THRESHOLD_TICKS);

		verify(collector, never()).reset();
	}

	/**
	 * A failed write re-marks the snapshot dirty so the next interval retries it.
	 */
	@Test
	public void failedWriteIsRetriedAtTheNextInterval()
	{
		when(writer.write(any(), any())).thenReturn(false, true);

		loginAndCollect();
		verify(writer, times(1)).write(any(), any());

		tick(SAVE_THRESHOLD_TICKS);
		verify(writer, times(2)).write(any(), any());

		tick(SAVE_THRESHOLD_TICKS * 2);
		verify(writer, times(2)).write(any(), any());
	}

	/**
	 * Shutting down while logged in writes a last snapshot if one is pending.
	 */
	@Test
	public void shutDownWhileLoggedInSavesPendingChanges()
	{
		loginAndCollect();
		makeDirty();

		plugin.shutDown();

		verify(writer, times(2)).write(any(), any());
	}
}
