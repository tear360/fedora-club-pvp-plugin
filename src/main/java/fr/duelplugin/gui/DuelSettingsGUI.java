package fr.duelplugin.gui;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.utils.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class DuelSettingsGUI {

    private final DuelPlugin plugin;

    public DuelSettingsGUI(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        UUID uuid = player.getUniqueId();
        boolean discordEnabled = plugin.getSettingsManager().discordNotificationsEnabled(uuid);

        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text("Paramètres de duel", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).name(" ").build());
        }

        inv.setItem(11, new ItemBuilder(Material.REDSTONE)
                .name("§5⚙ Discord")
                .lore(
                        "",
                        discordEnabled ? "§a✔ Envoyer les résultats sur Discord" : "§c✖ Envoyer les résultats sur Discord",
                        "",
                        discordEnabled ? "§aActivé" : "§cDésactivé",
                        "",
                        "§7Cliquez pour basculer"
                ).build());

        inv.setItem(13, new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .name("§cRetour")
                .build());

        player.openInventory(inv);
    }
}
