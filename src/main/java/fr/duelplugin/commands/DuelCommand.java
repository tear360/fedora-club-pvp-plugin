package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.gui.DuelGUI;
import fr.duelplugin.models.DuelGameMode;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DuelCommand implements CommandExecutor, TabCompleter {

    private final DuelPlugin plugin;

    public DuelCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        if (args.length == 0) {
            openDuelMenu(player, null);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "send" -> {
                if (args.length < 2) {
                    player.sendMessage(plugin.getPrefix() + "§cUsage: /duel <joueur>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(plugin.getMessage("player-not-found"));
                    return true;
                }
                openDuelMenu(player, target);
            }
            case "help" -> {
                player.sendMessage("§6=== §eFedora Club §6- Aide Duel ===");
                player.sendMessage("§e/duel <joueur> §7- Défier un joueur");
                player.sendMessage("§e/acceptduel <joueur> §7- Accepter un duel");
                player.sendMessage("§e/denyduel <joueur> §7- Refuser un duel");
                player.sendMessage("§e/da §7- Administration");
            }
            default -> {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null) {
                    openDuelMenu(player, target);
                } else {
                    player.sendMessage(plugin.getMessage("player-not-found"));
                }
            }
        }
        return true;
    }

    private void openDuelMenu(Player player, Player target) {
        DuelGUI gui = new DuelGUI(plugin);
        gui.openModeSelector(player, target);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).collect(Collectors.toList()));
            completions.add("help");
        }
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
