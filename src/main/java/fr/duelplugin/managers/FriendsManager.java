package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FriendsManager {

    private final DuelPlugin plugin;
    private final File friendsFile;
    private final FileConfiguration friendsConfig;
    private final Map<UUID, Set<UUID>> friends = new HashMap<>();

    public FriendsManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.friendsFile = new File(plugin.getDataFolder(), "friends.yml");
        if (!friendsFile.exists()) {
            try { friendsFile.createNewFile(); } catch (IOException ignored) {}
        }
        this.friendsConfig = YamlConfiguration.loadConfiguration(friendsFile);
        loadAll();
    }

    private void loadAll() {
        friends.clear();
        if (!friendsConfig.contains("players")) return;
        for (String uuidStr : friendsConfig.getConfigurationSection("players").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            List<String> friendStrings = friendsConfig.getStringList("players." + uuidStr);
            Set<UUID> friendSet = new HashSet<>();
            for (String fs : friendStrings) {
                try {
                    friendSet.add(UUID.fromString(fs));
                } catch (IllegalArgumentException ignored) {}
            }
            friends.put(uuid, friendSet);
        }
    }

    public boolean addFriend(UUID player, UUID friend) {
        if (player.equals(friend)) return false;
        Set<UUID> list = friends.computeIfAbsent(player, k -> new HashSet<>());
        if (list.contains(friend)) return false;
        list.add(friend);
        save();
        return true;
    }

    public boolean removeFriend(UUID player, UUID friend) {
        Set<UUID> list = friends.get(player);
        if (list == null || !list.contains(friend)) return false;
        list.remove(friend);
        save();
        return true;
    }

    public boolean isFriend(UUID player, UUID other) {
        Set<UUID> list = friends.get(player);
        return list != null && list.contains(other);
    }

    public Set<UUID> getFriends(UUID player) {
        return friends.getOrDefault(player, new HashSet<>());
    }

    private void save() {
        for (Map.Entry<UUID, Set<UUID>> entry : friends.entrySet()) {
            List<String> friendStrings = new ArrayList<>();
            for (UUID uuid : entry.getValue()) {
                friendStrings.add(uuid.toString());
            }
            friendsConfig.set("players." + entry.getKey().toString(), friendStrings);
        }
        try {
            friendsConfig.save(friendsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save friends.yml");
        }
    }
}
