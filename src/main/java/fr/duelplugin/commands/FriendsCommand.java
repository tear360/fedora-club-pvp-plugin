package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.managers.LanguageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FriendsCommand implements CommandExecutor, TabCompleter {

    private final DuelPlugin plugin;
    private final Map<UUID, UUID> pendingRequests = new ConcurrentHashMap<>();

    public FriendsCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    private LanguageManager lang() {
        return plugin.getLanguageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(lang().msgRaw(null, "command_only_players"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> handleAdd(player, args);
            case "accept" -> handleAccept(player);
            case "deny" -> handleDeny(player);
            case "remove" -> handleRemove(player, args);
            case "list" -> handleList(player);
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleAdd(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(lang().msg(player, "friend_usage_add"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(lang().msg(player, "player_not_found"));
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(lang().msg(player, "friend_cannot_self"));
            return;
        }

        if (plugin.getFriendsManager().isFriend(player.getUniqueId(), target.getUniqueId())) {
            player.sendMessage(lang().msg(player, "friend_already_exists", "%player%", target.getName()));
            return;
        }

        if (!plugin.getSettingsManager().acceptsFriendRequests(target.getUniqueId())) {
            player.sendMessage(lang().msg(player, "friend_disabled", "%player%", target.getName()));
            return;
        }

        if (pendingRequests.containsValue(player.getUniqueId())) {
            player.sendMessage(lang().msg(player, "friend_accept_no_requests"));
            return;
        }

        pendingRequests.put(target.getUniqueId(), player.getUniqueId());

        target.sendMessage("");
        target.sendMessage("§5§l═══════════════════════════");
        target.sendMessage(lang().msg(target, "friend_request_received", "%player%", player.getName()));

        Component acceptButton = Component.text(lang().msgRaw(target, "friend_request_accept"), NamedTextColor.GREEN, TextDecoration.BOLD)
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/f accept"))
                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(lang().msgRaw(target, "friend_request_hover_accept"), NamedTextColor.GREEN)));

        Component denyButton = Component.text(" " + lang().msgRaw(target, "friend_request_deny"), NamedTextColor.RED, TextDecoration.BOLD)
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/f deny"))
                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(lang().msgRaw(target, "friend_request_hover_deny"), NamedTextColor.RED)));

        target.sendMessage(Component.text(lang().msgRaw(target, "duel_action"), NamedTextColor.LIGHT_PURPLE).append(acceptButton).append(denyButton));
        target.sendMessage("§5§l═══════════════════════════");
        target.sendMessage("");

        player.sendMessage(lang().msg(player, "friend_request_sent", "%player%", target.getName()));
    }

    private void handleAccept(Player player) {
        UUID senderUuid = pendingRequests.remove(player.getUniqueId());
        if (senderUuid == null) {
            player.sendMessage(lang().msg(player, "friend_accept_no_requests"));
            return;
        }

        Player sender = Bukkit.getPlayer(senderUuid);
        if (sender == null || !sender.isOnline()) {
            player.sendMessage(lang().msg(player, "duel_target_online"));
            return;
        }

        plugin.getFriendsManager().addFriend(player.getUniqueId(), senderUuid);
        plugin.getFriendsManager().addFriend(senderUuid, player.getUniqueId());

        player.sendMessage(lang().msg(player, "friend_added", "%player%", sender.getName()));
        sender.sendMessage(lang().msg(sender, "friend_accept_success", "%player%", player.getName()));
    }

    private void handleDeny(Player player) {
        UUID senderUuid = pendingRequests.remove(player.getUniqueId());
        if (senderUuid == null) {
            player.sendMessage(lang().msg(player, "friend_accept_no_requests"));
            return;
        }

        Player sender = Bukkit.getPlayer(senderUuid);
        if (sender != null && sender.isOnline()) {
            sender.sendMessage(lang().msg(sender, "friend_deny_by_target", "%player%", player.getName()));
        }
        player.sendMessage(lang().msg(player, "friend_deny_refused"));
    }

    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(lang().msg(player, "friend_usage_remove"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        UUID targetUuid = null;

        if (target != null) {
            targetUuid = target.getUniqueId();
        } else {
            player.sendMessage(lang().msg(player, "player_not_found"));
            return;
        }

        if (!plugin.getFriendsManager().isFriend(player.getUniqueId(), targetUuid)) {
            player.sendMessage(lang().msg(player, "friend_not_friend", "%player%", args[1]));
            return;
        }

        plugin.getFriendsManager().removeFriend(player.getUniqueId(), targetUuid);
        plugin.getFriendsManager().removeFriend(targetUuid, player.getUniqueId());
        player.sendMessage(lang().msg(player, "friend_removed", "%player%", args[1]));
    }

    private void handleList(Player player) {
        Set<UUID> friendUuids = plugin.getFriendsManager().getFriends(player.getUniqueId());

        player.sendMessage("");
        player.sendMessage("§5§l═══════════════════════════");
        player.sendMessage(lang().msgRaw(player, "friend_list_title", "%count%", String.valueOf(friendUuids.size())));

        if (friendUuids.isEmpty()) {
            player.sendMessage(lang().msgRaw(player, "friend_list_empty"));
        } else {
            for (UUID uuid : friendUuids) {
                Player online = Bukkit.getPlayer(uuid);
                if (online != null) {
                    player.sendMessage(lang().msgRaw(player, "friend_list_online", "%player%", online.getName()));
                } else {
                    player.sendMessage(lang().msgRaw(player, "friend_list_offline", "%player%", uuid.toString().substring(0, 8) + "..."));
                }
            }
        }

        player.sendMessage("§5§l═══════════════════════════");
        player.sendMessage("");
    }

    private void sendHelp(Player player) {
        player.sendMessage("");
        player.sendMessage("§5§l═══════════════════════════");
        player.sendMessage(lang().msgRaw(player, "friend_help_title"));
        player.sendMessage("");
        player.sendMessage(lang().msgRaw(player, "friend_help_add"));
        player.sendMessage(lang().msgRaw(player, "friend_help_accept"));
        player.sendMessage(lang().msgRaw(player, "friend_help_deny"));
        player.sendMessage(lang().msgRaw(player, "friend_help_remove"));
        player.sendMessage(lang().msgRaw(player, "friend_help_list"));
        player.sendMessage("§5§l═══════════════════════════");
        player.sendMessage("");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("add", "accept", "deny", "remove", "list"));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("add")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            } else if (sub.equals("remove")) {
                if (sender instanceof Player player) {
                    for (UUID uuid : plugin.getFriendsManager().getFriends(player.getUniqueId())) {
                        Player online = Bukkit.getPlayer(uuid);
                        if (online != null) completions.add(online.getName());
                    }
                }
            }
        }
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
