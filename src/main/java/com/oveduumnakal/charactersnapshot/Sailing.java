/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BoatData;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.SailingData;

import net.runelite.api.Client;
import net.runelite.api.gameval.DBTableID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

/**
 * Reads Sailing state from its named game variables: owned boats and their parts, unlocked crew, charting
 * progress and Barracuda Trials performance. Boat type and port are also named from the game's database tables;
 * part tiers are raw game values. Every read runs on the client thread.
 */
final class Sailing
{
	private static final int[] BOAT_OWNED = {
		VarbitID.SAILING_BOAT_1_OWNED, VarbitID.SAILING_BOAT_2_OWNED, VarbitID.SAILING_BOAT_3_OWNED,
		VarbitID.SAILING_BOAT_4_OWNED, VarbitID.SAILING_BOAT_5_OWNED
	};

	private static final int[] BOAT_TYPE = {
		VarbitID.SAILING_BOAT_1_TYPE, VarbitID.SAILING_BOAT_2_TYPE, VarbitID.SAILING_BOAT_3_TYPE,
		VarbitID.SAILING_BOAT_4_TYPE, VarbitID.SAILING_BOAT_5_TYPE
	};

	private static final int[] BOAT_PORT = {
		VarbitID.SAILING_BOAT_1_PORT, VarbitID.SAILING_BOAT_2_PORT, VarbitID.SAILING_BOAT_3_PORT,
		VarbitID.SAILING_BOAT_4_PORT, VarbitID.SAILING_BOAT_5_PORT
	};

	private static final int[] BOAT_HULL = {
		VarbitID.SAILING_BOAT_1_HULL, VarbitID.SAILING_BOAT_2_HULL, VarbitID.SAILING_BOAT_3_HULL,
		VarbitID.SAILING_BOAT_4_HULL, VarbitID.SAILING_BOAT_5_HULL
	};

	private static final int[] BOAT_KEEL = {
		VarbitID.SAILING_BOAT_1_KEEL, VarbitID.SAILING_BOAT_2_KEEL, VarbitID.SAILING_BOAT_3_KEEL,
		VarbitID.SAILING_BOAT_4_KEEL, VarbitID.SAILING_BOAT_5_KEEL
	};

	private static final int[] BOAT_SAIL = {
		VarbitID.SAILING_BOAT_1_SAIL, VarbitID.SAILING_BOAT_2_SAIL, VarbitID.SAILING_BOAT_3_SAIL,
		VarbitID.SAILING_BOAT_4_SAIL, VarbitID.SAILING_BOAT_5_SAIL
	};

	private static final int[] BOAT_STEERING = {
		VarbitID.SAILING_BOAT_1_STEERING, VarbitID.SAILING_BOAT_2_STEERING, VarbitID.SAILING_BOAT_3_STEERING,
		VarbitID.SAILING_BOAT_4_STEERING, VarbitID.SAILING_BOAT_5_STEERING
	};

	private static final int[] BOAT_STORED_HP = {
		VarbitID.SAILING_BOAT_1_STORED_HP, VarbitID.SAILING_BOAT_2_STORED_HP, VarbitID.SAILING_BOAT_3_STORED_HP,
		VarbitID.SAILING_BOAT_4_STORED_HP, VarbitID.SAILING_BOAT_5_STORED_HP
	};

	private static final int[] BOAT_STORED_MAXHP = {
		VarbitID.SAILING_BOAT_1_STORED_MAXHP, VarbitID.SAILING_BOAT_2_STORED_MAXHP,
		VarbitID.SAILING_BOAT_3_STORED_MAXHP, VarbitID.SAILING_BOAT_4_STORED_MAXHP,
		VarbitID.SAILING_BOAT_5_STORED_MAXHP
	};

	/** Charting completion bitfield varps; each set bit is one completed charting task. */
	static final int[] CHARTING_COMPLETION = {
		VarPlayerID.SAILING_CHARTING_COMPLETION_0, VarPlayerID.SAILING_CHARTING_COMPLETION_1,
		VarPlayerID.SAILING_CHARTING_COMPLETION_2, VarPlayerID.SAILING_CHARTING_COMPLETION_3,
		VarPlayerID.SAILING_CHARTING_COMPLETION_4, VarPlayerID.SAILING_CHARTING_COMPLETION_5,
		VarPlayerID.SAILING_CHARTING_COMPLETION_6, VarPlayerID.SAILING_CHARTING_COMPLETION_7,
		VarPlayerID.SAILING_CHARTING_COMPLETION_8, VarPlayerID.SAILING_CHARTING_COMPLETION_9,
		VarPlayerID.SAILING_CHARTING_COMPLETION_10, VarPlayerID.SAILING_CHARTING_COMPLETION_11
	};

