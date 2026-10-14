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
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.config.ConfigManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests boss kill counts: parsing kill-count chat lines, reading the Chat Commands plugin's stored counts,
 * merging the two, restoring from a previous export, and the config toggle.
 */
public class KillCountTest
{
	private static final String PROFILE = "rsprofile.abc";

	/**
	 * A plain kill-count line is parsed under the lower-cased boss name.
	 */
	@Test
	public void parsesKillCountLine()
	{
		Map.Entry<String, Integer> kc = ChatCounts.parseKillCount(
			"Your Zulrah kill count is: <col=ff0000>1,234</col>.");

		assertEquals("zulrah", kc.getKey());
		assertEquals(Integer.valueOf(1234), kc.getValue());
	}

	/**
	 * Colour-tagged boss names, raid completion counts, colons, and the Barrows rename match the keys the
	 * Chat Commands plugin uses.
	 */
	@Test
	public void normalisesBossKeys()
	{
		assertEquals("chambers of xeric",
			ChatCounts.parseKillCount("Your completion count for Chambers of Xeric is: <col=ff0000>7</col>.")
				.getKey());
		assertEquals("barrows chests",
			ChatCounts.parseKillCount("Your Barrows chest count is: <col=ff0000>50</col>.").getKey());
		assertEquals("theatre of blood",
			ChatCounts.parseKillCount("Your completed <col=ff0000>Theatre: of Blood</col> count is: "
				+ "<col=ff0000>3</col>.").getKey());
	}

	/**
	 * Lines that are not kill counts, or carry neither a prefix nor a count word, are ignored.
	 */
	@Test
	public void ignoresOtherLines()
	{
		assertNull(ChatCounts.parseKillCount("You have completed 5 easy Treasure Trails."));
		assertNull(ChatCounts.parseKillCount("Your Zulrah is: <col=ff0000>5</col>"));
		assertNull(ChatCounts.parseKillCount(null));
	}

	/**
	 * A chat line updates the cached count, reports a change only when the value differs, and replaces a
	 * higher stored value because chat is current.
	 */
	@Test
	public void chatLineSetsCount()
	{
		SnapshotCollector collector = collector(mock(ConfigManager.class), true);

		assertTrue(collector.onChatMessage("Your Vorkath kill count is: <col=ff0000>10</col>."));
		assertFalse(collector.onChatMessage("Your Vorkath kill count is: <col=ff0000>10</col>."));
		assertTrue(collector.onChatMessage("Your Vorkath kill count is: <col=ff0000>4</col>."));

		assertEquals(Integer.valueOf(4), collector.buildSnapshot().killCounts.get("vorkath"));
	}

	/**
	 * Counts stored by the Chat Commands plugin are read from the profile config.
	 */
	@Test
	public void readsStoredCounts()
	{
		ConfigManager cm = stored("zulrah", 100, "vorkath", 7);
		SnapshotCollector collector = collector(cm, true);

		assertTrue(collector.pollKillCounts());
		assertFalse(collector.pollKillCounts());

		Map<String, Integer> counts = collector.buildSnapshot().killCounts;
		assertEquals(Integer.valueOf(100), counts.get("zulrah"));
		assertEquals(Integer.valueOf(7), counts.get("vorkath"));
	}

	/**
	 * A stale stored count never lowers a count already parsed from chat or restored.
	 */
	@Test
	public void staleStoredCountDoesNotLowerCache()
	{
		ConfigManager cm = stored("zulrah", 100);
		SnapshotCollector collector = collector(cm, true);

		collector.onChatMessage("Your Zulrah kill count is: <col=ff0000>120</col>.");
		assertFalse(collector.pollKillCounts());

		assertEquals(Integer.valueOf(120), collector.buildSnapshot().killCounts.get("zulrah"));
	}

	/**
	 * With no profile or nothing stored, polling changes nothing; without a config manager it is a no-op.
	 */
	@Test
	public void emptyOrMissingConfigIsNoOp()
	{
		assertFalse(collector(mock(ConfigManager.class), true).pollKillCounts());
		assertFalse(new SnapshotCollector(client(), config(true)).pollKillCounts());
	}

	/**
	 * Counts from the previous export survive until live data arrives, and a login on another character
	 * never inherits them.
	 */
	@Test
	public void restoresAndResets()
	{
		SnapshotCollector collector = collector(mock(ConfigManager.class), true);
		CharacterSnapshot previous = new CharacterSnapshot();
		previous.lastUpdated = "2026-09-21T12:00:00Z";
		previous.player = new PlayerInfo("Zezima", 126, 302, "NORMAL", true);
		previous.killCounts = new TreeMap<>();
		previous.killCounts.put("zulrah", 55);

		collector.restore(previous);
		assertEquals(Integer.valueOf(55), collector.buildSnapshot().killCounts.get("zulrah"));

		collector.reset();
		assertNull(collector.buildSnapshot().killCounts);
	}

	/**
	 * Turning the toggle off omits the section.
	 */
	@Test
	public void toggleOffOmitsSection()
	{
		SnapshotCollector collector = collector(mock(ConfigManager.class), false);
		collector.onChatMessage("Your Vorkath kill count is: <col=ff0000>10</col>.");

		assertNull(collector.buildSnapshot().killCounts);
	}

