package fr.duelplugin.listeners;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.DuelGameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LobbyItemListener implements Listener {

    private final DuelPlugin plugin;

    public LobbyItemListener(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDuelManager().isInDuel(player)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;
        if (item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null) return;

        String name = item.getItemMeta().getDisplayName();

        if (name.contains("§d§lDéfi")) {
            event.setCancelled(true);
            player.chat("/duel");
        } else if (name.contains("§5§lKits")) {
            event.setCancelled(true);
            plugin.getKitEditorGUI().openModeSelector(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (title == null) return;

        if (title.contains("Éditeur de kits")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            if (event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) return;
            if (event.getCurrentItem().getItemMeta() == null) return;

            String name = event.getCurrentItem().getItemMeta().getDisplayName();
            for (DuelGameMode mode : DuelGameMode.values()) {
                if (name.contains(mode.getDisplayName())) {
                    plugin.getKitEditorGUI().openKitEditor(player, mode);
                    break;
                }
            }
            return;
        }

        if (title.contains("Kit ")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            String name = event.getCurrentItem().getItemMeta() != null ? event.getCurrentItem().getItemMeta().getDisplayName() : "";

            if (name.contains("Sauvegarder")) {
                DuelGameMode mode = plugin.getKitEditorGUI().getEditingMode(player.getUniqueId());
                if (mode != null) {
                    plugin.getKitEditorGUI().saveKit(player, mode, event.getInventory());
                }
                plugin.getKitEditorGUI().removeEditingMode(player.getUniqueId());
                plugin.getKitEditorGUI().openModeSelector(player);
                return;
            }

            if (name.contains("Réinitialiser")) {
                DuelGameMode mode = plugin.getKitEditorGUI().getEditingMode(player.getUniqueId());
                if (mode != null) {
                    plugin.getKitEditorGUI().resetKit(player, mode);
                }
                plugin.getKitEditorGUI().removeEditingMode(player.getUniqueId());
                plugin.getKitEditorGUI().openModeSelector(player);
                return;
            }

            if (name.contains("Retour")) {
                plugin.getKitEditorGUI().removeEditingMode(player.getUniqueId());
                plugin.getKitEditorGUI().openModeSelector(player);
                return;
            }
        }
    }
}
