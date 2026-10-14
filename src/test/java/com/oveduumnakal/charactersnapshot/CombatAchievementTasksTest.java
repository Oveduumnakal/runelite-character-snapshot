/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.CombatAchievementData;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.CombatAchievementTask;
import org.junit.Test;

import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.Player;
import net.runelite.api.StructComposition;
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
 * Tests per-task combat-achievement export: varp bits to task ids, cache metadata resolution, and
 * the string-only {@code completedTasks} compatibility list.
 */
public class CombatAchievementTasksTest
{
	/**
	 * Builds a task struct mock.
	 *
	 * @param id      task id param
	 * @param name    task name param
	 * @param monster monster category id param
	 * @return the mock struct
	 */
	private static StructComposition struct(int id, String name, int monster)
	{
		StructComposition s = mock(StructComposition.class);
		when(s.getIntValue(1306)).thenReturn(id);
		when(s.getStringValue(1308)).thenReturn(name);
		when(s.getIntValue(1312)).thenReturn(monster);
		return s;
	}

	/**
	 * Builds a logged-in client with no cache data.
	 *
	 * @return the mock client
	 */
	private static Client bareClient()
	{
		Client client = mock(Client.class);
		Player player = mock(Player.class);
		when(player.getName()).thenReturn("Zezima");
		when(client.getLocalPlayer()).thenReturn(player);
		when(client.getWorldType()).thenReturn(EnumSet.noneOf(WorldType.class));
		return client;
	}

	/**
	 * Builds a client with two easy tasks (ids 0 and 33) and one hard task (id 5) in the cache.
	 *
	 * @return the mock client
	 */
	private static Client clientWithCache()
	{
		Client client = bareClient();
		EnumComposition easy = mock(EnumComposition.class);
		when(easy.getIntVals()).thenReturn(new int[] {100, 101});
		EnumComposition hard = mock(EnumComposition.class);
		when(hard.getIntVals()).thenReturn(new int[] {102});
		EnumComposition monsters = mock(EnumComposition.class);
		when(monsters.getKeys()).thenReturn(new int[] {7, 8});
		when(monsters.getStringVals()).thenReturn(new String[] {"Zulrah", "Vorkath"});
		when(client.getEnum(3981)).thenReturn(easy);
		when(client.getEnum(3983)).thenReturn(hard);
		when(client.getEnum(3971)).thenReturn(monsters);
		StructComposition s0 = struct(0, "Task A", 7);
		when(client.getStructComposition(100)).thenReturn(s0);
		StructComposition s1 = struct(33, "Task B", 8);
		when(client.getStructComposition(101)).thenReturn(s1);
		StructComposition s2 = struct(5, "Task C", 7);
		when(client.getStructComposition(102)).thenReturn(s2);
		return client;
	}

	/**
	 * Builds a collector with only combat achievements enabled.
	 *
	 * @param client the client
	 * @return the collector
	 */
	private static SnapshotCollector collector(Client client)
	{
		CharacterSnapshotConfig config = mock(CharacterSnapshotConfig.class);
		when(config.syncCombatAchievements()).thenReturn(true);
		return new SnapshotCollector(client, config);
	}

	/**
	 * Finds a task by id.
	 *
	 * @param tasks the tasks
	 * @param id    the id
	 * @return the task, or null
	 */
	private static CombatAchievementTask find(List<CombatAchievementTask> tasks, int id)
	{
		for (CombatAchievementTask t : tasks)
			if (t.id == id)
				return t;

		return null;
	}

	/**
	 * Varp bits map to task ids and names resolve from the mocked cache.
	 */
	@Test
	public void bitsMapToIdsAndNamesResolve()
	{
		Client client = clientWithCache();
		when(client.getVarpValue(VarPlayerID.CA_TASK_COMPLETED_0)).thenReturn(1 | (1 << 5));
		SnapshotCollector collector = collector(client);

		assertTrue(collector.pollCombatAchievements());
		assertFalse(collector.pollCombatAchievements());
		List<CombatAchievementTask> tasks = collector.buildSnapshot().combatAchievements.tasks;

		assertEquals(3, tasks.size());
		CombatAchievementTask a = find(tasks, 0);
		assertTrue(a.completed);
		assertEquals("Task A", a.name);
		assertEquals("EASY", a.tier);
		assertEquals("Zulrah", a.monster);
		assertFalse(find(tasks, 33).completed);
		assertEquals("Vorkath", find(tasks, 33).monster);
		assertTrue(find(tasks, 5).completed);
		assertEquals("HARD", find(tasks, 5).tier);
	}

