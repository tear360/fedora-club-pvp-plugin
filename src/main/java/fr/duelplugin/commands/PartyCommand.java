package fr.duelplugin.commands;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.managers.LanguageManager;
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
            player.sendMessage(lang().msg(player, "party_already_in"));
            return;
        }
        if (plugin.getPartyManager().createParty(player)) {
            player.sendMessage(lang().msg(player, "party_created"));
        }
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(lang().msg(player, "party_kick_usage"));
            return;
        }

        UUID partyLeader = plugin.getPartyManager().getPartyLeader(player.getUniqueId());
        if (partyLeader == null || !partyLeader.equals(player.getUniqueId())) {
            player.sendMessage(lang().msg(player, "party_leader_only"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(lang().msg(player, "player_not_found"));
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(lang().msg(player, "party_cannot_self_invite"));
            return;
        }

        if (plugin.getPartyManager().isInParty(target.getUniqueId())) {
            player.sendMessage(lang().msg(player, "party_target_already_in", "%player%", target.getName()));
            return;
        }

        if (plugin.getPartyManager().invitePlayer(player, target)) {
            int maxSize = plugin.getPartyManager().getMaxSize(player);
            int count = plugin.getPartyManager().getMemberCount(plugin.getPartyManager().getPartyLeader(player.getUniqueId()));
            player.sendMessage(lang().msg(player, "party_invite_sent", "%player%", target.getName(), "%count%", String.valueOf(count), "%max%", String.valueOf(maxSize)));
            target.sendMessage(lang().msg(target, "party_invite_received", "%player%", player.getName()));
        } else {
            player.sendMessage(lang().msg(player, "party_full", "%max%", String.valueOf(plugin.getPartyManager().getMaxSize(player))));
        }
    }

    private void handleJoin(Player player) {
        if (plugin.getPartyManager().isInParty(player.getUniqueId())) {
            player.sendMessage(lang().msg(player, "party_already_in"));
            return;
        }

        UUID partyLeader = plugin.getPartyManager().getPendingInvite(player.getUniqueId());
        if (partyLeader == null) {
            player.sendMessage(lang().msg(player, "party_no_pending_invite"));
            return;
        }

        PartyManager.Party party = plugin.getPartyManager().getPartyByLeader(partyLeader);
        if (party == null) {
            player.sendMessage(lang().msg(player, "party_invite_expired"));
            return;
        }

        if (plugin.getPartyManager().acceptInvite(player)) {
            Player leader = Bukkit.getPlayer(partyLeader);
            int maxSize = plugin.getPartyManager().getMaxSize(leader);
            player.sendMessage(lang().msg(player, "party_joined", "%player%", (leader != null ? leader.getName() : "???"), "%count%", String.valueOf(party.getSize()), "%max%", String.valueOf(maxSize)));
            if (leader != null) {
                leader.sendMessage(lang().msg(leader, "party_joined_broadcast", "%player%", player.getName()));
            }
            for (UUID m : party.getMembers()) {
                if (m.equals(player.getUniqueId()) || m.equals(partyLeader)) continue;
                Player member = Bukkit.getPlayer(m);
                if (member != null) {
                    member.sendMessage(lang().msg(member, "party_joined_broadcast", "%player%", player.getName()));
                }
            }
        }
    }

    private void handleLeave(Player player) {
        if (!plugin.getPartyManager().isInParty(player.getUniqueId())) {
            player.sendMessage(lang().msg(player, "party_not_in"));
            return;
        }

        boolean wasLeader = plugin.getPartyManager().isLeader(player.getUniqueId());
        PartyManager.Party party = plugin.getPartyManager().getParty(player.getUniqueId());
        UUID partyLeader = plugin.getPartyManager().getPartyLeader(player.getUniqueId());

        plugin.getPartyManager().leaveParty(player);
        player.sendMessage(lang().msg(player, "party_left"));

        if (party != null) {
            for (UUID m : party.getMembers()) {
                if (m.equals(player.getUniqueId())) continue;
                Player member = Bukkit.getPlayer(m);
                if (member != null) {
                    member.sendMessage(lang().msg(member, "party_member_left", "%player%", player.getName()));
                    if (wasLeader) {
                        UUID newLeader = plugin.getPartyManager().getPartyLeader(m);
                        if (newLeader != null) {
                            Player newLeaderPlayer = Bukkit.getPlayer(newLeader);
                            member.sendMessage(lang().msg(member, "party_transfer", "%player%", (newLeaderPlayer != null ? newLeaderPlayer.getName() : "???")));
                        }
                    }
                }
            }
        }
    }

    private void handleKick(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(lang().msg(player, "party_kick_usage"));
            return;
        }

        UUID partyLeader = plugin.getPartyManager().getPartyLeader(player.getUniqueId());
        if (partyLeader == null || !partyLeader.equals(player.getUniqueId())) {
            player.sendMessage(lang().msg(player, "party_kick_only_leader"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(lang().msg(player, "player_not_found"));
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(lang().msg(player, "party_kick_cannot_self"));
            return;
        }

        PartyManager.Party party = plugin.getPartyManager().getParty(player.getUniqueId());
        if (party == null || !party.hasMember(target.getUniqueId())) {
            player.sendMessage(lang().msg(player, "party_kick_not_member", "%player%", target.getName()));
            return;
        }

        if (plugin.getPartyManager().kickPlayer(player, target)) {
            target.sendMessage(lang().msg(target, "party_kick_self"));
            player.sendMessage(lang().msg(player, "party_kick_target", "%player%", target.getName()));
            for (UUID m : party.getMembers()) {
                if (m.equals(player.getUniqueId()) || m.equals(target.getUniqueId())) continue;
                Player member = Bukkit.getPlayer(m);
                if (member != null) {
                    member.sendMessage(lang().msg(member, "party_kicked_broadcast", "%player%", target.getName()));
                }
            }
        }
    }

    private void handleDisband(Player player) {
        if (!plugin.getPartyManager().isLeader(player.getUniqueId())) {
            player.sendMessage(lang().msg(player, "party_disband_only_leader"));
            return;
        }

        PartyManager.Party party = plugin.getPartyManager().getParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage(lang().msg(player, "party_not_in"));
            return;
        }

        for (UUID m : party.getMembers()) {
            Player member = Bukkit.getPlayer(m);
            if (member != null) {
                member.sendMessage(lang().msg(member, "party_disbanded_by_leader"));
                member.showTitle(net.kyori.adventure.title.Title.title(
                        net.kyori.adventure.text.Component.text(lang().msgRaw(member, "title_party_disbanded"), net.kyori.adventure.text.format.NamedTextColor.RED, TextDecoration.BOLD),
                        net.kyori.adventure.text.Component.empty(),
                        net.kyori.adventure.title.Title.Times.times(java.time.Duration.ZERO, java.time.Duration.ofSeconds(2), java.time.Duration.ofSeconds(1))
                ));
            }
        }
        player.showTitle(net.kyori.adventure.title.Title.title(
                net.kyori.adventure.text.Component.text(lang().msgRaw(player, "title_party_disbanded"), net.kyori.adventure.text.format.NamedTextColor.RED, TextDecoration.BOLD),
                net.kyori.adventure.text.Component.empty(),
                net.kyori.adventure.title.Title.Times.times(java.time.Duration.ZERO, java.time.Duration.ofSeconds(2), java.time.Duration.ofSeconds(1))
        ));

        plugin.getPartyManager().disbandParty(player);
        player.sendMessage(lang().msg(player, "party_disbanded"));
    }

    private void handleList(Player player) {
        PartyManager.Party party = plugin.getPartyManager().getParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage(lang().msg(player, "party_not_in"));
            return;
        }

        Player leader = Bukkit.getPlayer(party.getLeader());

        player.sendMessage("");
        player.sendMessage("§5§l═══════════════════════════");
        player.sendMessage(lang().msgRaw(player, "party_title_list", "%count%", String.valueOf(party.getSize()), "%s%", party.getSize() > 1 ? "s" : ""));
        player.sendMessage("");
        player.sendMessage(lang().msgRaw(player, "party_leader_display", "%player%", (leader != null ? leader.getName() : "???")));
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
        if (!plugin.getRankManager().isVip(player.getUniqueId())) {
            player.sendMessage(lang().msg(player, "party_vip_only"));
            return;
        }

        UUID partyLeader = plugin.getPartyManager().getPartyLeader(player.getUniqueId());
        if (partyLeader == null || !partyLeader.equals(player.getUniqueId())) {
            player.sendMessage(lang().msg(player, "party_leader_only"));
            return;
        }

        if (plugin.getPartyManager().isPubOpen(player.getUniqueId())) {
            plugin.getPartyManager().closePub(player);
            player.sendMessage(lang().msg(player, "party_invite_disabled"));
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
                .hoverEvent(HoverEvent.showText(Component.text().append(Component.text(lang().msgRaw(player, "party_pub_hover", "%player%", playerName, "%count%", sizeInfo), NamedTextColor.GREEN)).build()))
                .clickEvent(ClickEvent.runCommand("/party pubjoin " + leaderUuid.toString()));

        Component message = Component.text()
                .append(Component.text(lang().msgRaw(player, "party_pub_chat") + " ", NamedTextColor.GRAY))
                .append(Component.text(playerName, NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
                .append(Component.text(" : ", NamedTextColor.GRAY))
                .append(joinButton)
                .build();

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (plugin.getDuelManager().isInDuel(online)) continue;
            if (online.getWorld() != player.getWorld()) continue;
            online.sendMessage(message);
        }
        player.sendMessage(lang().msg(player, "party_pub_success"));
    }

    private void handlePubJoin(Player player, String[] args) {
        if (args.length < 2) return;
        if (plugin.getPartyManager().isInParty(player.getUniqueId())) {
            player.sendMessage(lang().msg(player, "party_already_in"));
            return;
        }

        UUID leaderUuid;
        try {
            leaderUuid = UUID.fromString(args[1]);
        } catch (IllegalArgumentException e) {
            return;
        }

        if (!plugin.getPartyManager().isPubOpen(leaderUuid)) {
            player.sendMessage(lang().msg(player, "party_no_pub_available"));
            return;
        }

        if (plugin.getPartyManager().pubJoin(player, leaderUuid)) {
            Player leader = Bukkit.getPlayer(leaderUuid);
            PartyManager.Party party = plugin.getPartyManager().getParty(leaderUuid);
            int maxSize = plugin.getPartyManager().getMaxSize(leader);
            int count = party != null ? party.getSize() : 1;
            player.sendMessage(lang().msg(player, "party_joined", "%player%", (leader != null ? leader.getName() : "???"), "%count%", String.valueOf(count), "%max%", String.valueOf(maxSize)));
            if (leader != null) {
                leader.sendMessage(lang().msg(leader, "party_joined_pub_broadcast", "%player%", player.getName()));
            }
            if (party != null) {
                for (UUID m : party.getMembers()) {
                    if (m.equals(player.getUniqueId()) || m.equals(leaderUuid)) continue;
                    Player member = Bukkit.getPlayer(m);
                    if (member != null) {
                        member.sendMessage(lang().msg(member, "party_joined_broadcast", "%player%", player.getName()));
                    }
                }
            }
        } else {
            player.sendMessage(lang().msg(player, "party_pub_full"));
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("");
        player.sendMessage("§5§l═══════════════════════════");
        player.sendMessage(lang().msgRaw(player, "party_help_title"));
        player.sendMessage("");
        player.sendMessage(lang().msgRaw(player, "party_help_create"));
        player.sendMessage(lang().msgRaw(player, "party_help_invite"));
        player.sendMessage(lang().msgRaw(player, "party_help_join"));
        player.sendMessage(lang().msgRaw(player, "party_help_leave"));
        player.sendMessage(lang().msgRaw(player, "party_help_kick"));
        player.sendMessage(lang().msgRaw(player, "party_help_disband"));
        player.sendMessage(lang().msgRaw(player, "party_help_list"));
        player.sendMessage(lang().msgRaw(player, "party_help_pub"));
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
