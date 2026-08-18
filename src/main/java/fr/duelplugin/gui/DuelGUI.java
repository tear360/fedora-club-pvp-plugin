package fr.duelplugin.gui;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.DuelGameMode;
import fr.duelplugin.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
                plugin.colorize(target != null ?
                        "&8Sélection de mode &7→ &f" + target.getName() :
                        "&8Sélection de mode de duel"));

        for (int i = 0; i < 45; i++) {
            if (i == 10 || i == 11 || i == 12 || i == 13 || i == 14 ||
                    i == 19 || i == 20 || i == 21 || i == 22 || i == 23) {
                continue;
            }
            inv.setItem(i, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build());
        }

        DuelGameMode[] modes = DuelGameMode.values();
        int[] slots = {10, 11, 12, 13, 14, 19, 20, 21, 22, 23};

        for (int i = 0; i < modes.length && i < slots.length; i++) {
            DuelGameMode mode = modes[i];
            Material icon = getModeIcon(mode);
            boolean hasArena = !mode.isArenaRestricted() || plugin.getArenaManager().getAvailableArena(mode) != null;

            inv.setItem(slots[i], new ItemBuilder(icon)
                    .name(mode.getColoredName())
                    .lore(
                            "",
                            mode.isArenaRestricted() ?
                                    (hasArena ? "&a&lArènes disponibles" : "&c&lAucune arène") :
                                    "&a&lMode libre",
                            "&7Blocs: " + (mode.canBreakBlocks() ? "&aCassables" : "&cNon cassables"),
                            "",
                            "&e&lCliquez pour jouer"
                    ).build());
        }

        player.openInventory(inv);
    }

    private Material getModeIcon(DuelGameMode mode) {
        return switch (mode) {
            case SWORD -> Material.DIAMOND_SWORD;
            case AXE -> Material.DIAMOND_AXE;
            case UHC -> Material.GOLDEN_APPLE;
            case MACE -> Material.MACE;
            case VANILLA -> Material.END_CRYSTAL;
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
