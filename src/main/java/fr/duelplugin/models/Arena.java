package fr.duelplugin.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Arena {

    private final String name;
    private DuelGameMode gameMode;
    private Location spawn1;
    private Location spawn2;
    private Location minCorner;
    private Location maxCorner;
    private final List<BlockSnapshot> originalBlocks;
    private boolean snapshotActive;

    public Arena(String name, DuelGameMode gameMode) {
        this.name = name;
        this.gameMode = gameMode;
        this.originalBlocks = new ArrayList<>();
        this.snapshotActive = false;
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

    public boolean canInteractBlocks() {
        return gameMode == DuelGameMode.UHC || gameMode == DuelGameMode.DIASMP;
    }

    public void takeSnapshot() {
        originalBlocks.clear();
        if (minCorner == null || maxCorner == null) return;
        World world = minCorner.getWorld();
        if (world == null) return;

        int minX = (int) Math.min(minCorner.getX(), maxCorner.getX());
        int minY = (int) Math.min(minCorner.getY(), maxCorner.getY());
        int minZ = (int) Math.min(minCorner.getZ(), maxCorner.getZ());
        int maxX = (int) Math.max(minCorner.getX(), maxCorner.getX());
        int maxY = (int) Math.max(minCorner.getY(), maxCorner.getY());
        int maxZ = (int) Math.max(minCorner.getZ(), maxCorner.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() != Material.AIR && block.getType() != Material.CAVE_AIR) {
                        originalBlocks.add(new BlockSnapshot(
                                world.getName(), x, y, z,
                                block.getType(), block.getBlockData().getAsString()
                        ));
                    }
                }
            }
        }
        snapshotActive = true;
    }

    public void restoreFromSnapshot() {
        if (!snapshotActive) return;
        World world = Bukkit.getWorlds().get(0);
        if (minCorner != null && minCorner.getWorld() != null) {
            world = minCorner.getWorld();
        }

        for (BlockSnapshot snap : originalBlocks) {
            World w = Bukkit.getWorld(snap.worldName);
            if (w == null) w = world;
            Block block = w.getBlockAt(snap.x, snap.y, snap.z);
            try {
                block.setType(snap.material);
                block.setBlockData(Bukkit.createBlockData(snap.blockData));
            } catch (Exception e) {
                block.setType(snap.material);
            }
        }

        int minX = (int) Math.min(minCorner.getX(), maxCorner.getX());
        int minY = (int) Math.min(minCorner.getY(), maxCorner.getY());
        int minZ = (int) Math.min(minCorner.getZ(), maxCorner.getZ());
        int maxX = (int) Math.max(minCorner.getX(), maxCorner.getX());
        int maxY = (int) Math.max(minCorner.getY(), maxCorner.getY());
        int maxZ = (int) Math.max(minCorner.getZ(), maxCorner.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean found = false;
                    for (BlockSnapshot snap : originalBlocks) {
                        if (snap.x == x && snap.y == y && snap.z == z) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        Block block = world.getBlockAt(x, y, z);
                        block.setType(Material.AIR);
                    }
                }
            }
        }

        snapshotActive = false;
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

    public static class BlockSnapshot {
        public final String worldName;
        public final int x, y, z;
        public final Material material;
        public final String blockData;

        public BlockSnapshot(String worldName, int x, int y, int z, Material material, String blockData) {
            this.worldName = worldName;
            this.x = x;
            this.y = y;
            this.z = z;
            this.material = material;
            this.blockData = blockData;
        }
    }
}
