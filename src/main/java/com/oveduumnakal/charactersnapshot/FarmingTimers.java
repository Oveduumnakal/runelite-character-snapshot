/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * Prediction and storage formats derived from RuneLite's time tracking plugin (FarmingTracker,
 * BirdHouseTracker, BirdHouseState, BirdHouse), BSD 2-Clause:
 * Copyright (c) 2018 Abex
 * Copyright (c) 2018, Daniel Teo
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BirdhouseTimer;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.FarmingTimer;

import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.config.ConfigManager;

/**
 * Reads the farming patch and birdhouse state that RuneLite's built-in Time Tracking plugin keeps in the
 * RuneScape-profile config (group {@code timetracking}) and turns it into timers. Read-only: this class never
 * writes to that config and never touches the client.
 *
 * <p>Time Tracking stores, per patch, {@code <regionId>.<varbit> = "<varbitValue>:<unixSeconds>"} the last time
 * the player was near the patch, and per birdhouse {@code birdhouse.<varp> = "<varpValue>:<unixSeconds>"}. The
 * stored value is decoded with tables copied from that plugin ({@link FarmingData}) and the growth is projected
 * to the present with the same tick maths. Everything here is therefore only as fresh as the player's last visit
 * to each patch, and is absent entirely when Time Tracking has never run (or is disabled).
 */
final class FarmingTimers
{
	/** Config group of the Time Tracking plugin. */
	static final String CONFIG_GROUP = "timetracking";

	/** Seconds a birdhouse takes to finish once seeded (Time Tracking's average for ten birds, 50 minutes). */
	static final long BIRDHOUSE_SECONDS = 50 * 60;

	private static final String BIRDHOUSE_PREFIX = "birdhouse";

	private static final String TICK_OFFSET_KEY = "farmTickOffset";

	private static final String TICK_OFFSET_PRECISION_KEY = "farmTickOffsetPrecision";

	private static final String[] BIRDHOUSE_NAMES = {
		"Bird House", "Oak Bird House", "Willow Bird House", "Teak Bird House", "Maple Bird House",
		"Mahogany Bird House", "Yew Bird House", "Magic Bird House", "Redwood Bird House"
	};

	private static final String[] SPACE_NAMES = {
		"Mushroom Meadow (North)", "Mushroom Meadow (South)", "Verdant Valley (Northeast)",
		"Verdant Valley (Southwest)"
	};

	private static final int[] SPACE_VARPS = {
		VarPlayerID.BIRDHOUSE_TRANSMIT_A, VarPlayerID.BIRDHOUSE_TRANSMIT_B,
		VarPlayerID.BIRDHOUSE_TRANSMIT_C, VarPlayerID.BIRDHOUSE_TRANSMIT_D
	};

	private static final Map<String, Crop> CROPS = new HashMap<>();

	private static final Map<String, List<Range>> RANGES = new HashMap<>();

	private static final List<PatchDef> PATCHES = new ArrayList<>();

	static
	{
		for (String row : FarmingData.CROPS)
		{
			String[] f = row.split("\\|");
			CROPS.put(f[0], new Crop(f[1], Integer.parseInt(f[2]), Integer.parseInt(f[3]),
				Integer.parseInt(f[4]), Integer.parseInt(f[5])));
		}

		for (String row : FarmingData.DECODE)
		{
			String[] f = row.split("\\|");
			String[] r = f[1].split(" ");
			RANGES.computeIfAbsent(f[0], k -> new ArrayList<>()).add(new Range(
				Integer.parseInt(r[0]), Integer.parseInt(r[1]), r[2], r[3],
				Integer.parseInt(r[4]), Integer.parseInt(r[5])));
		}

		for (String row : FarmingData.PATCHES)
		{
			String[] f = row.split("\\|", -1);
			PATCHES.add(new PatchDef(f[0], Integer.parseInt(f[1]), f[2], Integer.parseInt(f[3]), f[4]));
		}
	}

	private FarmingTimers()
	{
	}

	/**
	 * Growth timings and display name of one crop.
	 */
	static final class Crop
	{
		/** Display name, e.g. {@code Ranarr}. */
		final String name;

		/** Minutes per growth tick. */
		final int tickRate;

		/** Number of growth stages. */
		final int stages;

		/** Minutes per regrowth tick, 0 when the crop does not regrow. */
		final int regrowTickRate;

		/** Number of harvest stages. */
		final int harvestStages;

		/**
		 * @param name           display name
		 * @param tickRate       minutes per growth tick
		 * @param stages         growth stages
		 * @param regrowTickRate minutes per regrowth tick
		 * @param harvestStages  harvest stages
		 */
		Crop(String name, int tickRate, int stages, int regrowTickRate, int harvestStages)
		{
			this.name = name;
			this.tickRate = tickRate;
			this.stages = stages;
			this.regrowTickRate = regrowTickRate;
			this.harvestStages = harvestStages;
		}
	}

	/**
	 * One decode range: a varbit value in {@code [low, high]} means a crop in a crop state.
	 */
	static final class Range
	{
		/** First varbit value of the range. */
		final int low;

