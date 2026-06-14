# JConomy

_Design economies, not storage systems._

JConomy is a shared economy infrastructure layer for Minecraft servers running Spigot, Paper, and compatible platforms.

**The next gameplay system shouldn't require another persistence implementation.**

JConomy provides a shared backend for currencies and other quantity-like systems, allowing plugins to focus on gameplay while JConomy handles persistence, formatting, and integration.

**Think beyond money.** Gold. Event tokens. Faction reputation. Guild prestige. Honor. Seasonal points. Premium credits.

If your plugin needs to track a number that belongs to a player, JConomy can provide the infrastructure behind it.

## Why JConomy?

Minecraft servers often evolve beyond a single currency.

You might introduce:

- Premium currencies
- Event tokens
- Faction reputation
- Guild prestige
- Seasonal points
- Dungeon keys
- Honor systems
- Custom reward systems

As servers grow, new gameplay systems often bring new persistence layers with them.

One plugin stores balances in YAML. Another uses SQLite. A third defines its own database schema.

Over time, economy infrastructure becomes fragmented and increasingly difficult to maintain.

JConomy centralizes that infrastructure so that new systems can build on a shared foundation instead of reinventing it.

Your plugins remain responsible for gameplay mechanics. JConomy takes care of the plumbing.

**No duplicate infrastructure. No reinventing economy systems.**

## What it is

JConomy is a shared economy infrastructure layer.

It provides the services needed to define, persist, and expose currencies and other balance-like systems to plugins throughout your server ecosystem.

JConomy handles:

- Persistent storage
- Currency definition and formatting
- VaultUnlocked integration
- Optional Vault compatibility for legacy plugins
- Shared account management
- Storage backend abstraction
- An expansion API for extending functionality without forking the plugin

## What it isn't

JConomy is **not** a complete economy suite.

It intentionally does not provide:

- `/pay`
- `/balance`
- `/eco`
- `/baltop`
- Administrative economy commands
- Economy GUIs

Those are application concerns.

Different currencies often have different rules governing how they are earned, transferred, consumed, and administered. JConomy focuses exclusively on shared infrastructure, leaving gameplay behavior to the plugins that define it.

Continue using the economy applications and commands that already fit your server.

## Who is it for?

### Plugin developers

You build plugins that track balances, points, reputation, prestige, influence, or other quantity-like values.

Instead of creating another repository, database schema, migration strategy, and formatting system, build against VaultUnlocked and let JConomy handle the infrastructure.

### Growing servers

Your server already has multiple plugins maintaining their own balance data, and you'd prefer future systems to share a common backend.

JConomy lets you stop creating new silos without requiring you to migrate existing ones.

### Existing economy users

Already happy with Essentials or another traditional economy plugin?

Keep it.

JConomy does not require you to replace your existing economy provider. Existing systems can continue to operate exactly as they do today while new systems take advantage of shared infrastructure.

## Is JConomy for me?

JConomy is probably a good fit if:

- You're developing custom plugins with economy mechanics.
- Your server uses multiple balance-like systems.
- You want new gameplay systems to share common infrastructure.
- You're tired of implementing persistence every time you introduce a new currency, reputation system, or point-based mechanic.

JConomy may be more flexibility than you need if:

- Your server uses a single currency.
- You're happy with your existing economy plugin.
- Your server relies entirely on off-the-shelf plugins.
- You have no plans to introduce custom balance-driven systems.

JConomy is infrastructure.

If you're building custom gameplay experiences, JConomy gives those systems a shared foundation. If your existing setup already meets your needs, there's no reason to replace it.

## Requirements

- Spigot / Paper 1.21+
- Java 21+
- VaultUnlocked
- Vault (optional, for legacy compatibility)

## Features

- **Multi-currency support** — Define any number of currencies in `config.yml`.
- **Per-world accounts** — Track balances per world, with configurable default-world behavior.
- **Flexible formatting** — Configure symbols, grouping separators, decimal precision, rounding, and display templates.
- **VaultUnlocked integration** — Expose currencies to VaultUnlocked-aware plugins.
- **Optional Vault compatibility** — Expose a selected currency to legacy Vault plugins when explicitly enabled.
- **Account caching** — Reduce disk access through configurable in-memory caching.
- **Storage abstraction** — Separate gameplay logic from persistence concerns.
- **Expansion API** — Extend JConomy with custom storage backends and integrations without forking the project.

## Installation

1. Drop `JConomy.jar` into your `plugins/` folder.
1. Install VaultUnlocked.
1. Start the server to generate the default configuration.
1. Define your currencies and restart the server.

## Configuration overview

The generated `config.yml` is fully documented.

Key settings include:

| Key                    | Description                                             |
|------------------------|---------------------------------------------------------|
| `default-world`        | World used for default balance lookups.                 |
| `default-currency`     | Currency exposed through the optional Vault adapter.    |
| `currencies.<name>`    | Currency definitions, formatting, and display settings. |
| `cache.lru-limit`      | Maximum number of cached accounts.                      |
| `legacy-vault.enabled` | Whether to register the legacy Vault adapter.           |

## Expansion API

JConomy provides an extension model for adding new capabilities without modifying the core plugin.

Examples include:

- Additional storage backends
- Custom integrations
- Supplemental services

See the API documentation for more information.
