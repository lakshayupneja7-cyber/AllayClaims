package com.allaymc.landclaimremastered.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ClaimProfile {
    private final String claimId;
    private final UUID ownerUuid;
    private String displayName;
    private PerkKey selectedPerk;
    private ClaimTrustMode trustMode;
    private final Set<UUID> perkWhitelist = new HashSet<>();

    public ClaimProfile(String claimId, UUID ownerUuid) {
        this.claimId = claimId;
        this.ownerUuid = ownerUuid;
        this.displayName = "Claim #" + claimId;
        this.selectedPerk = null;
        this.trustMode = ClaimTrustMode.ALL_TRUSTED;
    }

    public String getClaimId() { return claimId; }
    public UUID getOwnerUuid() { return ownerUuid; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public PerkKey getSelectedPerk() { return selectedPerk; }
    public void setSelectedPerk(PerkKey selectedPerk) { this.selectedPerk = selectedPerk; }
    public ClaimTrustMode getTrustMode() { return trustMode; }
    public void setTrustMode(ClaimTrustMode trustMode) { this.trustMode = trustMode; }
    public Set<UUID> getPerkWhitelist() { return perkWhitelist; }
}
