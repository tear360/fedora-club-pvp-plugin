package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.managers.LanguageManager;
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

    private LanguageManager lang() {
        return plugin.getLanguageManager();
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
                    sender.sendMessage(lang().msgRaw(null, "command_only_players"));
                    return true;
                }
                handleColor(player, args);
            }
            case "info" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(lang().msgRaw(null, "command_only_players"));
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
            sender.sendMessage(lang().msgRaw(null, "no_permission"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /vip set <joueur>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(lang().msgRaw(null, "player_not_found"));
            return;
        }
        plugin.getVipManager().setVip(target.getUniqueId(), true);
        if (sender instanceof Player p) {
            sender.sendMessage(lang().msg(p, "vip_set", "%player%", target.getName()));
        } else {
            sender.sendMessage("§d" + target.getName() + " §aest maintenant VIP!");
        }
        target.sendMessage(lang().msg(target, "vip_received"));
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("duelplugin.admin")) {
            sender.sendMessage(lang().msgRaw(null, "no_permission"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /vip remove <joueur>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(lang().msgRaw(null, "player_not_found"));
            return;
        }
        plugin.getVipManager().setVip(target.getUniqueId(), false);
        if (sender instanceof Player p) {
            sender.sendMessage(lang().msg(p, "vip_removed", "%player%", target.getName()));
        } else {
            sender.sendMessage("§d" + target.getName() + " §c n'est plus VIP.");
        }
        target.sendMessage(lang().msg(target, "vip_removed_self"));
    }

    private void handleColor(Player player, String[] args) {
        UUID uuid = player.getUniqueId();
        if (!plugin.getVipManager().isVip(uuid)) {
            player.sendMessage(lang().msg(player, "vip_not_vip"));
            return;
        }

        List<String> colors = plugin.getVipManager().getAvailableColors();
        List<String> colorNames = plugin.getVipManager().getColorNames();

        if (args.length < 2) {
            player.sendMessage(lang().msg(player, "vip_colors_available"));
            for (int i = 0; i < colors.size(); i++) {
                player.sendMessage("  " + colors.get(i) + "• " + lang().msgRaw(player, "vip_color_" + colorNames.get(i).toLowerCase()));
            }
            player.sendMessage("§7Utilisez: /vip color <nom>");
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
            player.sendMessage(lang().msg(player, "vip_color_invalid"));
            return;
        }

        String color = colors.get(index);
        plugin.getVipManager().setNameColor(uuid, color);
        player.sendMessage(lang().msg(player, "vip_color_changed", "%player%", colorNames.get(index)));
    }

    private void handleInfo(Player player) {
        UUID uuid = player.getUniqueId();
        boolean isVip = plugin.getVipManager().isVip(uuid);
        String color = plugin.getVipManager().getNameColor(uuid);
        player.sendMessage(lang().msg(player, "vip_info_title"));
        player.sendMessage(lang().msgRaw(player, "vip_info_status", "%status%", isVip ? lang().msgRaw(player, "vip_info_yes") : lang().msgRaw(player, "vip_info_no")));
        if (isVip && color != null) {
            player.sendMessage(lang().msgRaw(player, "vip_info_color", "%color%", color));
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(lang().msgRaw(player, "vip_info_title"));
        if (player.hasPermission("duelplugin.admin")) {
            player.sendMessage(lang().msgRaw(player, "vip_help_set"));
            player.sendMessage(lang().msgRaw(player, "vip_help_remove"));
        }
        player.sendMessage(lang().msgRaw(player, "vip_help_color"));
        player.sendMessage(lang().msgRaw(player, "vip_help_info"));
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
