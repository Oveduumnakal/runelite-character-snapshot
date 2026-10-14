/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.ArrayList;
import java.util.List;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.HouseData;

import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;

/**
 * Reads the player-owned house fields the game keeps in varbits. Room contents (portals, pool, altar) are
 * not in varbits and are not read. Values are the game's raw varbit values; see {@code SCHEMA.md}. Every
 * read runs on the client thread.
 */
final class House
{
	/** Portal nexus teleport slot varbits; slot {@code n} is element {@code n - 1}. */
	static final int[] NEXUS_TELEPORTS = {
		VarbitID.POH_NEXUS_TELE_1, VarbitID.POH_NEXUS_TELE_2, VarbitID.POH_NEXUS_TELE_3, VarbitID.POH_NEXUS_TELE_4,
		VarbitID.POH_NEXUS_TELE_5, VarbitID.POH_NEXUS_TELE_6, VarbitID.POH_NEXUS_TELE_7, VarbitID.POH_NEXUS_TELE_8,
		VarbitID.POH_NEXUS_TELE_9, VarbitID.POH_NEXUS_TELE_10, VarbitID.POH_NEXUS_TELE_11,
		VarbitID.POH_NEXUS_TELE_12, VarbitID.POH_NEXUS_TELE_13, VarbitID.POH_NEXUS_TELE_14,
		VarbitID.POH_NEXUS_TELE_15, VarbitID.POH_NEXUS_TELE_16, VarbitID.POH_NEXUS_TELE_17,
		VarbitID.POH_NEXUS_TELE_18, VarbitID.POH_NEXUS_TELE_19, VarbitID.POH_NEXUS_TELE_20,
		VarbitID.POH_NEXUS_TELE_21, VarbitID.POH_NEXUS_TELE_22, VarbitID.POH_NEXUS_TELE_23,
		VarbitID.POH_NEXUS_TELE_24, VarbitID.POH_NEXUS_TELE_25, VarbitID.POH_NEXUS_TELE_26,
		VarbitID.POH_NEXUS_TELE_27, VarbitID.POH_NEXUS_TELE_28, VarbitID.POH_NEXUS_TELE_29,
		VarbitID.POH_NEXUS_TELE_30, VarbitID.POH_NEXUS_TELE_31, VarbitID.POH_NEXUS_TELE_32,
		VarbitID.POH_NEXUS_TELE_33, VarbitID.POH_NEXUS_TELE_34, VarbitID.POH_NEXUS_TELE_35,
		VarbitID.POH_NEXUS_TELE_36, VarbitID.POH_NEXUS_TELE_37, VarbitID.POH_NEXUS_TELE_38,
		VarbitID.POH_NEXUS_TELE_39, VarbitID.POH_NEXUS_TELE_40, VarbitID.POH_NEXUS_TELE_41,
		VarbitID.POH_NEXUS_TELE_42, VarbitID.POH_NEXUS_TELE_43, VarbitID.POH_NEXUS_TELE_44,
		VarbitID.POH_NEXUS_TELE_45
	};

	private House()
	{
	}

	/**
	 * Reads the house.
	 *
	 * @param client the client
	 * @return the house state, or {@code null} when the player owns no house
	 */
	static HouseData read(Client client)
	{
		int location = client.getVarbitValue(VarbitID.POH_HOUSE_LOCATION);
		if (location <= 0)
			return null;

		List<Integer> nexus = new ArrayList<>();
		for (int slot = 1; slot <= NEXUS_TELEPORTS.length; slot++)
		{
			if (client.getVarbitValue(NEXUS_TELEPORTS[slot - 1]) > 0)
				nexus.add(slot);
		}

		return new HouseData(
			location,
			client.getVarbitValue(VarbitID.POH_HOUSE_STYLE),
			client.getVarbitValue(VarbitID.POH_HOUSE_SIZE),
			client.getVarbitValue(VarbitID.POH_SERVANT_TYPE),
			client.getVarbitValue(VarbitID.POH_JEWELLERYBOX_MULTI),
			nexus);
	}
}
