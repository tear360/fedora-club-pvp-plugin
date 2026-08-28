package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.managers.LanguageManager;
import fr.duelplugin.models.DuelRequest;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AcceptDuelCommand implements CommandExecutor, TabCompleter {

    private final DuelPlugin plugin;

    public AcceptDuelCommand(DuelPlugin plugin) {
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

        if (!player.hasPermission("duelplugin.acceptduel")) {
            player.sendMessage(lang().msg(player, "duel_accept_no_perm"));
            return true;
        }

        DuelRequest req;
        if (args.length > 0) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(lang().msg(player, "player_not_found"));
                return true;
            }
            req = plugin.getDuelManager().getRequestFromSender(player.getUniqueId(), target.getUniqueId());
        } else {
            req = plugin.getDuelManager().getPendingRequest(player);
        }

        if (req == null) {
            player.sendMessage(lang().msg(player, "duel_not_in"));
            return true;
        }

        Player senderPlayer = req.getSenderPlayer();
        if (senderPlayer == null || !senderPlayer.isOnline()) {
            plugin.getDuelManager().removeRequest(player.getUniqueId());
            player.sendMessage(lang().msg(player, "duel_target_online"));
            return true;
        }

        if (plugin.getDuelManager().acceptRequest(player)) {
            senderPlayer.sendMessage(lang().msg(senderPlayer, "duel_accept_success", "%player%", player.getName()));
        } else {
            player.sendMessage(lang().msg(player, "duel_accept_fail"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).collect(Collectors.toList()));
        }
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
