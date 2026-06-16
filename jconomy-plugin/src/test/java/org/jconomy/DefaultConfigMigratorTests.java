package org.jconomy;

import static org.mockito.Mockito.*;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultConfigMigratorTests {

    private JavaPlugin plugin;
    private FileConfiguration config;
    private DefaultConfigMigrator migrator;

    @BeforeEach
    void setUp() {
        config = mock(FileConfiguration.class);
        plugin = mock(JavaPlugin.class);
        when(plugin.getConfig()).thenReturn(config);
        migrator = new DefaultConfigMigrator(plugin);
    }

    @Test
    void migrate_saves_default_config() {
        when(config.getInt("config-version", 0)).thenReturn(1);

        migrator.migrate();

        verify(plugin).saveDefaultConfig();
    }

    @Test
    void migrate_saves_config_after_migrations() {
        when(config.getInt("config-version", 0)).thenReturn(1);

        migrator.migrate();

        verify(plugin).saveConfig();
    }

    @Test
    void migrate_sets_lru_limit_default_when_absent_on_version_0_config() {
        when(config.getInt("config-version", 0)).thenReturn(0);
        when(config.contains("cache.lru-limit")).thenReturn(false);

        migrator.migrate();

        verify(config).set("cache.lru-limit", 10000);
        verify(config).set("config-version", 1);
    }

    @Test
    void migrate_does_not_overwrite_existing_lru_limit_on_version_0_config() {
        when(config.getInt("config-version", 0)).thenReturn(0);
        when(config.contains("cache.lru-limit")).thenReturn(true);

        migrator.migrate();

        verify(config, never()).set(eq("cache.lru-limit"), any());
        verify(config).set("config-version", 1);
    }

    @Test
    void migrate_sets_periodic_flush_enabled_default_when_absent_on_version_0_config() {
        when(config.getInt("config-version", 0)).thenReturn(0);
        when(config.contains("cache.periodic-flush.enabled")).thenReturn(false);

        migrator.migrate();

        verify(config).set("cache.periodic-flush.enabled", true);
        verify(config).set("config-version", 1);
    }

    @Test
    void migrate_sets_periodic_flush_interval_ticks_default_when_absent_on_version_0_config() {
        when(config.getInt("config-version", 0)).thenReturn(0);
        when(config.contains("cache.periodic-flush.interval-ticks")).thenReturn(false);

        migrator.migrate();

        verify(config).set("cache.periodic-flush.interval-ticks", 1200);
        verify(config).set("config-version", 1);
    }

    @Test
    void migrate_does_not_overwrite_existing_periodic_flush_enabled_on_version_0_config() {
        when(config.getInt("config-version", 0)).thenReturn(0);
        when(config.contains("cache.periodic-flush.enabled")).thenReturn(true);

        migrator.migrate();

        verify(config, never()).set(eq("cache.periodic-flush.enabled"), any());
        verify(config).set("config-version", 1);
    }

    @Test
    void migrate_does_not_overwrite_existing_periodic_flush_interval_ticks_on_version_0_config() {
        when(config.getInt("config-version", 0)).thenReturn(0);
        when(config.contains("cache.periodic-flush.interval-ticks")).thenReturn(true);

        migrator.migrate();

        verify(config, never()).set(eq("cache.periodic-flush.interval-ticks"), any());
        verify(config).set("config-version", 1);
    }

    @Test
    void migrate_skips_version_1_migration_when_already_at_version_1() {
        when(config.getInt("config-version", 0)).thenReturn(1);

        migrator.migrate();

        verify(config, never()).set(eq("cache.lru-limit"), any());
        verify(config, never()).set(eq("cache.periodic-flush.enabled"), any());
        verify(config, never()).set(eq("cache.periodic-flush.interval-ticks"), any());
    }
}
