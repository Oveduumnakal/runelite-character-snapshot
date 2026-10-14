/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * Collection log search script id and argument layout derived from WikiSync
 * (https://github.com/weirdgloop/WikiSync), BSD 2-Clause:
 * Copyright (c) 2021, andmcadams
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.inject.Provides;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

/**
 * Writes a live snapshot of the logged-in character to a local JSON file for external tools. State is
 * collected on the client thread from events and periodic polling, and written off-thread. The plugin
 * makes no network calls of any kind.
 */
@Slf4j
@PluginDescriptor(
	name = "Character Snapshot",
	description = "Saves a live snapshot of your character to a local JSON file for external tools",
	tags = {"snapshot", "live", "state", "character", "data", "json", "local"}
)
public class CharacterSnapshotPlugin extends Plugin
{
	private static final int INITIAL_DELAY_TICKS = 10;

	private static final int POLL_INTERVAL_TICKS = 30;

	@Inject
	Client client;

	@Inject
	CharacterSnapshotConfig config;

	@Inject
	ScheduledExecutorService executor;

	@Inject
	ConfigManager configManager;

	@Inject
	Gson gson;

	/** Default export folder; replaced in tests so they never touch the real RuneLite directory. */
	File defaultDir = new File(RuneLite.RUNELITE_DIR, "osrs-companion");

	/** The last invalid custom folder a warning was logged for, so the warning is not repeated. */
	private String warnedInvalidDir = null;

	private volatile boolean dirty = false;

	private int tickCounter = 0;

	private int syncTickThreshold = 100;

	private boolean initialCollectionDone = false;

	private String lastPlayerName = null;

	/**
	 * Initializes the collector and writer and computes the save interval.
	 */
	@Override
	protected void startUp()
	{
		collector = new SnapshotCollector(client, config, configManager);
		writer = new SnapshotWriter(gson);
		recalcSyncThreshold();
	}

	/**
	 * Writes a final snapshot when still logged in, then releases resources.
	 */
	@Override
	protected void shutDown()
	{
		if (client.getGameState() == GameState.LOGGED_IN)
			doSave();

		collector = null;
		writer = null;
	}

	/**
	 * Provides the plugin configuration.
	 *
	 * @param configManager the config manager
	 * @return the bound configuration
	 */
	@Provides
	CharacterSnapshotConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CharacterSnapshotConfig.class);
	}

	/**
	 * Recomputes the save interval when this plugin's configuration changes.
	 *
	 * @param event the config-changed event
	 */
	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (CharacterSnapshotConfig.GROUP.equals(event.getGroup()))
			recalcSyncThreshold();
	}

	/**
	 * Schedules the initial collection on login and clears state on logout or world hop so one
	 * character's data cannot leak into another's file. The initial collection restores the
	 * character's own last known items from its previous export.
	 *
	 * @param event the game-state event
	 */
	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOGGED_IN)
		{
			tickCounter = -INITIAL_DELAY_TICKS;
			initialCollectionDone = false;
		}
		else if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
		{
			doSave();
			if (collector != null)
				collector.reset();

			initialCollectionDone = false;
		}
	}

	/**
	 * Caches a changed skill (real, experience, and boosted levels).
	 *
	 * @param event the stat-changed event
	 */
	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (collector == null || !config.syncSkills())
			return;

		collector.updateSkill(event.getSkill(), event.getLevel(), event.getXp(), event.getBoostedLevel());
		dirty = true;
	}

	/**
	 * Caches bank, inventory, or equipment changes.
	 *
	 * @param event the container-changed event
	 */
	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (collector == null)
			return;

		int id = event.getContainerId();
		if (id == InventoryID.BANK && config.syncBank())
		{
			collector.updateBank(event.getItemContainer());
			dirty = true;
		}
		else if (id == InventoryID.INV && config.syncInventory())
		{
			collector.updateInventory(event.getItemContainer());
			if (config.syncRunePouch())
				collector.pollRunePouch();

			dirty = true;
		}
		else if (id == InventoryID.WORN && config.syncEquipment())
		{
			collector.updateEquipment(event.getItemContainer());
			dirty = true;
		}
		else if (SnapshotCollector.storageKey(id) != null && config.syncStorages())
		{
			collector.updateStorage(SnapshotCollector.storageKey(id), event.getItemContainer());
			dirty = true;
		}
	}

	/**
	 * Captures the collection log page whenever the log redraws its item list.
	 *
	 * @param event the script-post-fired event
	 */
	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (event.getScriptId() != ScriptID.COLLECTION_DRAW_LIST || collector == null
				|| !config.syncCollectionLog())
			return;

		if (collector.captureCollectionLog())
			dirty = true;
	}

	/**
	 * Merges every obtained item the game reports while the player opens the collection log's Search. The
	 * pass is closed once per pass on a later game tick, not per item.
	 *
	 * @param event the script-pre-fired event
	 */
	@Subscribe
	public void onScriptPreFired(ScriptPreFired event)
	{
		if (event.getScriptId() != SnapshotCollector.CLOG_ITEM_SCRIPT_ID || collector == null
				|| !config.syncCollectionLog() || event.getScriptEvent() == null)
			return;

		collector.recordCollectionLogItem(event.getScriptEvent().getArguments());
	}

	/**
	 * Reads kill-count and treasure-trail completion lines from game messages.
	 *
	 * @param event the chat-message event
	 */
	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (collector == null)
			return;

		ChatMessageType type = event.getType();
		if (type != ChatMessageType.GAMEMESSAGE && type != ChatMessageType.SPAM)
			return;

		if (config.syncKillCounts() && collector.onChatMessage(event.getMessage()))
			dirty = true;

		boolean clue = config.syncClueCounts() && type == ChatMessageType.GAMEMESSAGE;
		if (clue && collector.onClueMessage(event.getMessage()))
			dirty = true;
	}

	/**
	 * Caches a changed Grand Exchange offer.
	 *
	 * @param event the offer-changed event
	 */
	@Subscribe
	public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged event)
	{
		if (collector == null || !config.syncGrandExchange())
			return;

		collector.updateGeOffer(event.getSlot(), event.getOffer());
		dirty = true;
	}

	/**
	 * Drives the initial collection, periodic polling, and interval-gated saves.
	 *
	 * @param event the game-tick event
	 */
	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN || collector == null)
			return;

		Player local = client.getLocalPlayer();
		if (local == null)
			return;

		String currentName = local.getName();
		if (initialCollectionDone && currentName != null && !currentName.equals(lastPlayerName))
		{
			collector.reset();
			initialCollectionDone = false;
			tickCounter = 0;
		}

		tickCounter++;

		if (!initialCollectionDone && tickCounter >= 0)
		{
			collector.restore(writer.read(currentName, resolveSyncDir()));
			doFullCollection();
			lastPlayerName = currentName;
			initialCollectionDone = true;
			dirty = true;
			doSave();
			return;
		}

		if (collector.finishCollectionLogSearch())
			dirty = true;

		if (collector.pollMovement())
			dirty = true;

		if (tickCounter % POLL_INTERVAL_TICKS == 0)
			pollPeriodic();

		if (dirty && tickCounter >= syncTickThreshold)
		{
			tickCounter = 0;
			doSave();
		}
	}

	/**
	 * Polls the data sources that are not event-driven, marking the snapshot dirty on any change.
	 */
	private void pollPeriodic()
	{
		boolean changed = false;
		if (questsStale)
		{
			questsStale = false;
			if (config.syncQuests())
				changed |= collector.pollQuests();
		}

		if (config.syncDiaries())
			changed |= collector.pollDiaries();

		if (config.syncCombatAchievements())
			changed |= collector.pollCombatAchievements();

		if (config.syncSlayer())
			changed |= collector.pollSlayer();

		if (config.syncRunePouch())
			changed |= collector.pollRunePouch();

		if (config.syncAccountInfo())
		{
			changed |= collector.pollSpellbook();
			changed |= collector.pollPrayerMagic();
		}

		if (config.syncKillCounts())
			changed |= collector.pollKillCounts();

		if (config.syncTimers())
			changed |= collector.pollTimers();

		if (config.syncTransport())
			changed |= collector.pollTransport();

		if (config.syncStorages())
			changed |= collector.pollSkillingStorages();

		if (config.syncMinigamePoints())
			changed |= collector.pollMinigamePoints();

		if (config.syncDailies())
			changed |= collector.pollDailies();

		if (changed)
			dirty = true;
	}

	/**
	 * Runs a one-off full collection of every enabled data source after login.
	 */
	private void doFullCollection()
	{
		if (config.syncSkills())
			collector.pollAllSkills();

		if (config.syncBank())
		{
			ItemContainer bank = client.getItemContainer(InventoryID.BANK);
			if (bank != null)
				collector.updateBank(bank);
		}

		if (config.syncInventory())
		{
			ItemContainer inv = client.getItemContainer(InventoryID.INV);
			if (inv != null)
				collector.updateInventory(inv);
		}

		if (config.syncEquipment())
		{
			ItemContainer equip = client.getItemContainer(InventoryID.WORN);
			if (equip != null)
				collector.updateEquipment(equip);
		}

		if (config.syncStorages())
		{
			for (int containerId : SnapshotCollector.storageContainerIds())
			{
				ItemContainer storage = client.getItemContainer(containerId);
				if (storage != null)
					collector.updateStorage(SnapshotCollector.storageKey(containerId), storage);
			}
		}

		questsStale = false;
		if (config.syncQuests())
			collector.pollQuests();

		if (config.syncDiaries())
			collector.pollDiaries();

		if (config.syncCombatAchievements())
			collector.pollCombatAchievements();

		if (config.syncSlayer())
			collector.pollSlayer();

		if (config.syncAccountInfo())
		{
			collector.pollSpellbook();
			collector.pollPrayerMagic();
		}

		if (config.syncGrandExchange())
			collector.pollAllGeOffers();

		if (config.syncRunePouch())
			collector.pollRunePouch();

		if (config.syncKillCounts())
			collector.pollKillCounts();

		if (config.syncTimers())
			collector.pollTimers();

		if (config.syncTransport())
			collector.pollTransport();

		if (config.syncStorages())
			collector.pollSkillingStorages();

		if (config.syncMinigamePoints())
			collector.pollMinigamePoints();

		if (config.syncDailies())
			collector.pollDailies();
	}

	/**
	 * Builds a snapshot on the client thread and writes it off-thread. Clears the dirty flag
	 * optimistically and re-sets it if the write fails, so a failed write is retried.
	 */
	private void doSave()
	{
		if (collector == null || writer == null || !dirty)
			return;

		CharacterSnapshot snapshot = collector.buildSnapshot();
		if (snapshot == null)
			return;

		dirty = false;
		File dir = resolveSyncDir();
		executor.submit(() ->
		{
			if (!writer.write(snapshot, dir))
				dirty = true;
		});
	}

	/**
	 * Resolves the export directory: the configured folder if it can be created and written to, else
	 * the default under the RuneLite directory. An unusable folder is logged once, not per save.
	 *
	 * @return the directory to write export files into
	 */
	File resolveSyncDir()
	{
		String custom = config.saveDirectory();
		if (custom == null || custom.trim().isEmpty())
			return defaultDir;

		String trimmed = custom.trim();
		if (isUsableDirectory(trimmed))
		{
			warnedInvalidDir = null;
			return new File(trimmed);
		}

		if (!trimmed.equals(warnedInvalidDir))
		{
			warnedInvalidDir = trimmed;
			log.warn("Character Snapshot: save folder '{}' is not usable, using {}", trimmed, defaultDir);
		}

		return defaultDir;
	}

	/**
	 * Checks that a folder exists (creating it if absent) and is writable.
	 *
	 * @param path the folder path
	 * @return whether exports can be written there
	 */
	private static boolean isUsableDirectory(String path)
	{
		try
		{
			Path dir = new File(path).toPath();
			Files.createDirectories(dir);
			return Files.isDirectory(dir) && Files.isWritable(dir);
		}
		catch (IOException | RuntimeException e)
		{
			return false;
		}
	}

	/**
	 * Recomputes the save threshold in game ticks (one tick is about 0.6 seconds).
	 */
	private void recalcSyncThreshold()
	{
		int seconds = Math.max(30, config.syncIntervalSeconds());
		syncTickThreshold = (int) (seconds / 0.6);
	}

	SnapshotCollector collector;

	SnapshotWriter writer;

	private volatile boolean questsStale = false;

	/**
	 * Marks the quests stale on any varp or varbit change, since quest progress lives in those. The next
	 * periodic poll then re-reads the quests; without a change they are not polled again.
	 *
	 * @param event the varbit-changed event
	 */
	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		questsStale = true;
	}
}