	/**
	 * Varp values export under readable boss keys, and zero varps are left out.
	 */
	@Test
	public void varpsProduceKillCounts()
	{
		Client client = client();
		when(client.getVarpValue(VarPlayerID.TOTAL_COWBOSS_KILLS)).thenReturn(646);
		when(client.getVarpValue(VarPlayerID.TOTAL_TEMPOROSS_KILLS)).thenReturn(179);
		SnapshotCollector collector = new SnapshotCollector(client, config(true), mock(ConfigManager.class));

		assertTrue(collector.pollKillCounts());
		assertFalse(collector.pollKillCounts());

		Map<String, Integer> counts = collector.buildSnapshot().killCounts;
		assertEquals(2, counts.size());
		assertEquals(Integer.valueOf(646), counts.get("brutus"));
		assertEquals(Integer.valueOf(179), counts.get("tempoross"));
	}

	/**
	 * A stale stored or chat count never overrides the varp value for the same boss.
	 */
	@Test
	public void varpWinsOverStaleStoredCount()
	{
		Client client = client();
		when(client.getVarpValue(VarPlayerID.TOTAL_COWBOSS_KILLS)).thenReturn(646);
		SnapshotCollector collector = new SnapshotCollector(client, config(true), stored("brutus", 700));

		collector.pollKillCounts();
		collector.onChatMessage("Your Brutus kill count is: <col=ff0000>613</col>.");

		assertEquals(Integer.valueOf(646), collector.buildSnapshot().killCounts.get("brutus"));
	}

	/**
	 * Agility laps and other non-boss counts go to activityCounts, never killCounts.
	 */
	@Test
	public void lapsAreActivityCounts()
	{
		ConfigManager cm = stored("canifis rooftop", 253, "zulrah", 5);
		SnapshotCollector collector = collector(cm, true);
		collector.pollKillCounts();
		collector.onChatMessage("Your Seers' Village Rooftop lap count is: <col=ff0000>9</col>.");

		CharacterSnapshot snapshot = collector.buildSnapshot();
		assertEquals(1, snapshot.killCounts.size());
		assertEquals(Integer.valueOf(5), snapshot.killCounts.get("zulrah"));
		assertEquals(Integer.valueOf(253), snapshot.activityCounts.get("canifis rooftop"));
		assertEquals(Integer.valueOf(9), snapshot.activityCounts.get("seers' village rooftop"));
	}

	/**
	 * Agility courses stored under their full names and Barracuda Trials are activity counts too.
	 */
	@Test
	public void fullCourseNamesAreActivityCounts()
	{
		ConfigManager cm = stored("colossal wyrm agility course (advanced)", 264, "gnome stronghold agility", 8);
		SnapshotCollector collector = collector(cm, true);
		collector.pollKillCounts();
		collector.onChatMessage("Your Tempor Tantrum completion count is: <col=ff0000>16</col>.");

		CharacterSnapshot snapshot = collector.buildSnapshot();
		assertTrue(snapshot.killCounts == null || snapshot.killCounts.isEmpty());
		assertEquals(Integer.valueOf(264), snapshot.activityCounts.get("colossal wyrm agility course (advanced)"));
		assertEquals(Integer.valueOf(8), snapshot.activityCounts.get("gnome stronghold agility"));
		assertEquals(Integer.valueOf(16), snapshot.activityCounts.get("tempor tantrum"));
	}

	/**
	 * Laps in an old export's killCounts are moved to activityCounts on restore.
	 */
	@Test
	public void restoreSplitsOldLaps()
	{
		SnapshotCollector collector = collector(mock(ConfigManager.class), true);
		CharacterSnapshot previous = new CharacterSnapshot();
		previous.lastUpdated = "2026-09-21T12:00:00Z";
		previous.player = new PlayerInfo("Zezima", 126, 302, "NORMAL", true);
		previous.killCounts = new TreeMap<>();
		previous.killCounts.put("zulrah", 55);
		previous.killCounts.put("canifis rooftop", 253);

		collector.restore(previous);

		CharacterSnapshot snapshot = collector.buildSnapshot();
		assertEquals(Integer.valueOf(55), snapshot.killCounts.get("zulrah"));
		assertNull(snapshot.killCounts.get("canifis rooftop"));
		assertEquals(Integer.valueOf(253), snapshot.activityCounts.get("canifis rooftop"));
	}

	/**
	 * Builds a collector for a logged-in player.
	 *
	 * @param cm      the config manager
	 * @param enabled the kill-count toggle
	 * @return the collector
	 */
	private static SnapshotCollector collector(ConfigManager cm, boolean enabled)
	{
		return new SnapshotCollector(client(), config(enabled), cm);
	}

	/**
	 * @return a mock client with a logged-in player named Zezima
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
	 * @param enabled the kill-count toggle
	 * @return a mock config
	 */
	private static CharacterSnapshotConfig config(boolean enabled)
	{
		CharacterSnapshotConfig config = mock(CharacterSnapshotConfig.class);
		when(config.syncKillCounts()).thenReturn(enabled);
		return config;
	}

	/**
	 * Stubs a config manager holding stored kill counts.
	 *
	 * @param pairs alternating key and count
	 * @return the mock config manager
	 */
	private static ConfigManager stored(Object... pairs)
	{
		ConfigManager cm = mock(ConfigManager.class);
		when(cm.getRSProfileKey()).thenReturn(PROFILE);
		String[] keys = new String[pairs.length / 2];
		for (int i = 0; i < pairs.length; i += 2)
		{
			keys[i / 2] = (String) pairs[i];
			when(cm.getRSProfileConfiguration("killcount", (String) pairs[i], int.class))
				.thenReturn((Integer) pairs[i + 1]);
		}

		when(cm.getRSProfileConfigurationKeys("killcount", PROFILE, "")).thenReturn(Arrays.asList(keys));
		return cm;
	}
}
