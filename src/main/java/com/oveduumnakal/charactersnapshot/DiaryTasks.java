/*
 * Copyright (c) 2026, isaac
 * Copyright (c) 2026, Oveduumnakal
 * All rights reserved.
 */
package com.oveduumnakal.charactersnapshot;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.runelite.api.Client;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

/**
 * Table of the individual achievement-diary tasks and where the game stores whether each is done.
 *
 * <p>RuneLite exposes no per-task API, so each task is read straight from client state in one of two
 * ways: a single bit of a diary varp ({@link Task#isComplete(Client)} tests {@code (varp >> bit) & 1}),
 * or a Karamja varbit that counts up from zero and is complete once it reaches a minimum value.
 * A task is only listed when its storage location is known from a source; unsourced tasks are left
 * out rather than guessed.
 *
 * <p><b>Source.</b> The task names, varps, bit positions, and Karamja varbits come from the Quest Helper
 * RuneLite plugin (https://github.com/Zoinkwiz/quest-helper, BSD 2-Clause licence), package
 * {@code com.questhelper.helpers.achievementdiaries}, where each task's "not done" requirement is a
 * {@code VarplayerRequirement(VarPlayerID.*_ACHIEVEMENT_DIARY[2], false, bit)} or a
 * {@code VarbitRequirement(VarbitID.ATJUN_*, ...)} and the task is titled by its panel. Quest Helper's
 * panel titles are terse; the Falador task names were rewritten in plain English from that plugin's
 * step text. Quest Helper's copyright notice and BSD 2-Clause licence terms apply to this derived table.
 *
 * <p>The table holds 492 tasks, matching the 492 tasks the wiki lists across the twelve diaries. Within a
 * tier, varp tasks are ordered by varp then bit, which is not the in-game journal order.
 */
public final class DiaryTasks
{
	/** Varp {@code ARDOUNGE_ACHIEVEMENT_DIARY}. */
	private static final int ARDOUGNE_1 = VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY;

	/** Varp {@code ARDOUNGE_ACHIEVEMENT_DIARY2}. */
	private static final int ARDOUGNE_2 = VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY2;

	/** Varp {@code DESERT_ACHIEVEMENT_DIARY}. */
	private static final int DESERT_1 = VarPlayerID.DESERT_ACHIEVEMENT_DIARY;

	/** Varp {@code DESERT_ACHIEVEMENT_DIARY2}. */
	private static final int DESERT_2 = VarPlayerID.DESERT_ACHIEVEMENT_DIARY2;

	/** Varp {@code FALADOR_ACHIEVEMENT_DIARY}. */
	private static final int FALADOR_1 = VarPlayerID.FALADOR_ACHIEVEMENT_DIARY;

	/** Varp {@code FALADOR_ACHIEVEMENT_DIARY2}. */
	private static final int FALADOR_2 = VarPlayerID.FALADOR_ACHIEVEMENT_DIARY2;

	/** Varp {@code FREMENNIK_ACHIEVEMENT_DIARY}. */
	private static final int FREMENNIK_1 = VarPlayerID.FREMENNIK_ACHIEVEMENT_DIARY;

	/** Varp {@code FREMENNIK_ACHIEVEMENT_DIARY2}. */
	private static final int FREMENNIK_2 = VarPlayerID.FREMENNIK_ACHIEVEMENT_DIARY2;

	/** Varp {@code KANDARIN_ACHIEVEMENT_DIARY}. */
	private static final int KANDARIN_1 = VarPlayerID.KANDARIN_ACHIEVEMENT_DIARY;

	/** Varp {@code KANDARIN_ACHIEVEMENT_DIARY2}. */
	private static final int KANDARIN_2 = VarPlayerID.KANDARIN_ACHIEVEMENT_DIARY2;

	/** Varp {@code ATJUN_TASKS_4}. */
	private static final int KARAMJA_4 = VarPlayerID.ATJUN_TASKS_4;

	/** Varp {@code KOUREND_ACHIEVEMENT_DIARY}. */
	private static final int KOUREND_1 = VarPlayerID.KOUREND_ACHIEVEMENT_DIARY;

	/** Varp {@code KOUREND_ACHIEVEMENT_DIARY2}. */
	private static final int KOUREND_2 = VarPlayerID.KOUREND_ACHIEVEMENT_DIARY2;

	/** Varp {@code LUMB_DRAY_ACHIEVEMENT_DIARY}. */
	private static final int LUMBRIDGE_1 = VarPlayerID.LUMB_DRAY_ACHIEVEMENT_DIARY;

	/** Varp {@code LUMB_DRAY_ACHIEVEMENT_DIARY2}. */
	private static final int LUMBRIDGE_2 = VarPlayerID.LUMB_DRAY_ACHIEVEMENT_DIARY2;

	/** Varp {@code MORYTANIA_ACHIEVEMENT_DIARY}. */
	private static final int MORYTANIA_1 = VarPlayerID.MORYTANIA_ACHIEVEMENT_DIARY;

	/** Varp {@code MORYTANIA_ACHIEVEMENT_DIARY2}. */
	private static final int MORYTANIA_2 = VarPlayerID.MORYTANIA_ACHIEVEMENT_DIARY2;

	/** Varp {@code VARROCK_ACHIEVEMENT_DIARY}. */
	private static final int VARROCK_1 = VarPlayerID.VARROCK_ACHIEVEMENT_DIARY;

	/** Varp {@code VARROCK_ACHIEVEMENT_DIARY2}. */
	private static final int VARROCK_2 = VarPlayerID.VARROCK_ACHIEVEMENT_DIARY2;

	/** Varp {@code WESTERN_ACHIEVEMENT_DIARY}. */
	private static final int WESTERN_1 = VarPlayerID.WESTERN_ACHIEVEMENT_DIARY;

	/** Varp {@code WESTERN_ACHIEVEMENT_DIARY2}. */
	private static final int WESTERN_2 = VarPlayerID.WESTERN_ACHIEVEMENT_DIARY2;

	/** Varp {@code WILDERNESS_ACHIEVEMENT_DIARY}. */
	private static final int WILDERNESS_1 = VarPlayerID.WILDERNESS_ACHIEVEMENT_DIARY;

	/** Varp {@code WILDERNESS_ACHIEVEMENT_DIARY2}. */
	private static final int WILDERNESS_2 = VarPlayerID.WILDERNESS_ACHIEVEMENT_DIARY2;

