/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.TreeMap;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.PlayerInfo;
import org.junit.Test;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.WorldType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests completed clue scroll counts: parsing the completion chat line, tier ordering, carry-over from a
 * previous export, and the config toggle.
 */
public class ClueCountTest
{
	/**
	 * A completion line, with or without colour tags, yields the tier and running total.
	 */
	@Test
	public void parsesCompletionLine()
	{
		Map.Entry<String, Integer> plain = ClueChat.parse("You have completed 12 medium Treasure Trails.");
		Map.Entry<String, Integer> tagged = ClueChat.parse(
			"You have completed <col=ef1020>1</col> hard Treasure Trail.");

		assertEquals("medium", plain.getKey());
		assertEquals(Integer.valueOf(12), plain.getValue());
		assertEquals("hard", tagged.getKey());
		assertEquals(Integer.valueOf(1), tagged.getValue());
	}

	/**
	 * Unrelated lines and unknown tiers are ignored.
	 */
	@Test
	public void ignoresOtherLines()
	{
		assertNull(ClueChat.parse("Your Zulrah kill count is: 5."));
		assertNull(ClueChat.parse("You have completed 3 shiny Treasure Trails."));
		assertNull(ClueChat.parse(null));
	}

	/**
	 * Counts are exported in tier order, only for tiers seen, and a repeat of the same value is no change.
	 */
	@Test
	public void exportsTiersInOrder()
	{
		SnapshotCollector collector = collector(true);

		assertTrue(collector.onClueMessage("You have completed 4 master Treasure Trails."));
		assertTrue(collector.onClueMessage("You have completed 9 easy Treasure Trails."));
		assertFalse(collector.onClueMessage("You have completed 9 easy Treasure Trails."));

		Map<String, Integer> counts = collector.buildSnapshot().clueCounts;
		assertEquals(Arrays.asList("easy", "master"), Arrays.asList(counts.keySet().toArray()));
		assertEquals(Integer.valueOf(9), counts.get("easy"));
		assertEquals(Integer.valueOf(4), counts.get("master"));
	}

	/**
	 * Counts from the previous export are kept, a chat line replaces them, and a reset drops them.
	 */
	@Test
	public void restoresReplacesAndResets()
	{
		SnapshotCollector collector = collector(true);
		CharacterSnapshot previous = new CharacterSnapshot();
		previous.lastUpdated = "2026-09-21T12:00:00Z";
		previous.player = new PlayerInfo("Zezima", 126, 302, "NORMAL", true);
		previous.clueCounts = new TreeMap<>();
		previous.clueCounts.put("hard", 30);
		previous.clueCounts.put("bogus", 5);

		collector.restore(previous);
		Map<String, Integer> restored = collector.buildSnapshot().clueCounts;
		assertEquals(Integer.valueOf(30), restored.get("hard"));
		assertFalse(restored.containsKey("bogus"));

		collector.onClueMessage("You have completed 31 hard Treasure Trails.");
		assertEquals(Integer.valueOf(31), collector.buildSnapshot().clueCounts.get("hard"));

		collector.reset();
		assertNull(collector.buildSnapshot().clueCounts);
	}

	/**
	 * Turning the toggle off omits the section.
	 */
	@Test
	public void toggleOffOmitsSection()
	{
		SnapshotCollector collector = collector(false);
		collector.onClueMessage("You have completed 4 master Treasure Trails.");

		assertNull(collector.buildSnapshot().clueCounts);
	}

	/**
	 * Builds a collector for a logged-in player.
	 *
	 * @param enabled the clue-count toggle
	 * @return the collector
	 */
	private static SnapshotCollector collector(boolean enabled)
	{
		Client client = mock(Client.class);
		Player player = mock(Player.class);
		when(player.getName()).thenReturn("Zezima");
		when(client.getLocalPlayer()).thenReturn(player);
		when(client.getWorldType()).thenReturn(EnumSet.noneOf(WorldType.class));
		CharacterSnapshotConfig config = mock(CharacterSnapshotConfig.class);
		when(config.syncClueCounts()).thenReturn(enabled);
		return new SnapshotCollector(client, config);
	}
}
