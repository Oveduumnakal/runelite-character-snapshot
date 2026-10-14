/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * Completion-message pattern derived from RuneLite's LootTrackerPlugin, BSD 2-Clause:
 * Copyright (c) 2018, Psikoi
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the treasure-trail completion chat line. None of RuneLite's built-in plugins keeps a clue
 * completion count (Chat Commands asks the hiscores over the network), so the chat line is the only
 * offline source.
 */
final class ClueChat
{
	/** Clue tiers in export order. */
	static final List<String> TIERS = Arrays.asList("beginner", "easy", "medium", "hard", "elite", "master");

	private static final Pattern PATTERN = Pattern.compile(
		"You have completed (?<count>[0-9,]+) (?<tier>[a-z]+) Treasure Trails?\\.");

	private static final Pattern TAGS = Pattern.compile("<[^>]*>");

	private ClueChat()
	{
	}

	/**
	 * Parses a line such as {@code You have completed 12 medium Treasure Trails.} Colour tags are stripped
	 * first.
	 *
	 * @param message the raw chat message
	 * @return the lower-case tier and the number of trails completed, or {@code null} when the line is not a
	 *         completion line for a known tier
	 */
	static Map.Entry<String, Integer> parse(String message)
	{
		if (message == null)
			return null;

		Matcher matcher = PATTERN.matcher(TAGS.matcher(message).replaceAll(""));
		if (!matcher.find() || !TIERS.contains(matcher.group("tier")))
			return null;

		try
		{
			return new SimpleImmutableEntry<>(matcher.group("tier"),
				Integer.parseInt(matcher.group("count").replace(",", "")));
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}
}
