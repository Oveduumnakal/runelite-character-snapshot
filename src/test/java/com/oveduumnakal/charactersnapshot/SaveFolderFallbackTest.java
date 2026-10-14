/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.io.File;
import java.nio.file.Files;

import com.google.gson.Gson;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot.PlayerInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that {@link CharacterSnapshotPlugin#resolveSyncDir()} falls back to the default folder when the
 * configured save folder cannot be created or written to.
 */
public class SaveFolderFallbackTest
{
	/** Scratch directory for the default and custom folders. */
	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	private CharacterSnapshotPlugin plugin;

	private CharacterSnapshotConfig config;

	private File defaultDir;

	/**
	 * Builds a plugin with a mocked config and a temporary default folder.
	 *
	 * @throws Exception if the temporary folder cannot be created
	 */
	@Before
	public void setUp() throws Exception
	{
		config = mock(CharacterSnapshotConfig.class);
		defaultDir = temp.newFolder("default");
		plugin = new CharacterSnapshotPlugin();
		plugin.config = config;
		plugin.defaultDir = defaultDir;
	}

	/**
	 * A blank setting resolves to the default folder.
	 */
	@Test
	public void blankUsesDefault()
	{
		when(config.saveDirectory()).thenReturn("  ");

		assertEquals(defaultDir, plugin.resolveSyncDir());
	}

	/**
	 * A valid custom folder is created if absent and used.
	 */
	@Test
	public void validCustomFolderIsUsed()
	{
		File custom = new File(temp.getRoot(), "custom/nested");
		when(config.saveDirectory()).thenReturn(custom.getPath());

		assertEquals(custom, plugin.resolveSyncDir());
		assertTrue(custom.isDirectory());
	}

	/**
	 * A path beneath a regular file cannot be created, so the default folder is used and the write lands there.
	 *
	 * @throws Exception on I/O failure
	 */
	@Test
	public void uncreatableFolderFallsBackAndWriteLandsInDefault() throws Exception
	{
		File regularFile = temp.newFile("blocker");
		File bad = new File(regularFile, "exports");
		when(config.saveDirectory()).thenReturn(bad.getPath());

		File resolved = plugin.resolveSyncDir();
		assertEquals(defaultDir, resolved);
		assertEquals(defaultDir, plugin.resolveSyncDir());

		CharacterSnapshot snapshot = new CharacterSnapshot();
		snapshot.player = new PlayerInfo("Zezima", 3, 301, null, false);
		assertTrue(new SnapshotWriter(new Gson()).write(snapshot, resolved));
		assertTrue(Files.isRegularFile(new File(defaultDir, "zezima.json").toPath()));
	}

	/**
	 * A path that is itself a regular file is not a usable folder.
	 *
	 * @throws Exception on I/O failure
	 */
	@Test
	public void folderThatIsAFileFallsBack() throws Exception
	{
		File regularFile = temp.newFile("afile");
		when(config.saveDirectory()).thenReturn(regularFile.getPath());

		assertEquals(defaultDir, plugin.resolveSyncDir());
	}
}
