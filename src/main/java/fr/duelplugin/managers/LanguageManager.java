package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {

    private final DuelPlugin plugin;
    private final Map<Language, Map<String, String>> translations = new HashMap<>();
    private final Language defaultLanguage;

    public LanguageManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.defaultLanguage = Language.fromString(plugin.getConfig().getString("messages.default-language", "EN"));
        for (Language lang : Language.values()) {
            translations.put(lang, new HashMap<>());
        }
        loadTranslations();
    }

    public Language getDefaultLanguage() {
        return defaultLanguage;
    }

    public void reload() {
        loadTranslations();
    }

    private void loadTranslations() {
        for (Language lang : Language.values()) {
            Map<String, String> map = translations.get(lang);
            map.clear();

            // Base: bundled resource file (defaults)
            try (InputStream in = plugin.getResource("lang/" + lang.getFile() + ".yml")) {
                if (in != null) {
                    YamlConfiguration yml = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
                    for (String key : yml.getKeys(false)) {
                        map.put(key, yml.getString(key));
                    }
                }
            } catch (Exception ignored) {
            }

            // Overlay: editable file in the plugin data folder (user overrides)
            File file = getLangFile(lang);
            if (file.exists()) {
                try {
                    YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
                    for (String key : yml.getKeys(false)) {
                        map.put(key, yml.getString(key));
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private File getLangFile(Language lang) {
        File folder = new File(plugin.getDataFolder(), "lang");
        File file = new File(folder, lang.getFile() + ".yml");
        if (!file.exists()) {
            folder.mkdirs();
            try (InputStream in = plugin.getResource("lang/" + lang.getFile() + ".yml")) {
                if (in != null) {
                    Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception ignored) {
            }
        }
        return file;
    }

    private Language getLang(Player player) {
        if (player == null) return defaultLanguage;
        return plugin.getSettingsManager().getLanguage(player.getUniqueId());
    }

    private String resolve(Language lang, String key) {
        String pattern = translations.getOrDefault(lang, translations.get(defaultLanguage)).get(key);
        if (pattern == null && lang != defaultLanguage) {
            pattern = translations.get(defaultLanguage).get(key);
        }
        if (pattern == null) {
            pattern = translations.get(Language.FR).get(key);
        }
        return pattern;
    }

    private String apply(String pattern, String... args) {
        String result = pattern;
        for (int i = 0; i < args.length - 1; i += 2) {
            result = result.replace(args[i], args[i + 1]);
        }
        String serverName = plugin.getConfig().getString("server-info.name", "My Server");
        String serverIp = plugin.getConfig().getString("server-info.ip", "play.example.com");
        result = result.replace("%server_name%", serverName)
                       .replace("%server_ip%", serverIp);
        return result;
    }

    public String msg(Player player, String key, String... args) {
        String p = resolve(getLang(player), key);
        if (p == null) return plugin.getPrefix() + ChatColor.RED + "Missing: " + key;
        return plugin.getPrefix() + ChatColor.translateAlternateColorCodes('&', apply(p, args));
    }

    public String msgRaw(Player player, String key, String... args) {
        String p = resolve(getLang(player), key);
        if (p == null) return ChatColor.RED + "Missing: " + key;
        return ChatColor.translateAlternateColorCodes('&', apply(p, args));
    }

    public String msgNoPrefix(Player player, String key, String... args) {
        String p = resolve(getLang(player), key);
        if (p == null) return ChatColor.RED + "Missing: " + key;
        return ChatColor.translateAlternateColorCodes('&', apply(p, args));
    }
}