	/** Tier keys in easy to elite order. */
	public static final List<String> TIERS = List.of("easy", "medium", "hard", "elite");

	/** Region name to tier to tasks, in the same region order as the tier booleans. */
	private static final Map<String, Map<String, List<Task>>> TABLE = buildTable();

	private DiaryTasks()
	{
	}

	/**
	 * Returns the known tasks of a region.
	 *
	 * @param region region key such as {@code VARROCK}
	 * @return tier key ({@code easy}, {@code medium}, {@code hard}, {@code elite}) to its tasks, or an
	 *         empty map when no task of the region is mapped
	 */
	public static Map<String, List<Task>> tasks(String region)
	{
		Map<String, List<Task>> tiers = TABLE.get(region);
		return tiers == null ? Collections.emptyMap() : tiers;
	}

	/**
	 * One diary task and the client state that says whether it is complete.
	 */
	public static final class Task
	{
		/** Task text shown in the export. */
		public final String name;

		private final boolean varbit;

		private final int id;

		private final int arg;

		private Task(String name, boolean varbit, int id, int arg)
		{
			this.name = name;
			this.varbit = varbit;
			this.id = id;
			this.arg = arg;
		}

		/**
		 * Reads this task's completion from the client.
		 *
		 * @param client the game client
		 * @return whether the task is complete
		 */
		public boolean isComplete(Client client)
		{
			if (varbit)
				return client.getVarbitValue(id) >= arg;

			return ((client.getVarpValue(id) >>> arg) & 1) == 1;
		}
	}

	private static Map<String, Map<String, List<Task>>> buildTable()
	{
		Map<String, Map<String, List<Task>>> table = new LinkedHashMap<>();
		table.put("ARDOUGNE", ardougne());
		table.put("DESERT", desert());
		table.put("FALADOR", falador());
		table.put("FREMENNIK", fremennik());
		table.put("KANDARIN", kandarin());
		table.put("KARAMJA", karamja());
		table.put("KOUREND", kourend());
		table.put("LUMBRIDGE", lumbridge());
		table.put("MORYTANIA", morytania());
		table.put("VARROCK", varrock());
		table.put("WESTERN", western());
		table.put("WILDERNESS", wilderness());
		return table;
	}

	private static Task varp(String name, int varp, int bit)
	{
		return new Task(name, false, varp, bit);
	}

	private static Task varbit(String name, int varbit, int minValue)
	{
		return new Task(name, true, varbit, minValue);
	}

	/**
	 * Tasks of the Ardougne diary, by tier.
	 *
	 * @return the tiers in easy to elite order
	 */
	private static Map<String, List<Task>> ardougne()
	{
		Map<String, List<Task>> tiers = new LinkedHashMap<>();
		tiers.put("easy", List.of(
			varp("Essence Mine", ARDOUGNE_1, 0),
			varp("Steal Cake", ARDOUGNE_1, 1),
			varp("Sell Silk", ARDOUGNE_1, 2),
			varp("Restore Prayer", ARDOUGNE_1, 4),
			varp("Fishing Trawler", ARDOUGNE_1, 5),
			varp("Combat Camp", ARDOUGNE_1, 6),
			varp("Identify Sword", ARDOUGNE_1, 7),
			varp("Wilderness Lever", ARDOUGNE_1, 9),
			varp("Aleck's Hunter Emporium", ARDOUGNE_1, 11),
			varp("Pet Insurance", ARDOUGNE_1, 12)));
		tiers.put("medium", List.of(
			varp("Fairy Ring to Unicorn Pen", ARDOUGNE_1, 13),
			varp("Yanille Wall Grapple", ARDOUGNE_1, 14),
			varp("Ardougne Strawberries", ARDOUGNE_1, 15),
			varp("Teleport to Ardougne", ARDOUGNE_1, 16),
			varp("Castle Wars Balloon", ARDOUGNE_1, 17),
			varp("Claim Sand", ARDOUGNE_1, 18),
			varp("Fishing Platform", ARDOUGNE_1, 19),
			varp("Pickpocket Master Farmer", ARDOUGNE_1, 20),
			varp("Cave Nightshade", ARDOUGNE_1, 21),
			varp("Kill Swordchick", ARDOUGNE_1, 23),
			varp("Iban Upgrade", ARDOUGNE_1, 24),
			varp("Fairy Ring to Necromancer Tower", ARDOUGNE_1, 25)));
		tiers.put("hard", List.of(
			varp("Recharge Jewelry", ARDOUGNE_1, 26),
			varp("Magic Guild", ARDOUGNE_1, 27),
			varp("Stealing from Ardougne Royalty", ARDOUGNE_1, 28),
			varp("Monkey in a Cage", ARDOUGNE_1, 29),
			varp("Teleport to Watchtower", ARDOUGNE_1, 30),
			varp("Red Salamander", ARDOUGNE_1, 31),
			varp("Tree Gnome Village Palm Tree", ARDOUGNE_2, 0),
			varp("Monastery Poison Ivy", ARDOUGNE_2, 1),
			varp("Mithril Platebody", ARDOUGNE_2, 2),
			varp("Yanille POH", ARDOUGNE_2, 3),
			varp("Smith Dragon Square in West Ardougne", ARDOUGNE_2, 4),
			varp("Craft Death Runes", ARDOUGNE_2, 5)));
		tiers.put("elite", List.of(
			varp("Fishing Trawler Manta Ray", ARDOUGNE_2, 6),
			varp("Yanille Agility Dungeon", ARDOUGNE_2, 7),
			varp("Rune Crossbow in Yanille / Witchaven", ARDOUGNE_2, 8),
			varp("Pickpocket Hero", ARDOUGNE_2, 9),
			varp("Imbued Salve Amulet", ARDOUGNE_2, 10),
			varp("Ardougne Torstol", ARDOUGNE_2, 11),
			varp("Ardougne Rooftop Agility", ARDOUGNE_2, 12),
			varp("Ice Barrage in Castle Wars", ARDOUGNE_2, 13)));
		return tiers;
	}

