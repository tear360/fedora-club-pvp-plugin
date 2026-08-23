package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.managers.BanManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BanCommand implements CommandExecutor {

    private final DuelPlugin plugin;

    public BanCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("duelplugin.admin.ban")) {
            sender.sendMessage(plugin.getLanguageManager().msg(null, "no_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§dUsage: /ban <joueur> [durée] [raison]");
            sender.sendMessage("§7Exemples: §f/ban Player1 7d hack");
            sender.sendMessage("§7           §f/ban Player1 Permanent cheat");
            return true;
        }

        String targetName = args[0];
        String reason = "";
        long duration = -1;

        if (args.length >= 2) {
            duration = BanManager.parseDuration(args[1]);
            if (duration == -1 && !args[1].equalsIgnoreCase("permanent") && !args[1].equalsIgnoreCase("perm")) {
                sender.sendMessage("§cDurée invalide. Utilisez: 30s, 10m, 2h, 7d, 2w, ou perm");
                return true;
            }
            if (args[1].equalsIgnoreCase("permanent") || args[1].equalsIgnoreCase("perm")) {
                duration = -1;
            }
        }

        if (args.length >= 3) {
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                if (i > 2) sb.append(" ");
                sb.append(args[i]);
            }
            reason = sb.toString();
        }

        Player target = Bukkit.getPlayer(targetName);
        UUID uuid;
        if (target != null) {
            uuid = target.getUniqueId();
        } else {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
            uuid = offline.getUniqueId();
        }

        String bannerName = sender instanceof Player ? sender.getName() : "Console";
        plugin.getBanManager().ban(uuid, targetName, reason, bannerName, duration);

        String durationStr = duration == -1 ? "Permanent" : BanManager.formatDuration(duration);
        sender.sendMessage("§a§l" + targetName + " §7a été banni.");
        sender.sendMessage("§7Durée: §f" + durationStr);
        if (!reason.isEmpty()) sender.sendMessage("§7Raison: §f" + reason);

        if (target != null) {
            target.kick(plugin.getBanManager().buildBanScreen(uuid));
        }

        return true;
    }
}
