package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SettingsManager {

    private final DuelPlugin plugin;
    private final File settingsFile;
    private final FileConfiguration settingsConfig;
    private final Map<UUID, Boolean> acceptFriendRequests = new HashMap<>();
    private final Map<UUID, Boolean> acceptDuelRequests = new HashMap<>();
    private final Map<UUID, Boolean> discordNotifications = new HashMap<>();
    private final Map<UUID, Integer> roundCounts = new HashMap<>();
    private final Map<UUID, Language> languages = new HashMap<>();

    public SettingsManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.settingsFile = new File(plugin.getDataFolder(), "settings.yml");
        if (!settingsFile.exists()) {
            try { settingsFile.createNewFile(); } catch (IOException ignored) {}
        }
        this.settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);
        loadAll();
    }

    private void loadAll() {
        acceptFriendRequests.clear();
        acceptDuelRequests.clear();
        discordNotifications.clear();
        roundCounts.clear();
        languages.clear();
        if (!settingsConfig.contains("players")) return;
        for (String uuidStr : settingsConfig.getConfigurationSection("players").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            String path = "players." + uuidStr;
            if (settingsConfig.contains(path + ".accept-friends")) {
                acceptFriendRequests.put(uuid, settingsConfig.getBoolean(path + ".accept-friends"));
            }
            if (settingsConfig.contains(path + ".accept-duels")) {
                acceptDuelRequests.put(uuid, settingsConfig.getBoolean(path + ".accept-duels"));
            }
            if (settingsConfig.contains(path + ".discord-notifications")) {
                discordNotifications.put(uuid, settingsConfig.getBoolean(path + ".discord-notifications"));
            }
            if (settingsConfig.contains(path + ".round-count")) {
                roundCounts.put(uuid, settingsConfig.getInt(path + ".round-count"));
            }
            if (settingsConfig.contains(path + ".language")) {
                languages.put(uuid, Language.fromString(settingsConfig.getString(path + ".language")));
            }
        }
    }

    public boolean acceptsFriendRequests(UUID uuid) {
        return acceptFriendRequests.getOrDefault(uuid, true);
    }

    public boolean acceptsDuelRequests(UUID uuid) {
        return acceptDuelRequests.getOrDefault(uuid, true);
    }

    public Language getLanguage(UUID uuid) {
        Language def = Language.fromString(plugin.getConfig().getString("messages.default-language", "EN"));
        return languages.getOrDefault(uuid, def);
    }

    public boolean discordNotificationsEnabled(UUID uuid) {
        return discordNotifications.getOrDefault(uuid, true);
    }

    public int getRoundCount(UUID uuid) {
        return roundCounts.getOrDefault(uuid, 2);
    }

    public void setAcceptFriendRequests(UUID uuid, boolean accept) {
        acceptFriendRequests.put(uuid, accept);
        settingsConfig.set("players." + uuid.toString() + ".accept-friends", accept);
        save();
    }

    public void setAcceptDuelRequests(UUID uuid, boolean accept) {
        acceptDuelRequests.put(uuid, accept);
        settingsConfig.set("players." + uuid.toString() + ".accept-duels", accept);
        save();
    }

    public void setLanguage(UUID uuid, Language lang) {
        languages.put(uuid, lang);
        settingsConfig.set("players." + uuid.toString() + ".language", lang.name());
        save();
    }

    public void setDiscordNotifications(UUID uuid, boolean enabled) {
        discordNotifications.put(uuid, enabled);
        settingsConfig.set("players." + uuid.toString() + ".discord-notifications", enabled);
        save();
    }

    public void setRoundCount(UUID uuid, int rounds) {
        roundCounts.put(uuid, rounds);
        settingsConfig.set("players." + uuid.toString() + ".round-count", rounds);
        save();
    }

    private void save() {
        try {
            settingsConfig.save(settingsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save settings.yml");
        }
    }
}
