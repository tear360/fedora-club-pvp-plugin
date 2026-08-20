package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
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
            player.sendMessage(plugin.getPrefix() + "§cUsage: /f add <joueur>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(plugin.getPrefix() + "§cJoueur introuvable ou hors ligne.");
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§cVous ne pouvez pas vous ajouter vous-même.");
            return;
        }

        if (plugin.getFriendsManager().isFriend(player.getUniqueId(), target.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§c" + target.getName() + " est déjà votre ami.");
            return;
        }

        if (pendingRequests.containsValue(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§cVous avez déjà une demande en attente.");
            return;
        }

        pendingRequests.put(target.getUniqueId(), player.getUniqueId());

        target.sendMessage("");
        target.sendMessage("§5§l═══════════════════════════");
        target.sendMessage("§d" + player.getName() + " §7vous a envoyé une demande d'ami!");

        Component acceptButton = Component.text("[ACCEPTER]", NamedTextColor.GREEN, TextDecoration.BOLD)
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/f accept"))
                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Accepter la demande", NamedTextColor.GREEN)));

        Component denyButton = Component.text(" [REFUSER]", NamedTextColor.RED, TextDecoration.BOLD)
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/f deny"))
                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Refuser la demande", NamedTextColor.RED)));

        target.sendMessage(Component.text("Action: ", NamedTextColor.LIGHT_PURPLE).append(acceptButton).append(denyButton));
        target.sendMessage("§5§l═══════════════════════════");
        target.sendMessage("");

        player.sendMessage(plugin.getPrefix() + "§dDemande d'ami envoyée à §f" + target.getName() + "§d!");
    }

    private void handleAccept(Player player) {
        UUID senderUuid = pendingRequests.remove(player.getUniqueId());
        if (senderUuid == null) {
            player.sendMessage(plugin.getPrefix() + "§cAucune demande d'ami en attente.");
            return;
        }

        Player sender = Bukkit.getPlayer(senderUuid);
        if (sender == null || !sender.isOnline()) {
            player.sendMessage(plugin.getPrefix() + "§cCe joueur n'est plus en ligne.");
            return;
        }

        plugin.getFriendsManager().addFriend(player.getUniqueId(), senderUuid);
        plugin.getFriendsManager().addFriend(senderUuid, player.getUniqueId());

        player.sendMessage(plugin.getPrefix() + "§d" + sender.getName() + " §aajouté(e) en ami!");
        sender.sendMessage(plugin.getPrefix() + "§d" + player.getName() + " §aaccepté votre demande d'ami!");
    }

    private void handleDeny(Player player) {
        UUID senderUuid = pendingRequests.remove(player.getUniqueId());
        if (senderUuid == null) {
            player.sendMessage(plugin.getPrefix() + "§cAucune demande d'ami en attente.");
            return;
        }

        Player sender = Bukkit.getPlayer(senderUuid);
        if (sender != null && sender.isOnline()) {
            sender.sendMessage(plugin.getPrefix() + "§d" + player.getName() + " §ca refusé votre demande d'ami.");
        }
        player.sendMessage(plugin.getPrefix() + "§cDemande refusée.");
    }

    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getPrefix() + "§cUsage: /f remove <joueur>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        UUID targetUuid = null;

        if (target != null) {
            targetUuid = target.getUniqueId();
        } else {
            player.sendMessage(plugin.getPrefix() + "§cJoueur introuvable ou hors ligne.");
            return;
        }

        if (!plugin.getFriendsManager().isFriend(player.getUniqueId(), targetUuid)) {
            player.sendMessage(plugin.getPrefix() + "§c" + args[1] + " n'est pas votre ami.");
            return;
        }

        plugin.getFriendsManager().removeFriend(player.getUniqueId(), targetUuid);
        plugin.getFriendsManager().removeFriend(targetUuid, player.getUniqueId());
        player.sendMessage(plugin.getPrefix() + "§d" + args[1] + " §cretiré(e) de vos amis.");
    }

    private void handleList(Player player) {
        Set<UUID> friendUuids = plugin.getFriendsManager().getFriends(player.getUniqueId());

        player.sendMessage("");
        player.sendMessage("§5§l═══════════════════════════");
        player.sendMessage("§d§lVos amis §7(" + friendUuids.size() + ")");

        if (friendUuids.isEmpty()) {
            player.sendMessage("§7Aucun ami. Utilisez §d/f add <joueur>");
        } else {
            for (UUID uuid : friendUuids) {
                Player online = Bukkit.getPlayer(uuid);
                if (online != null) {
                    player.sendMessage("§a● §f" + online.getName() + " §7(§aEn ligne§7)");
                } else {
                    player.sendMessage("§7● §8" + uuid.toString().substring(0, 8) + "... §7(§cHors ligne§7)");
                }
            }
        }

        player.sendMessage("§5§l═══════════════════════════");
        player.sendMessage("");
    }

    private void sendHelp(Player player) {
        player.sendMessage("");
        player.sendMessage("§5§l═══════════════════════════");
        player.sendMessage("§d§lCommandes Amis");
        player.sendMessage("");
        player.sendMessage("§d/f add <joueur> §7- Envoyer une demande d'ami");
        player.sendMessage("§d/f accept §7- Accepter une demande");
        player.sendMessage("§d/f deny §7- Refuser une demande");
        player.sendMessage("§d/f remove <joueur> §7- Retirer un ami");
        player.sendMessage("§d/f list §7- Liste de vos amis");
        player.sendMessage("§7Sneak en lobby pour voir vos amis");
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
