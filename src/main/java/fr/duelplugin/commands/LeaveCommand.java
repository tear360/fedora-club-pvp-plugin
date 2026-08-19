package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.managers.DuelManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LeaveCommand implements CommandExecutor {

    private final DuelPlugin plugin;

    public LeaveCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return true;
        }

        DuelManager.ActiveDuel duel = plugin.getDuelManager().getDuel(player.getUniqueId());
        if (duel == null) {
            player.sendMessage(plugin.getPrefix() + "§cVous n'êtes pas en duel.");
            return true;
        }

        UUID opponent = duel.getOpponent(player.getUniqueId());
        Player opponentPlayer = Bukkit.getPlayer(opponent);

        plugin.getDuelManager().endDuel(player.getUniqueId(), opponent, player.getUniqueId());

        player.sendMessage(plugin.getPrefix() + "§cVous avez quitté le duel.");
        if (opponentPlayer != null) {
            opponentPlayer.sendMessage(plugin.getPrefix() + "§d" + player.getName() + " §ca quitté le duel. §aVictoire!");
        }

        return true;
    }
}
