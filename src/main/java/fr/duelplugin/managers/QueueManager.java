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
    private final Map<UUID, Long> queueJoinTime = new HashMap<>();

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

                long elapsed = (System.currentTimeMillis() - queueJoinTime.getOrDefault(uuid, System.currentTimeMillis())) / 1000;
                String timeStr = formatTime(elapsed);

                p.sendActionBar(Component.text()
                        .append(Component.text(plugin.getLanguageManager().msgRaw(p, "queue_action_bar"), NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text(mode.getDisplayName(), NamedTextColor.WHITE))
                        .append(Component.text(" ... ", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text(plugin.getLanguageManager().msgRaw(p, "queue_action_bar_count", "%count%", String.valueOf(count), "%s%", count > 1 ? "s" : ""), NamedTextColor.GRAY))
                        .append(Component.text(" ⏱ ", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text(timeStr, NamedTextColor.WHITE))
                        .build());
            }
        }
    }

    private String formatTime(long seconds) {
        if (seconds < 60) return seconds + "s";
        long min = seconds / 60;
        long sec = seconds % 60;
        return min + "m " + sec + "s";
    }

    public void joinQueue(Player player, DuelGameMode mode) {
        if (isInAnyQueue(player)) return;
        if (plugin.getDuelManager().isInDuel(player)) return;
        queues.get(mode).add(player.getUniqueId());
        queueJoinTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void leaveQueue(Player player) {
        queueJoinTime.remove(player.getUniqueId());
        for (LinkedHashSet<UUID> queue : queues.values()) {
            queue.remove(player.getUniqueId());
        }
    }

    public void leaveQueue(Player player, DuelGameMode mode) {
        queueJoinTime.remove(player.getUniqueId());
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
