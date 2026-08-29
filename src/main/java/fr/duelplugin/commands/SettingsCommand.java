package fr.duelplugin.commands;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import fr.duelplugin.DuelPlugin;
import fr.duelplugin.managers.Language;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.stream.Collectors;

public class SettingsCommand implements CommandExecutor, TabCompleter, Listener {

    private final DuelPlugin plugin;

    private static final String FR_SKULL = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkwMzM0OWZhNDViZGQ4NzEyNmQ5Y2QzYzZjMGFiYmE3ZGJkNmY1NmZiOGQ3ODcwMTg3M2ExZTdjOGVlMzNjZiJ9fX0=";
    private static final String EN_SKULL = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODU2NDVhMDZmOWQ5MmIxZjcwNGQxNDY5OGM1ZTg0MmU5MGFlYzkwMmNhZmIzYWNiN2VlYjk3ZDgzOWJhMzA3YSJ9fX0=";

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
            openSettingsGUI(player);
            return true;
        }

        String setting = args[0].toLowerCase();
        switch (setting) {
            case "friends" -> toggleFriends(player);
            case "duels" -> toggleDuels(player);
            case "lang" -> openSettingsGUI(player);
            default -> openSettingsGUI(player);
        }

        return true;
    }

    private void openSettingsGUI(Player player) {
        LanguageManager lang = plugin.getLanguageManager();
        boolean friends = plugin.getSettingsManager().acceptsFriendRequests(player.getUniqueId());
        boolean duels = plugin.getSettingsManager().acceptsDuelRequests(player.getUniqueId());
        Language currentLang = plugin.getSettingsManager().getLanguage(player.getUniqueId());

        Inventory gui = Bukkit.createInventory(null, 27, Component.text("Paramètres / Settings", NamedTextColor.DARK_PURPLE));

        // Friend requests toggle
        ItemStack friendItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta friendMeta = (SkullMeta) friendItem.getItemMeta();
        friendMeta.displayName(Component.text("Amis / Friends", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
        friendMeta.lore(List.of(
            Component.text(friends ? lang.msgRaw(player, "settings_friends_on").replace("§dAmis: ", "") : lang.msgRaw(player, "settings_friends_off").replace("§dAmis: ", ""), friends ? NamedTextColor.GREEN : NamedTextColor.RED),
            Component.empty(),
            Component.text("§7" + (currentLang == Language.FR ? "Cliquez pour basculer" : "Click to toggle"))
        ));
        friendItem.setItemMeta(friendMeta);
        gui.setItem(11, friendItem);

        // Language toggle - single button
        Language targetLang = currentLang == Language.FR ? Language.EN : Language.FR;
        boolean isFr = currentLang == Language.FR;
        String langLabel = isFr ? "Français" : "English";
        String langSwitchLabel = isFr ? "English" : "Français";
        String langSwitchCode = isFr ? "§c" : "§9";

        ItemStack langItem = createSkullItem(isFr ? FR_SKULL : EN_SKULL);
        SkullMeta langMeta = (SkullMeta) langItem.getItemMeta();
        langMeta.displayName(Component.text(langLabel, NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
        langMeta.lore(List.of(
            Component.text("§a✔ " + langLabel, NamedTextColor.GREEN),
            Component.empty(),
            Component.text("§7Cliquer pour passer en " + langSwitchLabel, NamedTextColor.GRAY)
        ));
        langItem.setItemMeta(langMeta);
        gui.setItem(13, langItem);

        // Duel requests toggle
        ItemStack duelItem = new ItemStack(Material.DIAMOND_SWORD);
        duelItem.setItemMeta(duelItem.getItemMeta());
        net.kyori.adventure.text.event.HoverEvent hover = net.kyori.adventure.text.event.HoverEvent.showText(
            Component.text(duels ? lang.msgRaw(player, "settings_duels_on").replace("§dDuels: ", "") : lang.msgRaw(player, "settings_duels_off").replace("§dDuels: ", ""), duels ? NamedTextColor.GREEN : NamedTextColor.RED)
        );
        net.kyori.adventure.text.event.ClickEvent click = net.kyori.adventure.text.event.ClickEvent.runCommand("/settings duels");
        net.kyori.adventure.text.Component duelName = Component.text("Duels", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD)
            .hoverEvent(hover)
            .clickEvent(click);
        net.kyori.adventure.text.Component duelLore = Component.text(duels ? lang.msgRaw(player, "settings_duels_on").replace("§dDuels: ", "") : lang.msgRaw(player, "settings_duels_off").replace("§dDuels: ", ""), duels ? NamedTextColor.GREEN : NamedTextColor.RED);
        ItemStack duelDisplay = new ItemStack(Material.DIAMOND_SWORD);
        net.kyori.adventure.text.Component display = Component.empty()
            .append(Component.text("§d" + lang.msgRaw(player, "settings_duels_on").split(":")[0].replace("§d", "") + ": ", NamedTextColor.DARK_PURPLE))
            .append(Component.text(duels ? "§aActivé" : "§cDésactivé", duels ? NamedTextColor.GREEN : NamedTextColor.RED));
        org.bukkit.inventory.meta.ItemMeta duelMeta2 = duelDisplay.getItemMeta();
        duelMeta2.displayName(Component.text("Duels", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
        duelMeta2.lore(List.of(
            Component.text(duels ? "§aActivé" : "§cDésactivé", duels ? NamedTextColor.GREEN : NamedTextColor.RED),
            Component.empty(),
            Component.text("§7Click to toggle")
        ));
        duelDisplay.setItemMeta(duelMeta2);
        gui.setItem(15, duelDisplay);

        // Back item
        ItemStack back = new ItemStack(Material.ARROW);
        org.bukkit.inventory.meta.ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("Retour", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
        back.setItemMeta(backMeta);
        gui.setItem(22, back);

        player.openInventory(gui);
    }

    private ItemStack createSkullItem(String base64Texture) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "flag_head");
        profile.setProperty(new com.destroystokyo.paper.profile.ProfileProperty("textures", base64Texture));
        meta.setPlayerProfile(profile);
        skull.setItemMeta(meta);
        return skull;
    }

    @EventHandler
    public void onSettingsClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (!title.contains("Paramètres / Settings") && !title.contains("Settings / Paramètres")) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        switch (slot) {
            case 11 -> toggleFriends(player);
            case 13 -> {
                Language current = plugin.getSettingsManager().getLanguage(player.getUniqueId());
                Language next = current == Language.FR ? Language.EN : Language.FR;
                plugin.getSettingsManager().setLanguage(player.getUniqueId(), next);
                String name = next == Language.FR ? "Français" : "English";
                player.sendMessage(plugin.getLanguageManager().msg(player, "language_changed", "%language%", name));
                if (!plugin.getDuelManager().isInDuel(player.getUniqueId())) {
                    plugin.getScoreboardManager().createLobbyScoreboard(player, null, null);
                }
                openSettingsGUI(player);
            }
            case 15 -> toggleDuels(player);
            case 22 -> player.closeInventory();
        }
    }

    private void toggleFriends(Player player) {
        boolean current = plugin.getSettingsManager().acceptsFriendRequests(player.getUniqueId());
        plugin.getSettingsManager().setAcceptFriendRequests(player.getUniqueId(), !current);
        String status = plugin.getLanguageManager().msgRaw(player, !current ? "status_enabled" : "status_disabled");
        player.sendMessage(plugin.getLanguageManager().msg(player, "settings_friends_toggled", "%status%", status));
        openSettingsGUI(player);
    }

    private void toggleDuels(Player player) {
        boolean current = plugin.getSettingsManager().acceptsDuelRequests(player.getUniqueId());
        plugin.getSettingsManager().setAcceptDuelRequests(player.getUniqueId(), !current);
        String status = plugin.getLanguageManager().msgRaw(player, !current ? "status_enabled" : "status_disabled");
        player.sendMessage(plugin.getLanguageManager().msg(player, "settings_duels_toggled", "%status%", status));
        openSettingsGUI(player);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("friends", "duels", "lang"));
        }
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