	/** Barracuda Trials performance varps, one per trial slot. */
	static final int[] TRIAL_PERFORMANCE = {
		VarPlayerID.SAILING_BT_PERFORMANCE_1, VarPlayerID.SAILING_BT_PERFORMANCE_2,
		VarPlayerID.SAILING_BT_PERFORMANCE_3, VarPlayerID.SAILING_BT_PERFORMANCE_4,
		VarPlayerID.SAILING_BT_PERFORMANCE_5
	};

	/** Crew key to the varbit that is non-zero once that crew member is unlocked. */
	static final Map<String, Integer> CREW = new LinkedHashMap<>();

	static
	{
		CREW.put("generic1", VarbitID.SAILING_CREW_GENERIC_1_UNLOCKED);
		CREW.put("generic2", VarbitID.SAILING_CREW_GENERIC_2_UNLOCKED);
		CREW.put("generic3", VarbitID.SAILING_CREW_GENERIC_3_UNLOCKED);
		CREW.put("generic4", VarbitID.SAILING_CREW_GENERIC_4_UNLOCKED);
		CREW.put("generic5", VarbitID.SAILING_CREW_GENERIC_5_UNLOCKED);
		CREW.put("werewolf", VarbitID.SAILING_CREW_WEREWOLF_UNLOCKED);
		CREW.put("fremennik", VarbitID.SAILING_CREW_FREMENNIK_UNLOCKED);
		CREW.put("spiritAngler", VarbitID.SAILING_CREW_SPIRIT_ANGLER_UNLOCKED);
		CREW.put("captainSiad", VarbitID.SAILING_CREW_CAPTAIN_SIAD_UNLOCKED);
		CREW.put("ghostJenkins", VarbitID.SAILING_CREW_GHOST_JENKINS_UNLOCKED);
	}

	private Sailing()
	{
	}

	/**
	 * Reads the Sailing state.
	 *
	 * @param client the client
	 * @return the Sailing state
	 */
	static SailingData read(Client client)
	{
		List<BoatData> boats = new ArrayList<>();
		for (int i = 0; i < BOAT_OWNED.length; i++)
		{
			if (client.getVarbitValue(BOAT_OWNED[i]) <= 0)
				continue;

			int type = value(client, BOAT_TYPE, i);
			int port = value(client, BOAT_PORT, i);
			String typeName = tableName(client, DBTableID.SailingBoat.ID, DBTableID.SailingBoat.COL_TYPE_ID,
				DBTableID.SailingBoat.COL_DISPLAYNAME, type);
			String portName = tableName(client, DBTableID.SailingDock.ID, DBTableID.SailingDock.COL_DOCK_ID,
				DBTableID.SailingDock.COL_NICE_NAME, port);
			boats.add(new BoatData(i + 1, type, typeName, port, portName, value(client, BOAT_HULL, i),
				value(client, BOAT_KEEL, i), value(client, BOAT_SAIL, i), value(client, BOAT_STEERING, i),
				value(client, BOAT_STORED_HP, i), value(client, BOAT_STORED_MAXHP, i)));
		}

		List<String> crew = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : CREW.entrySet())
		{
			if (client.getVarbitValue(entry.getValue()) > 0)
				crew.add(entry.getKey());
		}

		int charted = 0;
		for (int varp : CHARTING_COMPLETION)
			charted += Integer.bitCount(client.getVarpValue(varp));

		List<Integer> trials = new ArrayList<>();
		for (int varp : TRIAL_PERFORMANCE)
			trials.add(client.getVarpValue(varp));

		return new SailingData(boats, crew, charted, client.getVarpValue(VarPlayerID.SAILING_CHARTING_TOTAL_ENTRIES),
			trials);
	}

	/**
	 * Looks up a display name in a game database table by its id column. The name comes from the game cache at
	 * runtime, so ports and boat types added later are named without a plugin update.
	 *
	 * @param client     the client
	 * @param table      the database table id
	 * @param idColumn   the column holding the raw id the boat varbits store
	 * @param nameColumn the column holding the display name
	 * @param id         the raw id to look up
	 * @return the name, or null when the id has no row or the name is not text
	 */
	static String tableName(Client client, int table, int idColumn, int nameColumn, int id)
	{
		List<Integer> rows = client.getDBRowsByValue(table, idColumn, 0, id);
		if (rows == null || rows.isEmpty())
			return null;

		Object[] field = client.getDBTableField(rows.get(0), nameColumn, 0);
		if (field == null || field.length == 0 || !(field[0] instanceof String))
			return null;

		return (String) field[0];
	}

	/**
	 * Reads one boat slot's varbit from a per-slot table.
	 *
	 * @param client the client
	 * @param table  the varbit for each of the five boat slots
	 * @param slot   zero-based boat slot
	 * @return the raw value
	 */
	private static int value(Client client, int[] table, int slot)
	{
		return client.getVarbitValue(table[slot]);
	}
}
