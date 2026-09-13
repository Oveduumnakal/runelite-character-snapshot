/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.inject.Provides;
import com.oveduumnakal.charactersnapshot.model.CharacterSnapshot;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.StatChanged;
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
	private Client client;

	@Inject
	private CharacterSnapshotConfig config;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private Gson gson;

	private SnapshotCollector collector;

	private SnapshotWriter writer;

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
		collector = new SnapshotCollector(client, config);
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
	 * character's data cannot leak into another's file.
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
			dirty = true;
		}
		else if (id == InventoryID.WORN && config.syncEquipment())
		{
			collector.updateEquipment(event.getItemContainer());
			dirty = true;
		}
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
			doFullCollection();
			lastPlayerName = currentName;
			initialCollectionDone = true;
			dirty = true;
			doSave();
			return;
		}

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
		if (config.syncQuests())
			changed |= collector.pollQuests();

		if (config.syncDiaries())
			changed |= collector.pollDiaries();

		if (config.syncCombatAchievements())
			changed |= collector.pollCombatAchievements();

		if (config.syncSlayer())
			changed |= collector.pollSlayer();

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

		if (config.syncQuests())
			collector.pollQuests();

		if (config.syncDiaries())
			collector.pollDiaries();

		if (config.syncCombatAchievements())
			collector.pollCombatAchievements();

		if (config.syncSlayer())
			collector.pollSlayer();

		if (config.syncGrandExchange())
			collector.pollAllGeOffers();
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
	 * Resolves the export directory: the configured folder if set, else the default under the
	 * RuneLite directory.
	 *
	 * @return the directory to write export files into
	 */
	private File resolveSyncDir()
	{
		String custom = config.saveDirectory();
		if (custom != null && !custom.trim().isEmpty())
			return new File(custom.trim());

		return new File(RuneLite.RUNELITE_DIR, "osrs-companion");
	}

	/**
	 * Recomputes the save threshold in game ticks (one tick is about 0.6 seconds).
	 */
	private void recalcSyncThreshold()
	{
		int seconds = Math.max(30, config.syncIntervalSeconds());
		syncTickThreshold = (int) (seconds / 0.6);
	}
}
