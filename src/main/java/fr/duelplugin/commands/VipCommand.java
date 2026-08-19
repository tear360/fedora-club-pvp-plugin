package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class VipCommand implements CommandExecutor, TabCompleter {

    private final DuelPlugin plugin;

    public VipCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return true;
        }

        if (!player.hasPermission("duelplugin.vip")) {
            player.sendMessage(plugin.getPrefix() + "§cVous n'avez pas la permission VIP.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "color" -> handleColor(player, args);
            case "info" -> handleInfo(player);
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleColor(Player player, String[] args) {
        UUID uuid = player.getUniqueId();
        if (!plugin.getVipManager().isVip(uuid)) {
            player.sendMessage(plugin.getPrefix() + "§cVous n'êtes pas VIP.");
            return;
        }

        List<String> colors = plugin.getVipManager().getAvailableColors();
        List<String> colorNames = plugin.getVipManager().getColorNames();

        if (args.length < 2) {
            player.sendMessage(plugin.getPrefix() + "§dCouleurs disponibles:");
            for (int i = 0; i < colors.size(); i++) {
                player.sendMessage("  " + colors.get(i) + "• " + colorNames.get(i));
            }
            player.sendMessage(plugin.getPrefix() + "§7Utilisez: /vip color <nom>");
            return;
        }

        String colorName = args[1].toLowerCase();
        int index = -1;
        for (int i = 0; i < colorNames.size(); i++) {
            if (colorNames.get(i).toLowerCase().equals(colorName)) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            player.sendMessage(plugin.getPrefix() + "§cCouleur introuvable. Utilisez /vip color pour voir les options.");
            return;
        }

        String color = colors.get(index);
        plugin.getVipManager().setNameColor(uuid, color);
        player.sendMessage(plugin.getPrefix() + "§dCouleur de nom changée en §f" + colorNames.get(index) + "§d!");
    }

    private void handleInfo(Player player) {
        UUID uuid = player.getUniqueId();
        boolean isVip = plugin.getVipManager().isVip(uuid);
        String color = plugin.getVipManager().getNameColor(uuid);
        player.sendMessage(plugin.getPrefix() + "§dVIP: " + (isVip ? "§aOui" : "§cNon"));
        if (isVip && color != null) {
            player.sendMessage(plugin.getPrefix() + "§dCouleur: " + color + "•");
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(plugin.getPrefix() + "§d§l--- VIP ---");
        player.sendMessage("§d/vip color §7- Changer la couleur de nom");
        player.sendMessage("§d/vip info §7- Infos VIP");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("color", "info"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("color")) {
            completions.addAll(Arrays.asList("Rouge", "Or", "Jaune", "Vert", "Aqua", "Rose", "Violet", "Blanc", "Gris", "Noir"));
        }
        return completions;
    }
}
