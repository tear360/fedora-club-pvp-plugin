package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;

public class DiscordBotManager extends ListenerAdapter {

    private final DuelPlugin plugin;
    private JDA jda;
    private final String bugReportChannelId;
    private final String duelResultChannelId;
    private final String reportChannelId;
    private static final String CLOSE_EMOJI = "\u2705";

    public DiscordBotManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.bugReportChannelId = plugin.getConfig().getString("discord.bug-report-channel-id", "");
        this.duelResultChannelId = plugin.getConfig().getString("discord.duel-result-channel-id", "");
        this.reportChannelId = plugin.getConfig().getString("discord.report-channel-id", "");
        initBot();
    }

    private void initBot() {
        String token = plugin.getConfig().getString("discord.bot-token", "");
        if (token == null || token.isBlank()) {
            plugin.getLogger().info("[Discord] No bot token configured, Discord bot disabled.");
            return;
        }

        try {
            jda = JDABuilder.createDefault(token,
                    EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS))
                    .addEventListeners(this)
                    .build();
            plugin.getLogger().info("[Discord] Bot connected successfully.");
        } catch (Exception e) {
            plugin.getLogger().severe("[Discord] Failed to start bot: " + e.getMessage());
        }
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdownNow();
        }
    }

    public boolean isEnabled() {
        return jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }

    // ─── DUEL RESULT ───────────────────────────────────────────

    public void sendDuelResult(String winnerName, String loserName, String modeName, long durationMs) {
        if (!isEnabled() || duelResultChannelId.isBlank()) return;

        CompletableFuture.runAsync(() -> {
            try {
                TextChannel channel = jda.getTextChannelById(duelResultChannelId);
                if (channel == null) return;

                String duration = formatDuration(durationMs);
                String timestamp = Instant.now().atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_INSTANT);

                channel.sendMessageEmbeds(new net.dv8tion.jda.api.EmbedBuilder()
                        .setTitle("\u2694 R\u00e9sultat de duel")
                        .setColor(0x959066)
                        .addField("\ud83c\udfc6 Gagnant", winnerName, true)
                        .addField("\ud83d\udc80 Perdant", loserName, true)
                        .addField("\ud83c\udfae Mode", modeName, true)
                        .addField("\u23f1 Dur\u00e9e", duration, true)
                        .setFooter("Fedora Club")
                        .setTimestamp(Instant.parse(timestamp))
                        .build()).queue();
            } catch (Exception e) {
                plugin.getLogger().warning("[Discord] Failed to send duel result: " + e.getMessage());
            }
        });
    }

    // ─── BUG REPORT FORUM POST ────────────────────────────────

    public void createBugReportThread(String playerName, String playerUuid, String bug) {
        if (!isEnabled() || bugReportChannelId.isBlank()) return;

        CompletableFuture.runAsync(() -> {
            try {
                ForumChannel forum = jda.getForumChannelById(bugReportChannelId);
                if (forum == null) return;

                String postName = "bug-" + playerName.toLowerCase() + "-" + System.currentTimeMillis();

                MessageCreateData content = new MessageCreateBuilder()
                        .addContent("\ud83d\udc1b **Bug report de " + playerName + "**\n\n" + bug)
                        .addEmbeds(new net.dv8tion.jda.api.EmbedBuilder()
                                .setTitle("\ud83d\udc1b Bug Report")
                                .setColor(0xFF0000)
                                .addField("\ud83d\udc64 Joueur", playerName, true)
                                .addField("\ud83d\udccb Bug", bug, false)
                                .setFooter("Fedora Club - Bug Report")
                                .setTimestamp(Instant.now())
                                .build())
                        .build();

                forum.createForumPost(postName, content).queue(post ->
                        post.getMessage().addReaction(Emoji.fromUnicode(CLOSE_EMOJI)).queue());
            } catch (Exception e) {
                plugin.getLogger().warning("[Discord] Failed to create bug report forum post: " + e.getMessage());
            }
        });
    }

    // ─── REPORT FORUM POST ─────────────────────────────────────

    public void createReportForumPost(String reporterName, String reportedName, String reason, int reportId) {
        if (!isEnabled() || reportChannelId.isBlank()) return;

        CompletableFuture.runAsync(() -> {
            try {
                ForumChannel forum = jda.getForumChannelById(reportChannelId);
                if (forum == null) return;

                String postName = "report-" + reportedName.toLowerCase().replace(' ', '-') + "-" + reportId;

                MessageCreateData content = new MessageCreateBuilder()
                        .addContent("\ud83d\udcdd **Report #" + reportId + " de " + reporterName + "**\n\n" + reason)
                        .addEmbeds(new net.dv8tion.jda.api.EmbedBuilder()
                                .setTitle("\ud83d\udcdd Report #" + reportId)
                                .setColor(0xFFA500)
                                .addField("\ud83d\udc64 Joueur signal\u00e9", reportedName, true)
                                .addField("\ud83d\udc64 Signal\u00e9 par", reporterName, true)
                                .addField("\ud83d\udccb Raison", reason, false)
                                .setFooter("Fedora Club - Report")
                                .setTimestamp(Instant.now())
                                .build())
                        .build();

                forum.createForumPost(postName, content).queue(post ->
                        post.getMessage().addReaction(Emoji.fromUnicode(CLOSE_EMOJI)).queue());
            } catch (Exception e) {
                plugin.getLogger().warning("[Discord] Failed to create report forum post: " + e.getMessage());
            }
        });
    }

    // ─── REACTION LISTENER ─────────────────────────────────────

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser() == null || event.getUser().isBot()) return;
        if (!event.getEmoji().equals(Emoji.fromUnicode(CLOSE_EMOJI))) return;

        if (!event.getMember().hasPermission(net.dv8tion.jda.api.Permission.MANAGE_CHANNEL)) return;

        long channelId = event.getChannel().getIdLong();

        if (bugReportChannelId.isBlank() && reportChannelId.isBlank()) return;

        try {
            if (event.getChannelType() == net.dv8tion.jda.api.entities.channel.ChannelType.GUILD_PUBLIC_THREAD) {
                ThreadChannel thread = event.getChannel().asThreadChannel();
                long parentId = thread.getParentChannel().getIdLong();
                boolean isBug = String.valueOf(parentId).equals(bugReportChannelId);
                boolean isReport = String.valueOf(parentId).equals(reportChannelId);
                if (isBug || isReport) {
                    thread.sendMessage("\ud83d\udeab Report trait\u00e9 par **" + event.getUser().getName() + "**").queue();
                    thread.getManager().setLocked(true).queue();
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        try {
                            thread.delete().queue();
                        } catch (Exception ignored) {}
                    }, 100L);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[Discord] Failed to handle reaction: " + e.getMessage());
        }
    }

    // ─── UTILS ─────────────────────────────────────────────────

    private String formatDuration(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }
}
