/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.dataexport;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

import com.google.gson.Gson;
import com.oveduumnakal.dataexport.model.PlayerSyncData;
import lombok.extern.slf4j.Slf4j;

/**
 * Serializes a {@link PlayerSyncData} snapshot to a per-character JSON file. The write is atomic:
 * the JSON is written to a temporary file in the target directory and then renamed over the
 * destination, so a second process polling the file never observes a half-written document.
 */
@Slf4j
public class PlayerDataWriter
{
	private final Gson gson;

	/**
	 * @param gson the injected Gson instance; a pretty-printing copy is derived from it
	 */
	public PlayerDataWriter(Gson gson)
	{
		this.gson = gson.newBuilder()
				.setPrettyPrinting()
				.create();
	}

	/**
	 * Writes the snapshot to {@code <syncDir>/<sanitized-username>.json} atomically.
	 *
	 * @param data    the snapshot to write
	 * @param syncDir the directory to write into (created if absent)
	 * @return {@code true} if the file was written, {@code false} on missing data or I/O error
	 */
	public boolean write(PlayerSyncData data, File syncDir)
	{
		if (data == null || data.player == null)
			return false;

		String filename = filenameFor(data.player.username);
		if (filename == null)
			return false;

		String json = gson.toJson(data);
		Path dir = syncDir.toPath();
		Path tmp = null;
		try
		{
			Files.createDirectories(dir);
			Path target = dir.resolve(filename);
			tmp = Files.createTempFile(dir, filename + ".", ".tmp");
			Files.write(tmp, json.getBytes(StandardCharsets.UTF_8));
			try
			{
				Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			}
			catch (AtomicMoveNotSupportedException e)
			{
				Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
			}

			return true;
		}
		catch (IOException e)
		{
			log.warn("Player Data Export: failed to write {}", filename, e);
			deleteQuietly(tmp);
			return false;
		}
	}

	/**
	 * Builds the JSON filename for a username, lower-casing it and replacing every character outside
	 * {@code [a-z0-9_-]} with an underscore so the name cannot escape the target directory.
	 *
	 * @param username the raw in-game name
	 * @return the sanitized {@code <name>.json} filename, or {@code null} for a null/blank name
	 */
	static String filenameFor(String username)
	{
		if (username == null)
			return null;

		String trimmed = username.trim();
		if (trimmed.isEmpty())
			return null;

		return trimmed.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "_") + ".json";
	}

	/**
	 * Deletes a temporary file, ignoring any failure.
	 *
	 * @param path the file to remove, may be {@code null}
	 */
	private static void deleteQuietly(Path path)
	{
		if (path == null)
			return;

		try
		{
			Files.deleteIfExists(path);
		}
		catch (IOException e)
		{
			// best-effort cleanup of the temp file
		}
	}
}
