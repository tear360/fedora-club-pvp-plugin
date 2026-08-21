package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.managers.DuelManager;
import fr.duelplugin.managers.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpecCommand implements CommandExecutor, TabCompleter {

    private final DuelPlugin plugin;

    public SpecCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    private LanguageManager lang() {
        return plugin.getLanguageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(lang().msgRaw(null, "command_only_players"));
            return true;
        }

        if (args.length == 0) {
            if (isSpectating(player)) {
                stopSpectating(player);
                return true;
            }
            player.sendMessage(lang().msg(player, "spec_usage"));
            return true;
        }

        if (isSpectating(player)) {
            stopSpectating(player);
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(lang().msg(player, "player_not_found"));
            return true;
        }

        if (!plugin.getDuelManager().isInDuel(target)) {
            player.sendMessage(lang().msg(player, "spec_target_not_in_duel"));
            return true;
        }

        if (plugin.getDuelManager().isInDuel(player)) {
            player.sendMessage(lang().msg(player, "spec_already_in_duel"));
            return true;
        }

        startSpectating(player, target);
        return true;
    }

    private void startSpectating(Player spectator, Player target) {
        DuelManager.ActiveDuel duel = plugin.getDuelManager().getDuel(target.getUniqueId());
        if (duel == null) return;

        spectator.teleport(target.getLocation());
        spectator.setGameMode(GameMode.SPECTATOR);

        plugin.getTabManager().addSpectator(duel.getPlayer1(), spectator.getUniqueId());

        spectator.sendMessage(lang().msg(spectator, "spec_started", "%player%", target.getName(), "%mode%", duel.getMode().getDisplayName()));
        target.sendMessage(lang().msg(target, "spec_target_notified", "%player%", spectator.getName()));

        UUID opponentUuid = duel.getOpponent(target.getUniqueId());
        Player opponent = Bukkit.getPlayer(opponentUuid);
        if (opponent != null) {
            opponent.sendMessage(lang().msg(opponent, "spec_target_broadcast", "%player%", spectator.getName()));
        }
    }

    private void stopSpectating(Player spectator) {
        plugin.getTabManager().removeSpectator(spectator.getUniqueId());
        spectator.setGameMode(GameMode.SURVIVAL);

        if (plugin.getLobbyManager().isLobbySet()) {
            spectator.teleport(plugin.getLobbyManager().resolveLobby());
        }

        spectator.sendMessage(lang().msg(spectator, "spec_stopped"));
    }

    private boolean isSpectating(Player player) {
        for (DuelManager.ActiveDuel duel : getActiveDuels()) {
            if (plugin.getTabManager().getSpectators(duel.getPlayer1()).contains(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    private List<DuelManager.ActiveDuel> getActiveDuels() {
        List<DuelManager.ActiveDuel> duels = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            DuelManager.ActiveDuel d = plugin.getDuelManager().getDuel(p.getUniqueId());
            if (d != null && !duels.contains(d)) {
                duels.add(d);
            }
        }
        return duels;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .filter(p -> plugin.getDuelManager().isInDuel(p))
                    .map(Player::getName)
                    .collect(Collectors.toList()));
        }
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
