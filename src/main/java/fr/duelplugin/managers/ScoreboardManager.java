package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.DuelGameMode;
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
        Objective obj = board.registerNewObjective("duel_lobby", Criteria.DUMMY, plugin.colorize("&5&lFEDORA &d&lCLUB"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int line = 8;
        addLine(obj, line--, "");
        addLine(obj, line--, "&8&m                    ");
        addLine(obj, line--, "&fJoueur: &d" + player.getName());
        addLine(obj, line--, "");
        addLine(obj, line--, "&fMode: &d" + (mode != null ? mode.getDisplayName() : "Aucun"));
        addLine(obj, line--, "&fArène: &d" + (arenaName != null ? arenaName : "Aucune"));
        addLine(obj, line--, "");
        addLine(obj, line--, "&8&m                    ");
        addLine(obj, line--, "&dfedora.free-node.ovh");

        player.setScoreboard(board);
        scoreboards.put(player.getUniqueId(), board);
    }

    public void createDuelScoreboard(Player player, Player opponent, DuelGameMode mode) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("duel_fight", Criteria.DUMMY, plugin.colorize("&5&lFEDORA &d&lCLUB"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int line = 9;
        addLine(obj, line--, "");
        addLine(obj, line--, "&8&m                    ");
        addLine(obj, line--, "&fJoueur: &d" + player.getName());
        addLine(obj, line--, "");
        addLine(obj, line--, "&fScore: &d0 &f- &d0");
        addLine(obj, line--, "&fAdversaire: &d" + opponent.getName());
        addLine(obj, line--, "&fMode: &d" + mode.getDisplayName());
        addLine(obj, line--, "");
        addLine(obj, line--, "&8&m                    ");

        player.setScoreboard(board);
        scoreboards.put(player.getUniqueId(), board);
    }

    public void removeScoreboard(Player player) {
        scoreboards.remove(player.getUniqueId());
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        player.setScoreboard(board);
    }

    private void addLine(Objective obj, int score, String text) {
        String colored = plugin.colorize(text);
        if (colored.length() > 40) colored = colored.substring(0, 40);
        Score s = obj.getScore(colored);
        s.setScore(score);
    }
}
