package com.allaymc.landclaimremastered;

import com.allaymc.landclaimremastered.commands.ClaimAdminCommand;
import com.allaymc.landclaimremastered.commands.ClaimCommand;
import com.allaymc.landclaimremastered.config.PluginConfig;
import com.allaymc.landclaimremastered.gui.GuiManager;
import com.allaymc.landclaimremastered.hooks.ClaimProviderManager;
import com.allaymc.landclaimremastered.listeners.GuiListener;
import com.allaymc.landclaimremastered.listeners.PlayerListener;
import com.allaymc.landclaimremastered.perks.PerkRegistry;
import com.allaymc.landclaimremastered.perks.PerkService;
import com.allaymc.landclaimremastered.service.ClaimProfileService;
import com.allaymc.landclaimremastered.service.PlayerProgressService;
import com.allaymc.landclaimremastered.service.TierService;
import com.allaymc.landclaimremastered.storage.ClaimProfileRepository;
import com.allaymc.landclaimremastered.storage.DatabaseManager;
import com.allaymc.landclaimremastered.storage.PlayerProgressRepository;
import com.allaymc.landclaimremastered.util.Chat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AllayClaimsPlugin extends JavaPlugin {
    private static AllayClaimsPlugin instance;

    private PluginConfig pluginConfig;
    private DatabaseManager databaseManager;
    private ClaimProviderManager claimProviderManager;
    private ClaimProfileRepository claimProfileRepository;
    private PlayerProgressRepository playerProgressRepository;
    private TierService tierService;
    private PlayerProgressService playerProgressService;
    private ClaimProfileService claimProfileService;
    private PerkRegistry perkRegistry;
    private PerkService perkService;
    private GuiManager guiManager;

    public static AllayClaimsPlugin get() { return instance; }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResourceIfMissing("messages.yml");
        Chat.reload(this);

        this.pluginConfig = new PluginConfig(this);
        this.databaseManager = new DatabaseManager(this, pluginConfig);
        this.databaseManager.start();
        this.claimProviderManager = new ClaimProviderManager(this);
        this.claimProviderManager.load();
        this.claimProfileRepository = new ClaimProfileRepository(databaseManager);
        this.playerProgressRepository = new PlayerProgressRepository(databaseManager);
        this.tierService = new TierService(pluginConfig);
        this.playerProgressService = new PlayerProgressService(claimProviderManager, tierService, playerProgressRepository);
        this.claimProfileService = new ClaimProfileService(claimProfileRepository);
        this.perkRegistry = new PerkRegistry();
        this.perkRegistry.registerDefaults();
        this.perkService = new PerkService(this, claimProviderManager, claimProfileService, playerProgressService, perkRegistry, pluginConfig);
        this.guiManager = new GuiManager(tierService, perkService, claimProfileService, playerProgressService);

        if (getCommand("allayclaim") != null) getCommand("allayclaim").setExecutor(new ClaimCommand(this));
        if (getCommand("claimadmin") != null) getCommand("claimadmin").setExecutor(new ClaimAdminCommand(this));

        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

        long refresh = pluginConfig.perkRefreshTicks();
        Bukkit.getScheduler().runTaskTimer(this, () -> Bukkit.getOnlinePlayers().forEach(perkService::refreshPerk), refresh, refresh);
        getLogger().info("AllayMc Land Claim Remastered 1.2.1 enabled.");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) databaseManager.shutdown();
    }

    private void saveResourceIfMissing(String name) {
        if (!new java.io.File(getDataFolder(), name).exists()) {
            saveResource(name, false);
        }
    }

    public PluginConfig getPluginConfig() { return pluginConfig; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public ClaimProviderManager getClaimProviderManager() { return claimProviderManager; }
    public ClaimProfileRepository getClaimProfileRepository() { return claimProfileRepository; }
    public PlayerProgressRepository getPlayerProgressRepository() { return playerProgressRepository; }
    public TierService getTierService() { return tierService; }
    public PlayerProgressService getPlayerProgressService() { return playerProgressService; }
    public ClaimProfileService getClaimProfileService() { return claimProfileService; }
    public PerkRegistry getPerkRegistry() { return perkRegistry; }
    public PerkService getPerkService() { return perkService; }
    public GuiManager getGuiManager() { return guiManager; }
}
