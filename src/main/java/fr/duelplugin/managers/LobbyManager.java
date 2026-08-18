package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class LobbyManager {

    private final DuelPlugin plugin;
    private Location lobbySpawn;

    public LobbyManager(DuelPlugin plugin) {
        this.plugin = plugin;
        loadLobby();
    }

    public void loadLobby() {
        FileConfiguration config = plugin.getConfig();
        String world = config.getString("lobby.world", "world");
        double x = config.getDouble("lobby.spawn-x", 0);
        double y = config.getDouble("lobby.spawn-y", 64);
        double z = config.getDouble("lobby.spawn-z", 0);
        lobbySpawn = new Location(
                org.bukkit.Bukkit.getWorld(world) != null ? org.bukkit.Bukkit.getWorld(world) : org.bukkit.Bukkit.getWorlds().get(0),
                x, y, z
        );
    }

    public void setLobby(Location loc) {
        this.lobbySpawn = loc.clone();
        FileConfiguration config = plugin.getConfig();
        config.set("lobby.world", loc.getWorld().getName());
        config.set("lobby.spawn-x", loc.getX());
        config.set("lobby.spawn-y", loc.getY());
        config.set("lobby.spawn-z", loc.getZ());
        plugin.saveConfig();
    }

    public Location getLobbySpawn() {
        return lobbySpawn.clone();
    }

    public boolean isLobbySet() {
        return lobbySpawn != null;
    }

    public void teleportToLobby(org.bukkit.entity.Player player) {
        if (lobbySpawn != null) {
            player.teleport(lobbySpawn);
        }
    }
}
