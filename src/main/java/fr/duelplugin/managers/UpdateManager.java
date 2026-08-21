package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class UpdateManager {

    private final DuelPlugin plugin;
    private static final String GITHUB_REPO = "tear360/fedora-club-pvp-plugin";
    private static final String API_URL = "https://api.github.com/repos/" + GITHUB_REPO + "/releases/latest";
    private boolean updateAvailable = false;
    private String latestVersion = "";
    private String downloadUrl = "";

    public UpdateManager(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdates() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String currentVersion = plugin.getDescription().getVersion();
                    String jsonResponse = fetchJson(API_URL);

                    if (jsonResponse == null) {
                        plugin.getLogger().warning("§5[Update] §cImpossible de contacter GitHub.");
                        return;
                    }

                    latestVersion = extractTag(jsonResponse);
                    downloadUrl = extractDownloadUrl(jsonResponse);

                    if (latestVersion == null || latestVersion.isEmpty()) {
                        plugin.getLogger().warning("§5[Update] §cImpossible de lire la version latest.");
                        return;
                    }

                    if (isNewerVersion(latestVersion, currentVersion)) {
                        updateAvailable = true;
                        plugin.getLogger().info("§5[Update] §dNouvelle version disponible: §f" + latestVersion + " §d(Vous: §f" + currentVersion + "§d)");
                        plugin.getLogger().info("§5[Update] §dTéléchargement en cours...");

                        downloadUpdate(downloadUrl, currentVersion);

                        plugin.getLogger().info("§5[Update] §aMise à jour téléchargée! Redémarrez le serveur pour appliquer.");
                    } else {
                        plugin.getLogger().info("§5[Update] §aVous êtes à jour (§f" + currentVersion + "§a).");
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "§5[Update] §cErreur lors de la vérification:", e);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private String fetchJson(String urlString) throws IOException {
        String token = plugin.getConfig().getString("github-token", "");

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
        conn.setRequestProperty("User-Agent", "FedoraClub-DuelPlugin");
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "token " + token);
        }
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int code = conn.getResponseCode();
        if (code != 200) {
            plugin.getLogger().warning("§5[Update] §cGitHub a répondu avec le code: " + code);
            if (code == 404) {
                plugin.getLogger().warning("§5[Update] §cDépôt ou release introuvable.");
            } else if (code == 401 || code == 403) {
                if (token == null || token.isEmpty()) {
                    plugin.getLogger().warning("§5[Update] §cDépôt privé. Ajoutez un token dans config.yml (github-token).");
                } else {
                    plugin.getLogger().warning("§5[Update] §cToken invalide ou sans permission sur le dépôt.");
                }
            }
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    private String extractTag(String json) {
        int tagIndex = json.indexOf("\"tag_name\":\"");
        if (tagIndex == -1) return null;
        int start = tagIndex + "\"tag_name\":\"".length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        String tag = json.substring(start, end);
        return tag.startsWith("v") ? tag.substring(1) : tag;
    }

    private String extractDownloadUrl(String json) {
        int assetsIndex = json.indexOf("\"browser_download_url\":\"");
        if (assetsIndex == -1) return null;
        int start = assetsIndex + "\"browser_download_url\":\"".length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }

    private boolean isNewerVersion(String latest, String current) {
        String[] latestParts = latest.split("\\.");
        String[] currentParts = current.split("\\.");

        int maxLength = Math.max(latestParts.length, currentParts.length);
        for (int i = 0; i < maxLength; i++) {
            int l = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
            int c = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            if (l > c) return true;
            if (l < c) return false;
        }
        return false;
    }

    private void downloadUpdate(String urlString, String currentVersion) {
        try {
            File pluginsFolder = plugin.getDataFolder().getParentFile();
            File currentJar = new File(pluginsFolder, "DuelPlugin-" + currentVersion + ".jar");
            File newJar = new File(pluginsFolder, "DuelPlugin-" + latestVersion + ".jar");

            if (newJar.exists()) {
                plugin.getLogger().info("§5[Update] §dLe fichier existe déjà: " + newJar.getName());
                return;
            }

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "FedoraClub-DuelPlugin");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);

            if (conn.getResponseCode() != 200) {
                plugin.getLogger().warning("§5[Update] §cErreur de téléchargement: HTTP " + conn.getResponseCode());
                return;
            }

            InputStream in = conn.getInputStream();
            OutputStream out = new FileOutputStream(newJar);

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }

            out.close();
            in.close();
            conn.disconnect();

            plugin.getLogger().info("§5[Update] §aTéléchargé: §f" + newJar.getName() + " §a(" + (totalBytes / 1024) + " KB)");
            plugin.getLogger().info("§5[Update] §dSupprimez l'ancien JAR (§f" + currentJar.getName() + "§d) après redémarrage.");

        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "§5[Update] §cErreur lors du téléchargement:", e);
        }
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}
