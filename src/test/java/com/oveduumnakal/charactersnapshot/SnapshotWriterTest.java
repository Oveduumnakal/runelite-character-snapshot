/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.google.gson.Gson;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.ItemEntry;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.PlayerInfo;
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
public class SnapshotWriterTest
{
	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	/**
	 * Lower-cases the name and replaces every character outside {@code [a-z0-9_-]}.
	 */
	@Test
	public void filenameForSanitizes()
	{
		assertEquals("player_1.json", SnapshotWriter.filenameFor("Player 1"));
		assertEquals("zezima-1_2.json", SnapshotWriter.filenameFor("Zezima-1_2"));
		assertEquals("a__b.json", SnapshotWriter.filenameFor("A/\\b"));
	}

	/**
	 * A null or blank name yields no filename, so the writer skips it.
	 */
	@Test
	public void filenameForRejectsBlank()
	{
		assertNull(SnapshotWriter.filenameFor(null));
		assertNull(SnapshotWriter.filenameFor(""));
		assertNull(SnapshotWriter.filenameFor("   "));
	}

	/**
	 * Writing creates the directory, produces parseable JSON, and leaves no {@code .tmp} file.
	 */
	@Test
	public void writeProducesValidJsonAndNoTempLeftover() throws Exception
	{
		File dir = new File(tmp.getRoot(), "nested/export");
		SnapshotWriter writer = new SnapshotWriter(new Gson());

		assertTrue(writer.write(sample("Zezima", "Dragon claws"), dir));

		File file = new File(dir, "zezima.json");
		assertTrue(file.exists());

		CharacterSnapshot parsed = read(file);
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
		SnapshotWriter writer = new SnapshotWriter(new Gson());

		writer.write(sample("Zezima", "Old item"), dir);
		writer.write(sample("Zezima", "New item"), dir);

		CharacterSnapshot parsed = read(new File(dir, "zezima.json"));
		assertEquals("New item", parsed.bank.items.get(0).name);
	}

	/**
	 * Non-ASCII item names round-trip through the UTF-8 write.
	 */
	@Test
	public void writePreservesUnicode() throws Exception
	{
		File dir = tmp.newFolder("export");
		SnapshotWriter writer = new SnapshotWriter(new Gson());

		writer.write(sample("Zezima", "Clanë"), dir);

		CharacterSnapshot parsed = read(new File(dir, "zezima.json"));
		assertEquals("Clanë", parsed.bank.items.get(0).name);
	}

	/**
	 * A snapshot with no player is not written.
	 */
	@Test
	public void writeSkipsMissingPlayer() throws Exception
	{
		File dir = tmp.newFolder("export");
		SnapshotWriter writer = new SnapshotWriter(new Gson());

		assertTrue(!writer.write(new CharacterSnapshot(), dir));
		assertTrue(!writer.write(null, dir));
	}

	/**
	 * Builds a minimal snapshot with one banked item.
	 *
	 * @param name     the player name
	 * @param itemName the single banked item's name
	 * @return the snapshot
	 */
	private static CharacterSnapshot sample(String name, String itemName)
	{
		CharacterSnapshot data = new CharacterSnapshot();
		data.player = new PlayerInfo(name, 126, 302, "NORMAL", true);
		data.bank = new CharacterSnapshot.BankData(
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
	private static CharacterSnapshot read(File file) throws Exception
	{
		String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		return new Gson().fromJson(json, CharacterSnapshot.class);
	}
}
