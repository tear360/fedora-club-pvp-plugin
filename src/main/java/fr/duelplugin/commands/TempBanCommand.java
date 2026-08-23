package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.managers.BanManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TempBanCommand implements CommandExecutor {

    private final DuelPlugin plugin;

    public TempBanCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("duelplugin.admin.tempban")) {
            sender.sendMessage(plugin.getLanguageManager().msg(null, "no_permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§dUsage: /tempban <joueur> <durée> [raison]");
            sender.sendMessage("§7Exemple: §f/tempban Player1 7d cheat");
            return true;
        }

        String targetName = args[0];
        long duration = BanManager.parseDuration(args[1]);
        if (duration <= 0) {
            sender.sendMessage("§cDurée invalide. Utilisez: 30s, 10m, 2h, 7d, 2w");
            return true;
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

        String bannerName = sender instanceof Player ? sender.getName() : "Console";
        plugin.getBanManager().ban(uuid, targetName, reason, bannerName, duration);

        sender.sendMessage("§a§l" + targetName + " §7a été banni temporairement.");
        sender.sendMessage("§7Durée: §f" + BanManager.formatDuration(duration));
        if (!reason.isEmpty()) sender.sendMessage("§7Raison: §f" + reason);

        if (target != null) {
            target.kick(plugin.getBanManager().buildBanScreen(uuid));
        }

        return true;
    }
}
