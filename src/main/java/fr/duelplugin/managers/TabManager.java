package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TabManager {

    private final DuelPlugin plugin;
    private final Map<UUID, Set<UUID>> spectators = new HashMap<>();

    public TabManager(DuelPlugin plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    private void startUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAllTabs();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void updateAllTabs() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getDuelManager().isInDuel(player)) {
                updateDuelTab(player);
            } else {
                updateLobbyTab(player);
            }
        }
    }

    private Component buildHeader() {
        return Component.text()
                .append(Component.text("\n", NamedTextColor.DARK_PURPLE))
                .append(Component.text("    §5§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", NamedTextColor.DARK_PURPLE))
                .append(Component.text("\n", NamedTextColor.DARK_PURPLE))
                .append(Component.text("\n", NamedTextColor.DARK_PURPLE))
                .append(Component.text("         §5§lFEDORA ", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
                .append(Component.text("§d§lCLUB", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
                .append(Component.text("\n", NamedTextColor.DARK_PURPLE))
                .append(Component.text("\n", NamedTextColor.DARK_PURPLE))
                .append(Component.text("    §5§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", NamedTextColor.DARK_PURPLE))
                .append(Component.text("\n", NamedTextColor.DARK_PURPLE))
                .build();
    }

    private Component buildLobbyFooter() {
        return Component.text("\n§7§l fedora.free-node.ovh\n");
    }

    private void updateLobbyTab(Player player) {
        player.sendPlayerListHeaderAndFooter(buildHeader(), buildLobbyFooter());

        DuelManager.ActiveDuel duel = plugin.getDuelManager().getDuel(player.getUniqueId());
        if (duel == null) {
            if (plugin.getVipManager().isVip(player.getUniqueId())) {
                player.playerListName(Component.text("★ " + player.getName(), NamedTextColor.LIGHT_PURPLE));
            } else {
                player.playerListName(Component.text(player.getName(), NamedTextColor.WHITE));
            }
        }
    }

    private void updateDuelTab(Player player) {
        DuelManager.ActiveDuel duel = plugin.getDuelManager().getDuel(player.getUniqueId());
        if (duel == null) return;

        UUID opponentUuid = duel.getOpponent(player.getUniqueId());
        Player opponent = Bukkit.getPlayer(opponentUuid);

        Set<UUID> specs = spectators.getOrDefault(duel.getPlayer1(), new HashSet<>());
        StringBuilder footerBuilder = new StringBuilder();
        footerBuilder.append("\n§5§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        footerBuilder.append("\n\n");
        footerBuilder.append("  §d⚔ ").append(player.getName()).append(" §7vs §d").append(opponent != null ? opponent.getName() : "???").append("\n");
        footerBuilder.append("  §7Mode: ").append(duel.getMode().getDisplayName()).append("\n");
        if (!specs.isEmpty()) {
            footerBuilder.append("  §8Spectateurs: §7").append(specs.size()).append("\n");
        }
        footerBuilder.append("\n§5§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n");

        Component footer = Component.text(footerBuilder.toString());

        player.sendPlayerListHeaderAndFooter(buildHeader(), footer);

        player.playerListName(Component.text("⚔ " + player.getName(), NamedTextColor.RED));

        if (opponent != null) {
            opponent.playerListName(Component.text("⚔ " + opponent.getName(), NamedTextColor.RED));
        }

        for (UUID specUuid : specs) {
            Player spec = Bukkit.getPlayer(specUuid);
            if (spec != null) {
                spec.playerListName(Component.text("👁 " + spec.getName(), NamedTextColor.GRAY));
            }
        }
    }

    public void addSpectator(UUID duelOwner, UUID spectator) {
        spectators.computeIfAbsent(duelOwner, k -> new HashSet<>()).add(spectator);
    }

    public void removeSpectator(UUID spectator) {
        for (Set<UUID> specs : spectators.values()) {
            specs.remove(spectator);
        }
    }

    public Set<UUID> getSpectators(UUID duelOwner) {
        return spectators.getOrDefault(duelOwner, new HashSet<>());
    }

    public void clearSpectators(UUID duelOwner) {
        spectators.remove(duelOwner);
    }
}
