package com.allaymc.landclaimremastered.commands;

import com.allaymc.landclaimremastered.AllayClaimsPlugin;
import com.allaymc.landclaimremastered.model.ClaimContext;
import com.allaymc.landclaimremastered.service.ClaimProfileService;
import com.allaymc.landclaimremastered.util.Chat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ClaimCommand implements CommandExecutor {
    private final AllayClaimsPlugin plugin;

    public ClaimCommand(AllayClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Chat.msg(plugin, "players-only"));
            return true;
        }

        Optional<ClaimContext> contextOptional = plugin.getPerkService().currentClaim(player);
        if (contextOptional.isEmpty()) {
            player.sendMessage(Chat.msg(plugin, "no-claim-here"));
            return true;
        }

        ClaimContext context = contextOptional.get();
        ClaimProfileService claimProfileService = plugin.getClaimProfileService();

        if (args.length >= 1 && args[0].equalsIgnoreCase("whitelist")) {
            if (!context.owner().equals(player.getUniqueId())) {
                player.sendMessage(Chat.msg(plugin, "owner-only"));
                return true;
            }

            if (args.length == 1 || args[1].equalsIgnoreCase("list")) {
                List<String> names = claimProfileService.getWhitelist(context.claimId(), context.owner()).stream()
                        .map(Bukkit::getOfflinePlayer)
                        .map(OfflinePlayer::getName)
                        .map(name -> name == null ? "Unknown" : name)
                        .collect(Collectors.toList());

                player.sendMessage(Chat.color("&bWhitelist for this claim: &f" + (names.isEmpty() ? "Empty" : String.join(", ", names))));
                return true;
            }

            if (args.length < 3) {
                player.sendMessage(Chat.color("&cUsage: /allayclaim whitelist <add|remove|list> <player>"));
                return true;
            }

            String sub = args[1].toLowerCase();
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
            UUID targetUuid = target.getUniqueId();

            if (sub.equals("add")) {
                boolean added = claimProfileService.addWhitelistPlayer(context.claimId(), context.owner(), targetUuid);
                player.sendMessage(Chat.color(added
                        ? "&aAdded &f" + (target.getName() == null ? args[2] : target.getName()) + " &ato this claim perk whitelist."
                        : "&eThat player is already in the whitelist."));
                return true;
            }

            if (sub.equals("remove")) {
                boolean removed = claimProfileService.removeWhitelistPlayer(context.claimId(), context.owner(), targetUuid);
                player.sendMessage(Chat.color(removed
                        ? "&aRemoved &f" + (target.getName() == null ? args[2] : target.getName()) + " &afrom this claim perk whitelist."
                        : "&eThat player is not in the whitelist."));
                return true;
            }

            player.sendMessage(Chat.color("&cUsage: /allayclaim whitelist <add|remove|list> <player>"));
            return true;
        }

        plugin.getGuiManager().openMainMenu(player, context);
        return true;
    }
}
