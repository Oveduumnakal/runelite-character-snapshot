/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.TransportData;

import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;

/**
 * Reads transport unlocks the game keeps in varbits: fairy ring access, travel-log entries, favourites and
 * last destination, and quetzal landing sites. Transports gated only by quests or diaries are left to those
 * sections. Every read runs on the client thread.
 */
final class Transport
{
	/** Fairy ring code to the varbit that is non-zero once the code is in the travel log. */
	static final Map<String, Integer> FAIRY_RING_LOG = new LinkedHashMap<>();

	/** Quetzal landing site name to the varbit that is non-zero once the site is available. */
	static final Map<String, Integer> QUETZAL_SITES = new LinkedHashMap<>();

	/** Letters of the three fairy ring dials, indexed by the dial varbit value. */
	static final String[] FAIRY_RING_DIALS = {"ADCB", "ILKJ", "PSRQ"};

	/** Per favourite slot: the three dial varbits followed by the set-flag varbit, in slot order. */
	static final int[][] FAIRY_RING_FAVOURITES = {
		{VarbitID.FAIRYRING_FAVE_1_1, VarbitID.FAIRYRING_FAVE_1_2, VarbitID.FAIRYRING_FAVE_1_3,
			VarbitID.FAIRYRING_FAVE_SET_1},
		{VarbitID.FAIRYRING_FAVE_2_1, VarbitID.FAIRYRING_FAVE_2_2, VarbitID.FAIRYRING_FAVE_2_3,
			VarbitID.FAIRYRING_FAVE_SET_2},
		{VarbitID.FAIRYRING_FAVE_3_1, VarbitID.FAIRYRING_FAVE_3_2, VarbitID.FAIRYRING_FAVE_3_3,
			VarbitID.FAIRYRING_FAVE_SET_3},
		{VarbitID.FAIRYRING_FAVE_4_1, VarbitID.FAIRYRING_FAVE_4_2, VarbitID.FAIRYRING_FAVE_4_3,
			VarbitID.FAIRYRING_FAVE_SET_4},
		{VarbitID.FAIRYRING_FAVE_5_1, VarbitID.FAIRYRING_FAVE_5_2, VarbitID.FAIRYRING_FAVE_5_3,
			VarbitID.FAIRYRING_FAVE_SET_5},
		{VarbitID.FAIRYRING_FAVE_6_1, VarbitID.FAIRYRING_FAVE_6_2, VarbitID.FAIRYRING_FAVE_6_3,
			VarbitID.FAIRYRING_FAVE_SET_6},
		{VarbitID.FAIRYRING_FAVE_7_1, VarbitID.FAIRYRING_FAVE_7_2, VarbitID.FAIRYRING_FAVE_7_3,
			VarbitID.FAIRYRING_FAVE_SET_7},
		{VarbitID.FAIRYRING_FAVE_8_1, VarbitID.FAIRYRING_FAVE_8_2, VarbitID.FAIRYRING_FAVE_8_3,
			VarbitID.FAIRYRING_FAVE_SET_8},
		{VarbitID.FAIRYRING_FAVE_9_1, VarbitID.FAIRYRING_FAVE_9_2, VarbitID.FAIRYRING_FAVE_9_3,
			VarbitID.FAIRYRING_FAVE_SET_9},
		{VarbitID.FAIRYRING_FAVE_10_1, VarbitID.FAIRYRING_FAVE_10_2, VarbitID.FAIRYRING_FAVE_10_3,
			VarbitID.FAIRYRING_FAVE_SET_10},
	};

