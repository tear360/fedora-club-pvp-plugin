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
import fr.duelplugin.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
        receiver.sendMessage("§5§l═══════════════════════════");
        receiver.sendMessage("§d⚔ §5Demande de duel!");
        receiver.sendMessage("§dJoueur: §f" + sender.getName());
        receiver.sendMessage("§dMode: §f" + mode.getDisplayName());
        receiver.sendMessage("");

        Component acceptButton = Component.text("[ACCEPTER]", NamedTextColor.GREEN, TextDecoration.BOLD)
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptduel " + sender.getName()))
                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Cliquez pour accepter le duel", NamedTextColor.GREEN)));

        Component denyButton = Component.text(" [REFUSER]", NamedTextColor.RED, TextDecoration.BOLD)
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/denyduel " + sender.getName()))
                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Cliquez pour refuser le duel", NamedTextColor.RED)));

        receiver.sendMessage(Component.text("Action: ", NamedTextColor.LIGHT_PURPLE).append(acceptButton).append(denyButton));
        receiver.sendMessage("");
        receiver.sendMessage("§5§l═══════════════════════════");

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

        if (arena != null && arena.canInteractBlocks()) {
            arena.takeSnapshot();
        }

        Location loc1, loc2;
        if (arena != null && arena.getSpawn1() != null && arena.getSpawn2() != null) {
            loc1 = arena.getSpawn1().clone();
            loc2 = arena.getSpawn2().clone();
            if (loc1.getWorld() == null) {
                loc1.setWorld(Bukkit.getWorlds().get(0));
            }
            if (loc2.getWorld() == null) {
                loc2.setWorld(Bukkit.getWorlds().get(0));
            }
        } else {
            loc1 = plugin.getLobbyManager().getLobbySpawn().clone().add(2, 0, 0);
            loc2 = plugin.getLobbyManager().getLobbySpawn().clone().add(-2, 0, 0);
        }

        player1.teleport(loc1);
        player2.teleport(loc2);

        applyKit(player1, mode);
        applyKit(player2, mode);

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

        player1.sendMessage(plugin.getPrefix() + "§5§lDUEL COMMENCÉ! §dContre §f" + player2.getName() + " §den §f" + mode.getDisplayName());
        player2.sendMessage(plugin.getPrefix() + "§5§lDUEL COMMENCÉ! §dContre §f" + player1.getName() + " §den §f" + mode.getDisplayName());
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
            w.sendMessage("§5§l═══════════════════════════");
            w.sendMessage("§a§l⚔ VICTOIRE!");
            w.sendMessage("§aVous avez gagné contre §d" + (l != null ? l.getName() : "Unknown"));
            w.sendMessage("§5§l═══════════════════════════");
            w.sendMessage("");
            plugin.getScoreboardManager().removeScoreboard(w);
            if (plugin.getLobbyManager().isLobbySet()) {
                plugin.getLobbyManager().teleportToLobby(w);
                PlayerListener.giveLobbyItems(w);
            }
        }
        if (l != null) {
            restoreInventory(l);
            l.sendMessage("");
            l.sendMessage("§5§l═══════════════════════════");
            l.sendMessage("§c§l⚔ DÉFAITE");
            l.sendMessage("§cVous avez perdu contre §d" + (w != null ? w.getName() : "Unknown"));
            l.sendMessage("§5§l═══════════════════════════");
            l.sendMessage("");
            plugin.getScoreboardManager().removeScoreboard(l);
            if (plugin.getLobbyManager().isLobbySet()) {
                plugin.getLobbyManager().teleportToLobby(l);
                PlayerListener.giveLobbyItems(l);
            }
        }

        for (UUID specUuid : new HashSet<>(plugin.getTabManager().getSpectators(duel.getPlayer1()))) {
            Player spec = Bukkit.getPlayer(specUuid);
            if (spec != null) {
                spec.setGameMode(GameMode.ADVENTURE);
                if (plugin.getLobbyManager().isLobbySet()) {
                    plugin.getLobbyManager().teleportToLobby(spec);
                    PlayerListener.giveLobbyItems(spec);
                }
                spec.sendMessage(plugin.getPrefix() + "§7Le duel est terminé.");
            }
        }
        plugin.getTabManager().clearSpectators(duel.getPlayer1());

        if (duel.getArena() != null) {
            duel.getArena().restoreFromSnapshot();
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

    private void applyKit(Player player, DuelGameMode mode) {
        java.util.Map<String, ItemStack[]> customKit = plugin.getKitManager().loadKit(player.getUniqueId(), mode);
        if (customKit != null) {
            ItemStack[] contents = customKit.get("contents");
            if (contents != null) player.getInventory().setContents(contents);
            ItemStack[] armor = customKit.get("armor");
            if (armor != null) {
                player.getInventory().setArmorContents(armor);

                java.util.Map<Integer, org.bukkit.inventory.meta.trim.ArmorTrim> trims =
                        plugin.getKitManager().loadKitTrims(player.getUniqueId(), mode);
                if (trims != null) {
                    applyTrimsToArmor(player, trims);
                }
            }
            ItemStack[] offhand = customKit.get("offhand");
            if (offhand != null && offhand.length > 0) player.getInventory().setItemInOffHand(offhand[0]);
        } else {
            fr.duelplugin.models.Kit.giveKit(player, mode);
        }
    }

    private void applyTrimsToArmor(Player player, java.util.Map<Integer, org.bukkit.inventory.meta.trim.ArmorTrim> trims) {
        int[] armorSlots = {0, 1, 2, 3}; // boots, leggings, chest, helmet indices
        ItemStack[] armor = player.getInventory().getArmorContents();

        for (int i = 0; i < armorSlots.length && i < armor.length; i++) {
            int trimSlot = armorSlots[i];
            org.bukkit.inventory.meta.trim.ArmorTrim trim = trims.get(trimSlot);
            if (trim != null && armor[i] != null && armor[i].hasItemMeta() && armor[i].getItemMeta() instanceof org.bukkit.inventory.meta.ArmorMeta) {
                org.bukkit.inventory.meta.ArmorMeta meta = (org.bukkit.inventory.meta.ArmorMeta) armor[i].getItemMeta();
                meta.setTrim(trim);
                armor[i].setItemMeta(meta);
            }
        }
        player.getInventory().setArmorContents(armor);
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
