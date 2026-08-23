package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import org.bukkit.Bukkit;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public class DiscordWebhookManager {

    private final DuelPlugin plugin;
    private final HttpClient httpClient;

    public DiscordWebhookManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void sendDuelResult(String winnerName, String loserName, String modeName, long durationMs) {
        String webhookUrl = plugin.getConfig().getString("discord-webhook-url", "");
        if (webhookUrl == null || webhookUrl.isBlank()) return;

        String duration = formatDuration(durationMs);
        String timestamp = Instant.now().atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);

        String json = """
                {
                  "username": "Fedora Club",
                  "embeds": [{
                    "title": "⚔ Résultat de duel",
                    "color": 9807270,
                    "fields": [
                      {"name": "🏆 Gagnant", "value": "%s", "inline": true},
                      {"name": "💀 Perdant", "value": "%s", "inline": true},
                      {"name": "🎮 Mode", "value": "%s", "inline": true},
                      {"name": "⏱ Durée", "value": "%s", "inline": true}
                    ],
                    "footer": {"text": "Fedora Club"},
                    "timestamp": "%s"
                  }]
                }
                """.formatted(
                escapeJson(winnerName),
                escapeJson(loserName),
                escapeJson(modeName),
                escapeJson(duration),
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
                plugin.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
            }
        });
    }

    private String formatDuration(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    private String escapeJson(String text) {
        if (text == null) return "Unknown";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
