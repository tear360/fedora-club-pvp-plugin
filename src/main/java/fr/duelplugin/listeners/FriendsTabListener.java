package fr.duelplugin.listeners;

import fr.duelplugin.DuelPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class FriendsTabListener implements Listener {

    private final DuelPlugin plugin;

    public FriendsTabListener(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!event.isSneaking()) return;
        if (plugin.getDuelManager().isInDuel(player)) return;
        if (plugin.getQueueManager().isInAnyQueue(player)) return;

        plugin.getTabManager().toggleFriendTab(player);
    }
}
