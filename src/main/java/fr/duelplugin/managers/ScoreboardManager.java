package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.DuelGameMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final DuelPlugin plugin;
    private final Map<UUID, Scoreboard> scoreboards;

    public ScoreboardManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.scoreboards = new HashMap<>();
    }

    public void createLobbyScoreboard(Player player, DuelGameMode mode, String arenaName) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Component title = Component.text()
                .append(Component.text("FEDORA", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
                .append(Component.text(" "))
                .append(Component.text("CLUB", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
                .build();
        Objective obj = board.registerNewObjective("duel_lobby", Criteria.DUMMY, title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int line = 8;
        addLine(obj, line--, "");
        addLine(obj, line--, "--------------------");
        addLine(obj, line--, "Joueur: " + player.getName());
        addLine(obj, line--, "");
        addLine(obj, line--, "Mode: " + (mode != null ? mode.getDisplayName() : "Aucun"));
        addLine(obj, line--, "Arène: " + (arenaName != null ? arenaName : "Aucune"));
        addLine(obj, line--, "");
        addLine(obj, line--, "--------------------");
        addLine(obj, line--, "fedora.free-node.ovh");

        player.setScoreboard(board);
        scoreboards.put(player.getUniqueId(), board);
    }

    public void createDuelScoreboard(Player player, Player opponent, DuelGameMode mode) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Component title = Component.text()
                .append(Component.text("FEDORA", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
                .append(Component.text(" "))
                .append(Component.text("CLUB", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
                .build();
        Objective obj = board.registerNewObjective("duel_fight", Criteria.DUMMY, title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int line = 9;
        addLine(obj, line--, "");
        addLine(obj, line--, "--------------------");
        addLine(obj, line--, "Joueur: " + player.getName());
        addLine(obj, line--, "");
        addLine(obj, line--, "Score: 0 - 0");
        addLine(obj, line--, "Adversaire: " + opponent.getName());
        addLine(obj, line--, "Mode: " + mode.getDisplayName());
        addLine(obj, line--, "");
        addLine(obj, line--, "--------------------");

        player.setScoreboard(board);
        scoreboards.put(player.getUniqueId(), board);
    }

    public void removeScoreboard(Player player) {
        scoreboards.remove(player.getUniqueId());
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        player.setScoreboard(board);
    }

    private void addLine(Objective obj, int score, String text) {
        if (text.length() > 40) text = text.substring(0, 40);
        Score s = obj.getScore(text);
        s.setScore(score);
    }
}
