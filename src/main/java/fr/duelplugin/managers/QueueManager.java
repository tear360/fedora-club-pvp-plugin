package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.Arena;
import fr.duelplugin.models.DuelGameMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class QueueManager {

    private final DuelPlugin plugin;
    private final Map<DuelGameMode, LinkedHashSet<UUID>> queues = new HashMap<>();

    public QueueManager(DuelPlugin plugin) {
        this.plugin = plugin;
        for (DuelGameMode mode : DuelGameMode.values()) {
            queues.put(mode, new LinkedHashSet<>());
        }
        startMatchTask();
    }

    private void startMatchTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateActionbars();
                tryMatch();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void tryMatch() {
        for (DuelGameMode mode : DuelGameMode.values()) {
            LinkedHashSet<UUID> queue = queues.get(mode);
            if (queue.size() < 2) continue;

            Arena arena = null;
            if (mode.isArenaRestricted()) {
                arena = plugin.getArenaManager().getAvailableArena(mode);
            }

            Iterator<UUID> it = queue.iterator();
            List<UUID> matched = new ArrayList<>();

            while (it.hasNext() && matched.size() < 2) {
                UUID uuid = it.next();
                Player p = Bukkit.getPlayer(uuid);
                if (p == null || !p.isOnline() || plugin.getDuelManager().isInDuel(p)) {
                    it.remove();
                    continue;
                }
                matched.add(uuid);
            }

            if (matched.size() >= 2) {
                queue.remove(matched.get(0));
                queue.remove(matched.get(1));

                Player p1 = Bukkit.getPlayer(matched.get(0));
                Player p2 = Bukkit.getPlayer(matched.get(1));

                if (p1 != null && p2 != null) {
                    plugin.getDuelManager().startDuel(p1, p2, mode, arena);
                }
            }
        }
    }

    private void updateActionbars() {
        for (DuelGameMode mode : DuelGameMode.values()) {
            LinkedHashSet<UUID> queue = queues.get(mode);
            if (queue.isEmpty()) continue;

            int count = queue.size();
            for (UUID uuid : queue) {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null || !p.isOnline()) continue;
                p.sendActionBar(Component.text()
                        .append(Component.text("En queue pour ", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text(mode.getDisplayName(), NamedTextColor.WHITE))
                        .append(Component.text(" ... ", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("(" + count + " joueur" + (count > 1 ? "s" : "") + ")", NamedTextColor.GRAY))
                        .build());
            }
        }
    }

    public void joinQueue(Player player, DuelGameMode mode) {
        if (isInAnyQueue(player)) return;
        if (plugin.getDuelManager().isInDuel(player)) return;
        queues.get(mode).add(player.getUniqueId());
    }

    public void leaveQueue(Player player) {
        for (LinkedHashSet<UUID> queue : queues.values()) {
            queue.remove(player.getUniqueId());
        }
    }

    public void leaveQueue(Player player, DuelGameMode mode) {
        queues.get(mode).remove(player.getUniqueId());
    }

    public boolean isInAnyQueue(Player player) {
        for (LinkedHashSet<UUID> queue : queues.values()) {
            if (queue.contains(player.getUniqueId())) return true;
        }
        return false;
    }

    public boolean isInQueue(Player player, DuelGameMode mode) {
        return queues.get(mode).contains(player.getUniqueId());
    }

    public int getQueueSize(DuelGameMode mode) {
        return queues.get(mode).size();
    }

    public void cleanup() {
        queues.values().forEach(LinkedHashSet::clear);
    }
}
