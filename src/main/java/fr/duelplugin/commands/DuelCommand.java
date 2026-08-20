package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.gui.DuelGUI;
import fr.duelplugin.models.DuelGameMode;
import fr.duelplugin.models.DuelRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DuelCommand implements CommandExecutor, TabCompleter {

    private final DuelPlugin plugin;

    public DuelCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§5Seuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.getPrefix() + "§dUsage: /duel <joueur> §7ou §d/duel leave");
            return true;
        }

        if (args[0].equalsIgnoreCase("leave")) {
            if (plugin.getQueueManager().isInAnyQueue(player)) {
                plugin.getQueueManager().leaveQueue(player);
                player.sendMessage(plugin.getPrefix() + "§cQueue quittée.");
            } else {
                player.sendMessage(plugin.getPrefix() + "§cVous n'êtes pas en queue.");
            }
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            plugin.getDuelGUI().openModeSelector(player, target);
        } else {
            player.sendMessage(plugin.getMessage("player-not-found"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("leave");
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).collect(Collectors.toList()));
        }
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
