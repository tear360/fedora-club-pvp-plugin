package fr.duelplugin.listeners;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.gui.DuelGUI;
import fr.duelplugin.models.Arena;
import fr.duelplugin.models.DuelGameMode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ArenaListener implements Listener {

    private final DuelPlugin plugin;
    private final DuelGUI duelGUI;

    public ArenaListener(DuelPlugin plugin) {
        this.plugin = plugin;
        this.duelGUI = new DuelGUI(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory inv = event.getInventory();
        if (inv == null) return;

        String title = event.getView().getTitle();
        if (!title.contains("Choisir un mode")) return;

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        if (item.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        if (item.getItemMeta() == null) return;
        String name = item.getItemMeta().getDisplayName();

        DuelGameMode mode = null;
        for (DuelGameMode m : DuelGameMode.values()) {
            if (name.contains(m.getDisplayName())) {
                mode = m;
                break;
            }
        }

        if (mode == null) return;

        UUID targetUuid = duelGUI.getPendingTarget(player.getUniqueId());
        if (targetUuid == null) {
            player.sendMessage(plugin.getPrefix() + "§cErreur: sélection de cible perdue. Réessayez.");
            player.closeInventory();
            return;
        }

        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.getMessage("player-not-found"));
            player.closeInventory();
            return;
        }

        player.closeInventory();

        if (mode.isArenaRestricted() && plugin.getArenaManager().getAvailableArena(mode) == null) {
            player.sendMessage(plugin.getPrefix() + "§cAucune arène disponible pour le mode §e" + mode.getDisplayName() + "§c.");
            return;
        }

        if (plugin.getDuelManager().sendRequest(player, target, mode)) {
            player.sendMessage(plugin.getPrefix() + "§aDemande envoyée à §e" + target.getName() + " §a(§f" + mode.getDisplayName() + "§a)");
        } else {
            player.sendMessage(plugin.getPrefix() + "§cImpossible d'envoyer cette demande.");
        }
    }
}
