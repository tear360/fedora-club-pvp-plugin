package fr.duelplugin.listeners;

import fr.duelplugin.DuelPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final DuelPlugin plugin;

    public ChatListener(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (plugin.getBanManager().isMuted(player.getUniqueId())) {
            event.setCancelled(true);
            long remaining = plugin.getBanManager().getMuteRemaining(player.getUniqueId());
            String remainingStr = remaining == -1 ? "Permanent" : fr.duelplugin.managers.BanManager.formatDuration(remaining);
            player.sendMessage("§cVous êtes mute! Temps restant: §4" + remainingStr);
            return;
        }

        String filtered = plugin.getChatFilterManager().censor(event.getMessage());
        event.setMessage(filtered);

        if (!plugin.getVipManager().isVip(player.getUniqueId())) return;

        String color = plugin.getVipManager().getNameColor(player.getUniqueId());
        if (color == null) color = "§d";

        String prefix = plugin.colorize(color + plugin.getVipManager().getBadge(player.getUniqueId()) + " ");
        String coloredName = plugin.colorize(color + player.getName());
        event.setFormat(prefix + coloredName + plugin.colorize("&7: ") + "%2$s");
    }
}