	/**
	 * Tasks of the Desert diary, by tier.
	 *
	 * @return the tiers in easy to elite order
	 */
	private static Map<String, List<Task>> desert()
	{
		Map<String, List<Task>> tiers = new LinkedHashMap<>();
		tiers.put("easy", List.of(
			varp("Golden Warbler", DESERT_1, 1),
			varp("Five clay", DESERT_1, 2),
			varp("Kalphite Hive", DESERT_1, 3),
			varp("Enter the Desert Dressed Appropriately", DESERT_1, 4),
			varp("Kill a Vulture", DESERT_1, 5),
			varp("Nardah Herb Cleaner", DESERT_1, 6),
			varp("Kalphite Cacti", DESERT_1, 7),
			varp("Sell Artefact", DESERT_1, 8),
			varp("First Sarcophagus", DESERT_1, 9),
			varp("Cut Desert Cacti", DESERT_1, 10),
			varp("Magic Carpet Ride", DESERT_1, 11)));
		tiers.put("medium", List.of(
			varp("Agility Pyramid", DESERT_1, 12),
			varp("Desert Lizard", DESERT_1, 13),
			varp("Orange Salamander", DESERT_1, 14),
			varp("Phoenix Feather", DESERT_1, 15),
			varp("Magic Carpet to Uzer", DESERT_1, 16),
			varp("Eagle Travel to Desert", DESERT_1, 17),
			varp("Pray at Elidinis Statuette", DESERT_1, 18),
			varp("Combat Potion", DESERT_1, 19),
			varp("Camulet to Enakhra's Temple", DESERT_1, 20),
			varp("Visit the Genie", DESERT_1, 21),
			varp("Pollnivneach House", DESERT_1, 22),
			varp("Chop Teak at Uzer", DESERT_1, 23)));
		tiers.put("hard", List.of(
			varp("Menaphite Thug", DESERT_1, 24),
			varp("Mine Granite", DESERT_1, 25),
			varp("Refill Waterskins", DESERT_1, 26),
			varp("Kalphite Queen", DESERT_1, 27),
			varp("Pollnivneach Rooftops", DESERT_1, 28),
			varp("Dust Devil", DESERT_1, 29),
			varp("Activate Ancient Magicks", DESERT_1, 30),
			varp("Kill Locust Rider with Keris", DESERT_1, 31),
			varp("Burn Yew on Nardah Mayor's Balcony", DESERT_2, 0),
			varp("Mithril Platebody", DESERT_2, 1)));
		tiers.put("elite", List.of(
			varp("Bake Wild Pie", DESERT_2, 2),
			varp("Ice Barrage", DESERT_2, 4),
			varp("Dragon Darts", DESERT_2, 5),
			varp("Kalphite Queen Head", DESERT_2, 6),
			varp("Grand Gold Chest", DESERT_2, 7),
			varp("Restore 85 Prayer", DESERT_2, 8)));
		return tiers;
	}

	/**
	 * Tasks of the Falador diary, by tier.
	 *
	 * @return the tiers in easy to elite order
	 */
	private static Map<String, List<Task>> falador()
	{
		Map<String, List<Task>> tiers = new LinkedHashMap<>();
		tiers.put("easy", List.of(
			varp("Family Crest", FALADOR_1, 0),
			varp("Climb West Wall", FALADOR_1, 1),
			varp("Sarah's Farming Shop", FALADOR_1, 2),
			varp("Get A Haircut", FALADOR_1, 3),
			varp("Fill Water Bucket", FALADOR_1, 4),
			varp("Kill The Duck", FALADOR_1, 5),
			varp("Mind Tiara", FALADOR_1, 6),
			varp("To the Holy Land!", FALADOR_1, 7),
			varp("Motherlode Repairs", FALADOR_1, 8),
			varp("Get A Security Book", FALADOR_1, 9),
			varp("Blurite Limbs", FALADOR_1, 10)));
		tiers.put("medium", List.of(
			varp("Light a bullseye lantern at the Chemist's in Rimmington", FALADOR_1, 11),
			varp("Telegrab a wine of Zamorak", FALADOR_1, 12),
			varp("Unlock the crystal chest in Taverley", FALADOR_1, 13),
			varp("Place a scarecrow in the Falador farm flower patch", FALADOR_1, 14),
			varp("Kill a Mogre at Mudskipper Point", FALADOR_1, 15),
			varp("Visit the Rat Pits in Port Sarim", FALADOR_1, 16),
			varp("Grapple up the north Falador wall", FALADOR_1, 17),
			varp("Pickpocket a Falador guard", FALADOR_1, 18),
			varp("Pray at the Altar of Guthix wearing Initiate armour", FALADOR_1, 20),
			varp("Mine gold at the Crafting Guild", FALADOR_1, 21),
			varp("Squeeze through the crevice in the Dwarven Mines", FALADOR_1, 22),
			varp("Chop and burn willow logs in Taverley", FALADOR_1, 23),
			varp("Make a basket on Sarah's Loom", FALADOR_1, 24),
			varp("Teleport to Falador", FALADOR_1, 25)));
		tiers.put("hard", List.of(
			varp("Craft mind runes", FALADOR_1, 26),
			varp("Change your family crest to Saradomin", FALADOR_1, 27),
			varp("Kill the Giant Mole", FALADOR_1, 28),
			varp("Kill a Skeletal Wyvern", FALADOR_1, 29),
			varp("Complete a lap of the Falador Agility Course", FALADOR_1, 30),
			varp("Enter the Mining Guild wearing a prospector helmet", FALADOR_1, 31),
			varp("Kill a blue dragon in the Heroes' Guild", FALADOR_2, 0),
			varp("Crack a wall safe in the Rogues' Den", FALADOR_2, 1),
			varp("Pray at the Port Sarim altar wearing Proselyte armour", FALADOR_2, 2),
			varp("Enter the Warriors' Guild", FALADOR_2, 3),
			varp("Equip a Dwarven helmet in the Dwarven Mines", FALADOR_2, 4)));
		tiers.put("elite", List.of(
			varp("Craft air runes at the Air Altar", FALADOR_2, 5),
			varp("Purchase a White 2h sword from Sir Vyvin", FALADOR_2, 6),
			varp("Grow a magic tree in Falador Park and dig up its roots", FALADOR_2, 7),
			varp("Perform the skill cape emote on the top of the White Knights' Castle", FALADOR_2, 8),
			varp("Jump over the Strange Floor in Taverley Dungeon", FALADOR_2, 9),
			varp("Make a Saradomin brew in the Falador East Bank", FALADOR_2, 10)));
		return tiers;
	}

