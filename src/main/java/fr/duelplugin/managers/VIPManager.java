package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VIPManager {

    private final DuelPlugin plugin;
    private final File vipFile;
    private final FileConfiguration vipConfig;
    private final Map<UUID, String> nameColors = new HashMap<>();
    private final Map<UUID, String> badges = new HashMap<>();
    private final Set<UUID> vips = new HashSet<>();

    private static final String DEFAULT_BADGE = "★";
    private static final List<String> AVAILABLE_BADGES = List.of("★", "❤", "✦", "⚡", "🏆");
    private static final List<String> BADGE_NAMES = List.of("Etoile", "Coeur", "Diamant", "Eclair", "Trophee");

    public VIPManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.vipFile = new File(plugin.getDataFolder(), "vip.yml");
        if (!vipFile.exists()) {
            try { vipFile.createNewFile(); } catch (IOException ignored) {}
        }
        this.vipConfig = YamlConfiguration.loadConfiguration(vipFile);
        loadAll();
    }

    private void loadAll() {
        if (!vipConfig.contains("players")) return;
        for (String uuidStr : vipConfig.getConfigurationSection("players").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            vips.add(uuid);
            String color = vipConfig.getString("players." + uuidStr + ".name-color");
            if (color != null) {
                nameColors.put(uuid, color);
            }
            String badge = vipConfig.getString("players." + uuidStr + ".badge");
            if (badge != null && AVAILABLE_BADGES.contains(badge)) {
                badges.put(uuid, badge);
            }
        }
    }

    public boolean isVip(UUID uuid) {
        return vips.contains(uuid);
    }

    public void setVip(UUID uuid, boolean vip) {
        if (vip) {
            vips.add(uuid);
        } else {
            vips.remove(uuid);
            nameColors.remove(uuid);
            badges.remove(uuid);
            vipConfig.set("players." + uuid.toString(), null);
            save();
        }
    }

    public String getNameColor(UUID uuid) {
        return nameColors.getOrDefault(uuid, null);
    }

    public void setNameColor(UUID uuid, String color) {
        nameColors.put(uuid, color);
        vipConfig.set("players." + uuid.toString() + ".name-color", color);
        save();
    }

    public String getColoredName(UUID uuid, String fallbackName) {
        if (!isVip(uuid)) return fallbackName;
        String color = getNameColor(uuid);
        if (color == null) return "§d" + fallbackName;
        return color + fallbackName;
    }

    public List<String> getAvailableColors() {
        return Arrays.asList(
                "§c", "§6", "§e", "§a", "§b", "§d", "§5", "§f", "§7", "§0"
        );
    }

    public List<String> getColorNames() {
        return Arrays.asList(
                "Rouge", "Or", "Jaune", "Vert", "Aqua", "Rose", "Violet", "Blanc", "Gris", "Noir"
        );
    }

    public String getBadge(UUID uuid) {
        return badges.getOrDefault(uuid, DEFAULT_BADGE);
    }

    public void setBadge(UUID uuid, String badge) {
        if (badge == null || badge.equals(DEFAULT_BADGE)) {
            badges.remove(uuid);
            vipConfig.set("players." + uuid.toString() + ".badge", null);
        } else {
            badges.put(uuid, badge);
            vipConfig.set("players." + uuid.toString() + ".badge", badge);
        }
        save();
    }

    public String getDefaultBadge() {
        return DEFAULT_BADGE;
    }

    public List<String> getAvailableBadges() {
        return AVAILABLE_BADGES;
    }

    public List<String> getBadgeNames() {
        return BADGE_NAMES;
    }

    private void save() {
        try {
            vipConfig.save(vipFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save vip.yml");
        }
    }
}
