package fr.duelplugin.listeners;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.gui.DuelGUI;
import fr.duelplugin.models.DuelGameMode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ArenaListener implements Listener {

    private final DuelPlugin plugin;

    public ArenaListener(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().getTitle() == null) return;
        String title = event.getView().getTitle();
        if (!title.contains("Sélection de mode")) return;

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        if (item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

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

        UUID targetUuid = plugin.getDuelGUI().getPendingTarget(player.getUniqueId());
        if (targetUuid == null) {
            player.sendMessage(plugin.getLanguageManager().msg(player, "duel_accept_fail"));
            player.closeInventory();
            return;
        }

        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.getLanguageManager().msg(player, "player_not_found"));
            player.closeInventory();
            return;
        }

        player.closeInventory();

        if (mode.isArenaRestricted() && plugin.getArenaManager().getAvailableArena(mode) == null) {
            player.sendMessage(plugin.getLanguageManager().msg(player, "queue_no_arena", "%mode%", mode.getDisplayName()));
            return;
        }

        if (plugin.getDuelManager().sendRequest(player, target, mode)) {
            player.sendMessage(plugin.getLanguageManager().msg(player, "duel_sent", "%player%", target.getName()));
        } else {
            player.sendMessage(plugin.getLanguageManager().msg(player, "duel_accept_fail"));
        }
    }
}
