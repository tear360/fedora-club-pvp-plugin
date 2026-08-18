package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.DuelRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        DuelRequest req;
        if (args.length > 0) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(plugin.getMessage("player-not-found"));
                return true;
            }
            req = plugin.getDuelManager().getRequestFromSender(player.getUniqueId(), target.getUniqueId());
        } else {
            req = plugin.getDuelManager().getPendingRequest(player);
        }

        if (req == null) {
            player.sendMessage(plugin.getMessage("no-duel-request"));
            return true;
        }

        Player senderPlayer = req.getSenderPlayer();
        if (senderPlayer == null || !senderPlayer.isOnline()) {
            plugin.getDuelManager().removeRequest(player.getUniqueId());
            player.sendMessage(plugin.getPrefix() + "§cCe joueur n'est plus en ligne.");
            return true;
        }

        if (plugin.getDuelManager().acceptRequest(player)) {
            senderPlayer.sendMessage(plugin.getPrefix() + "§a" + player.getName() + " §aaccepté votre duel!");
        } else {
            player.sendMessage(plugin.getPrefix() + "§cImpossible de démarrer le duel.");
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
