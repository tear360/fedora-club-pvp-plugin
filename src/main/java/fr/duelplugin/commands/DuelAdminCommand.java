package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.Arena;
import fr.duelplugin.models.DuelGameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DuelAdminCommand implements CommandExecutor, TabCompleter {

    private final DuelPlugin plugin;

    public DuelAdminCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        if (!player.hasPermission("duelplugin.admin")) {
            player.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getArenaManager().loadArenas();
                plugin.getLobbyManager().loadLobby();
                player.sendMessage(plugin.getPrefix() + "§aConfiguration rechargée.");
            }
            case "setlobby" -> {
                plugin.getLobbyManager().setLobby(player.getLocation());
                player.sendMessage(plugin.getMessage("lobby-set"));
            }
            case "create" -> {
                if (args.length < 3) {
                    player.sendMessage(plugin.getPrefix() + "§cUsage: /da create <nom> <mode>");
                    return true;
                }
                String name = args[1];
                DuelGameMode mode = DuelGameMode.fromName(args[2]);
                if (mode == null) {
                    player.sendMessage(plugin.getPrefix() + "§cMode inconnu. Modes: " +
                            Arrays.stream(DuelGameMode.values()).map(DuelGameMode::getDisplayName).collect(Collectors.joining(", ")));
                    return true;
                }
                if (plugin.getArenaManager().createArena(name, mode)) {
                    player.sendMessage(plugin.getPrefix() + "§aArène §e" + name + " §acréée! Mode: " + mode.getColoredName());
                } else {
                    player.sendMessage(plugin.getPrefix() + "§cCette arène existe déjà.");
                }
            }
            case "delete" -> {
                if (args.length < 2) {
                    player.sendMessage(plugin.getPrefix() + "§cUsage: /da delete <nom>");
                    return true;
                }
                if (plugin.getArenaManager().deleteArena(args[1])) {
                    player.sendMessage(plugin.getPrefix() + "§aArène §e" + args[1] + " §asupprimée.");
                } else {
                    player.sendMessage(plugin.getMessage("arena-not-found"));
                }
            }
            case "setspawn" -> {
                if (args.length < 3) {
                    player.sendMessage(plugin.getPrefix() + "§cUsage: /da setspawn <nom> <1|2>");
                    return true;
                }
                Arena arena = plugin.getArenaManager().getArena(args[1]);
                if (arena == null) {
                    player.sendMessage(plugin.getMessage("arena-not-found"));
                    return true;
                }
                int slot;
                try {
                    slot = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(plugin.getPrefix() + "§cSlot invalide. Utilisez 1 ou 2.");
                    return true;
                }
                Location loc = player.getLocation();
                if (slot == 1) {
                    arena.setSpawn1(loc);
                } else if (slot == 2) {
                    arena.setSpawn2(loc);
                } else {
                    player.sendMessage(plugin.getPrefix() + "§cSlot invalide. Utilisez 1 ou 2.");
                    return true;
                }
                plugin.getArenaManager().saveArenas();
                player.sendMessage(plugin.getPrefix() + "§aPoint d'apparition §e" + slot + " §adéfini pour §e" + arena.getName());
            }
            case "setmin" -> {
                if (args.length < 2) {
                    player.sendMessage(plugin.getPrefix() + "§cUsage: /da setmin <nom>");
                    return true;
                }
                Arena arena = plugin.getArenaManager().getArena(args[1]);
                if (arena == null) {
                    player.sendMessage(plugin.getMessage("arena-not-found"));
                    return true;
                }
                arena.setMinCorner(player.getLocation());
                plugin.getArenaManager().saveArenas();
                player.sendMessage(plugin.getPrefix() + "§aCoin minimum défini pour §e" + arena.getName());
            }
            case "setmax" -> {
                if (args.length < 2) {
                    player.sendMessage(plugin.getPrefix() + "§cUsage: /da setmax <nom>");
                    return true;
                }
                Arena arena = plugin.getArenaManager().getArena(args[1]);
                if (arena == null) {
                    player.sendMessage(plugin.getMessage("arena-not-found"));
                    return true;
                }
                arena.setMaxCorner(player.getLocation());
                plugin.getArenaManager().saveArenas();
                player.sendMessage(plugin.getPrefix() + "§aCoin maximum défini pour §e" + arena.getName());
            }
            case "info" -> {
                if (args.length < 2) {
                    player.sendMessage(plugin.getPrefix() + "§cUsage: /da info <nom>");
                    return true;
                }
                Arena arena = plugin.getArenaManager().getArena(args[1]);
                if (arena == null) {
                    player.sendMessage(plugin.getMessage("arena-not-found"));
                    return true;
                }
                player.sendMessage("§6=== Arène: " + arena.getName() + " ===");
                player.sendMessage("§eMode: " + arena.getGameMode().getColoredName());
                player.sendMessage("§eSpawn 1: " + formatLoc(arena.getSpawn1()));
                player.sendMessage("§eSpawn 2: " + formatLoc(arena.getSpawn2()));
                player.sendMessage("§eMin: " + formatLoc(arena.getMinCorner()));
                player.sendMessage("§eMax: " + formatLoc(arena.getMaxCorner()));
                player.sendMessage("§eConfigurée: " + (arena.isSetup() ? "§aOui" : "§cNon"));
            }
            case "list" -> {
                var arenas = plugin.getArenaManager().getAllArenas();
                if (arenas.isEmpty()) {
                    player.sendMessage(plugin.getPrefix() + "§cAucune arène configurée.");
                    return true;
                }
                player.sendMessage("§6=== Liste des arènes ===");
                for (Arena arena : arenas) {
                    player.sendMessage("§e- " + arena.getName() + " §7(" + arena.getGameMode().getColoredName() + "§7) " +
                            (arena.isSetup() ? "§aPrête" : "§cIncomplète"));
                }
            }
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6=== §eFedora Club §6- Admin ===");
        player.sendMessage("§e/da reload §7- Recharger la config");
        player.sendMessage("§e/da setlobby §7- Définir le lobby");
        player.sendMessage("§e/da create <nom> <mode> §7- Créer une arène");
        player.sendMessage("§e/da delete <nom> §7- Supprimer une arène");
        player.sendMessage("§e/da setspawn <nom> <1|2> §7- Définir les spawns");
        player.sendMessage("§e/da setmin <nom> §7- Coin minimum");
        player.sendMessage("§e/da setmax <nom> §7- Coin maximum");
        player.sendMessage("§e/da info <nom> §7- Infos d'une arène");
        player.sendMessage("§e/da list §7- Lister les arènes");
    }

    private String formatLoc(Location loc) {
        if (loc == null) return "§cNon défini";
        return String.format("§f%s §7(§f%.1f, %.1f, %.1f§7)", loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "setlobby", "create", "delete", "setspawn", "setmin", "setmax", "info", "list"));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("delete") || sub.equals("setspawn") || sub.equals("info") || sub.equals("setmin") || sub.equals("setmax")) {
                completions.addAll(plugin.getArenaManager().getAllArenas().stream()
                        .map(Arena::getName).collect(Collectors.toList()));
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            completions.addAll(Arrays.stream(DuelGameMode.values())
                    .map(DuelGameMode::getDisplayName).collect(Collectors.toList()));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("setspawn")) {
            completions.addAll(Arrays.asList("1", "2"));
        }
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
