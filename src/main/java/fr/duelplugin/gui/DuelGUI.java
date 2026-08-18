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

        Inventory inv = Bukkit.createInventory(null, 27,
                plugin.colorize(target != null ?
                        "&6&lChoisir un mode → §f" + target.getName() :
                        "&6&lChoisir un mode de duel"));

        int slot = 10;
        for (DuelGameMode mode : DuelGameMode.values()) {
            Material itemMat;
            switch (mode) {
                case MACE, SPEAR_MACE -> itemMat = Material.MACE;
                case VANILLA, SMP, DIASMP -> itemMat = Material.DIAMOND_SWORD;
                default -> itemMat = Material.NETHERITE_SWORD;
            }

            boolean hasArena = !mode.isArenaRestricted() || plugin.getArenaManager().getAvailableArena(mode) != null;

            inv.setItem(slot, new ItemBuilder(itemMat)
                    .name(mode.getColoredName())
                    .lore(
                            "",
                            mode.isArenaRestricted() ?
                                    (hasArena ? "&aArènes disponibles!" : "&cAucune arène disponible") :
                                    "&aMode libre",
                            "&eBlocs: " + (mode.canBreakBlocks() ? "&aCassables" : "&cNon cassables"),
                            "",
                            "&7Cliquez pour sélectionner"
                    ).build());
            slot++;
            if (slot == 17) slot = 19;
            if (slot > 25) break;
        }

        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .name(" ").build());
            }
        }

        player.openInventory(inv);
    }

    public UUID getPendingTarget(UUID playerUuid) {
        return pendingTarget.remove(playerUuid);
    }

    public UUID peekPendingTarget(UUID playerUuid) {
        return pendingTarget.get(playerUuid);
    }
}
