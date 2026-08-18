package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.DuelPlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private final DuelPlugin plugin;
    private final Map<UUID, DuelPlayer> players;
    private final File dataFile;
    private FileConfiguration dataConfig;

    public PlayerManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.players = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
        loadData();
    }

    public void loadData() {
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                DuelPlayer dp = new DuelPlayer(uuid);

                int totalKills = dataConfig.getInt(key + ".kills", 0);
                int totalDeaths = dataConfig.getInt(key + ".deaths", 0);
                int winStreak = dataConfig.getInt(key + ".winstreak", 0);
                int bestWinStreak = dataConfig.getInt(key + ".bestwinstreak", 0);

                // Load per-mode stats
                var modesSection = dataConfig.getConfigurationSection(key + ".modes");
                if (modesSection != null) {
                    for (String mode : modesSection.getKeys(false)) {
                        int modeKills = modesSection.getInt(mode + ".kills", 0);
                        int modeDeaths = modesSection.getInt(mode + ".deaths", 0);
                        for (int i = 0; i < modeKills; i++) dp.addKill(mode);
                        for (int i = 0; i < modeDeaths; i++) dp.addDeath(mode);
                    }
                }

                // Restore win streaks by simulating wins
                for (int i = 0; i < bestWinStreak; i++) dp.addWin();
                // Reset current streak to saved value
                for (int i = bestWinStreak; i > winStreak && dp.getWinStreak() > 0; i--) dp.resetWinStreak();

                players.put(uuid, dp);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load player data for: " + key);
            }
        }
    }

    public void saveData() {
        for (Map.Entry<UUID, DuelPlayer> entry : players.entrySet()) {
            String uuid = entry.getKey().toString();
            DuelPlayer dp = entry.getValue();
            dataConfig.set(uuid + ".kills", dp.getTotalKills());
            dataConfig.set(uuid + ".deaths", dp.getTotalDeaths());
            dataConfig.set(uuid + ".winstreak", dp.getWinStreak());
            dataConfig.set(uuid + ".bestwinstreak", dp.getBestWinStreak());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DuelPlayer getDuelPlayer(UUID uuid) {
        return players.computeIfAbsent(uuid, DuelPlayer::new);
    }

    public void saveAndUnload() {
        saveData();
        players.clear();
    }
}