	/**
	 * Completed tasks are listed in {@code completedTasks} as plain strings, from the second varp too.
	 */
	@Test
	public void completedTasksAreStrings()
	{
		Client client = clientWithCache();
		when(client.getVarpValue(VarPlayerID.CA_TASK_COMPLETED_0)).thenReturn(1);
		when(client.getVarpValue(VarPlayerID.CA_TASK_COMPLETED_1)).thenReturn(2);
		SnapshotCollector collector = collector(client);
		collector.pollCombatAchievements();

		List<String> names = collector.buildSnapshot().combatAchievements.completedTasks;

		assertEquals(Arrays.asList("Task A", "Task B"), names);
	}

	/**
	 * With no resolvable metadata, completed ids are still exported with null names and are
	 * excluded from {@code completedTasks}.
	 */
	@Test
	public void unresolvedMetadataKeepsIdsWithNullName()
	{
		Client client = bareClient();
		when(client.getVarpValue(VarPlayerID.CA_TASK_COMPLETED_1)).thenReturn(1 << 4);
		SnapshotCollector collector = collector(client);
		collector.pollCombatAchievements();

		CombatAchievementData ca = collector.buildSnapshot().combatAchievements;

		assertEquals(1, ca.tasks.size());
		assertEquals(36, ca.tasks.get(0).id);
		assertNull(ca.tasks.get(0).name);
		assertNull(ca.tasks.get(0).tier);
		assertTrue(ca.tasks.get(0).completed);
		assertTrue(ca.completedTasks.isEmpty());
	}

	/**
	 * Without a points varbit value, points are summed from completed tasks by tier, and per-tier
	 * completed and total counts are exported for all six tiers.
	 */
	@Test
	public void pointsComputedAndTierCountsExported()
	{
		Client client = clientWithCache();
		when(client.getVarpValue(VarPlayerID.CA_TASK_COMPLETED_0)).thenReturn(1 | (1 << 5));
		SnapshotCollector collector = collector(client);

		collector.pollCombatAchievements();
		CombatAchievementData ca = collector.buildSnapshot().combatAchievements;

		assertEquals(Integer.valueOf(4), ca.points);
		assertEquals(6, ca.tierCounts.size());
		assertEquals(1, ca.tierCounts.get("EASY").completed);
		assertEquals(2, ca.tierCounts.get("EASY").total);
		assertEquals(1, ca.tierCounts.get("HARD").completed);
		assertEquals(1, ca.tierCounts.get("HARD").total);
		assertEquals(0, ca.tierCounts.get("MASTER").total);
	}

	/**
	 * A non-zero points varbit wins over the computed sum.
	 */
	@Test
	public void pointsVarbitIsPreferred()
	{
		Client client = clientWithCache();
		when(client.getVarpValue(VarPlayerID.CA_TASK_COMPLETED_0)).thenReturn(1);
		when(client.getVarbitValue(VarbitID.CA_POINTS)).thenReturn(29);
		SnapshotCollector collector = collector(client);

		collector.pollCombatAchievements();

		assertEquals(Integer.valueOf(29), collector.buildSnapshot().combatAchievements.points);
	}

	/**
	 * With no tier metadata and a completed task, points cannot be computed and tier counts are absent.
	 */
	@Test
	public void unresolvedTiersGiveNullPointsAndCounts()
	{
		Client client = bareClient();
		when(client.getVarpValue(VarPlayerID.CA_TASK_COMPLETED_1)).thenReturn(1 << 4);
		SnapshotCollector collector = collector(client);

		collector.pollCombatAchievements();
		CombatAchievementData ca = collector.buildSnapshot().combatAchievements;

		assertNull(ca.points);
		assertNull(ca.tierCounts);
	}

	/**
	 * Per-point values are 1 to 6 from easy to grandmaster.
	 */
	@Test
	public void tierPointValues()
	{
		List<CombatAchievementTask> tasks = Arrays.asList(
			new CombatAchievementTask(0, "a", "EASY", null, true),
			new CombatAchievementTask(1, "b", "MEDIUM", null, true),
			new CombatAchievementTask(2, "c", "HARD", null, true),
			new CombatAchievementTask(3, "d", "ELITE", null, true),
			new CombatAchievementTask(4, "e", "MASTER", null, true),
			new CombatAchievementTask(5, "f", "GRANDMASTER", null, true));

		assertEquals(Integer.valueOf(21), new CombatAchievementData(1, 1, 1, 1, 1, 1, tasks).points);
	}
}
