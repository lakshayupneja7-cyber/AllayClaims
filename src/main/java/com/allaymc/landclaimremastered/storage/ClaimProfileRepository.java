package com.allaymc.landclaimremastered.storage;

import com.allaymc.landclaimremastered.model.ClaimProfile;
import com.allaymc.landclaimremastered.model.ClaimTrustMode;
import com.allaymc.landclaimremastered.model.PerkKey;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;

public final class ClaimProfileRepository {
    private final DatabaseManager databaseManager;

    public ClaimProfileRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Optional<ClaimProfile> find(String claimId) {
        try (PreparedStatement ps = databaseManager.connection().prepareStatement(
                "SELECT claim_id, owner_uuid, display_name, selected_perk, trust_mode FROM claim_profiles WHERE claim_id = ?"
        )) {
            ps.setString(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                ClaimProfile profile = new ClaimProfile(
                        rs.getString("claim_id"),
                        UUID.fromString(rs.getString("owner_uuid"))
                );
                profile.setDisplayName(rs.getString("display_name"));

                String selected = rs.getString("selected_perk");
                if (selected != null && !selected.isBlank()) {
                    profile.setSelectedPerk(PerkKey.valueOf(selected));
                }
                profile.setTrustMode(ClaimTrustMode.valueOf(rs.getString("trust_mode")));

                try (PreparedStatement wl = databaseManager.connection().prepareStatement(
                        "SELECT player_uuid FROM claim_whitelist WHERE claim_id = ?"
                )) {
                    wl.setString(1, claimId);
                    try (ResultSet wrs = wl.executeQuery()) {
                        while (wrs.next()) {
                            profile.getPerkWhitelist().add(UUID.fromString(wrs.getString("player_uuid")));
                        }
                    }
                }
                return Optional.of(profile);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load claim profile", e);
        }
    }

    public void save(ClaimProfile profile) {
        try (PreparedStatement ps = databaseManager.connection().prepareStatement("""
                INSERT INTO claim_profiles (claim_id, owner_uuid, display_name, selected_perk, trust_mode)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(claim_id) DO UPDATE SET
                    owner_uuid = excluded.owner_uuid,
                    display_name = excluded.display_name,
                    selected_perk = excluded.selected_perk,
                    trust_mode = excluded.trust_mode
        """)) {
            ps.setString(1, profile.getClaimId());
            ps.setString(2, profile.getOwnerUuid().toString());
            ps.setString(3, profile.getDisplayName());
            ps.setString(4, profile.getSelectedPerk() == null ? null : profile.getSelectedPerk().name());
            ps.setString(5, profile.getTrustMode().name());
            ps.executeUpdate();

            try (PreparedStatement del = databaseManager.connection().prepareStatement("DELETE FROM claim_whitelist WHERE claim_id = ?")) {
                del.setString(1, profile.getClaimId());
                del.executeUpdate();
            }

            for (UUID uuid : profile.getPerkWhitelist()) {
                try (PreparedStatement ins = databaseManager.connection().prepareStatement(
                        "INSERT INTO claim_whitelist (claim_id, player_uuid) VALUES (?, ?)"
                )) {
                    ins.setString(1, profile.getClaimId());
                    ins.setString(2, uuid.toString());
                    ins.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save claim profile", e);
        }
    }
}
