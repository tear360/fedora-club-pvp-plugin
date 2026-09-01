package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickCommand implements CommandExecutor {

    private final DuelPlugin plugin;

    public KickCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("duelplugin.admin.kick")) {
            sender.sendMessage(plugin.getLanguageManager().msg(null, "no_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§dUsage: /kick <player> [reason]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getLanguageManager().msg(null, "player_not_found"));
            return true;
        }

        String reason = "";
        if (args.length >= 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) sb.append(" ");
                sb.append(args[i]);
            }
            reason = sb.toString();
        }

        String kickerName = sender instanceof Player ? sender.getName() : "Console";
        final String finalReason = reason;

        Component kickScreen = Component.empty()
                .append(Component.text("═══════════════════════════════", NamedTextColor.DARK_PURPLE))
                .appendNewline()
                .append(Component.text("You have been kicked!", NamedTextColor.RED, TextDecoration.BOLD))
                .appendNewline()
                .appendNewline()
                .append(Component.text("Reason: ", NamedTextColor.GRAY)
                        .append(Component.text(finalReason.isEmpty() ? "None" : finalReason, NamedTextColor.WHITE)))
                .appendNewline()
                .append(Component.text("By: ", NamedTextColor.GRAY)
                        .append(Component.text(kickerName, NamedTextColor.RED)))
                .appendNewline()
                .appendNewline()
                .append(Component.text("You can reconnect now.", NamedTextColor.GRAY))
                .appendNewline()
                .append(Component.text("═══════════════════════════════", NamedTextColor.DARK_PURPLE));

        target.kick(kickScreen);
        sender.sendMessage("§a§l" + target.getName() + " §7has been kicked.");
        if (!reason.isEmpty()) sender.sendMessage("§7Reason: §f" + reason);

        return true;
    }
}
