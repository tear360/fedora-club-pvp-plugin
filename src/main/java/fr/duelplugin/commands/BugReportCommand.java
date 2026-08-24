package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BugReportCommand implements CommandExecutor, TabCompleter {

    private final DuelPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final long COOLDOWN_MS = 60 * 60 * 1000;

    public BugReportCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.getLanguageManager().msg(player, "bugreport_usage"));
            return true;
        }

        long now = System.currentTimeMillis();
        Long lastUse = cooldowns.get(player.getUniqueId());
        if (lastUse != null && (now - lastUse) < COOLDOWN_MS) {
            long remaining = (COOLDOWN_MS - (now - lastUse)) / 1000;
            long minutes = remaining / 60;
            long seconds = remaining % 60;
            player.sendMessage(plugin.getLanguageManager().msg(player, "bugreport_cooldown", "%time%", minutes + "m " + seconds + "s"));
            return true;
        }

        String bug = String.join(" ", args);
        cooldowns.put(player.getUniqueId(), now);

        sendBugReport(player.getName(), player.getUniqueId().toString(), bug);
        player.sendMessage(plugin.getLanguageManager().msg(player, "bugreport_sent"));
        return true;
    }

    private void sendBugReport(String playerName, String playerUuid, String bug) {
        String webhookUrl = plugin.getConfig().getString("discord-bugreport-webhook-url", "");
        if (webhookUrl == null || webhookUrl.isBlank()) return;

        String timestamp = Instant.now().atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);

        String json = """
                {
                  "username": "Fedora Club",
                  "embeds": [{
                    "title": "🐛 Bug Report",
                    "color": 16711680,
                    "fields": [
                      {"name": "👤 Joueur", "value": "%s", "inline": true},
                      {"name": "📋 Bug", "value": "%s", "inline": false}
                    ],
                    "footer": {"text": "Fedora Club - Bug Report"},
                    "timestamp": "%s"
                  }]
                }
                """.formatted(
                escapeJson(playerName),
                escapeJson(bug),
                timestamp
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofSeconds(10))
                .build();

        CompletableFuture.runAsync(() -> {
            try {
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send bug report webhook: " + e.getMessage());
            }
        });
    }

    private String escapeJson(String text) {
        if (text == null) return "Unknown";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
