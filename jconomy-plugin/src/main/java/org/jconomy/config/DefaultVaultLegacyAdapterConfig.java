package org.jconomy.config;

public class DefaultVaultLegacyAdapterConfig implements VaultLegacyAdapterConfig {
    private final JConomyConfig config;

    public DefaultVaultLegacyAdapterConfig(JConomyConfig config) {
        this.config = config;
    }

    @Override
    public boolean isEnabled() {
        return config.getSection("vault-legacy-adapter").getBoolean("enabled", false);
    }
}
