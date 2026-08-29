package fr.duelplugin;

import fr.duelplugin.commands.BugReportCommand;
import fr.duelplugin.commands.AcceptDuelCommand;
import fr.duelplugin.commands.BanCommand;
import fr.duelplugin.commands.DenyDuelCommand;
import fr.duelplugin.commands.DuelAdminCommand;
import fr.duelplugin.commands.DuelCommand;
import fr.duelplugin.commands.FriendsCommand;
import fr.duelplugin.commands.KickCommand;
import fr.duelplugin.commands.LeaveCommand;
import fr.duelplugin.commands.MuteCommand;
import fr.duelplugin.commands.PartyCommand;
import fr.duelplugin.commands.ReportCommand;
import fr.duelplugin.commands.SettingsCommand;
import fr.duelplugin.commands.SpecCommand;
import fr.duelplugin.commands.TempBanCommand;
import fr.duelplugin.commands.UnbanCommand;
import fr.duelplugin.commands.UnmuteCommand;
import fr.duelplugin.commands.VipCommand;
import fr.duelplugin.gui.DuelGUI;
import fr.duelplugin.gui.DuelSettingsGUI;
import fr.duelplugin.gui.KitEditorGUI;
import fr.duelplugin.gui.PartyGUI;
import fr.duelplugin.listeners.ArenaListener;
import fr.duelplugin.listeners.ChatListener;
import fr.duelplugin.listeners.FriendsTabListener;
import fr.duelplugin.listeners.GameListener;
import fr.duelplugin.listeners.LobbyItemListener;
import fr.duelplugin.listeners.PlayerListener;
import fr.duelplugin.managers.*;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DuelPlugin extends JavaPlugin {

    private static DuelPlugin instance;
    private ArenaManager arenaManager;
    private DuelManager duelManager;
    private PlayerManager playerManager;
    private ScoreboardManager scoreboardManager;
    private LobbyManager lobbyManager;
    private DuelGUI duelGUI;
    private DuelSettingsGUI duelSettingsGUI;
    private TabManager tabManager;
    private UpdateManager updateManager;
    private KitManager kitManager;
    private KitEditorGUI kitEditorGUI;
    private PartyGUI partyGUI;
    private QueueManager queueManager;
    private VIPManager vipManager;
    private RankManager rankManager;
    private FriendsManager friendsManager;
    private PartyManager partyManager;
    private SettingsManager settingsManager;
    private LanguageManager languageManager;
    private ChatFilterManager chatFilterManager;
    private BanManager banManager;
    private ReportManager reportManager;
    private DiscordBotManager discordBotManager;
    private DuelBotManager duelBotManager;
    private final Set<UUID> buildModePlayers = ConcurrentHashMap.newKeySet();

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        PacketEvents.getAPI().init();

        arenaManager = new ArenaManager(this);
        lobbyManager = new LobbyManager(this);
        playerManager = new PlayerManager(this);
        scoreboardManager = new ScoreboardManager(this);
        duelManager = new DuelManager(this);
        duelGUI = new DuelGUI(this);
        duelSettingsGUI = new DuelSettingsGUI(this);
        tabManager = new TabManager(this);
        updateManager = new UpdateManager(this);
        kitManager = new KitManager(this);
        kitEditorGUI = new KitEditorGUI(this);
        partyGUI = new PartyGUI(this);
        queueManager = new QueueManager(this);
        vipManager = new VIPManager(this);
        rankManager = new RankManager(this);
        friendsManager = new FriendsManager(this);
        partyManager = new PartyManager(this);
        settingsManager = new SettingsManager(this);
        languageManager = new LanguageManager(this);
        chatFilterManager = new ChatFilterManager(this);
        banManager = new BanManager(this);
        reportManager = new ReportManager(this);
        discordBotManager = new DiscordBotManager(this);
        duelBotManager = new DuelBotManager(this);

        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("duel").setTabCompleter(new DuelCommand(this));
        getCommand("acceptduel").setExecutor(new AcceptDuelCommand(this));
        getCommand("acceptduel").setTabCompleter(new AcceptDuelCommand(this));
        getCommand("denyduel").setExecutor(new DenyDuelCommand(this));
        getCommand("da").setExecutor(new DuelAdminCommand(this));
        getCommand("da").setTabCompleter(new DuelAdminCommand(this));
        getServer().getPluginManager().registerEvents(new DuelAdminCommand(this), this);
        getCommand("spec").setExecutor(new SpecCommand(this));
        getCommand("spec").setTabCompleter(new SpecCommand(this));
        getCommand("vip").setExecutor(new VipCommand(this));
        getCommand("vip").setTabCompleter(new VipCommand(this));
        getCommand("f").setExecutor(new FriendsCommand(this));
        getCommand("f").setTabCompleter(new FriendsCommand(this));
        getCommand("leave").setExecutor(new LeaveCommand(this));
        getCommand("party").setExecutor(new PartyCommand(this));
        getCommand("party").setTabCompleter(new PartyCommand(this));
        getCommand("settings").setExecutor(new SettingsCommand(this));
        getCommand("settings").setTabCompleter(new SettingsCommand(this));
        getServer().getPluginManager().registerEvents(new SettingsCommand(this), this);

        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("kick").setExecutor(new KickCommand(this));
        getCommand("tempban").setExecutor(new TempBanCommand(this));
        getCommand("mute").setExecutor(new MuteCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this));
        getCommand("unmute").setExecutor(new UnmuteCommand(this));
        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("report").setTabCompleter(new ReportCommand(this));
        getCommand("bugreport").setExecutor(new BugReportCommand(this));

        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getServer().getPluginManager().registerEvents(new ArenaListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new LobbyItemListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new FriendsTabListener(this), this);
        getServer().getPluginManager().registerEvents(duelBotManager, this);

        updateManager.checkForUpdates();

        getLogger().info("§5Fedora Club §d- DuelPlugin enabled!");
    }

    @Override
    public void onDisable() {
        queueManager.cleanup();
        duelManager.cleanup();
        duelBotManager.cleanup();
        arenaManager.saveArenas();
        playerManager.saveAndUnload();
        discordBotManager.shutdown();
        PacketEvents.getAPI().terminate();
        getLogger().info("§5Fedora Club §d- DuelPlugin disabled.");
    }

    public static DuelPlugin getInstance() { return instance; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public DuelManager getDuelManager() { return duelManager; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public LobbyManager getLobbyManager() { return lobbyManager; }
    public DuelGUI getDuelGUI() { return duelGUI; }
    public DuelSettingsGUI getDuelSettingsGUI() { return duelSettingsGUI; }
    public TabManager getTabManager() { return tabManager; }
    public UpdateManager getUpdateManager() { return updateManager; }
    public KitManager getKitManager() { return kitManager; }
    public KitEditorGUI getKitEditorGUI() { return kitEditorGUI; }
    public PartyGUI getPartyGUI() { return partyGUI; }
    public QueueManager getQueueManager() { return queueManager; }
    public VIPManager getVipManager() { return vipManager; }
    public RankManager getRankManager() { return rankManager; }
    public FriendsManager getFriendsManager() { return friendsManager; }
    public PartyManager getPartyManager() { return partyManager; }
    public SettingsManager getSettingsManager() { return settingsManager; }
    public LanguageManager getLanguageManager() { return languageManager; }
    public ChatFilterManager getChatFilterManager() { return chatFilterManager; }
    public BanManager getBanManager() { return banManager; }
    public ReportManager getReportManager() { return reportManager; }
    public DiscordBotManager getDiscordBotManager() { return discordBotManager; }
    public DuelBotManager getDuelBotManager() { return duelBotManager; }
    public Set<UUID> getBuildModePlayers() { return Collections.unmodifiableSet(buildModePlayers); }
    public boolean isBuildMode(UUID uuid) { return buildModePlayers.contains(uuid); }
    public void setBuildMode(UUID uuid, boolean enabled) {
        if (enabled) buildModePlayers.add(uuid);
        else buildModePlayers.remove(uuid);
    }

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
