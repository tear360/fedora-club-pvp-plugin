package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.DuelGameMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ScoreboardManager {

    private final DuelPlugin plugin;

    public ScoreboardManager(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    public void createLobbyScoreboard(Player player, DuelGameMode mode, String arenaName) {
        Scoreboard board = plugin.getTabManager().getOrCreateScoreboard(player);
        clearSidebar(board);

        Component title = Component.text()
                .append(Component.text("FEDORA", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
                .append(Component.text(" "))
                .append(Component.text("CLUB", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
                .build();
        Objective obj = board.registerNewObjective("duel_lobby", Criteria.DUMMY, title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

        int line = 12;
        addLine(obj, line--, "");
        addLine(obj, line--, "§8» " + player.getName());
        addLine(obj, line--, "");
        addLine(obj, line--, plugin.getLanguageManager().msgRaw(player, "sb_use_queue"));
        addLine(obj, line--, plugin.getLanguageManager().msgRaw(player, "sb_use_duel"));
        addLine(obj, line--, "");
        addLine(obj, line--, "&7" + date);
        addLine(obj, line--, "");
        addLine(obj, line--, plugin.getLanguageManager().msgRaw(player, "sb_ip"));

        player.setScoreboard(board);
    }

    public void createDuelScoreboard(Player player, Player opponent, DuelGameMode mode) {
        Scoreboard board = plugin.getTabManager().getOrCreateScoreboard(player);
        clearSidebar(board);

        Component title = Component.text()
                .append(Component.text("FEDORA", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
                .append(Component.text(" "))
                .append(Component.text("CLUB", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
                .build();
        Objective obj = board.registerNewObjective("duel_fight", Criteria.DUMMY, title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

        int line = 12;
        addLine(obj, line--, "");
        addLine(obj, line--, "§c⚔ " + player.getName());
        addLine(obj, line--, plugin.getLanguageManager().msgRaw(player, "sb_vs") + " §c" + opponent.getName());
        addLine(obj, line--, "");
        addLine(obj, line--, plugin.getLanguageManager().msgRaw(player, "sb_mode", "%mode%", mode.getDisplayName()));
        addLine(obj, line--, plugin.getLanguageManager().msgRaw(player, "sb_kills", "%count%", String.valueOf(plugin.getPlayerManager().getDuelPlayer(player.getUniqueId()).getTotalKills())));
        addLine(obj, line--, "");
        addLine(obj, line--, "§7" + date);
        addLine(obj, line--, "");
        addLine(obj, line--, plugin.getLanguageManager().msgRaw(player, "sb_ip"));

        player.setScoreboard(board);
    }

    public void removeScoreboard(Player player) {
        Scoreboard board = player.getScoreboard();
        clearSidebar(board);
        player.setScoreboard(board);
    }

    private void clearSidebar(Scoreboard board) {
        for (Objective obj : board.getObjectives()) {
            if (obj.getDisplaySlot() == DisplaySlot.SIDEBAR) {
                obj.unregister();
            }
        }
    }

    private void addLine(Objective obj, int score, String text) {
        if (text.length() > 40) text = text.substring(0, 40);
        Score s = obj.getScore(text);
        s.setScore(score);
    }
}
