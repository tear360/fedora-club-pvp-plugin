package fr.duelplugin.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class Arena {

    private final String name;
    private DuelGameMode gameMode;
    private Location spawn1;
    private Location spawn2;
    private Location minCorner;
    private Location maxCorner;
    private final Map<Location, Integer> originalBlocks;

    public Arena(String name, DuelGameMode gameMode) {
        this.name = name;
        this.gameMode = gameMode;
        this.originalBlocks = new HashMap<>();
    }

    public String getName() { return name; }

    public DuelGameMode getGameMode() { return gameMode; }
    public void setGameMode(DuelGameMode gameMode) { this.gameMode = gameMode; }

    public Location getSpawn1() { return spawn1; }
    public void setSpawn1(Location spawn1) { this.spawn1 = spawn1; }

    public Location getSpawn2() { return spawn2; }
    public void setSpawn2(Location spawn2) { this.spawn2 = spawn2; }

    public Location getMinCorner() { return minCorner; }
    public void setMinCorner(Location minCorner) { this.minCorner = minCorner; }

    public Location getMaxCorner() { return maxCorner; }
    public void setMaxCorner(Location maxCorner) { this.maxCorner = maxCorner; }

    public Map<Location, Integer> getOriginalBlocks() { return originalBlocks; }

    public boolean isSetup() {
        return spawn1 != null && spawn2 != null;
    }

    public boolean isInArena(Location loc) {
        if (minCorner == null || maxCorner == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().equals(minCorner.getWorld())) return false;
        double x = loc.getX(), y = loc.getY(), z = loc.getZ();
        double minX = Math.min(minCorner.getX(), maxCorner.getX());
        double minY = Math.min(minCorner.getY(), maxCorner.getY());
        double minZ = Math.min(minCorner.getZ(), maxCorner.getZ());
        double maxX = Math.max(minCorner.getX(), maxCorner.getX());
        double maxY = Math.max(minCorner.getY(), maxCorner.getY());
        double maxZ = Math.max(minCorner.getZ(), maxCorner.getZ());
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public void saveOriginalBlock(Location loc) {
        if (!originalBlocks.containsKey(loc)) {
            originalBlocks.put(loc.clone(), loc.getBlock().getTypeId());
        }
    }

    public void restoreBlocks() {
        for (Map.Entry<Location, Integer> entry : originalBlocks.entrySet()) {
            Location loc = entry.getKey();
            if (loc.getWorld() != null) {
                loc.getBlock().setType(org.bukkit.Material.AIR);
            }
        }
        originalBlocks.clear();
    }

    public void saveToConfig(ConfigurationSection section) {
        section.set("gamemode", gameMode.getConfigName());
        if (spawn1 != null) saveLocation(section.createSection("spawn1"), spawn1);
        if (spawn2 != null) saveLocation(section.createSection("spawn2"), spawn2);
        if (minCorner != null) saveLocation(section.createSection("min"), minCorner);
        if (maxCorner != null) saveLocation(section.createSection("max"), maxCorner);
    }

    public static Arena loadFromConfig(String name, ConfigurationSection section) {
        DuelGameMode mode = DuelGameMode.fromConfig(section.getString("gamemode", "sword"));
        if (mode == null) mode = DuelGameMode.SWORD;
        Arena arena = new Arena(name, mode);
        if (section.contains("spawn1")) arena.setSpawn1(loadLocation(section.getConfigurationSection("spawn1")));
        if (section.contains("spawn2")) arena.setSpawn2(loadLocation(section.getConfigurationSection("spawn2")));
        if (section.contains("min")) arena.setMinCorner(loadLocation(section.getConfigurationSection("min")));
        if (section.contains("max")) arena.setMaxCorner(loadLocation(section.getConfigurationSection("max")));
        return arena;
    }

    private void saveLocation(ConfigurationSection section, Location loc) {
        section.set("world", loc.getWorld().getName());
        section.set("x", loc.getX());
        section.set("y", loc.getY());
        section.set("z", loc.getZ());
        section.set("yaw", (double) loc.getYaw());
        section.set("pitch", (double) loc.getPitch());
    }

    private static Location loadLocation(ConfigurationSection section) {
        World world = Bukkit.getWorld(section.getString("world", "world"));
        if (world == null) world = Bukkit.getWorlds().get(0);
        return new Location(world, section.getDouble("x"), section.getDouble("y"), section.getDouble("z"),
                (float) section.getDouble("yaw"), (float) section.getDouble("pitch"));
    }
}
