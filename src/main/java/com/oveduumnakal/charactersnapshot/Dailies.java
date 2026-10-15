/*
 * Copyright (c) 2026, Oveduumnakal
 * Daily reward unlock and claim conditions derived from RuneLite's Daily Task Indicators and Kingdom of
 * Miscellania plugins (https://github.com/runelite/runelite), BSD 2-Clause:
 * Copyright (c) 2018, Infinitay <https://github.com/Infinitay>
 * Copyright (c) 2018-2019, Shaun Dreclin <https://github.com/ShaunDreclin>
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.DailyTask;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.MiscellaniaData;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;

/**
 * Reads the claimable daily rewards and the Kingdom of Miscellania state. Every read runs on the client
 * thread.
 */
final class Dailies
{
	/** NMZ herb boxes purchasable per day. */
	static final int HERB_BOX_MAX = 15;

	/** Robin's bonemeal and slime per day for each Morytania diary tier from medium up. */
	static final int BONEMEAL_PER_DIARY = 13;

	/** The Hand in the Sand progress value once Bert's daily sand is unlocked. */
	static final int SAND_QUEST_COMPLETE = 160;

	/** {@code VarbitID.IRONMAN} value of an ultimate ironman, who cannot claim Bert's sand. */
	static final int ULTIMATE_IRONMAN = 2;

	/** Kingdom approval varbit maximum, i.e. 100 percent. */
	static final int MAX_APPROVAL = 127;

	/** Config group of RuneLite's Kingdom of Miscellania plugin (RuneScape-profile scoped). */
	static final String KINGDOM_GROUP = "kingdomofmiscellania";

	/** Map regions of Miscellania and Etceteria, where the game sends the kingdom values. */
	private static final int[] KINGDOM_REGIONS = {10044, 10300};

	private Dailies()
	{
	}

	/**
	 * Reads every daily reward.
	 *
	 * @param client the client
	 * @return reward key to its state, in a fixed order
	 */
	static Map<String, DailyTask> read(Client client)
	{
		Map<String, DailyTask> dailies = new LinkedHashMap<>();
		int ironman = client.getVarbitValue(VarbitID.IRONMAN);

		int herbBoxes = client.getVarbitValue(VarbitID.NZONE_HERBBOXES_PURCHASED);
		dailies.put("herbBoxes", new DailyTask(ironman == 0, herbBoxes >= HERB_BOX_MAX, herbBoxes, HERB_BOX_MAX));
		dailies.put("battlestaves", flag(client, VarbitID.VARROCK_DIARY_EASY_COMPLETE, VarbitID.ZAFF_LAST_CLAIMED));
		dailies.put("pureEssence", flag(client, VarbitID.ARDOUGNE_DIARY_MEDIUM_COMPLETE,
			VarbitID.ARDOUGNE_FREE_ESSENCE));
		dailies.put("randomRunes", flag(client, VarbitID.WILDERNESS_DIARY_EASY_COMPLETE,
			VarbitID.LUNDAIL_LAST_CLAIMED));

		boolean sandUnlocked = ironman != ULTIMATE_IRONMAN
			&& client.getVarbitValue(VarbitID.HANDSAND_QUEST) >= SAND_QUEST_COMPLETE;
		dailies.put("sand", new DailyTask(sandUnlocked, client.getVarbitValue(VarbitID.YANILLE_SAND_CLAIMED) != 0,
			null, null));
		dailies.put("bowstrings", flag(client, VarbitID.KANDARIN_DIARY_EASY_COMPLETE, VarbitID.SEERS_FREE_FLAX));
		dailies.put("ogreArrows", flag(client, VarbitID.WESTERN_DIARY_EASY_COMPLETE, VarbitID.WESTERN_RANTZ_ARROWS));
		dailies.put("bonemeal", bonemeal(client));
		dailies.put("dynamite", flag(client, VarbitID.KOUREND_DIARY_MEDIUM_COMPLETE, VarbitID.KOUREND_FREE_DYNAMITE));
		return dailies;
	}

