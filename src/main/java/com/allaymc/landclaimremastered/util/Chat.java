package com.allaymc.landclaimremastered.util;

import com.allaymc.landclaimremastered.AllayClaimsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class Chat {
    private static YamlConfiguration messages;

    private Chat() {
    }

    public static void reload(AllayClaimsPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String msg(AllayClaimsPlugin plugin, String key) {
        if (messages == null) reload(plugin);
        String prefix = messages.getString("prefix", "&b&lAllayClaims &7» ");
        String value = messages.getString(key, "&cMissing message: " + key);
        return color(prefix + value);
    }

    public static String raw(AllayClaimsPlugin plugin, String key) {
        if (messages == null) reload(plugin);
        return color(messages.getString(key, "&cMissing message: " + key));
    }
}
