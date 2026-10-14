/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.runelite.api.gameval.VarPlayerID;

/**
 * The game's per-boss kill-count varps and the export key each one is stored under. Keys follow the OSRS
 * hiscores / Chat Commands boss names, lower-cased, so a chat-sourced count for the same boss lands on the
 * same key. Skipped on purpose because their meaning could not be verified: {@code TOTAL_CATA_BOSS_KILLS},
 * {@code TOTAL_GRYPHON_BOSS_KILLS}, {@code TOTAL_MAGGOT_KING_KILLS}, {@code TOTAL_MAD_ANGEL_KILLS}; and
 * {@code SOUL_WARS_TOTAL_KILLS}, which is not a boss.
 */
final class KillCountVarps
{
	/** Varp id to export key, in varp order. */
	static final Map<Integer, String> BOSSES;

	static
	{
		Map<Integer, String> map = new LinkedHashMap<>();
		map.put(VarPlayerID.TOTAL_ARMADYL_KILLS, "kree'arra");
		map.put(VarPlayerID.TOTAL_BANDOS_KILLS, "general graardor");
		map.put(VarPlayerID.TOTAL_SARADOMIN_KILLS, "commander zilyana");
		map.put(VarPlayerID.TOTAL_ZAMORAK_KILLS, "k'ril tsutsaroth");
		map.put(VarPlayerID.TOTAL_PRIME_KILLS, "dagannoth prime");
		map.put(VarPlayerID.TOTAL_REX_KILLS, "dagannoth rex");
		map.put(VarPlayerID.TOTAL_SUPREME_KILLS, "dagannoth supreme");
		map.put(VarPlayerID.TOTAL_CALLISTO_KILLS, "callisto");
		map.put(VarPlayerID.TOTAL_VENENATIS_KILLS, "venenatis");
		map.put(VarPlayerID.TOTAL_VETION_KILLS, "vet'ion");
		map.put(VarPlayerID.TOTAL_CHAOSELE_KILLS, "chaos elemental");
		map.put(VarPlayerID.TOTAL_KBD_KILLS, "king black dragon");
		map.put(VarPlayerID.TOTAL_MOLE_KILLS, "giant mole");
		map.put(VarPlayerID.TOTAL_KALPHITE_KILLS, "kalphite queen");
		map.put(VarPlayerID.TOTAL_CORP_KILLS, "corporeal beast");
		map.put(VarPlayerID.TOTAL_SNAKEBOSS_KILLS, "zulrah");
		map.put(VarPlayerID.TOTAL_CHAOSFANATIC_KILLS, "chaos fanatic");
		map.put(VarPlayerID.TOTAL_SCORPIA_KILLS, "scorpia");
		map.put(VarPlayerID.TOTAL_CRAZYARCHAEOLOGIST_KILLS, "crazy archaeologist");
		map.put(VarPlayerID.TOTAL_JAD_KILLS, "tztok-jad");
		map.put(VarPlayerID.TOTAL_KRAKEN_BOSS_KILLS, "kraken");
		map.put(VarPlayerID.TOTAL_THERMY_KILLS, "thermonuclear smoke devil");
		map.put(VarPlayerID.TOTAL_CERBERUS_KILLS, "cerberus");
		map.put(VarPlayerID.TOTAL_ABYSSALSIRE_KILLS, "abyssal sire");
		map.put(VarPlayerID.TOTAL_WINTERTODT_KILLS, "wintertodt");
		map.put(VarPlayerID.TOTAL_HILLGIANT_BOSS_KILLS, "obor");
		map.put(VarPlayerID.TOTAL_ZUK_KILLS, "tzkal-zuk");
		map.put(VarPlayerID.TOTAL_DERANGEDARCHAEOLOGIST_KILLS, "deranged archaeologist");
		map.put(VarPlayerID.TOTAL_GARGBOSS_KILLS, "grotesque guardians");
		map.put(VarPlayerID.TOTAL_VORKATH_KILLS, "vorkath");
		map.put(VarPlayerID.TOTAL_BRYOPHYTA_KILLS, "bryophyta");
		map.put(VarPlayerID.TOTAL_HYDRABOSS_KILLS, "alchemical hydra");
		map.put(VarPlayerID.TOTAL_HESPORI_KILLS, "hespori");
		map.put(VarPlayerID.TOTAL_MIMIC_KILLS, "mimic");
		map.put(VarPlayerID.TOTAL_SARACHNIS_KILLS, "sarachnis");
		map.put(VarPlayerID.TOTAL_ZALCANO_KILLS, "zalcano");
		map.put(VarPlayerID.TOTAL_NIGHTMARE_KILLS, "nightmare");
		map.put(VarPlayerID.TOTAL_NIGHTMARE_CHALLENGE_KILLS, "phosani's nightmare");
		map.put(VarPlayerID.TOTAL_TEMPOROSS_KILLS, "tempoross");
		map.put(VarPlayerID.TOTAL_NEX_KILLS, "nex");
		map.put(VarPlayerID.TOTAL_GOTR_KILLS, "guardians of the rift");
		map.put(VarPlayerID.TOTAL_MUSPAH_KILLS, "phantom muspah");
		map.put(VarPlayerID.TOTAL_ARTIO_KILLS, "artio");
		map.put(VarPlayerID.TOTAL_SPINDEL_KILLS, "spindel");
		map.put(VarPlayerID.TOTAL_CALVARION_KILLS, "calvar'ion");
		map.put(VarPlayerID.TOTAL_DUKE_SUCELLUS_KILLS, "duke sucellus");
		map.put(VarPlayerID.TOTAL_LEVIATHAN_KILLS, "leviathan");
		map.put(VarPlayerID.TOTAL_WHISPERER_KILLS, "whisperer");
		map.put(VarPlayerID.TOTAL_VARDORVIS_KILLS, "vardorvis");
		map.put(VarPlayerID.TOTAL_VARDORVIS_AWAKENED_KILLS, "vardorvis (awakened)");
		map.put(VarPlayerID.TOTAL_WHISPERER_AWAKENED_KILLS, "whisperer (awakened)");
		map.put(VarPlayerID.TOTAL_LEVIATHAN_AWAKENED_KILLS, "leviathan (awakened)");
		map.put(VarPlayerID.TOTAL_DUKE_SUCELLUS_AWAKENED_KILLS, "duke sucellus (awakened)");
		map.put(VarPlayerID.TOTAL_RAT_BOSS_KILLS, "scurrius");
		map.put(VarPlayerID.TOTAL_SOL_KILLS, "sol heredit");
		map.put(VarPlayerID.TOTAL_ARAXXOR_KILLS, "araxxor");
		map.put(VarPlayerID.TOTAL_AMOXLIATL_KILLS, "amoxliatl");
		map.put(VarPlayerID.TOTAL_HUEY_KILLS, "hueycoatl");
		map.put(VarPlayerID.TOTAL_ROYAL_TITAN_KILLS, "the royal titans");
		map.put(VarPlayerID.TOTAL_YAMA_KILLS, "yama");
		map.put(VarPlayerID.TOTAL_GEMSTONE_CRAB_KILLS, "gemstone crab");
		map.put(VarPlayerID.TOTAL_COWBOSS_KILLS, "brutus");
		map.put(VarPlayerID.TOTAL_COWBOSS_HARDMODE_KILLS, "brutus (hard mode)");
		BOSSES = Collections.unmodifiableMap(map);
	}

	private KillCountVarps()
	{
	}
}
