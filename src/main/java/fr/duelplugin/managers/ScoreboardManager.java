package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.DuelGameMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.text.SimpleDateFormat;
import java.util.Date;
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

        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

        int line = 10;
        addLine(obj, line--, "");
        addLine(obj, line--, "+" + player.getName());
        addLine(obj, line--, "");
        addLine(obj, line--, "Use §b⚔§7 to queue");
        addLine(obj, line--, "or §b/duel§7 to duel.");
        addLine(obj, line--, "");
        addLine(obj, line--, "§7" + date);
        addLine(obj, line--, "");
        addLine(obj, line--, "§6fedora.free-node.ovh");

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

        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

        int line = 11;
        addLine(obj, line--, "");
        addLine(obj, line--, "§c⚔ " + player.getName());
        addLine(obj, line--, "§7vs §c" + opponent.getName());
        addLine(obj, line--, "");
        addLine(obj, line--, "§dMode: §f" + mode.getDisplayName());
        addLine(obj, line--, "§dKills: §f0");
        addLine(obj, line--, "");
        addLine(obj, line--, "§7" + date);
        addLine(obj, line--, "");
        addLine(obj, line--, "§6fedora.free-node.ovh");

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
