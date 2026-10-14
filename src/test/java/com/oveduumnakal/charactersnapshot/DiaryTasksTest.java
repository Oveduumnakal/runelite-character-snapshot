/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.DiaryRegion;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.DiaryTask;
import org.junit.Test;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests per-task achievement-diary export with a mocked {@link Client}: per-task completion read from
 * varp bits and Karamja varbits, unchanged tier booleans, change detection, and the JSON shape.
 */
public class DiaryTasksTest
{
	/**
	 * Each set bit of a diary varp marks exactly that task complete.
	 */
	@Test
	public void varpBitsMapToTasks()
	{
		Client client = client();
		when(client.getVarpValue(VarPlayerID.VARROCK_ACHIEVEMENT_DIARY)).thenReturn((1 << 1) | (1 << 8));

		DiaryRegion varrock = poll(client).get("VARROCK");

		List<DiaryTask> easy = varrock.tasks.get("easy");
		assertEquals(14, easy.size());
		assertTrue(completed(easy, "Browse Thessalia's Store"));
		assertTrue(completed(easy, "Buy Newspaper"));
		assertFalse(completed(easy, "Make Plank"));
		assertEquals(2, easy.stream().filter(t -> t.completed).count());
		assertEquals(0, varrock.tasks.get("medium").stream().filter(t -> t.completed).count());
	}

	/**
	 * A varp bit above 30 is read as a set bit, not as a negative number.
	 */
	@Test
	public void highVarpBitIsRead()
	{
		Client client = client();
		when(client.getVarpValue(VarPlayerID.VARROCK_ACHIEVEMENT_DIARY)).thenReturn(1 << 31);

		DiaryRegion varrock = poll(client).get("VARROCK");

		assertTrue(completed(varrock.tasks.get("hard"), "Edgeville Wakka"));
		assertEquals(1, allTasks(varrock).stream().filter(t -> t.completed).count());
	}

	/**
	 * Karamja tasks are read from their varbits; a counting task needs its full count.
	 */
	@Test
	public void karamjaVarbitsMapToTasks()
	{
		Client client = client();
		when(client.getVarbitValue(VarbitID.ATJUN_EASY_BANANA)).thenReturn(4);
		when(client.getVarbitValue(VarbitID.ATJUN_EASY_SWING)).thenReturn(1);

		DiaryRegion karamja = poll(client).get("KARAMJA");
		assertFalse(completed(karamja.tasks.get("easy"), "Pick 5 Bananas"));
		assertTrue(completed(karamja.tasks.get("easy"), "Swing Rope to Moss Giant Isle"));

		when(client.getVarbitValue(VarbitID.ATJUN_EASY_BANANA)).thenReturn(5);
		karamja = poll(client).get("KARAMJA");
		assertTrue(completed(karamja.tasks.get("easy"), "Pick 5 Bananas"));
	}

	/**
	 * The tier booleans keep coming from the tier varbits and do not depend on task state.
	 */
	@Test
	@SuppressWarnings("deprecation")
	public void tierBooleansUnchanged()
	{
		Client client = client();
		when(client.getVarbitValue(Varbits.DIARY_VARROCK_EASY)).thenReturn(1);
		when(client.getVarpValue(VarPlayerID.VARROCK_ACHIEVEMENT_DIARY)).thenReturn(0);

		DiaryRegion varrock = poll(client).get("VARROCK");

		assertTrue(varrock.easy);
		assertFalse(varrock.medium);
		assertFalse(varrock.hard);
		assertFalse(varrock.elite);
		assertEquals(0, varrock.tasks.get("easy").stream().filter(t -> t.completed).count());
	}

	/**
	 * Completing a single task is reported as a change; polling again with no change is not.
	 */
	@Test
	public void singleTaskChangeIsDetected()
	{
		Client client = client();
		SnapshotCollector collector = new SnapshotCollector(client, config());

		assertTrue(collector.pollDiaries());
		assertFalse(collector.pollDiaries());

		when(client.getVarpValue(VarPlayerID.FALADOR_ACHIEVEMENT_DIARY2)).thenReturn(1 << 10);

		assertTrue(collector.pollDiaries());
		assertFalse(collector.pollDiaries());
	}

	/**
	 * The exported JSON keeps the tier booleans and adds {@code tasks} with name and completed per task.
	 */
	@Test
	public void jsonShape()
	{
		Client client = client();
		when(client.getVarpValue(VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY)).thenReturn(1);

		String json = new Gson().toJson(poll(client).get("ARDOUGNE"));

		assertTrue(json.startsWith("{\"easy\":false,\"medium\":false,\"hard\":false,\"elite\":false,"));
		assertTrue(json.contains("\"tasks\":{\"easy\":[{\"name\":\"Essence Mine\",\"completed\":true}"));
		assertTrue(json.contains("\"elite\":["));
	}

	/**
	 * Every region exposes all four tiers, task names are unique within a tier, and 492 tasks are mapped.
	 */
	@Test
	public void tableIsComplete()
	{
		int total = 0;
		for (Map.Entry<String, DiaryRegion> region : poll(client()).entrySet())
		{
			assertNotNull(region.getKey(), region.getValue().tasks);
			for (String tier : DiaryTasks.TIERS)
			{
				List<DiaryTask> tasks = region.getValue().tasks.get(tier);
				assertNotNull(region.getKey() + " " + tier, tasks);
				assertFalse(tasks.isEmpty());
				Set<String> names = new HashSet<>();
				for (DiaryTask task : tasks)
					assertTrue(region.getKey() + " " + tier + " " + task.name, names.add(task.name));

				total += tasks.size();
			}
		}

		assertEquals(492, total);
	}

	private static boolean completed(List<DiaryTask> tasks, String name)
	{
		for (DiaryTask task : tasks)
		{
			if (task.name.equals(name))
				return task.completed;
		}

		throw new AssertionError("no task named " + name);
	}

	private static List<DiaryTask> allTasks(DiaryRegion region)
	{
		List<DiaryTask> all = new ArrayList<>();
		region.tasks.values().forEach(all::addAll);
		return all;
	}

	private static Map<String, DiaryRegion> poll(Client client)
	{
		SnapshotCollector collector = new SnapshotCollector(client, config());
		collector.pollDiaries();
		CharacterSnapshot data = collector.buildSnapshot();
		return data.achievementDiaries;
	}

	private static Client client()
	{
		Client client = mock(Client.class);
		Player player = mock(Player.class);
		when(player.getName()).thenReturn("Zezima");
		when(client.getLocalPlayer()).thenReturn(player);
		when(client.getWorldType()).thenReturn(EnumSet.noneOf(WorldType.class));
		return client;
	}

	private static CharacterSnapshotConfig config()
	{
		CharacterSnapshotConfig config = mock(CharacterSnapshotConfig.class);
		when(config.syncDiaries()).thenReturn(true);
		return config;
	}
}
