package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.managers.LanguageManager;
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

    private LanguageManager lang() {
        return plugin.getLanguageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(lang().msgRaw(null, "command_only_players"));
            return true;
        }

        if (!player.hasPermission("duelplugin.admin")) {
            player.sendMessage(lang().msg(player, "no_permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.getArenaManager().loadArenas();
                plugin.getLobbyManager().loadLobby();
                player.sendMessage(lang().msg(player, "arena_config_reloaded"));
            }
            case "setlobby" -> {
                plugin.getLobbyManager().setLobby(player.getLocation());
                player.sendMessage(lang().msg(player, "arena_config_reloaded"));
            }
            case "arena" -> handleArenaCommand(player, args);
            default -> sendHelp(player);
        }
        return true;
    }

    private void handleArenaCommand(Player player, String[] args) {
        if (args.length < 2) {
            sendArenaHelp(player);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "create" -> {
                if (args.length < 4) {
                    player.sendMessage(lang().msg(player, "arena_usage_create"));
                    return;
                }
                String name = args[2];
                DuelGameMode mode = DuelGameMode.fromName(args[3]);
                if (mode == null) {
                    player.sendMessage(lang().msg(player, "arena_unknown_mode") +
                            Arrays.stream(DuelGameMode.values()).map(DuelGameMode::getDisplayName).collect(Collectors.joining(", ")));
                    return;
                }
                if (plugin.getArenaManager().createArena(name, mode)) {
                    player.sendMessage(lang().msg(player, "arena_created", "%name%", name, "%mode%", mode.getColoredName()));
                } else {
                    player.sendMessage(lang().msg(player, "arena_already_exists"));
                }
            }
            case "delete" -> {
                if (args.length < 3) {
                    player.sendMessage(lang().msg(player, "arena_usage_delete"));
                    return;
                }
                if (plugin.getArenaManager().deleteArena(args[2])) {
                    player.sendMessage(lang().msg(player, "arena_deleted", "%name%", args[2]));
                } else {
                    player.sendMessage(lang().msg(player, "arena_not_found"));
                }
            }
            case "setspawn" -> {
                if (args.length < 4) {
                    player.sendMessage(lang().msg(player, "arena_usage_setspawn"));
                    return;
                }
                Arena arena = plugin.getArenaManager().getArena(args[2]);
                if (arena == null) {
                    player.sendMessage(lang().msg(player, "arena_not_found"));
                    return;
                }
                int slot;
                try {
                    slot = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage(lang().msg(player, "arena_invalid_slot"));
                    return;
                }
                Location loc = player.getLocation();
                if (slot == 1) {
                    arena.setSpawn1(loc);
                } else if (slot == 2) {
                    arena.setSpawn2(loc);
                } else {
                    player.sendMessage(lang().msg(player, "arena_invalid_slot"));
                    return;
                }
                plugin.getArenaManager().saveArenas();
                player.sendMessage(lang().msg(player, "arena_spawn_set", "%slot%", String.valueOf(slot), "%name%", arena.getName()));
            }
            case "setmin" -> {
                if (args.length < 3) {
                    player.sendMessage(lang().msg(player, "arena_usage_setmin"));
                    return;
                }
                Arena arena = plugin.getArenaManager().getArena(args[2]);
                if (arena == null) {
                    player.sendMessage(lang().msg(player, "arena_not_found"));
                    return;
                }
                arena.setMinCorner(player.getLocation());
                plugin.getArenaManager().saveArenas();
                player.sendMessage(lang().msg(player, "arena_min_set", "%name%", arena.getName()));
            }
            case "setmax" -> {
                if (args.length < 3) {
                    player.sendMessage(lang().msg(player, "arena_usage_setmax"));
                    return;
                }
                Arena arena = plugin.getArenaManager().getArena(args[2]);
                if (arena == null) {
                    player.sendMessage(lang().msg(player, "arena_not_found"));
                    return;
                }
                arena.setMaxCorner(player.getLocation());
                plugin.getArenaManager().saveArenas();
                player.sendMessage(lang().msg(player, "arena_max_set", "%name%", arena.getName()));
            }
            case "info" -> {
                if (args.length < 3) {
                    player.sendMessage(lang().msg(player, "arena_usage_info"));
                    return;
                }
                Arena arena = plugin.getArenaManager().getArena(args[2]);
                if (arena == null) {
                    player.sendMessage(lang().msg(player, "arena_not_found"));
                    return;
                }
                player.sendMessage("§5═══════════════════════");
                player.sendMessage(lang().msgRaw(player, "arena_info_name", "%name%", arena.getName()));
                player.sendMessage("§dMode: " + arena.getGameMode().getColoredName());
                player.sendMessage("§dSpawn 1: " + formatLoc(arena.resolveSpawn1()));
                player.sendMessage("§dSpawn 2: " + formatLoc(arena.resolveSpawn2()));
                player.sendMessage("§dMin: " + formatLoc(arena.resolveMinCorner()));
                player.sendMessage("§dMax: " + formatLoc(arena.resolveMaxCorner()));
                player.sendMessage("§dConfigurée: " + (arena.isSetup() ? lang().msgRaw(player, "arena_configured") : lang().msgRaw(player, "arena_not_configured")));
                player.sendMessage("§5═══════════════════════");
            }
            case "tp" -> {
                if (args.length < 3) {
                    player.sendMessage(lang().msg(player, "arena_usage_tp"));
                    return;
                }
                Arena arena = plugin.getArenaManager().getArena(args[2]);
                if (arena == null) {
                    player.sendMessage(lang().msg(player, "arena_not_found"));
                    return;
                }
                Location spawn = arena.resolveSpawn1();
                if (spawn == null) {
                    player.sendMessage(lang().msg(player, "arena_no_spawn", "%name%", arena.getName()));
                    return;
                }
                player.teleport(spawn);
                player.sendMessage(lang().msg(player, "arena_teleported", "%name%", arena.getName()));
            }
            case "list" -> {
                var arenas = plugin.getArenaManager().getAllArenas();
                if (arenas.isEmpty()) {
                    player.sendMessage(lang().msg(player, "arena_list_empty"));
                    return;
                }
                player.sendMessage("§5═══════════════════════");
                player.sendMessage(lang().msgRaw(player, "arena_list_header"));
                for (Arena arena : arenas) {
                    player.sendMessage(lang().msgRaw(player, "arena_list_entry", "%name%", arena.getName(), "%mode%", arena.getGameMode().getColoredName()));
                }
                player.sendMessage("§5═══════════════════════");
            }
            default -> sendArenaHelp(player);
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("§5═══════════════════════");
        player.sendMessage("§d§lFedora Club §7- Admin");
        player.sendMessage("");
        player.sendMessage("§d/da reload §7- Recharger la config");
        player.sendMessage("§d/da setlobby §7- Définir le lobby");
        player.sendMessage("§d/da arena <cmd> §7- Gestion des arènes");
        player.sendMessage("§5═══════════════════════");
    }

    private void sendArenaHelp(Player player) {
        player.sendMessage("§5═══════════════════════");
        player.sendMessage("§d§lGestion des arènes:");
        player.sendMessage("");
        player.sendMessage("§d/da arena create <nom> <mode>");
        player.sendMessage("§d/da arena delete <nom>");
        player.sendMessage("§d/da arena setspawn <nom> <1|2>");
        player.sendMessage("§d/da arena setmin <nom>");
        player.sendMessage("§d/da arena setmax <nom>");
        player.sendMessage("§d/da arena info <nom>");
        player.sendMessage("§d/da arena tp <nom>");
        player.sendMessage("§d/da arena list");
        player.sendMessage("§5═══════════════════════");
    }

    private String formatLoc(Location loc) {
        if (loc == null) return "§cNon défini";
        String worldName = loc.getWorld() != null ? loc.getWorld().getName() : "inconnu";
        return String.format("§f%s §7(§f%.1f, %.1f, %.1f§7)", worldName, loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "setlobby", "arena"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("arena")) {
            completions.addAll(Arrays.asList("create", "delete", "setspawn", "setmin", "setmax", "tp", "info", "list"));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("arena")) {
            String sub = args[1].toLowerCase();
            if (sub.equals("delete") || sub.equals("setspawn") || sub.equals("info") || sub.equals("setmin") || sub.equals("setmax") || sub.equals("tp")) {
                completions.addAll(plugin.getArenaManager().getAllArenas().stream()
                        .map(Arena::getName).collect(Collectors.toList()));
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("arena") && args[1].equalsIgnoreCase("create")) {
            completions.addAll(Arrays.stream(DuelGameMode.values())
                    .map(DuelGameMode::getDisplayName).collect(Collectors.toList()));
        } else if (args.length == 4 && args[0].equalsIgnoreCase("arena") && args[1].equalsIgnoreCase("setspawn")) {
            completions.addAll(Arrays.asList("1", "2"));
        }
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
