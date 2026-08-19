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
    private boolean explicitlySet = false;

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

        if (x == 0 && y == 64 && z == 0 && "world".equals(world)) {
            lobbySpawn = null;
            explicitlySet = false;
            return;
        }

        World w = Bukkit.getWorld(world);
        if (w == null) {
            w = Bukkit.getWorlds().get(0);
        }
        lobbySpawn = new Location(w, x, y, z);
        explicitlySet = true;
    }

    public void setLobby(Location loc) {
        this.lobbySpawn = loc.clone();
        this.explicitlySet = true;
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
        String worldName = lobbySpawn.getWorld() != null ? lobbySpawn.getWorld().getName() : plugin.getConfig().getString("lobby.world", "world");
        World w = Bukkit.getWorld(worldName);
        if (w != null) {
            loc.setWorld(w);
        }
        return loc;
    }

    public boolean isLobbySet() {
        return explicitlySet && lobbySpawn != null;
    }

    public void teleportToLobby(Player player) {
        if (!isLobbySet()) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            Location target = getLobbySpawn();
            if (target == null || target.getWorld() == null) return;

            player.teleportAsync(target, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
        }, 2L);
    }
}
