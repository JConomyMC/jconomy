# JConomy Default Implementations

The internal implementation module for [JConomy](../README.md). Provides production-ready default implementations for all core abstractions defined in `jconomy-api`.

## Purpose

This module contains:

- **Default implementations** for storage, caching, account/balance access, and configuration
- **Bootstrap wiring** via `JConomyImplRegistrar` — centralized registration of all default services
- **Comprehensive test coverage** — 123 tests validating core behavior in isolation

The plugin module (`jconomy-plugin`) delegates to this module's bootstrap registrar during service setup, ensuring consistent behavior across all JConomy instances.

## Architecture

### Storage & Caching
- `SqliteConnectionFactory`, `SqliteMigrator` — SQLite schema management and connections
- `SqliteAccountRepository`, `SqliteBalanceRepository` — persistent storage
- `LruAccountCache`, `LruBalanceCache` — configurable LRU eviction with dirty-balance tracking

### Data Access
- `DefaultAccountAccess` — cache-aside account reads, write-through updates
- `DefaultBalanceAccess` — cache-aside reads, write-behind with automatic flush on dirty records

### Configuration
- `DefaultCacheConfig` — LRU limit and periodic flush settings
- `DefaultFeatureManager` — feature flags (currently unused; reserved for future feature gating)
- `DefaultVaultLegacyAdapterConfig` — legacy Vault adapter enabled/disabled flag
- `DefaultJConomyConfig` — main config wrapper around Bukkit YAML
- `YamlEconomyConfig` — per-currency formatting and display names

### Presentation
- `DefaultNumberFormatter` — grouping and fractional digit formatting
- `DefaultCurrencyFormatter` — currency symbol and name substitution

### Bootstrap
- `JConomyImplRegistrar` — registers all defaults with a `JConomyServiceBuilder`
  - Called by the plugin during `JConomyServiceRegistrar.registerServices()`
  - Ensures consistent DI wiring across installations

## Testing

Each implementation has unit tests with mocked or in-memory dependencies:
- Mock-based tests for pure logic (e.g., `DefaultCacheConfig`)
- In-memory SQLite tests for storage (e.g., `SqliteAccountRepositoryTests`)
- Integration-style tests for data access (e.g., `DefaultBalanceAccessTests`)

Run tests:
```bash
mvn -pl jconomy-impl test
```

## Dependencies

- **jconomy-api** — contracts
- **Bukkit API (provided)** — for config section wrappers
- **SQLite JDBC** — in-memory and file-based storage
- **JUnit 5 + Mockito** — testing
- **commons-lang** — utility functions
- **com.jellyrekt.storage** — config provider wrappers

## Not in this module

Plugin-specific concerns stay in `jconomy-plugin`:
- Command registration and execution
- Event listeners (player join, etc.)
- Extension loading
- Service registration orchestration

Extension-facing contracts stay in `jconomy-api`:
- Service abstractions (`AccountRepository`, `BalanceRepository`, etc.)
- Extension lifecycle (`JConomyExtension`)
- Dependency injection API (`JConomyServiceBuilder`, `JConomyServiceProvider`)
