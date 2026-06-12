# JConomy

> Design economies, not storage systems.

JConomy lets you create and manage as many currencies as your server needs from a single configuration. Just define your currencies and build the gameplay around them.

JConomy is an economy backend for Minecraft servers running Spigot, Paper, and compatible platforms—[see requirements for more info](#requirements).

## Why JConomy?

When servers need additional currencies, they often end up building new plugins, each manging its own configuration, storage, formatting, and migration logic. Over time, economy infrastructure becomes fragmented and increasingly difficult to maintain.

Every new currency shouldn't require a new plugin.

With JConomy, you define your currencies once and expose them through a shared economy backend. Your custom plugins build against VaultUnlocked using the currencies they need, while JConomy handles persistence, configuration, formatting, and storage.

No duplicate infrastructure. No reinventing economy systems.

### What it is

JConomy is an Economy provider.

It provides the infrastructure needed to define, persist, and expose your currencies to other plugins. It handles
- Persistent storage
- Currency definition and formatting
- [VaultUnlocked](https://github.com/TheNewEconomy/VaultUnlockedAPI and [Vault classic](https://github.com/MilkBowl/Vault) integration
- Exposing currencies to both Unlocked-aware and classic-aware plugins
- An expansion API for modifying functionality without forking JConomy

### What it isn't

JConomy is **not** a complete economy suite.

It intentially does not provide any player-facing or admin-facing interface for paying players or checking balances.

As a pure infrastructure provider, JConomy will pair any Vault-aware plugin to bring you full economy support, e.g.
- [Essentials](https://essentialsx.net/)
- [CMI](https://www.zrips.net/cmi/)

### Who is it for

JConomy is designed for server owners and plugin developers who need more flexibility than traditional single-currency economy plugins provide. Choose JConomy when

#### You have more than one currency

Your server uses multiple currencies, such as
* Gold
* Gems
* Event tokens
* Premium currencies

Instead of introducing a new storage solution for every currency, JConomy gives you a single place to define and manage them all.

#### You build plugins with custom economy mechanics

Maybe you
* Award tokens for participating in events
* Rotate seasonal currencies
* Bank money, xp, or items in a virtual vault
* Introduce entirely new ways for players to earn and spend money

JConomy provides the infrastructure for all of these use cases, allowing you to keep your plugin focused on gameplay logic.

#### You want flexibility without complexity

JConomy ships with a Sqlite storage backend out of the box and provides a simple configuration structure.

You don't need to design a database schema or build a persistence layer just to introduce a new currency.

#### Is JConomy for me?

JConomy is probably a good fit if your server uses **multiple currencies**, **custom economy mechanics**, or **plugins that maintain their own balance data**.

If you're happy with a single currency managed entirely by an existing economy plugin, JConomy may be more flexibility than you need.

## Requirements

- Spigot / Paper 1.21+
- Java 21+
- [VaultUnlocked](https://github.com/TheNewEconomy/VaultUnlocked) (and optionally classic [Vault](https://github.com/MilkBowl/VaultAPI) for broad plugin compatibility)

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

| Key                 | Description                                                                       |
|---------------------|-----------------------------------------------------------------------------------|
| `default-world`     | World used for a player's default balance                                         |
| `default-currency`  | Primary currency key (used by legacy Vault)                                       |
| `currencies.<name>` | Define a currency with display names, symbol, format string, and number formatter |
| `cache.lru-limit`   | Maximum number of accounts held in memory at once                                 |

## Importing from another plugin

**Coming or going?** The JConomy API supports data transfer expansions, allowing you to import data from your current Vault provider or export it to your new one.

See [officially supported transfer expansions](https://github.com/JConomyMC/jconomy-import/) or [roll your own](#)!

## Expansion API

Developers can extend JConomy — adding custom importers, storage backends, or services — through the expansion API. See the [API README](jconomy-api/README.md) for an overview.
