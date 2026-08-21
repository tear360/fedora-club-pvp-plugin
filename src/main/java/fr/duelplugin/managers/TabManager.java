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
    private final Set<UUID> friendTabActive = new HashSet<>();

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

    private Component buildLobbyFooter(Player player, boolean friendMode) {
        net.kyori.adventure.text.TextComponent.Builder footer = Component.text();
        footer.append(Component.text("\n"));

        if (friendMode) {
            Set<UUID> friends = plugin.getFriendsManager().getFriends(player.getUniqueId());
            List<String> offlineNames = new ArrayList<>();
            for (UUID friendUuid : friends) {
                if (Bukkit.getPlayer(friendUuid) == null) {
                    org.bukkit.OfflinePlayer offline = Bukkit.getOfflinePlayer(friendUuid);
                    String name = offline.getName();
                    if (name != null) offlineNames.add(name);
                }
            }
            if (!offlineNames.isEmpty()) {
                footer.append(Component.text("\n"));
                for (String name : offlineNames) {
                    footer.append(Component.text("  " + name + "\n", NamedTextColor.GRAY));
                }
            }

            footer.append(Component.text("\n"));
            footer.append(Component.text(plugin.getLanguageManager().msgRaw(player, "tab_mode_friends"), NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
        } else {
            footer.append(Component.text(" fedora.free-node.ovh", NamedTextColor.GRAY, TextDecoration.BOLD));
        }

        footer.append(Component.text("\n"));
        return footer.build();
    }

    private void updateLobbyTab(Player player) {
        boolean isFriendMode = friendTabActive.contains(player.getUniqueId());
        player.sendPlayerListHeaderAndFooter(buildHeader(), buildLobbyFooter(player, isFriendMode));

        String prefix = "";
        if (plugin.getPartyManager().isInParty(player.getUniqueId())) {
            if (plugin.getPartyManager().isLeader(player.getUniqueId())) {
                prefix = "§6👑 ";
            } else {
                prefix = "§7» ";
            }
        }

        if (plugin.getVipManager().isVip(player.getUniqueId())) {
            String colorCode = plugin.getVipManager().getNameColor(player.getUniqueId());
            if (colorCode == null) colorCode = "§d";
            TextColor color = COLOR_MAP.getOrDefault(colorCode, NamedTextColor.LIGHT_PURPLE);
            player.playerListName(Component.text().append(Component.text(prefix + "★ ", color)).append(Component.text(player.getName(), color)).build());
        } else {
            player.playerListName(Component.text(prefix + player.getName(), NamedTextColor.WHITE));
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
                .append(Component.text("    ▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", NamedTextColor.DARK_PURPLE))
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
            footerBuilder.append(Component.text("\n"))
                    .append(Component.text("  " + plugin.getLanguageManager().msgRaw(player, "tab_spectators") + ": ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(String.valueOf(specs.size()), NamedTextColor.GRAY))
                    .append(Component.text("\n"));
        }

        footerBuilder.append(Component.text("\n"))
                .append(Component.text("    ▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", NamedTextColor.DARK_PURPLE))
                .append(Component.text("\n"));

        player.sendPlayerListHeaderAndFooter(buildHeader(), footerBuilder.build());

        player.playerListName(Component.text().append(Component.text("⚔ ", NamedTextColor.DARK_PURPLE)).append(Component.text(player.getName(), NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD)).build());

        if (opponent != null) {
            opponent.playerListName(Component.text().append(Component.text("⚔ ", NamedTextColor.DARK_PURPLE)).append(Component.text(opponent.getName(), NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD)).build());
        }

        for (UUID specUuid : specs) {
            Player spec = Bukkit.getPlayer(specUuid);
            if (spec != null) {
                spec.playerListName(Component.text().append(Component.text("👁 ", NamedTextColor.GRAY)).append(Component.text(spec.getName(), NamedTextColor.GRAY)).build());
            }
        }
    }

    public void toggleFriendTab(Player player) {
        UUID uuid = player.getUniqueId();

        if (!friendTabActive.add(uuid)) {
            friendTabActive.remove(uuid);
            showAllPlayers(player);
        } else {
            hideNonFriends(player);
        }
    }

    private void hideNonFriends(Player player) {
        UUID uuid = player.getUniqueId();
        Set<UUID> friends = plugin.getFriendsManager().getFriends(uuid);

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.getUniqueId().equals(uuid)) continue;
            if (friends.contains(other.getUniqueId())) {
                player.showPlayer(plugin, other);
                other.playerListName(Component.text("✦ " + other.getName(), NamedTextColor.GREEN));
            } else {
                player.hidePlayer(plugin, other);
            }
        }
    }

    private void showAllPlayers(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.getUniqueId().equals(player.getUniqueId())) continue;
            player.showPlayer(plugin, other);
        }
    }

    public boolean isFriendTabActive(UUID uuid) {
        return friendTabActive.contains(uuid);
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
