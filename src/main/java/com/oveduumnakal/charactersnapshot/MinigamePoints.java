/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.LinkedHashMap;
import java.util.Map;

import net.runelite.api.Client;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

/**
 * Reads minigame reward-shop currencies. Each currency is one named game variable; minigames whose currency
 * has no clearly named variable (Pest Control, Mahogany Homes, Last Man Standing, Guardians of the Rift,
 * Barbarian Assault) are left out rather than guessed.
 */
final class MinigamePoints
{
	/** Currency key to its varp id. */
	static final Map<String, Integer> VARPS = new LinkedHashMap<>();

	/** Currency key to its varbit id. */
	static final Map<String, Integer> VARBITS = new LinkedHashMap<>();

	static
	{
		VARPS.put("nightmareZone", VarPlayerID.NZONE_REWARDPOINTS);
		VARPS.put("giantsFoundry", VarPlayerID.GIANTS_FOUNDRY_REWARD_SHOP_POINTS);
		VARPS.put("mixologyMox", VarPlayerID.MIXOLOGY_MOX_POINTS);
		VARPS.put("mixologyAga", VarPlayerID.MIXOLOGY_AGA_POINTS);
		VARPS.put("mixologyLye", VarPlayerID.MIXOLOGY_LYE_POINTS);
		VARPS.put("soulWarsZeal", VarPlayerID.SOUL_WARS_ZEAL);
		VARPS.put("valeTotemsResearch", VarPlayerID.ENT_TOTEMS_RESEARCH_POINTS);
		VARBITS.put("titheFarm", VarbitID.HOSIDIUS_TITHE_REWARDPOINTS);
		VARBITS.put("temporossPermits", VarbitID.TEMPOROSS_REWARDPERMITS);
		VARBITS.put("museumKudos", VarbitID.VM_KUDOS);
		VARBITS.put("pvpArena", VarbitID.PVPA_POINTS_CURRENCY);
	}

	private MinigamePoints()
	{
	}

	/**
	 * Reads every known currency.
	 *
	 * @param client the client
	 * @return currency key to balance, varps first then varbits, each in table order
	 */
	static Map<String, Integer> read(Client client)
	{
		Map<String, Integer> points = new LinkedHashMap<>();
		for (Map.Entry<String, Integer> entry : VARPS.entrySet())
			points.put(entry.getKey(), Math.max(0, client.getVarpValue(entry.getValue())));

		for (Map.Entry<String, Integer> entry : VARBITS.entrySet())
			points.put(entry.getKey(), client.getVarbitValue(entry.getValue()));

		return points;
	}
}
