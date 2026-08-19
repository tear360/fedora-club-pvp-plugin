package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.managers.PartyManager;
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
import java.util.stream.Collectors;

public class PartyCommand implements CommandExecutor, TabCompleter {

    private final DuelPlugin plugin;

    public PartyCommand(DuelPlugin plugin) {
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
            case "create" -> handleCreate(player);
            case "invite" -> handleInvite(player, args);
            case "join" -> handleJoin(player);
            case "leave" -> handleLeave(player);
            case "kick" -> handleKick(player, args);
            case "disband" -> handleDisband(player);
            case "list" -> handleList(player);
            case "info" -> handleInfo(player);
            case "pub" -> handlePub(player);
            case "pubjoin" -> handlePubJoin(player, args);
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleCreate(Player player) {
        if (plugin.getPartyManager().isInParty(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§cVous êtes déjà dans une party.");
            return;
        }
        if (plugin.getPartyManager().createParty(player)) {
            player.sendMessage(plugin.getPrefix() + "§aParty §dcréée! §7Invitez des joueurs avec §d/party invite <joueur>");
        }
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getPrefix() + "§cUsage: /party invite <joueur>");
            return;
        }

        UUID partyLeader = plugin.getPartyManager().getPartyLeader(player.getUniqueId());
        if (partyLeader == null || !partyLeader.equals(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§cSeul le leader peut inviter.");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(plugin.getPrefix() + "§cJoueur introuvable ou hors ligne.");
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§cVous ne pouvez pas vous inviter.");
            return;
        }

        if (plugin.getPartyManager().isInParty(target.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§c" + target.getName() + " est déjà dans une party.");
            return;
        }

        if (plugin.getPartyManager().invitePlayer(player, target)) {
            int maxSize = plugin.getPartyManager().getMaxSize(player);
            int count = plugin.getPartyManager().getMemberCount(plugin.getPartyManager().getPartyLeader(player.getUniqueId()));
            player.sendMessage(plugin.getPrefix() + "§dInvitation envoyée à §f" + target.getName() + " §d(" + count + "/" + maxSize + ")");
            target.sendMessage(plugin.getPrefix() + "§d" + player.getName() + " §7vous invite dans sa party! §d/party join §7pour accepter.");
        } else {
            player.sendMessage(plugin.getPrefix() + "§cLa party est pleine! (" + plugin.getPartyManager().getMaxSize(player) + " max)");
        }
    }

    private void handleJoin(Player player) {
        if (plugin.getPartyManager().isInParty(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§cVous êtes déjà dans une party.");
            return;
        }

        UUID partyLeader = plugin.getPartyManager().getPendingInvite(player.getUniqueId());
        if (partyLeader == null) {
            player.sendMessage(plugin.getPrefix() + "§cAucune invitation en attente.");
            return;
        }

        PartyManager.Party party = plugin.getPartyManager().getPartyByLeader(partyLeader);
        if (party == null) {
            player.sendMessage(plugin.getPrefix() + "§cCette party n'existe plus.");
            return;
        }

        if (plugin.getPartyManager().acceptInvite(player)) {
            Player leader = Bukkit.getPlayer(partyLeader);
            int maxSize = plugin.getPartyManager().getMaxSize(leader);
            player.sendMessage(plugin.getPrefix() + "§aVous avez rejoint la party de §d" + (leader != null ? leader.getName() : "???") + "§a! (" + party.getSize() + "/" + maxSize + ")");
            if (leader != null) {
                leader.sendMessage(plugin.getPrefix() + "§d" + player.getName() + " §aa rejoint la party!");
            }
            for (UUID m : party.getMembers()) {
                if (m.equals(player.getUniqueId()) || m.equals(partyLeader)) continue;
                Player member = Bukkit.getPlayer(m);
                if (member != null) {
                    member.sendMessage(plugin.getPrefix() + "§d" + player.getName() + " §aa rejoint la party!");
                }
            }
        }
    }

    private void handleLeave(Player player) {
        if (!plugin.getPartyManager().isInParty(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§cVous n'êtes pas dans une party.");
            return;
        }

        boolean wasLeader = plugin.getPartyManager().isLeader(player.getUniqueId());
        PartyManager.Party party = plugin.getPartyManager().getParty(player.getUniqueId());
        UUID partyLeader = plugin.getPartyManager().getPartyLeader(player.getUniqueId());

        plugin.getPartyManager().leaveParty(player);
        player.sendMessage(plugin.getPrefix() + "§cVous avez quitté la party.");

        if (party != null) {
            for (UUID m : party.getMembers()) {
                if (m.equals(player.getUniqueId())) continue;
                Player member = Bukkit.getPlayer(m);
                if (member != null) {
                    member.sendMessage(plugin.getPrefix() + "§d" + player.getName() + " §7a quitté la party.");
                    if (wasLeader) {
                        UUID newLeader = plugin.getPartyManager().getPartyLeader(m);
                        if (newLeader != null) {
                            Player newLeaderPlayer = Bukkit.getPlayer(newLeader);
                            member.sendMessage(plugin.getPrefix() + "§d" + (newLeaderPlayer != null ? newLeaderPlayer.getName() : "???") + " §aest maintenant le leader!");
                        }
                    }
                }
            }
        }
    }

    private void handleKick(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getPrefix() + "§cUsage: /party kick <joueur>");
            return;
        }

        UUID partyLeader = plugin.getPartyManager().getPartyLeader(player.getUniqueId());
        if (partyLeader == null || !partyLeader.equals(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§cSeul le leader peut kick.");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(plugin.getPrefix() + "§cJoueur introuvable ou hors ligne.");
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§cVous ne pouvez pas vous kick.");
            return;
        }

        PartyManager.Party party = plugin.getPartyManager().getParty(player.getUniqueId());
        if (party == null || !party.hasMember(target.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§c" + target.getName() + " n'est pas dans votre party.");
            return;
        }

        if (plugin.getPartyManager().kickPlayer(player, target)) {
            target.sendMessage(plugin.getPrefix() + "§cVous avez été kick de la party.");
            player.sendMessage(plugin.getPrefix() + "§d" + target.getName() + " §ckické de la party.");
            for (UUID m : party.getMembers()) {
                if (m.equals(player.getUniqueId()) || m.equals(target.getUniqueId())) continue;
                Player member = Bukkit.getPlayer(m);
                if (member != null) {
                    member.sendMessage(plugin.getPrefix() + "§d" + target.getName() + " §7a été kick de la party.");
                }
            }
        }
    }

    private void handleDisband(Player player) {
        if (!plugin.getPartyManager().isLeader(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§cSeul le leader peut disband la party.");
            return;
        }

        PartyManager.Party party = plugin.getPartyManager().getParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage(plugin.getPrefix() + "§cVous n'êtes pas dans une party.");
            return;
        }

        for (UUID m : party.getMembers()) {
            Player member = Bukkit.getPlayer(m);
            if (member != null) {
                member.sendMessage(plugin.getPrefix() + "§cLa party a été dissoute par le leader.");
            }
        }

        plugin.getPartyManager().disbandParty(player);
        player.sendMessage(plugin.getPrefix() + "§cParty dissoute.");
    }

    private void handleList(Player player) {
        PartyManager.Party party = plugin.getPartyManager().getParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage(plugin.getPrefix() + "§cVous n'êtes pas dans une party.");
            return;
        }

        Player leader = Bukkit.getPlayer(party.getLeader());

        player.sendMessage("");
        player.sendMessage("§5§l═══════════════════════════");
        player.sendMessage("§d§lParty §7(" + party.getSize() + " joueur" + (party.getSize() > 1 ? "s" : "") + ")");
        player.sendMessage("");
        player.sendMessage("§6👑 §f" + (leader != null ? leader.getName() : "???") + " §7(Leader)");
        for (UUID m : party.getMembers()) {
            Player member = Bukkit.getPlayer(m);
            String name = member != null ? member.getName() : m.toString().substring(0, 8) + "...";
            String status = member != null ? "§a●" : "§7●";
            player.sendMessage(status + " §f" + name);
        }
        player.sendMessage("§5§l═══════════════════════════");
        player.sendMessage("");
    }

    private void handleInfo(Player player) {
        handleList(player);
    }

    private void handlePub(Player player) {
        if (!plugin.getVipManager().isVip(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§cCette commande est réservée aux VIP.");
            return;
        }

        UUID partyLeader = plugin.getPartyManager().getPartyLeader(player.getUniqueId());
        if (partyLeader == null || !partyLeader.equals(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§cSeul le leader peut publier la party.");
            return;
        }

        if (plugin.getPartyManager().isPubOpen(player.getUniqueId())) {
            plugin.getPartyManager().closePub(player);
            player.sendMessage(plugin.getPrefix() + "§cPublication de party désactivée.");
            return;
        }

        plugin.getPartyManager().openPub(player);

        String playerName = player.getName();
        UUID leaderUuid = player.getUniqueId();
        PartyManager.Party party = plugin.getPartyManager().getParty(leaderUuid);
        int count = party != null ? party.getSize() : 1;
        int maxSize = plugin.getPartyManager().getMaxSize(player);
        String sizeInfo = count + "/" + maxSize;

        Component joinButton = Component.text("[Rejoindre]", NamedTextColor.GREEN, TextDecoration.BOLD)
                .hoverEvent(HoverEvent.showText(Component.text().append(Component.text("Cliquez pour rejoindre la party de " + playerName + " (" + sizeInfo + ")", NamedTextColor.GREEN)).build()))
                .clickEvent(ClickEvent.runCommand("/party pubjoin " + leaderUuid.toString()));

        Component message = Component.text()
                .append(Component.text("Rejoindez la party de ", NamedTextColor.GRAY))
                .append(Component.text(playerName, NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
                .append(Component.text(" : ", NamedTextColor.GRAY))
                .append(joinButton)
                .build();

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (plugin.getDuelManager().isInDuel(online)) continue;
            if (online.getWorld() != player.getWorld()) continue;
            online.sendMessage(message);
        }
        player.sendMessage(plugin.getPrefix() + "§dParty publiée dans le chat!");
    }

    private void handlePubJoin(Player player, String[] args) {
        if (args.length < 2) return;
        if (plugin.getPartyManager().isInParty(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "§cVous êtes déjà dans une party.");
            return;
        }

        UUID leaderUuid;
        try {
            leaderUuid = UUID.fromString(args[1]);
        } catch (IllegalArgumentException e) {
            return;
        }

        if (!plugin.getPartyManager().isPubOpen(leaderUuid)) {
            player.sendMessage(plugin.getPrefix() + "§cCette party n'est plus disponible.");
            return;
        }

        if (plugin.getPartyManager().pubJoin(player, leaderUuid)) {
            Player leader = Bukkit.getPlayer(leaderUuid);
            PartyManager.Party party = plugin.getPartyManager().getParty(leaderUuid);
            int maxSize = plugin.getPartyManager().getMaxSize(leader);
            int count = party != null ? party.getSize() : 1;
            player.sendMessage(plugin.getPrefix() + "§aVous avez rejoint la party de §d" + (leader != null ? leader.getName() : "???") + "§a! (" + count + "/" + maxSize + ")");
            if (leader != null) {
                leader.sendMessage(plugin.getPrefix() + "§d" + player.getName() + " §aa rejoint la party via la publication!");
            }
            if (party != null) {
                for (UUID m : party.getMembers()) {
                    if (m.equals(player.getUniqueId()) || m.equals(leaderUuid)) continue;
                    Player member = Bukkit.getPlayer(m);
                    if (member != null) {
                        member.sendMessage(plugin.getPrefix() + "§d" + player.getName() + " §aa rejoint la party!");
                    }
                }
            }
        } else {
            player.sendMessage(plugin.getPrefix() + "§cLa party est pleine ou n'est plus disponible.");
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("");
        player.sendMessage("§5§l═══════════════════════════");
        player.sendMessage("§d§lCommandes Party");
        player.sendMessage("");
        player.sendMessage("§d/party create §7- Créer une party");
        player.sendMessage("§d/party invite <joueur> §7- Inviter");
        player.sendMessage("§d/party join §7- Accepter l'invitation");
        player.sendMessage("§d/party leave §7- Quitter la party");
        player.sendMessage("§d/party kick <joueur> §7- Kick un membre");
        player.sendMessage("§d/party disband §7- Dissoudre la party");
        player.sendMessage("§d/party list §7- Liste des membres");
        player.sendMessage("§d/party pub §7- §5[VIP] §7Publier la party");
        player.sendMessage("§5§l═══════════════════════════");
        player.sendMessage("");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "invite", "join", "leave", "kick", "disband", "list", "info", "pub"));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("invite") || sub.equals("kick")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            }
        }
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