	/**
	 * Tasks of the Fremennik diary, by tier.
	 *
	 * @return the tiers in easy to elite order
	 */
	private static Map<String, List<Task>> fremennik()
	{
		Map<String, List<Task>> tiers = new LinkedHashMap<>();
		tiers.put("easy", List.of(
			varp("Catch a Cerulean Twitch", FREMENNIK_1, 1),
			varp("Change boots", FREMENNIK_1, 2),
			varp("Kill 5 Rock Crabs", FREMENNIK_1, 3),
			varp("Craft Tiara", FREMENNIK_1, 4),
			varp("Browse Stonemason's store", FREMENNIK_1, 5),
			varp("Collect snape grass", FREMENNIK_1, 6),
			varp("Steal from baker's stall", FREMENNIK_1, 7),
			varp("Fill bucket", FREMENNIK_1, 8),
			varp("Enter troll stronghold", FREMENNIK_1, 9),
			varp("Chop and burn", FREMENNIK_1, 10)));
		tiers.put("medium", List.of(
			varp("Brine Rat Slayer", FREMENNIK_1, 11),
			varp("Travel by Eagle", FREMENNIK_1, 12),
			varp("Mine Coal", FREMENNIK_1, 13),
			varp("Steal Fish", FREMENNIK_1, 14),
			varp("Travel to Miscellania", FREMENNIK_1, 15),
			varp("Catch Snowy Knight", FREMENNIK_1, 17),
			varp("Pet Rock", FREMENNIK_1, 18),
			varp("Waterbirth to Lighthouse", FREMENNIK_1, 19),
			varp("Mine Gold", FREMENNIK_1, 20)));
		tiers.put("hard", List.of(
			varp("Teleport to Trollheim", FREMENNIK_1, 21),
			varp("Catch a saber-toothed kyatt", FREMENNIK_1, 23),
			varp("Mix a Super Defense", FREMENNIK_1, 24),
			varp("Steal from Gem Stall", FREMENNIK_1, 25),
			varp("Craft Shield on Neitiznot", FREMENNIK_1, 26),
			varp("Mine Adamantite on Jatizso", FREMENNIK_1, 27),
			varp("Miscellania 100% Support", FREMENNIK_1, 28),
			varp("Teleport to Waterbirth", FREMENNIK_1, 29),
			varp("Free Blast Furnace", FREMENNIK_1, 30)));
		tiers.put("elite", List.of(
			varp("Kill Dagannoth Kings", FREMENNIK_1, 31),
			varp("Astral Runes", FREMENNIK_2, 0),
			varp("Dragonstone Amulet", FREMENNIK_2, 1),
			varp("Rellekka Rooftops", FREMENNIK_2, 2),
			varp("God Wars Generals", FREMENNIK_2, 3),
			varp("Slay Spiritual Mage", FREMENNIK_2, 4)));
		return tiers;
	}

	/**
	 * Tasks of the Kandarin diary, by tier.
	 *
	 * @return the tiers in easy to elite order
	 */
	private static Map<String, List<Task>> kandarin()
	{
		Map<String, List<Task>> tiers = new LinkedHashMap<>();
		tiers.put("easy", List.of(
			varp("Catch a Mackerel", KANDARIN_1, 1),
			varp("Buy a Candle", KANDARIN_1, 2),
			varp("Collect 5 Flax", KANDARIN_1, 3),
			varp("Play the Church Organ", KANDARIN_1, 4),
			varp("Plant Jute", KANDARIN_1, 5),
			varp("Cup of Tea with Galahad", KANDARIN_1, 6),
			varp("Defeat Elementals", KANDARIN_1, 7),
			varp("Get a Pet Fish", KANDARIN_1, 8),
			varp("Buy Stew", KANDARIN_1, 9),
			varp("Talk to Sherlock", KANDARIN_1, 10),
			varp("Log Shortcut", KANDARIN_1, 11)));
		tiers.put("medium", List.of(
			varp("Barbarian Agility Course Lap", KANDARIN_1, 12),
			varp("Mix Superantipoison", KANDARIN_1, 13),
			varp("Enter the Ranging Guild", KANDARIN_1, 14),
			varp("Grapple from Water Obelisk", KANDARIN_1, 15),
			varp("Catch and Cook Bass", KANDARIN_1, 16),
			varp("Teleport to Camelot", KANDARIN_1, 17),
			varp("String Maple Shortbow in Seers' Bank", KANDARIN_1, 18),
			varp("Pick Limpwurt in Catherby", KANDARIN_1, 19),
			varp("Make a Mind Helm", KANDARIN_1, 20),
			varp("Kill a Fire Giant", KANDARIN_1, 21),
			varp("Barbarian Assault Wave", KANDARIN_1, 22),
			varp("Steal from Hemenster Chest", KANDARIN_1, 23),
			varp("Fairy Ring to McGrubor's Woods", KANDARIN_1, 24),
			varp("Mine Coal", KANDARIN_1, 25)));
		tiers.put("hard", List.of(
			varp("Fish a Leaping Sturgeon", KANDARIN_1, 26),
			varp("Seers' Village Rooftop", KANDARIN_1, 27),
			varp("Yew Longbow from Scratch", KANDARIN_1, 28),
			varp("Piety in the Courthouse", KANDARIN_1, 29),
			varp("Charge Water Orb", KANDARIN_1, 30),
			varp("Burn Maple logs with a bow", KANDARIN_1, 31),
			varp("Kill a Shadow Hound", KANDARIN_2, 0),
			varp("Kill a Mithril Dragon", KANDARIN_2, 1),
			varp("Purchase Granite Body", KANDARIN_2, 2),
			varp("Fancy Stone Decoration", KANDARIN_2, 3),
			varp("Smith an Adamant Spear", KANDARIN_2, 4)));
		tiers.put("elite", List.of(
			varp("Level 5 each Role", KANDARIN_2, 5),
			varp("Dwarf Weed in Catherby", KANDARIN_2, 6),
			varp("5 Sharks Caught and Cooked in Catherby", KANDARIN_2, 7),
			varp("Stamina Mix on the Bank", KANDARIN_2, 8),
			varp("Smith Rune Hasta", KANDARIN_2, 9),
			varp("Magic Pyre Ship", KANDARIN_2, 10),
			varp("Teleport to Catherby", KANDARIN_2, 11)));
		return tiers;
	}