		/** Last varbit value of the range, inclusive. */
		final int high;

		/** Crop enum name. */
		final String crop;

		/** Crop state enum name. */
		final String state;

		/** Multiplier of the varbit value in the stage formula. */
		final int mult;

		/** Constant of the stage formula {@code base + mult * value}. */
		final int base;

		/**
		 * @param low   first value
		 * @param high  last value
		 * @param crop  crop enum name
		 * @param state crop state enum name
		 * @param mult  stage multiplier
		 * @param base  stage constant
		 */
		Range(int low, int high, String crop, String state, int mult, int base)
		{
			this.low = low;
			this.high = high;
			this.crop = crop;
			this.state = state;
			this.mult = mult;
			this.base = base;
		}
	}

	/**
	 * A farming patch as Time Tracking lists it.
	 */
	static final class PatchDef
	{
		/** Region name, e.g. {@code Catherby}. */
		final String region;

		/** Region id used in the config key. */
		final int regionId;

		/** Patch label within the region, possibly empty. */
		final String name;

		/** Varbit that carries the patch state. */
		final int varbit;

		/** Patch implementation enum name, e.g. {@code HERB}. */
		final String implementation;

		/**
		 * @param region         region name
		 * @param regionId       region id
		 * @param name           patch label
		 * @param varbit         state varbit
		 * @param implementation implementation enum name
		 */
		PatchDef(String region, int regionId, String name, int varbit, String implementation)
		{
			this.region = region;
			this.regionId = regionId;
			this.name = name;
			this.varbit = varbit;
			this.implementation = implementation;
		}

		/**
		 * @return the config key Time Tracking stores this patch under
		 */
		String configKey()
		{
			return regionId + "." + varbit;
		}

		/**
		 * @return the human-readable patch label, e.g. {@code Catherby allotment (North)}
		 */
		String label()
		{
			String type = typeName(implementation);
			return region + " " + type + (name.isEmpty() ? "" : " (" + name + ")");
		}
	}

	/**
	 * A decoded patch state.
	 */
	static final class Decoded
	{
		/** The crop. */
		final Crop crop;

		/** Crop enum name. */
		final String cropKey;

		/** Crop state enum name: HARVESTABLE, GROWING, DISEASED, DEAD, EMPTY or FILLING. */
		final String state;

		/** Current growth stage. */
		final int stage;

		/**
		 * @param crop    the crop
		 * @param cropKey the crop enum name
		 * @param state   the crop state enum name
		 * @param stage   the stage
		 */
		Decoded(Crop crop, String cropKey, String state, int stage)
		{
			this.crop = crop;
			this.cropKey = cropKey;
			this.state = state;
			this.stage = stage;
		}

		/**
		 * @return the stage count that applies in this state
		 */
		int stages()
		{
			return "HARVESTABLE".equals(state) || "FILLING".equals(state) ? crop.harvestStages : crop.stages;
		}

		/**
		 * @return minutes per tick that applies in this state, 0 when the state does not progress
		 */
		int tickRate()
		{
			if ("HARVESTABLE".equals(state))
				return crop.regrowTickRate;

			return "GROWING".equals(state) ? crop.tickRate : 0;
		}
	}

	/**
	 * Decodes a patch varbit value.
	 *
	 * @param implementation patch implementation enum name
	 * @param value          the varbit value
	 * @return the decoded state, or {@code null} when the value is not a known state
	 */
	static Decoded decode(String implementation, int value)
	{
		List<Range> ranges = RANGES.get(implementation);
		if (ranges == null)
			return null;

		for (Range range : ranges)
		{
			if (value >= range.low && value <= range.high)
				return new Decoded(CROPS.get(range.crop), range.crop, range.state, range.base + range.mult * value);
		}

		return null;
	}

	/**
	 * Reads and projects every farming patch with stored data.
	 *
	 * @param configManager source of the Time Tracking config
	 * @param now           the current time, Unix seconds
	 * @param leagues       whether the world is a seasonal world, where farming ticks are five times faster
	 * @return one timer per patch with stored data, in table order; empty when none has been seen
	 */
	static List<FarmingTimer> readFarming(ConfigManager configManager, long now, boolean leagues)
	{
		List<FarmingTimer> timers = new ArrayList<>();
		Integer offset = configManager.getRSProfileConfiguration(CONFIG_GROUP, TICK_OFFSET_KEY, int.class);
		Integer precision = configManager.getRSProfileConfiguration(CONFIG_GROUP, TICK_OFFSET_PRECISION_KEY,
			int.class);
		for (PatchDef patch : PATCHES)
		{
			String stored = configManager.getRSProfileConfiguration(CONFIG_GROUP, patch.configKey());
			long[] parsed = parseStored(stored);
			if (parsed == null)
				continue;

			Decoded decoded = decode(patch.implementation, (int) parsed[0]);
			if (decoded == null)
				continue;

			timers.add(project(patch, decoded, parsed[1], now, leagues, offset, precision));
		}

		return timers;
	}

