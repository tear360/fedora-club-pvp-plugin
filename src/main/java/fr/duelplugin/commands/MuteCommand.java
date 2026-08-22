package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.managers.BanManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MuteCommand implements CommandExecutor {

    private final DuelPlugin plugin;

    public MuteCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("duelplugin.admin")) {
            sender.sendMessage(plugin.getLanguageManager().msg(null, "no_permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§dUsage: /mute <joueur> <durée> [raison]");
            sender.sendMessage("§7Exemples: §f/mute Player1 10m spam");
            sender.sendMessage("§7           §f/mute Player1 perm insulte");
            return true;
        }

        String targetName = args[0];
        String durationStr = args[1];
        long duration;

        if (durationStr.equalsIgnoreCase("perm") || durationStr.equalsIgnoreCase("permanent")) {
            duration = -1;
        } else {
            duration = BanManager.parseDuration(durationStr);
            if (duration <= 0) {
                sender.sendMessage("§cDurée invalide. Utilisez: 30s, 10m, 2h, 7d, 2w, ou perm");
                return true;
            }
        }

        String reason = "";
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
            uuid = Bukkit.getOfflinePlayer(targetName).getUniqueId();
        }

        String muterName = sender instanceof Player ? sender.getName() : "Console";
        plugin.getBanManager().mute(uuid, targetName, reason, muterName, duration);

        String durDisplay = duration == -1 ? "Permanent" : BanManager.formatDuration(duration);
        sender.sendMessage("§a§l" + targetName + " §7a été mute.");
        sender.sendMessage("§7Durée: §f" + durDisplay);
        if (!reason.isEmpty()) sender.sendMessage("§7Raison: §f" + reason);

        if (target != null) {
            target.sendMessage("§cVous avez été mute par §4" + muterName + "§c.");
            target.sendMessage("§7Durée: §f" + durDisplay);
            if (!reason.isEmpty()) target.sendMessage("§7Raison: §f" + reason);
        }

        return true;
    }
}
