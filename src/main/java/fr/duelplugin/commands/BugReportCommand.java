package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class BugReportCommand implements CommandExecutor, TabCompleter {

    private final DuelPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private static final long COOLDOWN_MS = 60 * 60 * 1000;

    public BugReportCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.getLanguageManager().msg(player, "bugreport_usage"));
            return true;
        }

        if (!plugin.getDiscordBotManager().isEnabled()) {
            player.sendMessage("§cLe bot Discord n'est pas connecté.");
            return true;
        }

        long now = System.currentTimeMillis();
        if (!player.hasPermission("duelplugin.bugreport.bypass")) {
            Long lastUse = cooldowns.get(player.getUniqueId());
            if (lastUse != null && (now - lastUse) < COOLDOWN_MS) {
                long remaining = (COOLDOWN_MS - (now - lastUse)) / 1000;
                long minutes = remaining / 60;
                long seconds = remaining % 60;
                player.sendMessage(plugin.getLanguageManager().msg(player, "bugreport_cooldown", "%time%", minutes + "m " + seconds + "s"));
                return true;
            }
        }

        String bug = String.join(" ", args);
        cooldowns.put(player.getUniqueId(), now);

        plugin.getDiscordBotManager().createBugReportThread(player.getName(), player.getUniqueId().toString(), bug);
        player.sendMessage(plugin.getLanguageManager().msg(player, "bugreport_sent"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