	/**
	 * Tasks of the Karamja diary, by tier.
	 *
	 * @return the tiers in easy to elite order
	 */
	private static Map<String, List<Task>> karamja()
	{
		Map<String, List<Task>> tiers = new LinkedHashMap<>();
		tiers.put("easy", List.of(
			varbit("Travel to Port Sarim", VarbitID.ATJUN_EASY_BOAT_SARIM, 1),
			varbit("Pick 5 Bananas", VarbitID.ATJUN_EASY_BANANA, 5),
			varbit("Fish North of Banana Plantation", VarbitID.ATJUN_EASY_FISHING, 1),
			varbit("Attempt the Fight Cave or TzHaar Fight Pits", VarbitID.ATJUN_EASY_TZHAAR, 1),
			varbit("Travel to Ardougne", VarbitID.ATJUN_EASY_BOAT_ARDY, 1),
			varbit("Mine gold", VarbitID.ATJUN_EASY_GOLD, 1),
			varbit("Swing Rope to Moss Giant Isle", VarbitID.ATJUN_EASY_SWING, 1),
			varbit("Pickup 5 Seaweed", VarbitID.ATJUN_EASY_SEAWEED, 5),
			varbit("Explore Cairn Isle", VarbitID.ATJUN_EASY_CAIRN, 1),
			varbit("Kill Jogre in Pothole Dungeon", VarbitID.ATJUN_EASY_JOGRE, 1)));
		tiers.put("medium", List.of(
			varbit("Grow a Healthy Fruit Tree", VarbitID.ATJUN_MED_FARMING, 1),
			varbit("Discover Hidden Wall in Volcano", VarbitID.ATJUN_MED_VOLCANO, 1),
			varbit("Enter Crandor", VarbitID.ATJUN_MED_CRANDOR, 1),
			varbit("Claim a ticket in The Agility Arena", VarbitID.ATJUN_MED_AGILITY, 1),
			varbit("Use Vigroy & Hajedy's Cart Service", VarbitID.ATJUN_MED_CART, 1),
			varbit("Mine Red Topaz", VarbitID.ATJUN_MED_TOPAZ, 1),
			varbit("Travel to Port Khazard", VarbitID.ATJUN_MED_KHAZARD, 1),
			varbit("Use Gnome Glider", VarbitID.ATJUN_MED_GLIDER, 1),
			varbit("Catch a Karambwan", VarbitID.ATJUN_MED_KARAMBWAN, 1),
			varbit("Charter a Ship in The Shipyard", VarbitID.ATJUN_MED_CHARTER, 1),
			varbit("Trap a Horned Graahk", VarbitID.ATJUN_MED_GRAAHK, 1),
			varbit("Earn 100% Favour in Tai Bwo Wannai Cleanup", VarbitID.ATJUN_MED_CLEANUP, 1),
			varbit("Cook a Spider on Stick", VarbitID.ATJUN_MED_SPIDER, 1),
			varbit("Cut a Teak Tree", VarbitID.ATJUN_MED_TEAK, 1),
			varbit("Cut a Mahogany Tree", VarbitID.ATJUN_MED_MAHOGANY, 1),
			varbit("Exchange Gems with Safta Doc", VarbitID.ATJUN_MED_MACHETTE, 1),
			varbit("Chop Vines in Brimhaven Dungeon", VarbitID.ATJUN_MED_SHILO_VINES, 1),
			varbit("Cross The Lava in Brimhaven Dungeon", VarbitID.ATJUN_MED_SHILO_LAVA, 1),
			varbit("Climb The Stairs in Brimhaven Dungeon", VarbitID.ATJUN_MED_SHILO_STAIRS, 1)));
		tiers.put("hard", List.of(
			varbit("Fight Pit Champion", VarbitID.ATJUN_HARD_FIGHTPITS, 1),
			varbit("Kill Ket-Zek", VarbitID.ATJUN_HARD_FIGHTCAVE, 1),
			varbit("Thoroughly Cook Karambwan", VarbitID.ATJUN_HARD_KARAMBWAN, 1),
			varbit("Kill Metal Dragon", VarbitID.ATJUN_HARD_DRAGON, 1),
			varbit("Craft Nature Rune", VarbitID.ATJUN_HARD_NATURE, 1),
			varbit("Get Task From Duradel", VarbitID.ATJUN_HARD_DURADEL, 1),
			varbit("Collect 5 Palm Leaves", VarbitID.ATJUN_HARD_PALM, 5),
			varbit("Kill a Deathwing", VarbitID.ATJUN_HARD_DEATHWING, 1),
			varbit("Use Crossbow Shortcut", VarbitID.ATJUN_HARD_XBOW, 1),
			varbit("Eat Oomlie Wrap", VarbitID.ATJUN_HARD_OOMLIE, 1)));
		tiers.put("elite", List.of(
			varp("Craft 56 Nature Runes", KARAMJA_4, 1),
			varp("Equip Fire / Infernal Cape", KARAMJA_4, 2),
			varp("Check Palm Tree Health", KARAMJA_4, 3),
			varp("Create Antivenom Potion", KARAMJA_4, 4),
			varp("Check Calquat Tree Health", KARAMJA_4, 5)));
		return tiers;
	}

