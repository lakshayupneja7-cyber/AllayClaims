package com.allaymc.landclaimremastered.storage;

import com.allaymc.landclaimremastered.AllayClaimsPlugin;
import com.allaymc.landclaimremastered.config.PluginConfig;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public final class DatabaseManager {
    private final AllayClaimsPlugin plugin;
    private final PluginConfig config;
    private Connection connection;

    public DatabaseManager(AllayClaimsPlugin plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void start() {
        try {
            File folder = plugin.getDataFolder();
            if (!folder.exists()) folder.mkdirs();
            File file = new File(folder, config.sqliteFile());
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());

            try (Statement st = connection.createStatement()) {
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS claim_profiles (
                        claim_id TEXT PRIMARY KEY,
                        owner_uuid TEXT NOT NULL,
                        display_name TEXT NOT NULL,
                        selected_perk TEXT NULL,
                        trust_mode TEXT NOT NULL
                    )
                """);
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS claim_whitelist (
                        claim_id TEXT NOT NULL,
                        player_uuid TEXT NOT NULL,
                        PRIMARY KEY (claim_id, player_uuid)
                    )
                """);
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS player_progress (
                        player_uuid TEXT PRIMARY KEY,
                        total_claim_blocks INTEGER NOT NULL,
                        current_tier INTEGER NOT NULL,
                        updated_at TEXT DEFAULT CURRENT_TIMESTAMP
                    )
                """);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public Connection connection() { return connection; }

    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (Exception ignored) {}
    }
}
