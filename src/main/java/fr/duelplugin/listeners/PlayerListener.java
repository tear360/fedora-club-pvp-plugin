package fr.duelplugin.listeners;

import fr.duelplugin.DuelPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
        }

        event.setJoinMessage(plugin.colorize("&a+ &e" + player.getName() + " &7a rejoint le serveur"));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getDuelManager().handleDisconnect(player);
        plugin.getScoreboardManager().removeScoreboard(player);
        event.setQuitMessage(plugin.colorize("&c- &e" + player.getName() + " &7a quitté le serveur"));
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
}
