package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.DuelGameMode;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class KitManager {

    private final DuelPlugin plugin;
    private final File kitsFolder;

    public KitManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.kitsFolder = new File(plugin.getDataFolder(), "kits");
        kitsFolder.mkdirs();
    }

    private File getKitFile(java.util.UUID playerUuid, DuelGameMode mode) {
        return new File(kitsFolder, playerUuid.toString() + "_" + mode.getConfigName() + ".yml");
    }

    public void saveKit(java.util.UUID playerUuid, DuelGameMode mode, ItemStack[] contents, ItemStack[] armor, ItemStack offHand) {
        File file = getKitFile(playerUuid, mode);
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                config.set("contents." + i, contents[i]);
            }
        }
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] != null) {
                config.set("armor." + i, armor[i]);
            }
        }
        if (offHand != null) {
            config.set("offhand", offHand);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save kit for " + playerUuid + " mode " + mode.getConfigName());
        }
    }

    public void saveKitTrims(java.util.UUID playerUuid, DuelGameMode mode, Map<Integer, ArmorTrim> trims) {
        File file = getKitFile(playerUuid, mode);
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("trims", null);
        if (trims != null) {
            for (Map.Entry<Integer, ArmorTrim> entry : trims.entrySet()) {
                config.set("trims." + entry.getKey() + ".pattern", entry.getValue().getPattern().getKey().asString());
                config.set("trims." + entry.getKey() + ".material", entry.getValue().getMaterial().getKey().asString());
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save kit trims for " + playerUuid + " mode " + mode.getConfigName());
        }
    }

    public Map<String, ItemStack[]> loadKit(java.util.UUID playerUuid, DuelGameMode mode) {
        File file = getKitFile(playerUuid, mode);
        if (!file.exists()) return null;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        Map<String, ItemStack[]> result = new HashMap<>();

        ItemStack[] contents = new ItemStack[36];
        if (config.contains("contents")) {
            for (String key : config.getConfigurationSection("contents").getKeys(false)) {
                int slot = Integer.parseInt(key);
                contents[slot] = config.getItemStack("contents." + slot);
            }
        }
        result.put("contents", contents);

        ItemStack[] armor = new ItemStack[4];
        if (config.contains("armor")) {
            for (String key : config.getConfigurationSection("armor").getKeys(false)) {
                int slot = Integer.parseInt(key);
                armor[slot] = config.getItemStack("armor." + slot);
            }
        }
        result.put("armor", armor);

        if (config.contains("offhand")) {
            result.put("offhand", new ItemStack[]{config.getItemStack("offhand")});
        }

        return result;
    }

    public Map<Integer, ArmorTrim> loadKitTrims(java.util.UUID playerUuid, DuelGameMode mode) {
        File file = getKitFile(playerUuid, mode);
        if (!file.exists()) return null;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.contains("trims")) return null;

        var patternRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN);
        var materialRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_MATERIAL);

        Map<Integer, ArmorTrim> trims = new HashMap<>();
        for (String key : config.getConfigurationSection("trims").getKeys(false)) {
            int slot = Integer.parseInt(key);
            String patternKey = config.getString("trims." + key + ".pattern");
            String materialKey = config.getString("trims." + key + ".material");

            if (patternKey == null || materialKey == null) continue;

            NamespacedKey pKey = NamespacedKey.fromString(patternKey);
            NamespacedKey mKey = NamespacedKey.fromString(materialKey);
            if (pKey == null || mKey == null) continue;

            TrimPattern pattern = patternRegistry.get(pKey);
            TrimMaterial material = materialRegistry.get(mKey);

            if (pattern != null && material != null) {
                trims.put(slot, new ArmorTrim(material, pattern));
            }
        }
        return trims.isEmpty() ? null : trims;
    }

    public boolean hasCustomKit(java.util.UUID playerUuid, DuelGameMode mode) {
        return getKitFile(playerUuid, mode).exists();
    }

    public void deleteKit(java.util.UUID playerUuid, DuelGameMode mode) {
        File file = getKitFile(playerUuid, mode);
        if (file.exists()) file.delete();
    }
}
