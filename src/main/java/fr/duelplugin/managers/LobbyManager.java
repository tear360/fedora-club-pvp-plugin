package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

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

        World w = Bukkit.getWorld(world);
        if (w == null) {
            w = Bukkit.getWorlds().get(0);
        }
        lobbySpawn = new Location(w, x, y, z);
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
        if (lobbySpawn == null) return null;
        Location loc = lobbySpawn.clone();
        if (loc.getWorld() == null) {
            World w = Bukkit.getWorld(plugin.getConfig().getString("lobby.world", "world"));
            if (w == null) w = Bukkit.getWorlds().get(0);
            loc.setWorld(w);
        }
        return loc;
    }

    public boolean isLobbySet() {
        return lobbySpawn != null;
    }

    public void teleportToLobby(Player player) {
        if (lobbySpawn == null) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            World targetWorld = Bukkit.getWorld(lobbySpawn.getWorld().getName());
            if (targetWorld == null) {
                targetWorld = Bukkit.getWorlds().get(0);
            }

            Location tpLoc = lobbySpawn.clone();
            tpLoc.setWorld(targetWorld);

            player.teleportAsync(tpLoc).thenAccept(success -> {
                if (!success) {
                    player.teleport(tpLoc);
                }
            });
        }, 1L);
    }
}
