package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReportCommand implements CommandExecutor, TabCompleter {

    private final DuelPlugin plugin;

    public ReportCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLanguageManager().msgRaw(null, "command_only_players"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getLanguageManager().msg(player, "report_usage"));
            return true;
        }

        String reportedName = args[0];
        Player reported = plugin.getServer().getPlayer(reportedName);
        if (reported == null) {
            player.sendMessage(plugin.getLanguageManager().msg(player, "player_not_found"));
            return true;
        }

        if (reported.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getLanguageManager().msg(player, "report_cannot_self"));
            return true;
        }

        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (reasonBuilder.length() > 0) reasonBuilder.append(" ");
            reasonBuilder.append(args[i]);
        }
        String reason = reasonBuilder.toString();

        int id = plugin.getReportManager().createReport(player.getName(), reported.getName(), reason);
        player.sendMessage(plugin.getLanguageManager().msg(player, "report_success", "%id%", String.valueOf(id), "%player%", reported.getName()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(prefix)) {
                    completions.add(p.getName());
                }
            }
        }
        return completions;
    }
}
