package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SettingsCommand implements CommandExecutor, TabCompleter {

    private final DuelPlugin plugin;

    public SettingsCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return true;
        }

        if (args.length == 0) {
            showSettings(player);
            return true;
        }

        String setting = args[0].toLowerCase();
        switch (setting) {
            case "friends" -> toggleFriends(player);
            case "duels" -> toggleDuels(player);
            default -> showSettings(player);
        }

        return true;
    }

    private void showSettings(Player player) {
        boolean friends = plugin.getSettingsManager().acceptsFriendRequests(player.getUniqueId());
        boolean duels = plugin.getSettingsManager().acceptsDuelRequests(player.getUniqueId());

        player.sendMessage("");
        player.sendMessage("§5§l═══════════════════════════");
        player.sendMessage("§d§lParamètres");
        player.sendMessage("");
        player.sendMessage("§dAmis: " + (friends ? "§aActivé" : "§cDésactivé"));
        player.sendMessage("§dDuels: " + (duels ? "§aActivé" : "§cDésactivé"));
        player.sendMessage("");
        player.sendMessage("§7Utilisez §d/settings friends §7ou §d/settings duels");
        player.sendMessage("§7pour basculer un paramètre.");
        player.sendMessage("§5§l═══════════════════════════");
        player.sendMessage("");
    }

    private void toggleFriends(Player player) {
        boolean current = plugin.getSettingsManager().acceptsFriendRequests(player.getUniqueId());
        plugin.getSettingsManager().setAcceptFriendRequests(player.getUniqueId(), !current);
        player.sendMessage(plugin.getPrefix() + "§dDemandes d'amis: " + (!current ? "§aActivé" : "§cDésactivé"));
    }

    private void toggleDuels(Player player) {
        boolean current = plugin.getSettingsManager().acceptsDuelRequests(player.getUniqueId());
        plugin.getSettingsManager().setAcceptDuelRequests(player.getUniqueId(), !current);
        player.sendMessage(plugin.getPrefix() + "§dDemandes de duel: " + (!current ? "§aActivé" : "§cDésactivé"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("friends", "duels"));
        }
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
