/*
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.LinkedHashMap;
import java.util.Map;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BottomlessBucket;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.ToolLeprechaunTotals;

import net.runelite.api.Client;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;

/**
 * Tables for the skilling storages the game keeps in varbits rather than item containers: the plank sack and
 * the tool leprechaun. Every read runs on the client thread.
 */
final class SkillingStorage
{
	/** Plank item id to the varbit holding how many of that plank the plank sack holds. */
	static final Map<Integer, Integer> PLANK_SACK = new LinkedHashMap<>();

	/**
	 * Tool leprechaun storage key to its varbit. Values are the raw varbit values: 0/1 for single tools, a type
	 * index for the watering can and bottomless bucket, and counts for buckets, compost and plant cure, where
	 * the {@code extra*} varbits hold the higher part of counts above the base varbit's range.
	 */
	static final Map<String, Integer> TOOL_LEPRECHAUN = new LinkedHashMap<>();

	static
	{
		PLANK_SACK.put(ItemID.WOODPLANK, VarbitID.PLANK_SACK_PLAIN);
		PLANK_SACK.put(ItemID.PLANK_OAK, VarbitID.PLANK_SACK_OAK);
		PLANK_SACK.put(ItemID.PLANK_TEAK, VarbitID.PLANK_SACK_TEAK);
		PLANK_SACK.put(ItemID.PLANK_MAHOGANY, VarbitID.PLANK_SACK_MAHOGANY);
		PLANK_SACK.put(ItemID.PLANK_CAMPHOR, VarbitID.PLANK_SACK_CAMPHOR);
		PLANK_SACK.put(ItemID.PLANK_IRONWOOD, VarbitID.PLANK_SACK_IRONWOOD);
		PLANK_SACK.put(ItemID.PLANK_ROSEWOOD, VarbitID.PLANK_SACK_ROSEWOOD);

		TOOL_LEPRECHAUN.put("rake", VarbitID.FARMING_TOOLS_RAKE);
		TOOL_LEPRECHAUN.put("extraRakes", VarbitID.FARMING_TOOLS_EXTRARAKES);
		TOOL_LEPRECHAUN.put("seedDibber", VarbitID.FARMING_TOOLS_DIBBER);
		TOOL_LEPRECHAUN.put("extraSeedDibbers", VarbitID.FARMING_TOOLS_EXTRADIBBERS);
		TOOL_LEPRECHAUN.put("spade", VarbitID.FARMING_TOOLS_SPADE);
		TOOL_LEPRECHAUN.put("extraSpades", VarbitID.FARMING_TOOLS_EXTRASPADES);
		TOOL_LEPRECHAUN.put("secateurs", VarbitID.FARMING_TOOLS_SECATEURS);
		TOOL_LEPRECHAUN.put("magicSecateurs", VarbitID.FARMING_TOOLS_FAIRYSECATEURS);
		TOOL_LEPRECHAUN.put("extraSecateurs", VarbitID.FARMING_TOOLS_EXTRASECATEURS);
		TOOL_LEPRECHAUN.put("wateringCan", VarbitID.FARMING_TOOLS_WATERINGCAN);
		TOOL_LEPRECHAUN.put("trowel", VarbitID.FARMING_TOOLS_TROWEL);
		TOOL_LEPRECHAUN.put("extraTrowels", VarbitID.FARMING_TOOLS_EXTRATROWELS);
		TOOL_LEPRECHAUN.put("buckets", VarbitID.FARMING_TOOLS_BUCKETS);
		TOOL_LEPRECHAUN.put("extraBuckets", VarbitID.FARMING_TOOLS_EXTRABUCKETS);
		TOOL_LEPRECHAUN.put("extra2Buckets", VarbitID.FARMING_TOOLS_EXTRA2BUCKETS);
		TOOL_LEPRECHAUN.put("compost", VarbitID.FARMING_TOOLS_COMPOST);
		TOOL_LEPRECHAUN.put("extraCompost", VarbitID.FARMING_TOOLS_EXTRACOMPOST);
		TOOL_LEPRECHAUN.put("supercompost", VarbitID.FARMING_TOOLS_SUPERCOMPOST);
		TOOL_LEPRECHAUN.put("extraSupercompost", VarbitID.FARMING_TOOLS_EXTRASUPERCOMPOST);
		TOOL_LEPRECHAUN.put("ultracompost", VarbitID.FARMING_TOOLS_ULTRACOMPOST);
		TOOL_LEPRECHAUN.put("plantCure", VarbitID.FARMING_TOOLS_PLANTCURE);
		TOOL_LEPRECHAUN.put("bottomlessBucketType", VarbitID.FARMING_TOOLS_BOTTOMLESS_BUCKET_TYPE);
		TOOL_LEPRECHAUN.put("bottomlessBucketQuantity", VarbitID.FARMING_TOOLS_BOTTOMLESS_BUCKET_QUANTITY);
	}

