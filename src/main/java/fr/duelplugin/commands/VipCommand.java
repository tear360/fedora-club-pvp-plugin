package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.managers.LanguageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
            case "badges" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(lang().msgRaw(null, "command_only_players"));
                    return true;
                }
                handleBadges(player);
            }
            default -> {
                if (sender instanceof Player p) sendHelp(p);
                else sender.sendMessage("§cUsage: /vip <color|badges|info>");
            }
        }

        return true;
    }

    private void handleColor(Player player, String[] args) {
        UUID uuid = player.getUniqueId();
        if (!plugin.getRankManager().isVip(uuid)) {
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
        boolean isVip = plugin.getRankManager().isVip(uuid);
        String color = plugin.getVipManager().getNameColor(uuid);
        player.sendMessage(lang().msg(player, "vip_info_title"));
        player.sendMessage(lang().msgRaw(player, "vip_info_status", "%status%", isVip ? lang().msgRaw(player, "vip_info_yes") : lang().msgRaw(player, "vip_info_no")));
        if (isVip && color != null) {
            player.sendMessage(lang().msgRaw(player, "vip_info_color", "%color%", color));
        }
    }

    private void handleBadges(Player player) {
        UUID uuid = player.getUniqueId();
        if (!plugin.getRankManager().isVip(uuid)) {
            player.sendMessage(lang().msg(player, "vip_not_vip"));
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 27, Component.text(lang().msgRaw(player, "vip_badges_title"), NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
        String currentBadge = plugin.getVipManager().getBadge(uuid);
        java.util.List<String> badges = plugin.getVipManager().getAvailableBadges();

        for (int i = 0; i < 27; i++) {
            gui.setItem(i, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        }

        int slot = 10;
        for (String badge : badges) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                boolean isSelected = badge.equals(currentBadge);
                meta.displayName(Component.text(badge, NamedTextColor.WHITE, TextDecoration.BOLD));
                if (isSelected) {
                    gui.setItem(slot - 1, new ItemStack(Material.LIME_STAINED_GLASS_PANE));
                    meta.lore(java.util.List.of(
                        Component.text(lang().msgRaw(player, "vip_badge_selected"), NamedTextColor.GREEN)
                    ));
                } else {
                    meta.lore(java.util.List.of(
                        Component.text(lang().msgRaw(player, "vip_badge_click"), NamedTextColor.GRAY)
                    ));
                }
                item.setItemMeta(meta);
            }
            gui.setItem(slot, item);
            slot += 2;
        }

        player.openInventory(gui);
    }

    private void sendHelp(Player player) {
        player.sendMessage(lang().msgRaw(player, "vip_info_title"));
        player.sendMessage(lang().msgRaw(player, "vip_help_color"));
        player.sendMessage(lang().msgRaw(player, "vip_help_badges"));
        player.sendMessage(lang().msgRaw(player, "vip_help_info"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("color", "badges", "info"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("color")) {
                completions.addAll(Arrays.asList("Rouge", "Or", "Jaune", "Vert", "Aqua", "Rose", "Violet", "Blanc", "Gris", "Noir"));
            }
        }
        return completions;
    }
}
