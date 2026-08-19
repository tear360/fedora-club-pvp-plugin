package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class FriendsCommand implements CommandExecutor, TabCompleter {

    private final DuelPlugin plugin;

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

        plugin.getFriendsManager().addFriend(player.getUniqueId(), target.getUniqueId());
        player.sendMessage(plugin.getPrefix() + "§d" + target.getName() + " §aajouté(e) en ami!");

        if (plugin.getFriendsManager().isFriend(target.getUniqueId(), player.getUniqueId())) {
            target.sendMessage(plugin.getPrefix() + "§d" + player.getName() + " §avous a ajouté(e) en ami!");
        } else {
            target.sendMessage(plugin.getPrefix() + "§d" + player.getName() + " §7vous a ajouté(e) en ami. Utilisez §d/f add " + player.getName() + " §7pour réciproque.");
        }
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
        player.sendMessage("§d/f add <joueur> §7- Ajouter un ami");
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
            completions.addAll(Arrays.asList("add", "remove", "list"));
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