	static
	{
		FAIRY_RING_LOG.put("AIQ", VarbitID.FAIRYRINGS_LOG_AIQ);
		FAIRY_RING_LOG.put("AIR", VarbitID.FAIRYRINGS_LOG_AIR);
		FAIRY_RING_LOG.put("AIS", VarbitID.FAIRYRINGS_LOG_AIS);
		FAIRY_RING_LOG.put("AJP", VarbitID.FAIRYRINGS_LOG_AJP);
		FAIRY_RING_LOG.put("AJQ", VarbitID.FAIRYRINGS_LOG_AJQ);
		FAIRY_RING_LOG.put("AJR", VarbitID.FAIRYRINGS_LOG_AJR);
		FAIRY_RING_LOG.put("AJS", VarbitID.FAIRYRINGS_LOG_AJS);
		FAIRY_RING_LOG.put("AKP", VarbitID.FAIRYRINGS_LOG_AKP);
		FAIRY_RING_LOG.put("AKQ", VarbitID.FAIRYRINGS_LOG_AKQ);
		FAIRY_RING_LOG.put("AKR", VarbitID.FAIRYRINGS_LOG_AKR);
		FAIRY_RING_LOG.put("AKS", VarbitID.FAIRYRINGS_LOG_AKS);
		FAIRY_RING_LOG.put("ALP", VarbitID.FAIRYRINGS_LOG_ALP);
		FAIRY_RING_LOG.put("ALQ", VarbitID.FAIRYRINGS_LOG_ALQ);
		FAIRY_RING_LOG.put("ALR", VarbitID.FAIRYRINGS_LOG_ALR);
		FAIRY_RING_LOG.put("ALS", VarbitID.FAIRYRINGS_LOG_ALS);
		FAIRY_RING_LOG.put("BIP", VarbitID.FAIRYRINGS_LOG_BIP);
		FAIRY_RING_LOG.put("BIQ", VarbitID.FAIRYRINGS_LOG_BIQ);
		FAIRY_RING_LOG.put("BIS", VarbitID.FAIRYRINGS_LOG_BIS);
		FAIRY_RING_LOG.put("BJP", VarbitID.FAIRYRINGS_LOG_BJP);
		FAIRY_RING_LOG.put("BJR", VarbitID.FAIRYRINGS_LOG_BJR);
		FAIRY_RING_LOG.put("BJS", VarbitID.FAIRYRINGS_LOG_BJS);
		FAIRY_RING_LOG.put("BKP", VarbitID.FAIRYRINGS_LOG_BKP);
		FAIRY_RING_LOG.put("BKQ", VarbitID.FAIRYRINGS_LOG_BKQ);
		FAIRY_RING_LOG.put("BKR", VarbitID.FAIRYRINGS_LOG_BKR);
		FAIRY_RING_LOG.put("BKS", VarbitID.FAIRYRINGS_LOG_BKS);
		FAIRY_RING_LOG.put("BLP", VarbitID.FAIRYRINGS_LOG_BLP);
		FAIRY_RING_LOG.put("BLQ", VarbitID.FAIRYRINGS_LOG_BLQ);
		FAIRY_RING_LOG.put("BLR", VarbitID.FAIRYRINGS_LOG_BLR);
		FAIRY_RING_LOG.put("BLS", VarbitID.FAIRYRINGS_LOG_BLS);
		FAIRY_RING_LOG.put("CIP", VarbitID.FAIRYRINGS_LOG_CIP);
		FAIRY_RING_LOG.put("CIQ", VarbitID.FAIRYRINGS_LOG_CIQ);
		FAIRY_RING_LOG.put("CIR", VarbitID.FAIRYRINGS_LOG_CIR);
		FAIRY_RING_LOG.put("CIS", VarbitID.FAIRYRINGS_LOG_CIS);
		FAIRY_RING_LOG.put("CJQ", VarbitID.FAIRYRINGS_LOG_CJQ);
		FAIRY_RING_LOG.put("CJR", VarbitID.FAIRYRINGS_LOG_CJR);
		FAIRY_RING_LOG.put("CKP", VarbitID.FAIRYRINGS_LOG_CKP);
		FAIRY_RING_LOG.put("CKQ", VarbitID.FAIRYRINGS_LOG_CKQ);
		FAIRY_RING_LOG.put("CKR", VarbitID.FAIRYRINGS_LOG_CKR);
		FAIRY_RING_LOG.put("CKS", VarbitID.FAIRYRINGS_LOG_CKS);
		FAIRY_RING_LOG.put("CLP", VarbitID.FAIRYRINGS_LOG_CLP);
		FAIRY_RING_LOG.put("CLR", VarbitID.FAIRYRINGS_LOG_CLR);
		FAIRY_RING_LOG.put("CLS", VarbitID.FAIRYRINGS_LOG_CLS);
		FAIRY_RING_LOG.put("DIP", VarbitID.FAIRYRINGS_LOG_DIP);
		FAIRY_RING_LOG.put("DIQ", VarbitID.FAIRYRINGS_LOG_DIQ);
		FAIRY_RING_LOG.put("DIR", VarbitID.FAIRYRINGS_LOG_DIR);
		FAIRY_RING_LOG.put("DIS", VarbitID.FAIRYRINGS_LOG_DIS);
		FAIRY_RING_LOG.put("DJP", VarbitID.FAIRYRINGS_LOG_DJP);
		FAIRY_RING_LOG.put("DJR", VarbitID.FAIRYRINGS_LOG_DJR);
		FAIRY_RING_LOG.put("DKP", VarbitID.FAIRYRINGS_LOG_DKP);
		FAIRY_RING_LOG.put("DKR", VarbitID.FAIRYRINGS_LOG_DKR);
		FAIRY_RING_LOG.put("DKS", VarbitID.FAIRYRINGS_LOG_DKS);
		FAIRY_RING_LOG.put("DLP", VarbitID.FAIRYRINGS_LOG_DLP);
		FAIRY_RING_LOG.put("DLQ", VarbitID.FAIRYRINGS_LOG_DLQ);
		FAIRY_RING_LOG.put("DLR", VarbitID.FAIRYRINGS_LOG_DLR);
		FAIRY_RING_LOG.put("DLS", VarbitID.FAIRYRINGS_LOG_DLS);
		QUETZAL_SITES.put("Civitas illa Fortis", VarbitID.QUETZAL_FORTIS);
		QUETZAL_SITES.put("The Teomat", VarbitID.QUETZAL_TEOMAT);
		QUETZAL_SITES.put("Sunset Coast", VarbitID.QUETZAL_SUNSETCOAST);
		QUETZAL_SITES.put("Hunter Guild", VarbitID.QUETZAL_HUNTERGUILD);
		QUETZAL_SITES.put("Cam Torum", VarbitID.QUETZAL_CAMTORUM);
		QUETZAL_SITES.put("Colossal Wyrm Remains", VarbitID.QUETZAL_COLOSSALWYRM);
		QUETZAL_SITES.put("Outer Fortis", VarbitID.QUETZAL_OUTERFORTIS);
		QUETZAL_SITES.put("Fortis Colosseum", VarbitID.QUETZAL_COLOSSEUM);
		QUETZAL_SITES.put("Aldarin", VarbitID.QUETZAL_ALDARIN);
		QUETZAL_SITES.put("Quetzacalli Gorge", VarbitID.QUETZAL_QUETZACALLIGORGE);
		QUETZAL_SITES.put("Salvager Overlook", VarbitID.QUETZAL_SALVAGEROVERLOOK);
		QUETZAL_SITES.put("Tal Teklan", VarbitID.QUETZAL_TALTEKLAN);
		QUETZAL_SITES.put("Auburn Valley", VarbitID.QUETZAL_AUBURNVALLEY);
		QUETZAL_SITES.put("Kastori", VarbitID.QUETZAL_KASTORI);
	}