	/**
	 * Builds a reward unlocked by one varbit and marked claimed by another being non-zero.
	 *
	 * @param client        the client
	 * @param unlockVarbit  varbit that is 1 once the reward is unlocked
	 * @param claimedVarbit varbit that is non-zero once today's reward is claimed
	 * @return the reward state
	 */
	private static DailyTask flag(Client client, int unlockVarbit, int claimedVarbit)
	{
		return new DailyTask(client.getVarbitValue(unlockVarbit) == 1, client.getVarbitValue(claimedVarbit) != 0,
			null, null);
	}

	/**
	 * Builds Robin's bonemeal and slime reward, whose daily maximum grows with each Morytania diary tier.
	 *
	 * @param client the client
	 * @return the reward state
	 */
	private static DailyTask bonemeal(Client client)
	{
		boolean unlocked = client.getVarbitValue(VarbitID.MORYTANIA_DIARY_MEDIUM_COMPLETE) == 1;
		int max = 0;
		if (unlocked)
		{
			max = BONEMEAL_PER_DIARY;
			if (client.getVarbitValue(VarbitID.MORYTANIA_DIARY_HARD_COMPLETE) == 1)
			{
				max += BONEMEAL_PER_DIARY;
				if (client.getVarbitValue(VarbitID.MORYTANIA_DIARY_ELITE_COMPLETE) == 1)
					max += BONEMEAL_PER_DIARY;
			}
		}

		int collected = client.getVarbitValue(VarbitID.MORYTANIA_SLIME_CLAIMED);
		return new DailyTask(unlocked, unlocked && collected >= max, collected, unlocked ? max : null);
	}

	/**
	 * Reads the kingdom's approval and coffer. The live varbits are used while the player is in the kingdom
	 * (or both are non-zero, as the Kingdom of Miscellania plugin does); otherwise the values that plugin
	 * stored on the last visit.
	 *
	 * @param client        the client
	 * @param configManager source of the Kingdom of Miscellania plugin's stored values, may be {@code null}
	 * @return the kingdom state, or {@code null} before Throne of Miscellania or when nothing is known
	 */
	static MiscellaniaData readMiscellania(Client client, ConfigManager configManager)
	{
		if (client.getVarpValue(VarPlayerID.MISC_QUEST) <= 0)
			return null;

		int coffer = client.getVarbitValue(VarbitID.MISC_COFFERS);
		int approval = client.getVarbitValue(VarbitID.MISC_APPROVAL);
		Instant lastChanged = null;
		boolean live = inKingdom(client) || coffer > 0 && approval > 0;
		if (configManager != null)
		{
			Integer storedCoffer = configManager.getRSProfileConfiguration(KINGDOM_GROUP, "coffer", int.class);
			Integer storedApproval = configManager.getRSProfileConfiguration(KINGDOM_GROUP, "approval", int.class);
			Instant storedChanged = configManager.getRSProfileConfiguration(KINGDOM_GROUP, "lastChanged",
				Instant.class);
			boolean storedMatches = storedCoffer != null && storedApproval != null && storedCoffer == coffer
				&& storedApproval == approval;
			if (!live && storedCoffer != null && storedApproval != null)
			{
				coffer = storedCoffer;
				approval = storedApproval;
				lastChanged = storedChanged;
			}
			else if (live && storedMatches)
			{
				lastChanged = storedChanged;
			}
		}

		if (!live && lastChanged == null && coffer == 0 && approval == 0)
			return null;

		return new MiscellaniaData(approval * 100 / MAX_APPROVAL, coffer,
			lastChanged != null ? lastChanged.toString() : null);
	}

	/**
	 * Checks whether the player stands in Miscellania or Etceteria.
	 *
	 * @param client the client
	 * @return whether the player is in the kingdom
	 */
	private static boolean inKingdom(Client client)
	{
		Player local = client.getLocalPlayer();
		WorldPoint point = local != null ? local.getWorldLocation() : null;
		if (point == null)
			return false;

		for (int region : KINGDOM_REGIONS)
		{
			if (point.getRegionID() == region)
				return true;
		}

		return false;
	}
}
