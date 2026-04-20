package com.allaymc.landclaimremastered.hooks;

import com.allaymc.landclaimremastered.AllayClaimsPlugin;
import com.allaymc.landclaimremastered.hooks.griefprevention.GriefPreventionProvider;

public final class ClaimProviderManager {
    private final AllayClaimsPlugin plugin;
    private ClaimProvider provider;

    public ClaimProviderManager(AllayClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        ClaimProvider gp = new GriefPreventionProvider(plugin);
        if (gp.isAvailable()) this.provider = gp;
    }

    public boolean isAvailable() {
        return provider != null && provider.isAvailable();
    }

    public ClaimProvider getProvider() {
        return provider;
    }

    public String getProviderName() {
        return provider == null ? "NONE" : provider.getName();
    }
}
