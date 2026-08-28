package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.Rank;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RankManager {

    private final DuelPlugin plugin;
    private final File rankFile;
    private final FileConfiguration rankConfig;
    private final Map<UUID, Rank> ranks = new HashMap<>();

    public RankManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.rankFile = new File(plugin.getDataFolder(), "ranks.yml");
        if (!rankFile.exists()) {
            try { rankFile.createNewFile(); } catch (IOException ignored) {}
        }
        this.rankConfig = YamlConfiguration.loadConfiguration(rankFile);
        loadAll();
        migrateOldVips();
    }

    private void loadAll() {
        if (!rankConfig.contains("players")) return;
        for (String uuidStr : rankConfig.getConfigurationSection("players").getKeys(false)) {
            Rank rank = Rank.fromId(rankConfig.getString("players." + uuidStr));
            if (rank == null) continue;
            try {
                ranks.put(UUID.fromString(uuidStr), rank);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private void migrateOldVips() {
        File vipFile = new File(plugin.getDataFolder(), "vip.yml");
        if (!vipFile.exists()) return;
        FileConfiguration vipConfig = YamlConfiguration.loadConfiguration(vipFile);
        if (!vipConfig.contains("players")) return;
        boolean changed = false;
        for (String uuidStr : vipConfig.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                if (!ranks.containsKey(uuid)) {
                    ranks.put(uuid, Rank.VIP);
                    rankConfig.set("players." + uuidStr, Rank.VIP.getId());
                    changed = true;
                }
            } catch (IllegalArgumentException ignored) {}
        }
        if (changed) save();
    }

    public Rank getRank(UUID uuid) {
        return ranks.get(uuid);
    }

    public boolean hasRank(UUID uuid) {
        return ranks.containsKey(uuid);
    }

    public boolean isVip(UUID uuid) {
        return ranks.get(uuid) == Rank.VIP;
    }

    public void setRank(UUID uuid, Rank rank) {
        if (rank == null) {
            ranks.remove(uuid);
            rankConfig.set("players." + uuid.toString(), null);
        } else {
            ranks.put(uuid, rank);
            rankConfig.set("players." + uuid.toString(), rank.getId());
        }
        save();
    }

    private void save() {
        try {
            rankConfig.save(rankFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save ranks.yml");
        }
    }
}