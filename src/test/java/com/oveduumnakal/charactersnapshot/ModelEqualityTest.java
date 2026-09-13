/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.BankData;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.CombatAchievementData;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.DiaryRegion;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.ItemEntry;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.QuestEntry;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.SlayerData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Verifies value equality on the polled model types. The collector marks a snapshot dirty by
 * comparing a fresh poll against the cached value, so these {@code equals} contracts must hold.
 */
public class ModelEqualityTest
{
	/**
	 * Diary regions compare by tier completion.
	 */
	@Test
	public void diaryRegionEquality()
	{
		assertEquals(new DiaryRegion(true, false, false, false), new DiaryRegion(true, false, false, false));
		assertNotEquals(new DiaryRegion(true, false, false, false), new DiaryRegion(false, false, false, false));
	}

	/**
	 * Combat-achievement data compares by tier values.
	 */
	@Test
	public void combatAchievementEquality()
	{
		assertEquals(new CombatAchievementData(1, 2, 3, 0, 0, 0), new CombatAchievementData(1, 2, 3, 0, 0, 0));
		assertNotEquals(new CombatAchievementData(1, 2, 3, 0, 0, 0), new CombatAchievementData(1, 2, 4, 0, 0, 0));
	}

	/**
	 * Slayer tasks compare by all fields.
	 */
	@Test
	public void slayerEquality()
	{
		assertEquals(new SlayerData(5, 40, 10, 3), new SlayerData(5, 40, 10, 3));
		assertNotEquals(new SlayerData(5, 40, 10, 3), new SlayerData(5, 39, 10, 3));
	}

	/**
	 * Quest entries compare by name, display name, and state.
	 */
	@Test
	public void questEntryEquality()
	{
		assertEquals(new QuestEntry("COOKS_ASSISTANT", "Cook's Assistant", "FINISHED"),
			new QuestEntry("COOKS_ASSISTANT", "Cook's Assistant", "FINISHED"));
		assertNotEquals(new QuestEntry("COOKS_ASSISTANT", "Cook's Assistant", "FINISHED"),
			new QuestEntry("COOKS_ASSISTANT", "Cook's Assistant", "IN_PROGRESS"));
	}

	/**
	 * Bank data exposes a single-tab compatibility view over the flat item list.
	 */
	@Test
	public void bankExposesSingleTabCompatView()
	{
		BankData bank = new BankData(1, java.util.Collections.singletonList(new ItemEntry(1, "Yew logs", 5)));
		assertEquals(1, bank.tabs.size());
		assertEquals(0, bank.tabs.get(0).tabIndex);
		ItemEntry first = bank.tabs.get(0).items.get(0);
		assertEquals("Yew logs", first.name);
	}

	/**
	 * Combat-achievement data derives compatibility booleans from the tier values.
	 */
	@Test
	public void combatAchievementCompatBooleans()
	{
		CombatAchievementData ca = new CombatAchievementData(2, 0, 0, 0, 0, 0);
		assertTrue(ca.easyComplete);
		assertTrue(!ca.mediumComplete);
		assertNotNull(ca.completedTasks);
	}
}