	private Transport()
	{
	}

	/**
	 * Reads the transport unlocks.
	 *
	 * @param client the client
	 * @return the transport state
	 */
	static TransportData read(Client client)
	{
		boolean fairyRings = client.getVarbitValue(VarbitID.FAIRYRING_PERMISSION) > 0;
		return new TransportData(fairyRings, set(client, FAIRY_RING_LOG), favourites(client),
			lastDestination(client), set(client, QUETZAL_SITES));
	}

	/**
	 * Decodes the last fairy ring code travelled to. The game packs it into six bits the same way as a
	 * favourite slot: dial 1 in bits 4-5, dial 2 in bits 2-3, dial 3 in bits 0-1.
	 *
	 * @param client the client
	 * @return the code, or null when no destination is recorded
	 */
	private static String lastDestination(Client client)
	{
		if (client.getVarbitValue(VarbitID.FAIRYRING_LASTLOC_SET) == 0)
			return null;

		int packed = client.getVarbitValue(VarbitID.FAIRYRING_LASTLOC);
		return code((packed >> 4) & 3, (packed >> 2) & 3, packed & 3);
	}

	/**
	 * Turns three dial values into a fairy ring code.
	 *
	 * @param dials the value of each dial, in dial order
	 * @return the code, or null when a dial holds a value outside its four letters
	 */
	private static String code(int... dials)
	{
		StringBuilder code = new StringBuilder();
		for (int dial = 0; dial < FAIRY_RING_DIALS.length; dial++)
		{
			int value = dials[dial];
			if (value < 0 || value >= FAIRY_RING_DIALS[dial].length())
				return null;

			code.append(FAIRY_RING_DIALS[dial].charAt(value));
		}

		return code.toString();
	}

	/**
	 * Decodes the fairy ring codes saved as favourites. A slot is skipped when its set flag is zero or a dial
	 * holds a value outside the four letters.
	 *
	 * @param client the client
	 * @return the saved codes, in slot order
	 */
	private static List<String> favourites(Client client)
	{
		List<String> codes = new ArrayList<>();
		for (int[] slot : FAIRY_RING_FAVOURITES)
		{
			if (client.getVarbitValue(slot[3]) == 0)
				continue;

			String code = code(client.getVarbitValue(slot[0]), client.getVarbitValue(slot[1]),
				client.getVarbitValue(slot[2]));
			if (code != null)
				codes.add(code);
		}

		return codes;
	}

	/**
	 * Lists the keys of a table whose varbit is non-zero.
	 *
	 * @param client the client
	 * @param table  key to varbit
	 * @return the keys set, in table order
	 */
	private static List<String> set(Client client, Map<String, Integer> table)
	{
		List<String> keys = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : table.entrySet())
		{
			if (client.getVarbitValue(entry.getValue()) > 0)
				keys.add(entry.getKey());
		}

		return keys;
	}
}
