/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.dataexport;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/** Development entry point that launches a RuneLite client with the plugin loaded (used by {@code ./gradlew run}). */
public class PlayerDataExportPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PlayerDataExportPlugin.class);
		RuneLite.main(args);
	}
}
