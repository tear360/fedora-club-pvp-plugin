package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class UnbanCommand implements CommandExecutor {

    private final DuelPlugin plugin;

    public UnbanCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("duelplugin.admin")) {
            sender.sendMessage(plugin.getLanguageManager().msg(null, "no_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§dUsage: /unban <joueur>");
            return true;
        }

        String targetName = args[0];
        OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
        UUID uuid = offline.getUniqueId();

        if (!plugin.getBanManager().isBanned(uuid)) {
            sender.sendMessage("§c" + targetName + " n'est pas banni.");
            return true;
        }

        plugin.getBanManager().unban(uuid);
        sender.sendMessage("§a§l" + targetName + " §7a été débanni.");

        return true;
    }
}
