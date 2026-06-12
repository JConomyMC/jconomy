# JConomy Plugin

A Spigot economy provider for Minecraft 1.21+. JConomy handles currency storage, formatting, and balance management — it does not add any in-game commands. It is designed to pair with a command plugin such as [EssentialsX](https://essentialsx.net/) or [CMI](https://www.zrips.net/cmi/) that provides `/balance`, `/pay`, `/eco give`, and similar commands.

## Requirements

- Spigot / Paper 1.21+
- Java 21+
- [VaultUnlocked](https://github.com/TheNewEconomy/VaultUnlocked) (and optionally legacy [Vault](https://github.com/MilkBowl/VaultAPI) for broad plugin compatibility)

## Features

- **Multi-currency** — define any number of currencies in `config.yml`
- **Per-world accounts** — balances are tracked per world, with a configurable default world
- **Flexible formatting** — configure grouping separators, decimal places, rounding, symbols, and a free-form display template per currency
- **Vault and VaultUnlocked** — registers as an economy provider for both legacy Vault and VaultUnlocked, maximising compatibility with economy-consuming plugins
- **Account caching** — in-memory cache with a configurable size limit keeps frequent lookups off disk
- **Expansion API** — extend JConomy with custom storage backends, importers, or services without forking the plugin (see [jconomy-api](jconomy-api/README.md))

## Installation

1. Drop `JConomy.jar` into your `plugins/` folder
2. Install VaultUnlocked (and Vault if needed)
3. Start the server — `plugins/JConomy/config.yml` is created automatically
4. Configure your currencies and restart

## Configuration overview

The generated `config.yml` is fully commented. Key settings:

| Key | Description |
|---|---|
| `default-world` | World used for a player's default balance |
| `default-currency` | Primary currency key (used by legacy Vault) |
| `currencies.<name>` | Define a currency with display names, symbol, format string, and number formatter |
| `cache.lru-limit` | Maximum number of accounts held in memory at once |

## Importing from another plugin

JConomy supports data importers via the expansion API. An importer runs once on startup and is tracked in `config.yml` under `is-importer-completed`. Set a completed importer's value back to `false` to re-run it.

## Expansion API

Developers can extend JConomy — adding custom importers, storage backends, or services — through the expansion API. See the [jconomy-api README](jconomy-api/README.md) for an overview.
