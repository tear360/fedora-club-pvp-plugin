package fr.duelplugin;

import fr.duelplugin.commands.AcceptDuelCommand;
import fr.duelplugin.commands.DenyDuelCommand;
import fr.duelplugin.commands.DuelAdminCommand;
import fr.duelplugin.commands.DuelCommand;
import fr.duelplugin.listeners.ArenaListener;
import fr.duelplugin.listeners.GameListener;
import fr.duelplugin.listeners.PlayerListener;
import fr.duelplugin.managers.*;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class DuelPlugin extends JavaPlugin {

    private static DuelPlugin instance;
    private ArenaManager arenaManager;
    private DuelManager duelManager;
    private PlayerManager playerManager;
    private ScoreboardManager scoreboardManager;
    private LobbyManager lobbyManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        arenaManager = new ArenaManager(this);
        lobbyManager = new LobbyManager(this);
        playerManager = new PlayerManager(this);
        scoreboardManager = new ScoreboardManager(this);
        duelManager = new DuelManager(this);

        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("duel").setTabCompleter(new DuelCommand(this));
        getCommand("acceptduel").setExecutor(new AcceptDuelCommand(this));
        getCommand("acceptduel").setTabCompleter(new AcceptDuelCommand(this));
        getCommand("denyduel").setExecutor(new DenyDuelCommand(this));
        getCommand("da").setExecutor(new DuelAdminCommand(this));
        getCommand("da").setTabCompleter(new DuelAdminCommand(this));

        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getServer().getPluginManager().registerEvents(new ArenaListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("Fedora Club - DuelPlugin enabled!");
    }

    @Override
    public void onDisable() {
        duelManager.cleanup();
        arenaManager.saveArenas();
        playerManager.saveAndUnload();
        getLogger().info("Fedora Club - DuelPlugin disabled.");
    }

    public static DuelPlugin getInstance() { return instance; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public DuelManager getDuelManager() { return duelManager; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public LobbyManager getLobbyManager() { return lobbyManager; }

    public String getPrefix() {
        return colorize(getConfig().getString("messages.prefix", "&8[&6Fedora &eClub&8] &r"));
    }

    public String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public String getMessage(String key) {
        return getPrefix() + colorize(getConfig().getString("messages." + key, "&cMessage not found: " + key));
    }
}