	private SkillingStorage()
	{
	}

	/**
	 * Reads the tool leprechaun's storage.
	 *
	 * @param client the client
	 * @return storage key to raw varbit value, in table order, or {@code null} when everything is zero
	 */
	static Map<String, Integer> readToolLeprechaun(Client client)
	{
		Map<String, Integer> stored = new LinkedHashMap<>();
		boolean any = false;
		for (Map.Entry<String, Integer> entry : TOOL_LEPRECHAUN.entrySet())
		{
			int value = client.getVarbitValue(entry.getValue());
			stored.put(entry.getKey(), value);
			any |= value != 0;
		}

		return any ? stored : null;
	}

	/**
	 * Decodes the raw tool leprechaun values into the totals the Store screen shows. Rakes, dibbers, spades,
	 * secateurs and trowels are {@code base + 2 * extra}; buckets are {@code base + 32 * extra} (the
	 * {@code extra2Buckets} weight is unverified, so it is ignored); compost and supercompost are
	 * {@code base + 256 * extra}; the watering can holds {@code value - 1} charges.
	 *
	 * @param raw storage key to raw varbit value, as from {@link #readToolLeprechaun}
	 * @return the decoded totals
	 */
	static ToolLeprechaunTotals decodeToolTotals(Map<String, Integer> raw)
	{
		ToolLeprechaunTotals totals = new ToolLeprechaunTotals();
		totals.rakes = raw(raw, "rake") + 2 * raw(raw, "extraRakes");
		totals.seedDibbers = raw(raw, "seedDibber") + 2 * raw(raw, "extraSeedDibbers");
		totals.spades = raw(raw, "spade") + 2 * raw(raw, "extraSpades");
		totals.secateurs = raw(raw, "secateurs") + 2 * raw(raw, "extraSecateurs");
		totals.magicSecateurs = raw(raw, "magicSecateurs") != 0;
		totals.trowels = raw(raw, "trowel") + 2 * raw(raw, "extraTrowels");
		int can = raw(raw, "wateringCan");
		totals.wateringCanCharges = can > 0 ? can - 1 : null;
		totals.buckets = raw(raw, "buckets") + 32 * raw(raw, "extraBuckets");
		totals.compost = raw(raw, "compost") + 256 * raw(raw, "extraCompost");
		totals.supercompost = raw(raw, "supercompost") + 256 * raw(raw, "extraSupercompost");
		totals.ultracompost = raw(raw, "ultracompost");
		totals.plantCure = raw(raw, "plantCure");
		int type = raw(raw, "bottomlessBucketType");
		int uses = raw(raw, "bottomlessBucketQuantity");
		totals.bottomlessBucket = type != 0 || uses != 0 ? new BottomlessBucket(type, uses) : null;
		return totals;
	}

	private static int raw(Map<String, Integer> raw, String key)
	{
		return raw.getOrDefault(key, 0);
	}
}
