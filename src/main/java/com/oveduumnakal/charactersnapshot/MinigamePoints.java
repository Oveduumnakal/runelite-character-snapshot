/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.LinkedHashMap;
import java.util.Map;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.MixologyHopper;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
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

	/** Region id of the Mastering Mixology lab. */
	static final int MIXOLOGY_LAB_REGION = 5521;

	/** Plane of the Mastering Mixology lab. */
	static final int MIXOLOGY_LAB_PLANE = 0;

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

	/**
	 * Reads the paste left in the Mastering Mixology lab hopper. The hopper varbits share their varps with the
	 * lab's mixer state, and the paste only changes inside the lab, so they are read only there (the same
	 * region and plane check the Mastering Mixology plugin makes).
	 *
	 * @param client the client
	 * @return the hopper contents, or {@code null} when the player is not in the lab
	 */
	static MixologyHopper readHopper(Client client)
	{
		Player player = client.getLocalPlayer();
		WorldPoint location = player != null ? player.getWorldLocation() : null;
		if (location == null || location.getRegionID() != MIXOLOGY_LAB_REGION
				|| location.getPlane() != MIXOLOGY_LAB_PLANE)
			return null;

		return new MixologyHopper(client.getVarbitValue(VarbitID.MM_AVAILABLE_MOX),
				client.getVarbitValue(VarbitID.MM_AVAILABLE_AGA),
				client.getVarbitValue(VarbitID.MM_AVAILABLE_LYE));
	}
}
