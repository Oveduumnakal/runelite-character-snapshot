/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.ParamID;
import net.runelite.api.Prayer;
import net.runelite.api.VarbitComposition;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

/**
 * Reads the character's prayer and magic setup: unlocked reward prayers, selected quick prayers and the
 * autocast spell. Every read runs on the client thread.
 */
final class PrayerMagic
{
	/** Reward prayer name to the varbit that is 1 once the prayer is unlocked. */
	static final Map<String, Integer> UNLOCK_VARBITS = new LinkedHashMap<>();

	static
	{
		UNLOCK_VARBITS.put("RIGOUR", VarbitID.PRAYER_RIGOUR_UNLOCKED);
		UNLOCK_VARBITS.put("AUGURY", VarbitID.PRAYER_AUGURY_UNLOCKED);
		UNLOCK_VARBITS.put("PRESERVE", VarbitID.PRAYER_PRESERVE_UNLOCKED);
		UNLOCK_VARBITS.put("DEADEYE", VarbitID.PRAYER_DEADEYE_UNLOCKED);
		UNLOCK_VARBITS.put("MYSTIC_VIGOUR", VarbitID.PRAYER_MYSTIC_VIGOUR_UNLOCKED);
	}

	private PrayerMagic()
	{
	}

	/**
	 * Lists the unlocked reward prayers.
	 *
	 * @param client the client
	 * @return unlocked prayer names in {@link #UNLOCK_VARBITS} order, empty when none
	 */
	static List<String> unlockedPrayers(Client client)
	{
		List<String> unlocked = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : UNLOCK_VARBITS.entrySet())
		{
			if (client.getVarbitValue(entry.getValue()) > 0)
				unlocked.add(entry.getKey());
		}

		return unlocked;
	}

	/**
	 * Lists the prayers selected as quick prayers. The quick-prayer mask shares its bit layout with the
	 * active-prayer mask, so each standard prayer's bit is found from its own active varbit's position in
	 * that mask (read from the varbit definitions). Prayers stored outside the active-prayer mask, such as
	 * the ruinous prayers, are skipped.
	 *
	 * @param client the client
	 * @return selected prayer names (RuneLite {@link Prayer} names), empty when none or not resolvable
	 */
	static List<String> quickPrayers(Client client)
	{
		VarbitComposition active = client.getVarbit(VarbitID.PRAYER_ALLACTIVE);
		if (active == null)
			return Collections.emptyList();

		int mask = client.getVarbitValue(VarbitID.QUICKPRAYER_SELECTED);
		List<String> selected = new ArrayList<>();
		for (Prayer prayer : Prayer.values())
		{
			VarbitComposition bit = client.getVarbit(prayer.getVarbit());
			if (bit == null || bit.getIndex() != active.getIndex())
				continue;

			int shift = bit.getLeastSignificantBit() - active.getLeastSignificantBit();
			if (shift >= 0 && shift < 32 && (mask >>> shift & 1) == 1)
				selected.add(prayer.name());
		}

		return selected;
	}

	/**
	 * Names the autocast spell from the spell object the game stores for it. Spell objects carry no item name;
	 * the name is their {@link ParamID#SPELL_NAME} param, with the item name as a fallback.
	 *
	 * @param client the client
	 * @return the spell name, or {@code null} when nothing is autocast or the name cannot be read
	 */
	static String autocastSpell(Client client)
	{
		int spellObj = client.getVarpValue(VarPlayerID.AUTOCAST_SPELL_OBJ);
		if (spellObj <= 0)
			return null;

		try
		{
			ItemComposition def = client.getItemDefinition(spellObj);
			if (def == null)
				return null;

			String name = def.getStringValue(ParamID.SPELL_NAME);
			if (isBlank(name))
				name = def.getName();

			return isBlank(name) ? null : name;
		}
		catch (RuntimeException e)
		{
			return null;
		}
	}

	/**
	 * Tells whether a definition name is missing; the game uses {@code "null"} for unnamed objects.
	 *
	 * @param name the name to check
	 * @return whether the name is null, empty or {@code "null"}
	 */
	private static boolean isBlank(String name)
	{
		return name == null || name.isEmpty() || "null".equals(name);
	}

	/**
	 * Reports whether the autocast is set to defensive casting.
	 *
	 * @param client the client
	 * @return whether defensive autocast is on
	 */
	static boolean autocastDefensive(Client client)
	{
		return client.getVarbitValue(VarbitID.AUTOCAST_DEFMODE) == 1;
	}
}
