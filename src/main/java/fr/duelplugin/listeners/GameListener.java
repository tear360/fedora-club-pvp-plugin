package fr.duelplugin.listeners;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.DuelGameMode;
import org.bukkit.GameMode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;
import java.util.Set;

public class GameListener implements Listener {

    private final DuelPlugin plugin;

    public GameListener(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getDuelManager().isInDuel(player)) return;

        var duel = plugin.getDuelManager().getDuel(player.getUniqueId());
        if (duel == null) return;

        DuelGameMode mode = duel.getMode();

        if (!mode.canBreakBlocks()) {
            event.setCancelled(true);
            player.sendMessage(plugin.getLanguageManager().msg(player, "gamemode_disabled"));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getDuelManager().isInDuel(player)) return;

        var duel = plugin.getDuelManager().getDuel(player.getUniqueId());
        if (duel == null) return;

        DuelGameMode mode = duel.getMode();

        if (!mode.canPlaceBlocks()) {
            event.setCancelled(true);
            player.sendMessage(plugin.getLanguageManager().msg(player, "gamemode_disabled"));
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        if (plugin.getDuelManager().isFrozen(victim) || plugin.getDuelManager().isFrozen(attacker)) {
            event.setCancelled(true);
            return;
        }

        if (!plugin.getDuelManager().isInDuel(attacker) || !plugin.getDuelManager().isInDuel(victim)) {
            event.setCancelled(true);
            return;
        }

        var duel = plugin.getDuelManager().getDuel(attacker.getUniqueId());
        if (duel == null) return;

        boolean isFFA = duel.isFFA();

        if (!isFFA) {
            UUID uuid1 = duel.getPlayer1();
            UUID uuid2 = duel.getPlayer2();
            boolean validFight = (attacker.getUniqueId().equals(uuid1) && victim.getUniqueId().equals(uuid2)) ||
                    (attacker.getUniqueId().equals(uuid2) && victim.getUniqueId().equals(uuid1));

            if (!validFight) {
                event.setCancelled(true);
                return;
            }
        }

        if (victim.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true);
            victim.setHealth(20.0);

            if (duel.isFFA()) {
                Set<UUID> participants = duel.getFFAParticipants();
                participants.remove(victim.getUniqueId());

                victim.sendMessage(plugin.getLanguageManager().msg(victim, "duel_eliminated_ffa"));
                victim.setGameMode(GameMode.SPECTATOR);
                if (plugin.getLobbyManager().isLobbySet()) {
                    plugin.getLobbyManager().teleportToLobby(victim);
                }

                if (participants.size() <= 1) {
                    UUID winnerId = participants.iterator().next();
                    Player winner = Bukkit.getPlayer(winnerId);
                    if (winner != null) {
                        winner.sendMessage(plugin.getLanguageManager().msg(winner, "duel_winner_ffa"));
                    }
                    plugin.getDuelManager().endDuel(attacker.getUniqueId(), winnerId, victim.getUniqueId());
                }
            } else {
                plugin.getDuelManager().endDuel(attacker.getUniqueId(), attacker.getUniqueId(), victim.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getDuelManager().isInDuel(player)) {
            event.setCancelled(true);
            player.setFoodLevel(20);
            return;
        }

        var duel = plugin.getDuelManager().getDuel(player.getUniqueId());
        if (duel == null) return;

        if (duel.getMode().isNaturalRegenDisabled() || duel.getMode().isPotionBased()) {
            event.setCancelled(true);
            player.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (plugin.getDuelManager().isFrozen(player)) {
            if (event.getFrom().getX() != event.getTo().getX() ||
                event.getFrom().getY() != event.getTo().getY() ||
                event.getFrom().getZ() != event.getTo().getZ()) {
                event.setTo(event.getFrom().clone());
            }
            return;
        }

        if (!plugin.getDuelManager().isInDuel(player)) return;

        var duel = plugin.getDuelManager().getDuel(player.getUniqueId());
        if (duel == null) return;

        if (duel.getArena() != null && duel.getArena().getMinCorner() != null && duel.getArena().getMaxCorner() != null) {
            if (!duel.getArena().isInArena(event.getTo())) {
                player.teleport(event.getFrom());
            }
        }
    }
}
