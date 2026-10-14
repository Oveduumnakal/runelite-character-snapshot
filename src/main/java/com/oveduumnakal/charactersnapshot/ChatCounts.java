/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * Kill count pattern and boss renaming derived from RuneLite's ChatCommandsPlugin, BSD 2-Clause:
 * Copyright (c) 2017. l2-
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the chat lines that report running totals: boss kill counts. The kill-count pattern and the
 * boss-name normalisation match RuneLite's Chat Commands plugin, so a count parsed here lands under the
 * same key that plugin stores it under.
 */
final class ChatCounts
{
	/** Config group in which the Chat Commands plugin stores kill counts (RuneScape-profile scoped). */
	static final String KILLCOUNT_GROUP = "killcount";

	private static final Pattern KILLCOUNT_PATTERN = Pattern.compile(
		"Your (?<pre>completion count for |subdued |completed )?(?:<col=[0-9a-f]{6}>)?(?<boss>.+?)(?:</col>)? "
			+ "(?<post>(?:(?:kill|harvest|lap|completion|success|Total Ticket) )?(?:count )?)is: ?"
			+ "(?:<col=[0-9a-f]{6}>|@.+?@)(?<kc>[0-9,]+)</col>");

	private static final Set<String> ACTIVITY_KEYS = new HashSet<>(Arrays.asList(
		"colossal wyrm basic", "colossal wyrm advanced", "prifddinas", "shayzien basic", "shayzien advanced",
		"ape atoll", "agility pyramid", "agility arena", "dorgesh-kaan", "gnome stronghold", "penguin",
		"werewolf", "wilderness", "barbarian outpost", "werewolf skullball", "herbiboar", "lunar chest",
		"hunter rumours", "colosseum glory", "bird's egg offerings", "crystal chest", "larran's small chest",
		"larran's big chest", "brimstone chest", "first challenge", "second challenge", "third challenge",
		"fourth challenge", "fifth challenge", "sixth challenge"));

	private ChatCounts()
	{
	}

	/**
	 * Parses a kill-count line such as {@code Your Zulrah kill count is: 12.}
	 *
	 * @param message the raw chat message, colour tags included
	 * @return the normalised boss key and its count, or {@code null} when the line is not a kill count
	 */
	static Map.Entry<String, Integer> parseKillCount(String message)
	{
		if (message == null)
			return null;

		Matcher matcher = KILLCOUNT_PATTERN.matcher(message);
		if (!matcher.find())
			return null;

		String pre = matcher.group("pre");
		String post = matcher.group("post");
		if ((pre == null || pre.isEmpty()) && (post == null || post.isEmpty()))
			return null;

		int count;
		try
		{
			count = Integer.parseInt(matcher.group("kc").replace(",", ""));
		}
		catch (NumberFormatException e)
		{
			return null;
		}

		return new SimpleImmutableEntry<>(killCountKey(matcher.group("boss")), count);
	}

	/**
	 * Normalises a boss name to the key the Chat Commands plugin stores it under: the Barrows rename
	 * applied, colons removed (the config service rejects them), then lower-cased.
	 *
	 * @param boss the boss name as shown in chat
	 * @return the config key
	 */
	static String killCountKey(String boss)
	{
		String renamed = "Barrows chest".equals(boss) ? "Barrows Chests" : boss;
		return renamed.replace(":", "").toLowerCase(Locale.ROOT);
	}

	/**
	 * Tells agility laps and other non-boss counts (chests, rumours, challenges) from boss kill counts.
	 *
	 * @param key a key from {@link #killCountKey(String)}
	 * @return whether the count belongs under {@code activityCounts} rather than {@code killCounts}
	 */
	static boolean isActivityKey(String key)
	{
		return ACTIVITY_KEYS.contains(key) || key.contains("rooftop") || key.startsWith("hallowed sepulchre");
	}
}
