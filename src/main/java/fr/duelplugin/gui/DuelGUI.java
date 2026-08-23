package fr.duelplugin.gui;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.DuelGameMode;
import fr.duelplugin.utils.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuelGUI {

    private final DuelPlugin plugin;
    private final Map<UUID, UUID> pendingTarget = new HashMap<>();

    public DuelGUI(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    public void openModeSelector(Player player, Player target) {
        if (target != null) {
            pendingTarget.put(player.getUniqueId(), target.getUniqueId());
        }

        Inventory inv = Bukkit.createInventory(null, 45,
                Component.text(target != null ? plugin.getLanguageManager().msgRaw(player, "gui_duel_title", "%target%", target.getName()) : plugin.getLanguageManager().msgRaw(player, "gui_mode_select"),
                        NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));

        for (int i = 0; i < 45; i++) {
            inv.setItem(i, new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).name(" ").build());
        }

        int[] border = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,37,38,39,40,41,42,43,44};
        for (int slot : border) {
            inv.setItem(slot, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());
        }

        DuelGameMode[] modes = DuelGameMode.values();
        int[] slots = {10, 11, 12, 13, 14, 19, 20, 21};

        for (int i = 0; i < modes.length && i < slots.length; i++) {
            DuelGameMode mode = modes[i];
            Material icon = getModeIcon(mode);
            boolean hasArena = !mode.isArenaRestricted() || plugin.getArenaManager().getAvailableArena(mode) != null;

            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add("");
            lore.add(mode.isArenaRestricted() ?
                    (hasArena ? plugin.getLanguageManager().msgRaw(player, "gui_arenas_available") : plugin.getLanguageManager().msgRaw(player, "gui_no_arena")) :
                    plugin.getLanguageManager().msgRaw(player, "gui_free_mode"));
            lore.add(plugin.getLanguageManager().msgRaw(player, "gui_blocks") + (mode.canBreakBlocks() ? plugin.getLanguageManager().msgRaw(player, "gui_blocks_breakable") : plugin.getLanguageManager().msgRaw(player, "gui_blocks_unbreakable")));
            if (mode.supportsRounds()) {
                int rounds = plugin.getSettingsManager().getRoundCount(player.getUniqueId());
                lore.add("§dRounds: §fFirst to " + rounds);
            }
            lore.add("");
            lore.add(plugin.getLanguageManager().msgRaw(player, "gui_click_to_play"));

            inv.setItem(slots[i], new ItemBuilder(icon)
                    .name(mode.getColoredName())
                    .lore(lore.toArray(new String[0]))
                    .build());
        }

        boolean discordEnabled = plugin.getSettingsManager().discordNotificationsEnabled(player.getUniqueId());
        inv.setItem(31, new ItemBuilder(Material.REDSTONE)
                .name("§5⚙ Paramètres de duel")
                .lore(
                        "",
                        discordEnabled ? "§a✔ Discord: Activé" : "§c✖ Discord: Désactivé",
                        "",
                        "§7Cliquez pour configurer"
                ).build());

        player.openInventory(inv);
    }

    private Material getModeIcon(DuelGameMode mode) {
        return switch (mode) {
            case SWORD -> Material.DIAMOND_SWORD;
            case AXE -> Material.DIAMOND_AXE;
            case UHC -> Material.GOLDEN_APPLE;
            case MACE -> Material.MACE;
            case SMP -> Material.SHIELD;
            case DIASMP -> Material.CHORUS_FRUIT;
            case POT -> Material.SPLASH_POTION;
            case NETHPOT -> Material.NETHERITE_HELMET;
        };
    }

    public UUID getPendingTarget(UUID playerUuid) {
        return pendingTarget.remove(playerUuid);
    }

    public UUID peekPendingTarget(UUID playerUuid) {
        return pendingTarget.get(playerUuid);
    }
}
