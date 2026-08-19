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
        if (args.length == 0) {
            if (sender instanceof Player p) sendHelp(p);
            else sender.sendMessage("§cUsage: /vip <set|remove|color|info>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set" -> handleSet(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "color" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cCommande réservée aux joueurs.");
                    return true;
                }
                handleColor(player, args);
            }
            case "info" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cCommande réservée aux joueurs.");
                    return true;
                }
                handleInfo(player);
            }
            default -> {
                if (sender instanceof Player p) sendHelp(p);
                else sender.sendMessage("§cUsage: /vip <set|remove|color|info>");
            }
        }

        return true;
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("duelplugin.admin")) {
            sender.sendMessage(plugin.getPrefix() + "§cVous n'avez pas la permission.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(plugin.getPrefix() + "§cUsage: /vip set <joueur>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getPrefix() + "§cJoueur introuvable.");
            return;
        }
        plugin.getVipManager().setVip(target.getUniqueId(), true);
        sender.sendMessage(plugin.getPrefix() + "§d" + target.getName() + " §aest maintenant VIP!");
        target.sendMessage(plugin.getPrefix() + "§aVous avez reçu le grade §dVIP§a!");
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("duelplugin.admin")) {
            sender.sendMessage(plugin.getPrefix() + "§cVous n'avez pas la permission.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(plugin.getPrefix() + "§cUsage: /vip remove <joueur>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getPrefix() + "§cJoueur introuvable.");
            return;
        }
        plugin.getVipManager().setVip(target.getUniqueId(), false);
        sender.sendMessage(plugin.getPrefix() + "§d" + target.getName() + " §c n'est plus VIP.");
        target.sendMessage(plugin.getPrefix() + "§cVotre grade VIP a été retiré.");
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
        if (player.hasPermission("duelplugin.admin")) {
            player.sendMessage("§d/vip set <joueur> §7- Donner le VIP");
            player.sendMessage("§d/vip remove <joueur> §7- Retirer le VIP");
        }
        player.sendMessage("§d/vip color §7- Changer la couleur de nom");
        player.sendMessage("§d/vip info §7- Infos VIP");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(Arrays.asList("color", "info"));
            if (sender.hasPermission("duelplugin.admin")) {
                subs.addAll(Arrays.asList("set", "remove"));
            }
            completions.addAll(subs);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("remove")) {
                if (sender.hasPermission("duelplugin.admin")) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        completions.add(p.getName());
                    }
                }
            } else if (args[0].equalsIgnoreCase("color")) {
                completions.addAll(Arrays.asList("Rouge", "Or", "Jaune", "Vert", "Aqua", "Rose", "Violet", "Blanc", "Gris", "Noir"));
            }
        }
        return completions;
    }
}
