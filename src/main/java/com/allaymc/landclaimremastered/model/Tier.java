package com.allaymc.landclaimremastered.model;

public enum Tier {
    I(1, 200),
    II(2, 500),
    III(3, 1000),
    IV(4, 2000),
    V(5, 4000),
    VI(6, 7000),
    VII(7, 12000),
    VIII(8, 20000),
    IX(9, 30000),
    X(10, 40000);

    private final int level;
    private final int defaultBlocks;

    Tier(int level, int defaultBlocks) {
        this.level = level;
        this.defaultBlocks = defaultBlocks;
    }

    public int getLevel() {
        return level;
    }

    public int getDefaultBlocks() {
        return defaultBlocks;
    }

    public static Tier byLevel(int level) {
        for (Tier tier : values()) {
            if (tier.level == level) return tier;
        }
        return I;
    }
}
