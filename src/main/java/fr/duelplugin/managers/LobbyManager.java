package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class LobbyManager {

    private final DuelPlugin plugin;
    private Location lobbySpawn;
    private boolean explicitlySet = false;
    private final File lobbyFile;
    private FileConfiguration lobbyConfig;

    public LobbyManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.lobbyFile = new File(plugin.getDataFolder(), "lobby.yml");
        loadLobby();
    }

    public void loadLobby() {
        if (!lobbyFile.exists()) {
            try { lobbyFile.createNewFile(); } catch (IOException ignored) {}
        }
        lobbyConfig = YamlConfiguration.loadConfiguration(lobbyFile);

        String world = lobbyConfig.getString("lobby.world", "world");
        double x = lobbyConfig.getDouble("lobby.spawn-x", 0);
        double y = lobbyConfig.getDouble("lobby.spawn-y", 64);
        double z = lobbyConfig.getDouble("lobby.spawn-z", 0);

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
        lobbyConfig.set("lobby.world", loc.getWorld().getName());
        lobbyConfig.set("lobby.spawn-x", loc.getX());
        lobbyConfig.set("lobby.spawn-y", loc.getY());
        lobbyConfig.set("lobby.spawn-z", loc.getZ());
        try {
            lobbyConfig.save(lobbyFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save lobby.yml");
        }

        if (loc.getWorld() != null) {
            loc.getWorld().setSpawnLocation(loc);
        }
    }

    public Location getLobbySpawn() {
        if (lobbySpawn == null) return null;

        Location loc = lobbySpawn.clone();
        String worldName = lobbySpawn.getWorld() != null ? lobbySpawn.getWorld().getName() : lobbyConfig.getString("lobby.world", "world");
        World w = Bukkit.getWorld(worldName);
        if (w != null) {
            loc.setWorld(w);
        }
        return loc;
    }

    public Location resolveLobby() {
        if (lobbySpawn == null) return null;
        Location loc = lobbySpawn.clone();
        String worldName = lobbyConfig.getString("lobby.world", "world");
        if (worldName != null) {
            World w = Bukkit.getWorld(worldName);
            if (w != null) {
                loc.setWorld(w);
            }
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

            Location target = resolveLobby();
            if (target == null || target.getWorld() == null) return;

            player.teleportAsync(target, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
        }, 2L);
    }
}
