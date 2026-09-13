<p align="center">
  <img src="banner.png" alt="Player Data Export banner">
</p>
<h1 align="center">Player Data Export</h1>

Player Data Export saves your character's data — skills, bank, inventory, quests, and more — to a
plain JSON file on your own computer, refreshed as you play. Any local tool you like can read that
file: a spreadsheet importer, a script, a personal dashboard, or a local assistant. The plugin makes
**no network calls of any kind** — nothing leaves your machine.

## Features

### Local, private, and readable

- **One JSON file per character.** Data is written to a file named after your character (by default
  under `.runelite/osrs-companion/`), so each account stays separate.

- **Never over the network.** The plugin only ever writes a local file. There is no HTTP client, no
  socket, and nothing is uploaded — your data stays on your computer.

- **Safe to read at any time.** Each save is written to a temporary file and then renamed into place,
  so a program reading the file never catches a half-written document.

### What it exports

- **Character and account.** Name, combat level, world, account type (ironman and friends), and
  members status.

- **Skills and vitals.** Real level, experience, and current boosted level for every skill, plus
  current hitpoints, prayer, and run energy.

- **Items.** Bank contents (captured when you open the bank), your current inventory, and worn
  equipment.

- **Progress.** Quest states and total quest points, achievement-diary completion for every region,
  and combat-achievement tier progress.

- **Activity.** Current slayer task, your world location, and any active Grand Exchange offers.

### Yours to tune

- **Toggle every category.** A checkbox for each kind of data, so you export only what you want.

- **Your interval, your folder.** Choose how often the file is saved and, if you like, a custom
  folder to save it into.

The exported JSON is a documented, versioned contract — see [`SCHEMA.md`](SCHEMA.md) for every field.

## Links

- [Report a bug](https://github.com/Oveduumnakal/runelite-player-data-export/issues/new?template=bug_report.yml)
- [Request a feature](https://github.com/Oveduumnakal/runelite-player-data-export/issues/new?template=feature_request.yml)
- [Buy me a coffee](https://buymeacoffee.com/oveduumnakal)
