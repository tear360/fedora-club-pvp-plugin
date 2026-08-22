package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ChatFilterManager {

    private final DuelPlugin plugin;
    private List<String> bannedWords = new ArrayList<>();
    private List<Pattern> patterns = new ArrayList<>();
    private File filterFile;
    private FileConfiguration filterConfig;

    public ChatFilterManager(DuelPlugin plugin) {
        this.plugin = plugin;
        loadFilter();
    }

    public void loadFilter() {
        filterFile = new File(plugin.getDataFolder(), "filter.yml");
        if (!filterFile.exists()) {
            plugin.saveResource("filter.yml", false);
        }
        filterConfig = YamlConfiguration.loadConfiguration(filterFile);
        bannedWords = filterConfig.getStringList("banned-words");
        buildPatterns();
        plugin.getLogger().info("Loaded " + bannedWords.size() + " banned words from filter.yml");
    }

    private void buildPatterns() {
        patterns.clear();
        for (String word : bannedWords) {
            String escaped = Pattern.quote(word);
            String regex = "(?i)" + escaped.replaceAll(" ", "[\\s._\\-*,!?]*");
            patterns.add(Pattern.compile(regex));
        }
    }

    public String censor(String message) {
        String result = message;
        for (Pattern pattern : patterns) {
            result = pattern.matcher(result).replaceAll(m -> {
                String matched = m.group();
                StringBuilder stars = new StringBuilder();
                for (int i = 0; i < matched.length(); i++) {
                    char c = matched.charAt(i);
                    if (Character.isLetter(c)) {
                        stars.append('*');
                    } else {
                        stars.append(c);
                    }
                }
                return stars.toString();
            });
        }
        return result;
    }

    public List<String> getBannedWords() {
        return bannedWords;
    }
}
