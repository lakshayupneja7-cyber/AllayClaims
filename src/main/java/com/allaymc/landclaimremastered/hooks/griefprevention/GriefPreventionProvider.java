package com.allaymc.landclaimremastered.hooks.griefprevention;

import com.allaymc.landclaimremastered.AllayClaimsPlugin;
import com.allaymc.landclaimremastered.hooks.ClaimProvider;
import com.allaymc.landclaimremastered.model.ClaimContext;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class GriefPreventionProvider implements ClaimProvider {
    private final AllayClaimsPlugin plugin;
    private long lastWarningAt = 0L;

    public GriefPreventionProvider(AllayClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() { return "GriefPrevention"; }

    @Override
    public boolean isAvailable() {
        return plugin.getServer().getPluginManager().getPlugin("GriefPrevention") != null;
    }

    @Override
    public Optional<ClaimContext> getClaimAt(Location location) {
        try {
            Object gpInstance = getGpInstance();
            Object dataStore = getFieldValue(gpInstance, "dataStore");
            if (dataStore == null) return Optional.empty();

            Object claim = findClaimAt(dataStore, location);
            if (claim == null) return Optional.empty();

            Object parent = getFieldValue(claim, "parent");
            if (parent != null) return Optional.empty();

            Object ownerRaw = getFieldValue(claim, "ownerID");
            if (!(ownerRaw instanceof UUID ownerUuid)) return Optional.empty();

            String claimId = String.valueOf(invokeNoArgs(claim, "getID"));
            Set<UUID> trusted = new HashSet<>();
            collectTrust(claim, "getManagers", trusted);
            collectTrust(claim, "getBuilders", trusted);
            collectTrust(claim, "getContainers", trusted);
            collectTrust(claim, "getAccessors", trusted);

            Object lesser = invokeNoArgs(claim, "getLesserBoundaryCorner");
            Object greater = invokeNoArgs(claim, "getGreaterBoundaryCorner");
            int lesserX = (int) invokeNoArgs(lesser, "getBlockX");
            int lesserZ = (int) invokeNoArgs(lesser, "getBlockZ");
            int greaterX = (int) invokeNoArgs(greater, "getBlockX");
            int greaterZ = (int) invokeNoArgs(greater, "getBlockZ");
            int area = (Math.abs(greaterX - lesserX) + 1) * (Math.abs(greaterZ - lesserZ) + 1);

            return Optional.of(new ClaimContext(
                claimId, ownerUuid, trusted, area,
                location.getWorld() == null ? "unknown" : location.getWorld().getName()
            ));
        } catch (Throwable t) {
            warnOnce("Failed to read GriefPrevention claim data: " + t.getClass().getSimpleName() + ": " + t.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public int getTotalClaimBlocks(Player player) {
        try {
            Object gpInstance = getGpInstance();
            Object dataStore = getFieldValue(gpInstance, "dataStore");
            if (dataStore == null) return 0;

            Method getPlayerData = findMethod(dataStore.getClass(), "getPlayerData", 1);
            if (getPlayerData == null) return 0;

            Object playerData = getPlayerData.invoke(dataStore, player.getUniqueId());
            int remaining = readIntMethod(playerData, "getRemainingClaimBlocks");
            int accrued = readIntField(playerData, "accruedClaimBlocks");
            int bonus = readIntField(playerData, "bonusClaimBlocks");
            int used = sumOwnedClaimArea(dataStore, player.getUniqueId());
            return Math.max(Math.max(remaining, accrued + bonus), remaining + used);
        } catch (Throwable t) {
            warnOnce("Failed to read GriefPrevention player data: " + t.getClass().getSimpleName() + ": " + t.getMessage());
            return 0;
        }
    }

    private Object getGpInstance() throws Exception {
        Class<?> gpClass = Class.forName("me.ryanhamshire.GriefPrevention.GriefPrevention");
        Field field = gpClass.getDeclaredField("instance");
        field.setAccessible(true);
        return field.get(null);
    }

    private Object findClaimAt(Object dataStore, Location location) throws Exception {
        for (Method method : dataStore.getClass().getMethods()) {
            if (!method.getName().equals("getClaimAt")) continue;
            Class<?>[] p = method.getParameterTypes();
            try {
                if (p.length == 3 && Location.class.isAssignableFrom(p[0]) && p[1] == boolean.class) {
                    return method.invoke(dataStore, location, false, null);
                }
                if (p.length == 2 && Location.class.isAssignableFrom(p[0]) && p[1] == boolean.class) {
                    return method.invoke(dataStore, location, false);
                }
                if (p.length == 1 && Location.class.isAssignableFrom(p[0])) {
                    return method.invoke(dataStore, location);
                }
            } catch (IllegalArgumentException ignored) {}
        }

        Method getClaims = findMethod(dataStore.getClass(), "getClaims", 0);
        if (getClaims != null) {
            Object result = getClaims.invoke(dataStore);
            if (result instanceof Collection<?> claims) {
                for (Object claim : claims) {
                    if (contains(claim, location)) return claim;
                }
            }
        }
        return null;
    }

    private int sumOwnedClaimArea(Object dataStore, UUID ownerUuid) {
        try {
            Method getClaims = findMethod(dataStore.getClass(), "getClaims", 0);
            if (getClaims == null) return 0;
            Object result = getClaims.invoke(dataStore);
            if (!(result instanceof Collection<?> claims)) return 0;
            int total = 0;
            for (Object claim : claims) {
                if (claim == null) continue;
                Object parent = getFieldValue(claim, "parent");
                if (parent != null) continue;
                Object ownerRaw = getFieldValue(claim, "ownerID");
                if (!(ownerRaw instanceof UUID claimOwner) || !ownerUuid.equals(claimOwner)) continue;
                Object lesser = invokeNoArgs(claim, "getLesserBoundaryCorner");
                Object greater = invokeNoArgs(claim, "getGreaterBoundaryCorner");
                int lesserX = (int) invokeNoArgs(lesser, "getBlockX");
                int lesserZ = (int) invokeNoArgs(lesser, "getBlockZ");
                int greaterX = (int) invokeNoArgs(greater, "getBlockX");
                int greaterZ = (int) invokeNoArgs(greater, "getBlockZ");
                total += (Math.abs(greaterX - lesserX) + 1) * (Math.abs(greaterZ - lesserZ) + 1);
            }
            return total;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    private boolean contains(Object claim, Location location) {
        try {
            Method contains = findMethod(claim.getClass(), "contains", 2);
            if (contains != null) {
                Object result = contains.invoke(claim, location, false);
                if (result instanceof Boolean bool) return bool;
            }
        } catch (Throwable ignored) {}

        try {
            Object lesser = invokeNoArgs(claim, "getLesserBoundaryCorner");
            Object greater = invokeNoArgs(claim, "getGreaterBoundaryCorner");
            if (location.getWorld() == null) return false;
            Object lesserWorld = invokeNoArgs(lesser, "getWorld");
            Object greaterWorld = invokeNoArgs(greater, "getWorld");
            if (!location.getWorld().equals(lesserWorld) || !location.getWorld().equals(greaterWorld)) return false;
            int lx = (int) invokeNoArgs(lesser, "getBlockX");
            int lz = (int) invokeNoArgs(lesser, "getBlockZ");
            int gx = (int) invokeNoArgs(greater, "getBlockX");
            int gz = (int) invokeNoArgs(greater, "getBlockZ");
            int x = location.getBlockX();
            int z = location.getBlockZ();
            return x >= Math.min(lx, gx) && x <= Math.max(lx, gx)
                && z >= Math.min(lz, gz) && z <= Math.max(lz, gz);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private void collectTrust(Object claim, String methodName, Set<UUID> out) {
        try {
            Object result = invokeNoArgs(claim, methodName);
            if (!(result instanceof Collection<?> c)) return;
            for (Object entry : c) {
                if (entry == null) continue;
                String name = String.valueOf(entry);
                if (name.isBlank()) continue;
                OfflinePlayer offline = plugin.getServer().getOfflinePlayer(name);
                if (offline.getUniqueId() != null) out.add(offline.getUniqueId());
            }
        } catch (Throwable ignored) {}
    }

    private Object invokeNoArgs(Object target, String name) throws Exception {
        Method m = target.getClass().getMethod(name);
        m.setAccessible(true);
        return m.invoke(target);
    }

    private Method findMethod(Class<?> type, String name, int params) {
        for (Method m : type.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == params) {
                m.setAccessible(true);
                return m;
            }
        }
        return null;
    }

    private Object getFieldValue(Object target, String fieldName) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(target);
        } catch (Throwable ignored) { return null; }
    }

    private int readIntField(Object target, String fieldName) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.getInt(target);
        } catch (Throwable ignored) { return 0; }
    }

    private int readIntMethod(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName);
            m.setAccessible(true);
            Object result = m.invoke(target);
            return result instanceof Integer i ? i : 0;
        } catch (Throwable ignored) { return 0; }
    }

    private void warnOnce(String message) {
        long now = System.currentTimeMillis();
        if (now - lastWarningAt < 10000L) return;
        lastWarningAt = now;
        plugin.getLogger().warning(message);
    }
}
