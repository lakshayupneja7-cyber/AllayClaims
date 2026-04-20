package com.allaymc.landclaimremastered.config;

import com.allaymc.landclaimremastered.AllayClaimsPlugin;
import com.allaymc.landclaimremastered.model.Tier;

public final class PluginConfig {
    private final AllayClaimsPlugin plugin;

    public PluginConfig(AllayClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    public int perkRefreshTicks() {
        return plugin.getConfig().getInt("perks.refresh-ticks", 40);
    }

    public int sqliteFileDummy() { return 0; } // avoid mixed old references

    public String sqliteFile() {
        return plugin.getConfig().getString("database.sqlite-file", "claims.db");
    }

    public int requiredBlocks(Tier tier) {
        return plugin.getConfig().getInt("tiers." + tier.getLevel(), tier.getDefaultBlocks());
    }
}