	/**
	 * Tasks of the Kourend diary, by tier.
	 *
	 * @return the tiers in easy to elite order
	 */
	private static Map<String, List<Task>> kourend()
	{
		Map<String, List<Task>> tiers = new LinkedHashMap<>();
		tiers.put("easy", List.of(
			varp("Mine Iron Ore", KOUREND_1, 1),
			varp("Kill Sand Crab", KOUREND_1, 2),
			varp("Hand In A Book", KOUREND_1, 3),
			varp("Steal Some Fruit", KOUREND_1, 4),
			varp("Browse The Warrens Store", KOUREND_1, 5),
			varp("Boat To Land's End", KOUREND_1, 6),
			varp("Pray At Kourend Castle", KOUREND_1, 7),
			varp("Dig Up Some Saltpetre", KOUREND_1, 8),
			varp("Hosidius House", KOUREND_1, 9),
			varp("Run the Agility Course", KOUREND_1, 10),
			varp("Make A Strength Potion", KOUREND_1, 11),
			varp("Fish A Trout", KOUREND_1, 12)));
		tiers.put("medium", List.of(
			varp("Kill A Lizardman", KOUREND_1, 13),
			varp("Travel With Kharedst's Memoirs", KOUREND_1, 14),
			varp("Mine volcanic sulphur", KOUREND_1, 15),
			varp("Arceuus Spellbook", KOUREND_1, 16),
			varp("Repair Crane", KOUREND_1, 17),
			varp("Deliver intelligence", KOUREND_1, 18),
			varp("Catch A Bluegill", KOUREND_1, 19),
			varp("Subdue the Wintertodt", KOUREND_1, 20),
			varp("Enter The Farming Guild", KOUREND_1, 21),
			varp("Leap the boulder", KOUREND_1, 22),
			varp("Catch A Chinchompa", KOUREND_1, 23),
			varp("Chop Mahogany Tree", KOUREND_1, 24),
			varp("Fairy Ring To Mount Karuulm", KOUREND_1, 25)));
		tiers.put("hard", List.of(
			varp("Enter the woodcutting guild", KOUREND_1, 26),
			varp("Smelt some adamantite", KOUREND_1, 27),
			varp("Kill a lizardman shaman", KOUREND_1, 28),
			varp("Mine some lovakite", KOUREND_1, 29),
			varp("Kill a zombie in the crypt", KOUREND_1, 30),
			varp("Plant some logavano seeds", KOUREND_1, 31),
			varp("Teleport to Xeric's Heart", KOUREND_2, 0),
			varp("Deliver an artifact", KOUREND_2, 1),
			varp("Slay a wyrm", KOUREND_2, 2),
			varp("Cast Monster Examine", KOUREND_2, 3)));
		tiers.put("elite", List.of(
			varp("Craft Blood Rune", KOUREND_2, 4),
			varp("Chop Redwood Logs", KOUREND_2, 5),
			varp("Defeat Skotizo", KOUREND_2, 6),
			varp("Catch And Cook Anglerfish", KOUREND_2, 7),
			varp("Kill Hydra", KOUREND_2, 8),
			varp("Ape Atoll Teleport Tablet", KOUREND_2, 9),
			varp("Complete Chambers Of Xeric Raid", KOUREND_2, 10),
			varp("Battlestaff From Scratch", KOUREND_2, 11)));
		return tiers;
	}

	/**
	 * Tasks of the Lumbridge diary, by tier.
	 *
	 * @return the tiers in easy to elite order
	 */
	private static Map<String, List<Task>> lumbridge()
	{
		Map<String, List<Task>> tiers = new LinkedHashMap<>();
		tiers.put("easy", List.of(
			varp("Draynor Rooftops", LUMBRIDGE_1, 1),
			varp("Kill Cave Bug", LUMBRIDGE_1, 2),
			varp("Rune Essence Mine", LUMBRIDGE_1, 3),
			varp("Craft Water Runes", LUMBRIDGE_1, 4),
			varp("Learn Age from Hans", LUMBRIDGE_1, 5),
			varp("Pickpocket Man or Woman", LUMBRIDGE_1, 6),
			varp("Chop and Burn Oak Logs", LUMBRIDGE_1, 7),
			varp("Kill Zombie in Draynor Sewers", LUMBRIDGE_1, 8),
			varp("Fish Anchovies in Al-Kharid", LUMBRIDGE_1, 9),
			varp("Cooking Bread", LUMBRIDGE_1, 10),
			varp("Mine Iron in Al-Kharid", LUMBRIDGE_1, 11),
			varp("Enter the HAM Hideout", LUMBRIDGE_1, 12)));
		tiers.put("medium", List.of(
			varp("Al Kharid Rooftop Course", LUMBRIDGE_1, 13),
			varp("Grapple River Lum", LUMBRIDGE_1, 14),
			varp("Ava's Accumulator", LUMBRIDGE_1, 15),
			varp("Travel to Wizards' Tower by Fairy Ring", LUMBRIDGE_1, 16),
			varp("Teleport to Lumbridge", LUMBRIDGE_1, 17),
			varp("Catch Salmon", LUMBRIDGE_1, 18),
			varp("Craft a coif in the cow pen", LUMBRIDGE_1, 19),
			varp("Chop Willow", LUMBRIDGE_1, 20),
			varp("Pickpocket Master Gardener", LUMBRIDGE_1, 21),
			varp("Slayer Task from Chaeldar", LUMBRIDGE_1, 22),
			varp("Catch Essence or Eclectic Imp", LUMBRIDGE_1, 23),
			varp("Craft Lava Runes", LUMBRIDGE_1, 24)));
		tiers.put("hard", List.of(
			varp("Bones to Peaches Palace", LUMBRIDGE_1, 25),
			varp("Jutting Wall", LUMBRIDGE_1, 26),
			varp("56 Cosmic Runes", LUMBRIDGE_1, 27),
			varp("Waka to Edgeville", LUMBRIDGE_1, 28),
			varp("100 Tears", LUMBRIDGE_1, 29),
			varp("Dorgesh Train", LUMBRIDGE_1, 30),
			varp("Barrows Gloves", LUMBRIDGE_1, 31),
			varp("Belladonna", LUMBRIDGE_2, 0),
			varp("Mining Helmet", LUMBRIDGE_2, 1),
			varp("Smite Altar", LUMBRIDGE_2, 2),
			varp("Power Amulet", LUMBRIDGE_2, 3)));
		tiers.put("elite", List.of(
			varp("Dorgesh-Kaan Rich Chest", LUMBRIDGE_2, 4),
			varp("Grapple Dorgesh-Kaan Pylon", LUMBRIDGE_2, 5),
			varp("Chop Magics", LUMBRIDGE_2, 6),
			varp("Adamantite Platebody", LUMBRIDGE_2, 7),
			varp("140 Water Runes", LUMBRIDGE_2, 8),
			varp("Quest Cape Emote", LUMBRIDGE_2, 9)));
		return tiers;
	}