	/**
	 * Projects one stored patch state to the present.
	 *
	 * @param patch     the patch
	 * @param decoded   the stored state
	 * @param stored    when the state was stored, Unix seconds
	 * @param now       the current time, Unix seconds
	 * @param leagues   whether farming ticks are five times faster
	 * @param offset    stored tick offset in minutes, may be {@code null}
	 * @param precision stored tick offset precision in minutes, may be {@code null}
	 * @return the timer
	 */
	private static FarmingTimer project(PatchDef patch, Decoded decoded, long stored, long now, boolean leagues,
		Integer offset, Integer precision)
	{
		int stage = decoded.stage;
		int stages = decoded.stages();
		int tickRate = decoded.tickRate();
		if (leagues)
			tickRate = tickRate / 5;

		long done = 0;
		if (tickRate > 0)
		{
			long tickNow = tickTime(tickRate, 0, now, offset, precision);
			long tickStored = tickTime(tickRate, 0, stored, offset, precision);
			int delta = (int) (tickNow - tickStored) / (tickRate * 60);
			done = tickTime(tickRate, stages - 1 - stage, tickStored, offset, precision);
			stage = Math.min(stage + delta, stages - 1);
		}

		boolean weeds = "WEEDS".equals(decoded.cropKey);
		String state = weeds ? "EMPTY" : decoded.state;
		String readyAt = null;
		if ("GROWING".equals(decoded.state) && !weeds)
		{
			readyAt = Instant.ofEpochSecond(done).toString();
			if (done < now)
				state = "HARVESTABLE";
		}

		return new FarmingTimer(patch.label(), patch.region, typeName(patch.implementation), decoded.crop.name,
			state, stage, stages, readyAt, Instant.ofEpochSecond(stored).toString());
	}

	/**
	 * Computes the time of a farming tick, with the player's calibrated tick offset applied like Time Tracking
	 * does.
	 *
	 * @param tickRate  minutes per tick
	 * @param ticks     ticks after the tick containing {@code requested}
	 * @param requested the reference time, Unix seconds
	 * @param offset    stored tick offset in minutes, may be {@code null}
	 * @param precision stored tick offset precision in minutes, may be {@code null}
	 * @return the tick time, Unix seconds
	 */
	static long tickTime(int tickRate, int ticks, long requested, Integer offset, Integer precision)
	{
		long calculatedOffset = 0L;
		if (precision != null && offset != null && (precision >= tickRate || precision >= 40))
			calculatedOffset = (offset % tickRate) * 60L;

		long shifted = requested + calculatedOffset;
		long currentTick = shifted - (shifted % (tickRate * 60L));
		return currentTick + ticks * tickRate * 60L - calculatedOffset;
	}

	/**
	 * Reads every birdhouse space with stored data.
	 *
	 * @param configManager source of the Time Tracking config
	 * @param now           the current time, Unix seconds
	 * @return one timer per birdhouse space with stored data, in space order
	 */
	static List<BirdhouseTimer> readBirdhouses(ConfigManager configManager, long now)
	{
		List<BirdhouseTimer> timers = new ArrayList<>();
		for (int i = 0; i < SPACE_VARPS.length; i++)
		{
			String stored = configManager.getRSProfileConfiguration(CONFIG_GROUP,
				BIRDHOUSE_PREFIX + "." + SPACE_VARPS[i]);
			long[] parsed = parseStored(stored);
			if (parsed == null)
				continue;

			int varp = (int) parsed[0];
			String house = null;
			String state;
			String readyAt = null;
			if (varp < 0 || varp > BIRDHOUSE_NAMES.length * 3)
			{
				state = "UNKNOWN";
			}
			else if (varp == 0)
			{
				state = "EMPTY";
			}
			else
			{
				house = BIRDHOUSE_NAMES[(varp - 1) / 3];
				if (varp % 3 == 0)
				{
					long done = parsed[1] + BIRDHOUSE_SECONDS;
					state = done <= now ? "READY" : "SEEDED";
					readyAt = Instant.ofEpochSecond(done).toString();
				}
				else
				{
					state = "BUILT";
				}
			}

			timers.add(new BirdhouseTimer(SPACE_NAMES[i], house, state, readyAt,
				Instant.ofEpochSecond(parsed[1]).toString()));
		}

		return timers;
	}

	/**
	 * Parses a stored {@code value:unixSeconds} pair like Time Tracking.
	 *
	 * @param stored the stored text, may be {@code null}
	 * @return {@code {value, seconds}}, or {@code null} when absent, malformed, or the time is not positive
	 */
	static long[] parseStored(String stored)
	{
		if (stored == null)
			return null;

		String[] parts = stored.split(":");
		if (parts.length != 2)
			return null;

		try
		{
			long value = Long.parseLong(parts[0]);
			long seconds = Long.parseLong(parts[1]);
			if (seconds <= 0 || value < Integer.MIN_VALUE || value > Integer.MAX_VALUE)
				return null;

			return new long[] {value, seconds};
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}

	/**
	 * @param implementation implementation enum name
	 * @return a lower-case display type, e.g. {@code fruit tree}
	 */
	private static String typeName(String implementation)
	{
		return implementation.toLowerCase(Locale.ROOT).replace('_', ' ');
	}
}
