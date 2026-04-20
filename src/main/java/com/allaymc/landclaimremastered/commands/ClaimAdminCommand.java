package com.allaymc.landclaimremastered.commands;

import com.allaymc.landclaimremastered.AllayClaimsPlugin;
import com.allaymc.landclaimremastered.util.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class ClaimAdminCommand implements CommandExecutor {
    private final AllayClaimsPlugin plugin;

    public ClaimAdminCommand(AllayClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("allayclaim.admin")) {
            sender.sendMessage(Chat.msg(plugin, "no-permission"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            Chat.reload(plugin);
            sender.sendMessage(Chat.msg(plugin, "reloaded"));
            return true;
        }

        sender.sendMessage(Chat.color("&bAllayClaims &7admin tools are online."));
        return true;
    }
}
