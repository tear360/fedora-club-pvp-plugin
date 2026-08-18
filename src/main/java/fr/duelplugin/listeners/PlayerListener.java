package fr.duelplugin.listeners;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private final DuelPlugin plugin;

    public PlayerListener(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerManager().getDuelPlayer(player.getUniqueId());

        if (plugin.getLobbyManager().isLobbySet()) {
            plugin.getLobbyManager().teleportToLobby(player);
            plugin.getScoreboardManager().createLobbyScoreboard(player, null, null);
            giveLobbyItems(player);
        }

        event.setJoinMessage(plugin.colorize("&5+ &d" + player.getName() + " &7a rejoint le serveur"));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getDuelManager().handleDisconnect(player);
        plugin.getScoreboardManager().removeScoreboard(player);
        event.setQuitMessage(plugin.colorize("&5- &d" + player.getName() + " &7a quitté le serveur"));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!plugin.getDuelManager().isInDuel(player)) return;

        event.getDrops().clear();
        event.setDroppedExp(0);
        event.setDeathMessage(null);

        var duel = plugin.getDuelManager().getDuel(player.getUniqueId());
        if (duel == null) return;

        Player killer = player.getKiller();
        if (killer == null) {
            killer = plugin.getServer().getPlayer(duel.getOpponent(player.getUniqueId()));
        }

        if (killer != null) {
            plugin.getDuelManager().endDuel(killer.getUniqueId(), killer.getUniqueId(), player.getUniqueId());
        }
    }

    public static void giveLobbyItems(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);

        player.getInventory().setItem(0, new ItemBuilder(Material.NETHERITE_SWORD)
                .name("§d§lDéfi")
                .lore("", "§7Ouvrez le menu de duel", "").build());

        player.getInventory().setItem(4, new ItemBuilder(Material.CRAFTING_TABLE)
                .name("§5§lKits")
                .lore("", "§7Éditez vos kits", "").build());
    }
}