	/**
	 * Tasks of the Morytania diary, by tier.
	 *
	 * @return the tiers in easy to elite order
	 */
	private static Map<String, List<Task>> morytania()
	{
		Map<String, List<Task>> tiers = new LinkedHashMap<>();
		tiers.put("easy", List.of(
			varp("Craft Snelm", MORYTANIA_1, 1),
			varp("Cook Thin Snail", MORYTANIA_1, 2),
			varp("Mazchna Slayer Task", MORYTANIA_1, 3),
			varp("Kill Banshee", MORYTANIA_1, 4),
			varp("Sbott Tanning Hide", MORYTANIA_1, 5),
			varp("Enter Mort Myre Swamp", MORYTANIA_1, 6),
			varp("Kill Ghoul", MORYTANIA_1, 7),
			varp("Place Scarecrow", MORYTANIA_1, 8),
			varp("Offer Bonemeal", MORYTANIA_1, 9),
			varp("Kill Werewolf in Human Form", MORYTANIA_1, 10),
			varp("Restore Prayer", MORYTANIA_1, 11)));
		tiers.put("medium", List.of(
			varp("Swamp Lizard", MORYTANIA_1, 12),
			varp("Canifis Rooftop Course", MORYTANIA_1, 13),
			varp("Hollow Tree", MORYTANIA_1, 14),
			varp("Dragontooth Island", MORYTANIA_1, 15),
			varp("Terror Dog", MORYTANIA_1, 16),
			varp("Trouble Brewing", MORYTANIA_1, 17),
			varp("Swamp Boaty", MORYTANIA_1, 18),
			varp("Cannonballs", MORYTANIA_1, 19),
			varp("Fever Spider", MORYTANIA_1, 20),
			varp("Ectophial", MORYTANIA_1, 21),
			varp("Guthix Balance", MORYTANIA_1, 22)));
		tiers.put("hard", List.of(
			varp("Kharyrll Portal", MORYTANIA_1, 23),
			varp("Advanced Spike Chain", MORYTANIA_1, 24),
			varp("Harmony Watermelons", MORYTANIA_1, 25),
			varp("Chop and Burn Mahogany", MORYTANIA_1, 26),
			varp("Hard Temple Trekk", MORYTANIA_1, 27),
			varp("Kill Cave Horror", MORYTANIA_1, 28),
			varp("Bittercap Mushrooms", MORYTANIA_1, 29),
			varp("Nature Grotto Piety", MORYTANIA_1, 30),
			varp("Shortcut Over the Salve", MORYTANIA_2, 1),
			varp("Haunted Mithril Ore", MORYTANIA_2, 2)));
		tiers.put("elite", List.of(
			varp("Grab Shark", MORYTANIA_2, 3),
			varp("Cremate Shade Remains", MORYTANIA_2, 4),
			varp("Fertilize Herb Patch", MORYTANIA_2, 5),
			varp("Craft Black D'hide Body", MORYTANIA_2, 6),
			varp("Kill Abyssal Demon", MORYTANIA_2, 7),
			varp("Barrows Looting", MORYTANIA_2, 8)));
		return tiers;
	}

	/**
	 * Tasks of the Varrock diary, by tier.
	 *
	 * @return the tiers in easy to elite order
	 */
	private static Map<String, List<Task>> varrock()
	{
		Map<String, List<Task>> tiers = new LinkedHashMap<>();
		tiers.put("easy", List.of(
			varp("Browse Thessalia's Store", VARROCK_1, 1),
			varp("Teleport to Essence Mine", VARROCK_1, 2),
			varp("Mine Iron South East", VARROCK_1, 3),
			varp("Make Plank", VARROCK_1, 4),
			varp("Stronghold Second Floor", VARROCK_1, 5),
			varp("Jump the Fence", VARROCK_1, 6),
			varp("Chop Down Dying Tree", VARROCK_1, 7),
			varp("Buy Newspaper", VARROCK_1, 8),
			varp("Give a Dog a Bone", VARROCK_1, 9),
			varp("Spin a Bowl in Barbarian Village", VARROCK_1, 10),
			varp("Speak to Haig Halen", VARROCK_1, 11),
			varp("Craft an Earth Rune", VARROCK_1, 12),
			varp("Fish a Trout", VARROCK_1, 13),
			varp("Steal from the Tea Stall", VARROCK_1, 14)));
		tiers.put("medium", List.of(
			varp("Apothecary Strength Potion", VARROCK_1, 15),
			varp("Enter Champions' Guild", VARROCK_1, 16),
			varp("Select Cat's Colour", VARROCK_1, 18),
			varp("Use The Spirit Tree", VARROCK_1, 19),
			varp("Use 4 Stronghold Emotes", VARROCK_1, 20),
			varp("Enter The Tolna Dungeon", VARROCK_1, 21),
			varp("Teleport to The Digsite", VARROCK_1, 22),
			varp("Teleport to Varrock", VARROCK_1, 23),
			varp("Slayer Task From Vannaka", VARROCK_1, 24),
			varp("Make 20 Mahogany Planks", VARROCK_1, 25),
			varp("Pick a White Fruit", VARROCK_1, 26),
			varp("Leave Varrock in a Balloon", VARROCK_1, 27),
			varp("Rooftop Course Lap", VARROCK_1, 28)));
		tiers.put("hard", List.of(
			varp("Trade for A Spottier Cape", VARROCK_1, 29),
			varp("Speak with Orlando With 153+ Kudos", VARROCK_1, 30),
			varp("Edgeville Wakka", VARROCK_1, 31),
			varp("Teleport to Paddewwa", VARROCK_2, 0),
			varp("Teleport with Skull Sceptre", VARROCK_2, 1),
			varp("Cut and Burn Yew Logs Atop the Church", VARROCK_2, 2),
			varp("Fancy Stone", VARROCK_2, 3),
			varp("Yew Roots", VARROCK_2, 4),
			varp("Altar Smited", VARROCK_2, 5),
			varp("Obstacle Pipe", VARROCK_2, 6)));
		tiers.put("elite", List.of(
			varp("Make Super Combat", VARROCK_2, 7),
			varp("Plank Make", VARROCK_2, 8),
			varp("Summer Pie", VARROCK_2, 9),
			varp("Smith and Fletch 10 Rune Darts", VARROCK_2, 10),
			varp("Craft 100 Earth runes", VARROCK_2, 11)));
		return tiers;
	}

