package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TabManager {

    private final DuelPlugin plugin;
    private final Map<UUID, Set<UUID>> spectators = new HashMap<>();

    private static final Map<String, TextColor> COLOR_MAP = Map.of(
            "§c", NamedTextColor.RED,
            "§6", NamedTextColor.GOLD,
            "§e", NamedTextColor.YELLOW,
            "§a", NamedTextColor.GREEN,
            "§b", NamedTextColor.AQUA,
            "§d", NamedTextColor.LIGHT_PURPLE,
            "§5", NamedTextColor.DARK_PURPLE,
            "§f", NamedTextColor.WHITE,
            "§7", NamedTextColor.GRAY,
            "§0", NamedTextColor.BLACK
    );

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
                .append(Component.text("\n"))
                .append(Component.text("    ▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", NamedTextColor.DARK_PURPLE))
                .append(Component.text("\n"))
                .append(Component.text("\n"))
                .append(Component.text("         "))
                .append(Component.text("FEDORA", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
                .append(Component.text(" "))
                .append(Component.text("CLUB", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
                .append(Component.text("\n"))
                .append(Component.text("\n"))
                .append(Component.text("    ▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", NamedTextColor.DARK_PURPLE))
                .append(Component.text("\n"))
                .build();
    }

    private Component buildLobbyFooter() {
        return Component.text()
                .append(Component.text("\n"))
                .append(Component.text(" fedora.free-node.ovh", NamedTextColor.GRAY, TextDecoration.BOLD))
                .append(Component.text("\n"))
                .build();
    }

    private void updateLobbyTab(Player player) {
        player.sendPlayerListHeaderAndFooter(buildHeader(), buildLobbyFooter());

        DuelManager.ActiveDuel duel = plugin.getDuelManager().getDuel(player.getUniqueId());
        if (duel == null) {
            if (plugin.getVipManager().isVip(player.getUniqueId())) {
                String colorCode = plugin.getVipManager().getNameColor(player.getUniqueId());
                if (colorCode == null) colorCode = "§d";
                TextColor color = COLOR_MAP.getOrDefault(colorCode, NamedTextColor.LIGHT_PURPLE);
                player.playerListName(Component.text().append(Component.text("★ ", color)).append(Component.text(player.getName(), NamedTextColor.WHITE)).build());
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

        net.kyori.adventure.text.TextComponent.Builder footerBuilder = Component.text();
        footerBuilder.append(Component.text("\n"))
                .append(Component.text("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", NamedTextColor.DARK_PURPLE))
                .append(Component.text("\n\n"))
                .append(Component.text("  ⚔ ", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(player.getName(), NamedTextColor.WHITE))
                .append(Component.text(" vs ", NamedTextColor.GRAY))
                .append(Component.text(opponent != null ? opponent.getName() : "???", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text("\n"))
                .append(Component.text("  Mode: ", NamedTextColor.GRAY))
                .append(Component.text(duel.getMode().getDisplayName(), NamedTextColor.LIGHT_PURPLE))
                .append(Component.text("\n"));

        if (!specs.isEmpty()) {
            footerBuilder.append(Component.text("  Spectateurs: ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(String.valueOf(specs.size()), NamedTextColor.GRAY))
                    .append(Component.text("\n"));
        }

        footerBuilder.append(Component.text("\n"))
                .append(Component.text("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", NamedTextColor.DARK_PURPLE))
                .append(Component.text("\n"));

        Component footer = footerBuilder.build();

        player.sendPlayerListHeaderAndFooter(buildHeader(), footer);

        player.playerListName(Component.text().append(Component.text("⚔ ", NamedTextColor.RED)).append(Component.text(player.getName(), NamedTextColor.RED)).build());

        if (opponent != null) {
            opponent.playerListName(Component.text().append(Component.text("⚔ ", NamedTextColor.RED)).append(Component.text(opponent.getName(), NamedTextColor.RED)).build());
        }

        for (UUID specUuid : specs) {
            Player spec = Bukkit.getPlayer(specUuid);
            if (spec != null) {
                spec.playerListName(Component.text().append(Component.text("👁 ", NamedTextColor.GRAY)).append(Component.text(spec.getName(), NamedTextColor.GRAY)).build());
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
