package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.Arena;
import fr.duelplugin.models.DuelGameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ArenaManager {

    private final DuelPlugin plugin;
    private final Map<String, Arena> arenas;

    public ArenaManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
        loadArenas();
    }

    public void loadArenas() {
        arenas.clear();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("arenas");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            ConfigurationSection arenaSection = section.getConfigurationSection(key);
            if (arenaSection != null) {
                Arena arena = Arena.loadFromConfig(key, arenaSection);
                arenas.put(key.toLowerCase(), arena);
            }
        }
        plugin.getLogger().info("Loaded " + arenas.size() + " arenas.");
    }

    public void saveArenas() {
        FileConfiguration config = plugin.getConfig();
        config.set("arenas", null);
        for (Map.Entry<String, Arena> entry : arenas.entrySet()) {
            Arena arena = entry.getValue();
            arena.saveToConfig(config.createSection("arenas." + entry.getKey()));
        }
        plugin.saveConfig();
    }

    public boolean createArena(String name, DuelGameMode mode) {
        String key = name.toLowerCase();
        if (arenas.containsKey(key)) return false;
        Arena arena = new Arena(name, mode);
        arenas.put(key, arena);
        saveArenas();
        return true;
    }

    public boolean deleteArena(String name) {
        String key = name.toLowerCase();
        Arena removed = arenas.remove(key);
        if (removed != null) {
            removed.restoreFromSnapshot();
            saveArenas();
            return true;
        }
        return false;
    }

    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    public Collection<Arena> getAllArenas() {
        return arenas.values();
    }

    public Collection<Arena> getArenasByMode(DuelGameMode mode) {
        return arenas.values().stream()
                .filter(a -> a.getGameMode() == mode)
                .collect(Collectors.toList());
    }

    public Arena getAvailableArena(DuelGameMode mode) {
        return arenas.values().stream()
                .filter(a -> a.getGameMode() == mode && a.isSetup())
                .findFirst()
                .orElse(null);
    }
}