	/**
	 * Tasks of the Western diary, by tier.
	 *
	 * @return the tiers in easy to elite order
	 */
	private static Map<String, List<Task>> western()
	{
		Map<String, List<Task>> tiers = new LinkedHashMap<>();
		tiers.put("easy", List.of(
			varp("Copper Longtail", WESTERN_1, 1),
			varp("Novice Pest Control", WESTERN_1, 2),
			varp("Mine Iron", WESTERN_1, 3),
			varp("Gnome Agility Course", WESTERN_1, 4),
			varp("Gnome Ball Goal", WESTERN_1, 5),
			varp("Chompy Bird Hat", WESTERN_1, 6),
			varp("Teleport to Pest Control", WESTERN_1, 7),
			varp("Collect Swamp Toad", WESTERN_1, 8),
			varp("Brimstail Essence Teleport", WESTERN_1, 9),
			varp("Fletch Oak Shortbow in Gnome Stronghold", WESTERN_1, 10),
			varp("Kill Terrorbird", WESTERN_1, 11)));
		tiers.put("medium", List.of(
			varp("Agility Shortcut To Otto", WESTERN_1, 12),
			varp("Spirit Tree To Gnome Stronghold", WESTERN_1, 13),
			varp("Spined Larupia", WESTERN_1, 14),
			varp("Bass On Ape Atoll", WESTERN_1, 15),
			varp("Teaks On Ape Atoll", WESTERN_1, 16),
			varp("Intermediate Pest Control", WESTERN_1, 17),
			varp("Glider To Feldip Hills", WESTERN_1, 18),
			varp("Chompy Bird Hat", WESTERN_1, 19),
			varp("Eagle To Feldip Hills", WESTERN_1, 20),
			varp("Chocolate Bomb", WESTERN_1, 21),
			varp("Gnome Restaurant Delivery", WESTERN_1, 22),
			varp("Crystal Saw", WESTERN_1, 23),
			varp("Gold Under The Grand Tree", WESTERN_1, 24)));
		tiers.put("hard", List.of(
			varp("Kill Elf With Crystal Bow", WESTERN_1, 25),
			varp("Catch And Cook Monkfish", WESTERN_1, 26),
			varp("Veteran Pest Control", WESTERN_1, 27),
			varp("Dashing Kebbit", WESTERN_1, 28),
			varp("Ape Atoll Agility Course", WESTERN_1, 29),
			varp("Mahogany On Ape Atoll", WESTERN_1, 30),
			varp("Mine Adamantite in Tirannwn", WESTERN_1, 31),
			varp("Lletya Palm tree", WESTERN_2, 0),
			varp("Chompy Bird Hat", WESTERN_2, 1),
			varp("Isafdar Painting In POH", WESTERN_2, 2),
			varp("Kill Zulrah", WESTERN_2, 3),
			varp("Teleport To Ape Atoll", WESTERN_2, 4),
			varp("Pickpocket A Gnome", WESTERN_2, 5)));
		tiers.put("elite", List.of(
			varp("Magic Longbow In Tirannwn", WESTERN_2, 6),
			varp("Thermonuclear Smoke Devil", WESTERN_2, 7),
			varp("Prissy Scilla's Protection", WESTERN_2, 8),
			varp("Advanced Elven Shortcut", WESTERN_2, 9),
			varp("Equip Full Void Set", WESTERN_2, 12),
			varp("Chompy Bird Hat", WESTERN_2, 13),
			varp("Pickpocket An Elf", WESTERN_2, 14)));
		return tiers;
	}

	/**
	 * Tasks of the Wilderness diary, by tier.
	 *
	 * @return the tiers in easy to elite order
	 */
	private static Map<String, List<Task>> wilderness()
	{
		Map<String, List<Task>> tiers = new LinkedHashMap<>();
		tiers.put("easy", List.of(
			varp("Free Low Alchemy", WILDERNESS_1, 1),
			varp("Wilderness Lever", WILDERNESS_1, 2),
			varp("Pray at Chaos Altar", WILDERNESS_1, 3),
			varp("Chaos RC Altar", WILDERNESS_1, 4),
			varp("Mammoth", WILDERNESS_1, 5),
			varp("Earth Warrior", WILDERNESS_1, 6),
			varp("Demonic Ruins", WILDERNESS_1, 7),
			varp("The Lair", WILDERNESS_1, 8),
			varp("Spider Eggs", WILDERNESS_1, 9),
			varp("Iron Ore", WILDERNESS_1, 10),
			varp("Enter the Abyss", WILDERNESS_1, 11),
			varp("Team Cape", WILDERNESS_1, 12)));
		tiers.put("medium", List.of(
			varp("Mine Mithril", WILDERNESS_1, 13),
			varp("Ent Yew", WILDERNESS_1, 14),
			varp("Enter Wilderness God Wars", WILDERNESS_1, 15),
			varp("Wilderness Agility Course", WILDERNESS_1, 16),
			varp("Kill Green Dragon", WILDERNESS_1, 18),
			varp("Kill Ankou", WILDERNESS_1, 19),
			varp("Earth Orb", WILDERNESS_1, 20),
			varp("Kill Bloodveld in God Wars Dungeon", WILDERNESS_1, 21),
			varp("Emblem Trader", WILDERNESS_1, 22),
			varp("Gold Helmet in Resource Area", WILDERNESS_1, 23),
			varp("Muddy Chest", WILDERNESS_1, 24)));
		tiers.put("hard", List.of(
			varp("God Spell on Player", WILDERNESS_1, 25),
			varp("Air Orb", WILDERNESS_1, 26),
			varp("Black Salamander", WILDERNESS_1, 27),
			varp("Adamant Scimitar in Resource Area", WILDERNESS_1, 28),
			varp("Lava Dragon and Bury", WILDERNESS_1, 29),
			varp("Chaos Elemental", WILDERNESS_1, 30),
			varp("Three Bosses", WILDERNESS_1, 31),
			varp("Agility Shortcut", WILDERNESS_2, 0),
			varp("Spiritual Warrior", WILDERNESS_2, 1),
			varp("Fishing a Raw Lava Eel", WILDERNESS_2, 2)));
		tiers.put("elite", List.of(
			varp("Three bosses", WILDERNESS_2, 3),
			varp("Teleport to Ghorrock", WILDERNESS_2, 5),
			varp("Dark Crab in Resource Area", WILDERNESS_2, 7),
			varp("Rune Scimitar in Resource Area", WILDERNESS_2, 8),
			varp("Rogues' Castle Chest", WILDERNESS_2, 9),
			varp("Spiritual Mage", WILDERNESS_2, 10),
			varp("Chop and Burn Magic Logs", WILDERNESS_2, 11)));
		return tiers;
	}
}
