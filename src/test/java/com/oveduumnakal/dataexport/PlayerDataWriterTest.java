/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.dataexport;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.google.gson.Gson;
import com.oveduumnakal.dataexport.model.PlayerSyncData;
import com.oveduumnakal.dataexport.model.PlayerSyncData.ItemEntry;
import com.oveduumnakal.dataexport.model.PlayerSyncData.PlayerInfo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the JSON writer: filename sanitization, the blank-name guard, and that the atomic write
 * leaves a valid, UTF-8 file and no temporary leftovers.
 */
public class PlayerDataWriterTest
{
	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	/**
	 * Lower-cases the name and replaces every character outside {@code [a-z0-9_-]}.
	 */
	@Test
	public void filenameForSanitizes()
	{
		assertEquals("player_1.json", PlayerDataWriter.filenameFor("Player 1"));
		assertEquals("zezima-1_2.json", PlayerDataWriter.filenameFor("Zezima-1_2"));
		assertEquals("a__b.json", PlayerDataWriter.filenameFor("A/\\b"));
	}

	/**
	 * A null or blank name yields no filename, so the writer skips it.
	 */
	@Test
	public void filenameForRejectsBlank()
	{
		assertNull(PlayerDataWriter.filenameFor(null));
		assertNull(PlayerDataWriter.filenameFor(""));
		assertNull(PlayerDataWriter.filenameFor("   "));
	}

	/**
	 * Writing creates the directory, produces parseable JSON, and leaves no {@code .tmp} file.
	 */
	@Test
	public void writeProducesValidJsonAndNoTempLeftover() throws Exception
	{
		File dir = new File(tmp.getRoot(), "nested/export");
		PlayerDataWriter writer = new PlayerDataWriter(new Gson());

		assertTrue(writer.write(sample("Zezima", "Dragon claws"), dir));

		File file = new File(dir, "zezima.json");
		assertTrue(file.exists());

		PlayerSyncData parsed = read(file);
		assertEquals(2, parsed.schemaVersion);
		assertEquals("Zezima", parsed.player.username);
		assertEquals("Dragon claws", parsed.bank.items.get(0).name);

		File[] leftovers = dir.listFiles((d, name) -> name.endsWith(".tmp"));
		assertTrue(leftovers != null && leftovers.length == 0);
	}

	/**
	 * A second write replaces the first atomically.
	 */
	@Test
	public void writeOverwritesPreviousFile() throws Exception
	{
		File dir = tmp.newFolder("export");
		PlayerDataWriter writer = new PlayerDataWriter(new Gson());

		writer.write(sample("Zezima", "Old item"), dir);
		writer.write(sample("Zezima", "New item"), dir);

		PlayerSyncData parsed = read(new File(dir, "zezima.json"));
		assertEquals("New item", parsed.bank.items.get(0).name);
	}

	/**
	 * Non-ASCII item names round-trip through the UTF-8 write.
	 */
	@Test
	public void writePreservesUnicode() throws Exception
	{
		File dir = tmp.newFolder("export");
		PlayerDataWriter writer = new PlayerDataWriter(new Gson());

		writer.write(sample("Zezima", "Clanë"), dir);

		PlayerSyncData parsed = read(new File(dir, "zezima.json"));
		assertEquals("Clanë", parsed.bank.items.get(0).name);
	}

	/**
	 * A snapshot with no player is not written.
	 */
	@Test
	public void writeSkipsMissingPlayer() throws Exception
	{
		File dir = tmp.newFolder("export");
		PlayerDataWriter writer = new PlayerDataWriter(new Gson());

		assertTrue(!writer.write(new PlayerSyncData(), dir));
		assertTrue(!writer.write(null, dir));
	}

	/**
	 * Builds a minimal snapshot with one banked item.
	 *
	 * @param name     the player name
	 * @param itemName the single banked item's name
	 * @return the snapshot
	 */
	private static PlayerSyncData sample(String name, String itemName)
	{
		PlayerSyncData data = new PlayerSyncData();
		data.player = new PlayerInfo(name, 126, 302, "NORMAL", true);
		data.bank = new PlayerSyncData.BankData(
			1,
			java.util.Collections.singletonList(new ItemEntry(1, itemName, 1)));
		return data;
	}

	/**
	 * Reads and parses a written JSON file.
	 *
	 * @param file the file to read
	 * @return the parsed snapshot
	 * @throws Exception on I/O failure
	 */
	private static PlayerSyncData read(File file) throws Exception
	{
		String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		return new Gson().fromJson(json, PlayerSyncData.class);
	}
}
