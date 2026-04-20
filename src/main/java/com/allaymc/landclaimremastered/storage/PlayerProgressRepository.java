package com.allaymc.landclaimremastered.storage;

import java.sql.PreparedStatement;
import java.util.UUID;

public final class PlayerProgressRepository {
    private final DatabaseManager databaseManager;

    public PlayerProgressRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void save(UUID uuid, int total, int tier) {
        try (PreparedStatement ps = databaseManager.connection().prepareStatement("""
                INSERT INTO player_progress (player_uuid, total_claim_blocks, current_tier, updated_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT(player_uuid) DO UPDATE SET
                    total_claim_blocks = excluded.total_claim_blocks,
                    current_tier = excluded.current_tier,
                    updated_at = CURRENT_TIMESTAMP
        """)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, total);
            ps.setInt(3, tier);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save player progress", e);
        }
    }
}
