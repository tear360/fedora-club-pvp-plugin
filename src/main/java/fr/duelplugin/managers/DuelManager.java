package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.Arena;
import fr.duelplugin.models.DuelGameMode;
import fr.duelplugin.models.DuelPlayer;
import fr.duelplugin.models.DuelRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class DuelManager {

    private final DuelPlugin plugin;
    private final ArenaManager arenaManager;
    private final PlayerManager playerManager;
    private final Map<UUID, DuelRequest> pendingRequests;
    private final Map<UUID, ActiveDuel> activeDuels;
    private final Map<UUID, ItemStack[]> savedInventories;
    private final Map<UUID, ItemStack[]> savedArmor;
    private final Map<UUID, Collection<PotionEffect>> savedEffects;

    public DuelManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();
        this.playerManager = plugin.getPlayerManager();
        this.pendingRequests = new HashMap<>();
        this.activeDuels = new HashMap<>();
        this.savedInventories = new HashMap<>();
        this.savedArmor = new HashMap<>();
        this.savedEffects = new HashMap<>();
    }

    public boolean sendRequest(Player sender, Player receiver, DuelGameMode mode) {
        if (sender.getUniqueId().equals(receiver.getUniqueId())) return false;
        if (isInDuel(sender) || isInDuel(receiver)) return false;
        if (mode.isArenaRestricted() && arenaManager.getAvailableArena(mode) == null) return false;

        pendingRequests.put(receiver.getUniqueId(), new DuelRequest(sender.getUniqueId(), receiver.getUniqueId(), mode));

        receiver.sendMessage("");
        receiver.sendMessage("§6§l=============================");
        receiver.sendMessage("§e⚔ §6Demande de duel!");
        receiver.sendMessage("§eJoueur: §f" + sender.getName());
        receiver.sendMessage("§eMode: §f" + mode.getDisplayName());
        receiver.sendMessage("");

        Component acceptButton = Component.text("§a§l[ACCEPTER]")
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptduel " + sender.getName()))
                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("§aCliquez pour accepter le duel")));

        Component denyButton = Component.text(" §c§l[REFUSER]")
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/denyduel " + sender.getName()))
                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("§cCliquez pour refuser le duel")));

        receiver.sendMessage(Component.text("§eAction: ").append(acceptButton).append(denyButton));
        receiver.sendMessage("");
        receiver.sendMessage("§6§l=============================");

        return true;
    }

    public DuelRequest getPendingRequest(Player player) {
        DuelRequest req = pendingRequests.get(player.getUniqueId());
        if (req != null && req.isExpired()) {
            pendingRequests.remove(player.getUniqueId());
            return null;
        }
        return req;
    }

    public DuelRequest getRequestFromSender(UUID receiver, UUID sender) {
        DuelRequest req = pendingRequests.get(receiver);
        if (req != null && req.getSender().equals(sender)) {
            if (req.isExpired()) {
                pendingRequests.remove(receiver);
                return null;
            }
            return req;
        }
        return null;
    }

    public void removeRequest(UUID uuid) {
        pendingRequests.remove(uuid);
    }

    public boolean acceptRequest(Player receiver) {
        DuelRequest request = getPendingRequest(receiver);
        if (request == null) return false;

        Player sender = request.getSenderPlayer();
        if (sender == null || !sender.isOnline()) {
            pendingRequests.remove(receiver.getUniqueId());
            return false;
        }

        DuelGameMode mode = request.getMode();
        Arena arena = null;
        if (mode.isArenaRestricted()) {
            arena = arenaManager.getAvailableArena(mode);
        }

        startDuel(sender, receiver, mode, arena);
        pendingRequests.remove(receiver.getUniqueId());
        return true;
    }

    public void startDuel(Player player1, Player player2, DuelGameMode mode, Arena arena) {
        saveInventory(player1);
        saveInventory(player2);

        Location loc1, loc2;
        if (arena != null && arena.getSpawn1() != null && arena.getSpawn2() != null) {
            loc1 = arena.getSpawn1().clone();
            loc2 = arena.getSpawn2().clone();
        } else {
            loc1 = plugin.getLobbyManager().getLobbySpawn().clone().add(2, 0, 0);
            loc2 = plugin.getLobbyManager().getLobbySpawn().clone().add(-2, 0, 0);
        }

        player1.teleport(loc1);
        player2.teleport(loc2);

        fr.duelplugin.models.Kit.giveKit(player1.getInventory(), mode);
        fr.duelplugin.models.Kit.giveKit(player2.getInventory(), mode);

        player1.setHealth(20.0);
        player2.setHealth(20.0);
        player1.setFoodLevel(20);
        player2.setFoodLevel(20);
        player1.setSaturation(20f);
        player2.setSaturation(20f);

        ActiveDuel duel = new ActiveDuel(player1.getUniqueId(), player2.getUniqueId(), mode, arena);
        activeDuels.put(player1.getUniqueId(), duel);
        activeDuels.put(player2.getUniqueId(), duel);

        plugin.getScoreboardManager().createDuelScoreboard(player1, player2, mode);
        plugin.getScoreboardManager().createDuelScoreboard(player2, player1, mode);

        player1.sendMessage(plugin.getPrefix() + "§6§lDUEL COMMENCÉ! §eContre §f" + player2.getName() + " §een §f" + mode.getDisplayName());
        player2.sendMessage(plugin.getPrefix() + "§6§lDUEL COMMENCÉ! §eContre §f" + player1.getName() + " §een §f" + mode.getDisplayName());
    }

    public void endDuel(UUID uuid, UUID winner, UUID loser) {
        ActiveDuel duel = activeDuels.remove(uuid);
        if (duel == null) return;

        activeDuels.remove(duel.getPlayer1());
        activeDuels.remove(duel.getPlayer2());

        Player w = Bukkit.getPlayer(winner);
        Player l = Bukkit.getPlayer(loser);

        DuelPlayer dw = playerManager.getDuelPlayer(winner);
        DuelPlayer dl = playerManager.getDuelPlayer(loser);

        dw.addKill(duel.getMode().getConfigName());
        dw.addWin();
        dl.addDeath(duel.getMode().getConfigName());
        dl.resetWinStreak();

        if (w != null) {
            restoreInventory(w);
            w.sendMessage("");
            w.sendMessage("§6§l=============================");
            w.sendMessage("§a§l⚔ VICTOIRE!");
            w.sendMessage("§aVous avez gagné contre §e" + (l != null ? l.getName() : "Unknown"));
            w.sendMessage("§6§l=============================");
            w.sendMessage("");
            plugin.getScoreboardManager().removeScoreboard(w);
        }
        if (l != null) {
            restoreInventory(l);
            l.sendMessage("");
            l.sendMessage("§6§l=============================");
            l.sendMessage("§c§l⚔ DÉFAITE");
            l.sendMessage("§cVous avez perdu contre §e" + (w != null ? w.getName() : "Unknown"));
            l.sendMessage("§6§l=============================");
            l.sendMessage("");
            plugin.getScoreboardManager().removeScoreboard(l);
        }

        if (duel.getArena() != null) {
            duel.getArena().restoreBlocks();
        }
    }

    public void handleDisconnect(Player player) {
        UUID uuid = player.getUniqueId();
        if (isInDuel(uuid)) {
            ActiveDuel duel = activeDuels.get(uuid);
            UUID opponent = duel.getOpponent(uuid);
            endDuel(uuid, opponent, uuid);
        }
        restoreInventory(player);
    }

    public boolean isInDuel(Player player) {
        return activeDuels.containsKey(player.getUniqueId());
    }

    public boolean isInDuel(UUID uuid) {
        return activeDuels.containsKey(uuid);
    }

    public ActiveDuel getDuel(UUID uuid) {
        return activeDuels.get(uuid);
    }

    public void saveInventory(Player player) {
        savedInventories.put(player.getUniqueId(), player.getInventory().getContents().clone());
        savedArmor.put(player.getUniqueId(), player.getInventory().getArmorContents().clone());
        savedEffects.put(player.getUniqueId(), new ArrayList<>(player.getActivePotionEffects()));
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);
        player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));
    }

    public void restoreInventory(Player player) {
        UUID uuid = player.getUniqueId();
        if (savedInventories.containsKey(uuid)) {
            player.getInventory().setContents(savedInventories.remove(uuid));
            player.getInventory().setArmorContents(savedArmor.remove(uuid));
            player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));
            Collection<PotionEffect> effects = savedEffects.remove(uuid);
            if (effects != null) {
                effects.forEach(pe -> player.addPotionEffect(pe));
            }
        }
    }

    public void cleanup() {
        for (UUID uuid : new ArrayList<>(activeDuels.keySet())) {
            ActiveDuel duel = activeDuels.get(uuid);
            if (duel != null) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) restoreInventory(p);
            }
        }
        activeDuels.clear();
        pendingRequests.clear();
    }

    public static class ActiveDuel {
        private final UUID player1;
        private final UUID player2;
        private final DuelGameMode mode;
        private final Arena arena;
        private final long startTime;

        public ActiveDuel(UUID player1, UUID player2, DuelGameMode mode, Arena arena) {
            this.player1 = player1;
            this.player2 = player2;
            this.mode = mode;
            this.arena = arena;
            this.startTime = System.currentTimeMillis();
        }

        public UUID getPlayer1() { return player1; }
        public UUID getPlayer2() { return player2; }
        public DuelGameMode getMode() { return mode; }
        public Arena getArena() { return arena; }
        public long getStartTime() { return startTime; }

        public UUID getOpponent(UUID uuid) {
            return uuid.equals(player1) ? player2 : player1;
        }
    }
}
