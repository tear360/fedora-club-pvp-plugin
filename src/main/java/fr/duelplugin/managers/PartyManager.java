package fr.duelplugin.managers;

import fr.duelplugin.DuelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PartyManager {

    public static final int MAX_SIZE_NORMAL = 10;
    public static final int MAX_SIZE_VIP = 20;

    private final DuelPlugin plugin;
    private final File partyFile;
    private final FileConfiguration partyConfig;

    private final Map<UUID, Party> parties = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerParty = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> pendingInvites = new ConcurrentHashMap<>();
    private final Set<UUID> pubParties = ConcurrentHashMap.newKeySet();

    public PartyManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.partyFile = new File(plugin.getDataFolder(), "parties.yml");
        if (!partyFile.exists()) {
            try { partyFile.createNewFile(); } catch (IOException ignored) {}
        }
        this.partyConfig = YamlConfiguration.loadConfiguration(partyFile);
        loadAll();
    }

    private void loadAll() {
        parties.clear();
        playerParty.clear();
        if (!partyConfig.contains("parties")) return;

        for (String partyId : partyConfig.getConfigurationSection("parties").getKeys(false)) {
            UUID leader = UUID.fromString(partyConfig.getString("parties." + partyId + ".leader"));
            List<String> memberStrings = partyConfig.getStringList("parties." + partyId + ".members");
            Set<UUID> members = new HashSet<>();
            for (String ms : memberStrings) {
                try { members.add(UUID.fromString(ms)); } catch (IllegalArgumentException ignored) {}
            }
            Party party = new Party(leader, members);
            parties.put(leader, party);
            playerParty.put(leader, leader);
            for (UUID m : members) {
                playerParty.put(m, leader);
            }
        }
    }

    public boolean createParty(Player leader) {
        UUID uuid = leader.getUniqueId();
        if (playerParty.containsKey(uuid)) return false;

        Party party = new Party(uuid, new HashSet<>());
        parties.put(uuid, party);
        playerParty.put(uuid, uuid);
        save();
        return true;
    }

    public boolean invitePlayer(Player inviter, Player target) {
        UUID inviterUuid = inviter.getUniqueId();
        UUID targetUuid = target.getUniqueId();

        UUID partyLeader = playerParty.get(inviterUuid);
        if (partyLeader == null) return false;
        if (!partyLeader.equals(inviterUuid)) return false;
        if (playerParty.containsKey(targetUuid)) return false;

        Party party = parties.get(partyLeader);
        if (party != null && party.isFull(inviter)) return false;

        pendingInvites.put(targetUuid, partyLeader);
        return true;
    }

    public boolean acceptInvite(Player player) {
        UUID uuid = player.getUniqueId();
        UUID partyLeader = pendingInvites.remove(uuid);
        if (partyLeader == null) return false;

        Party party = parties.get(partyLeader);
        if (party == null) return false;

        Player leader = Bukkit.getPlayer(partyLeader);
        if (party.isFull(leader)) return false;

        party.addMember(uuid);
        playerParty.put(uuid, partyLeader);
        save();
        return true;
    }

    public boolean openPub(Player leader) {
        UUID uuid = leader.getUniqueId();
        if (!isLeader(uuid)) return false;
        pubParties.add(uuid);
        return true;
    }

    public boolean closePub(Player leader) {
        return pubParties.remove(leader.getUniqueId());
    }

    public boolean isPubOpen(UUID leaderUuid) {
        return pubParties.contains(leaderUuid);
    }

    public boolean pubJoin(Player player, UUID leaderUuid) {
        UUID uuid = player.getUniqueId();
        if (playerParty.containsKey(uuid)) return false;
        if (!pubParties.contains(leaderUuid)) return false;

        Party party = parties.get(leaderUuid);
        if (party == null) return false;

        Player leader = Bukkit.getPlayer(leaderUuid);
        if (party.isFull(leader)) return false;

        party.addMember(uuid);
        playerParty.put(uuid, leaderUuid);
        save();
        return true;
    }

    public int getMaxSize(Player leader) {
        if (leader != null && plugin.getVipManager().isVip(leader.getUniqueId())) {
            return MAX_SIZE_VIP;
        }
        return MAX_SIZE_NORMAL;
    }

    public int getMemberCount(UUID leaderUuid) {
        Party party = parties.get(leaderUuid);
        return party != null ? party.getSize() : 0;
    }

    public void declineInvite(Player player) {
        pendingInvites.remove(player.getUniqueId());
    }

    public boolean leaveParty(Player player) {
        UUID uuid = player.getUniqueId();
        UUID partyLeader = playerParty.remove(uuid);
        if (partyLeader == null) return false;

        Party party = parties.get(partyLeader);
        if (party == null) return false;

        party.removeMember(uuid);

        if (uuid.equals(partyLeader)) {
            if (party.getMembers().isEmpty()) {
                parties.remove(partyLeader);
            } else {
                UUID newLeader = party.getMembers().iterator().next();
                Party newParty = new Party(newLeader, party.getMembers());
                newParty.removeMember(newLeader);
                parties.remove(partyLeader);
                parties.put(newLeader, newParty);
                playerParty.put(newLeader, newLeader);
                for (UUID m : newParty.getMembers()) {
                    playerParty.put(m, newLeader);
                }
            }
        }

        save();
        return true;
    }

    public boolean kickPlayer(Player leader, Player target) {
        UUID leaderUuid = leader.getUniqueId();
        UUID targetUuid = target.getUniqueId();

        UUID partyLeader = playerParty.get(leaderUuid);
        if (partyLeader == null || !partyLeader.equals(leaderUuid)) return false;

        Party party = parties.get(leaderUuid);
        if (party == null || !party.hasMember(targetUuid)) return false;

        party.removeMember(targetUuid);
        playerParty.remove(targetUuid);
        save();
        return true;
    }

    public boolean disbandParty(Player leader) {
        UUID leaderUuid = leader.getUniqueId();
        if (!playerParty.containsKey(leaderUuid) || !playerParty.get(leaderUuid).equals(leaderUuid)) return false;

        Party party = parties.remove(leaderUuid);
        if (party == null) return false;

        playerParty.remove(leaderUuid);
        for (UUID m : party.getMembers()) {
            playerParty.remove(m);
        }

        save();
        return true;
    }

    public boolean transferLeadership(Player leader, Player newLeader) {
        UUID leaderUuid = leader.getUniqueId();
        UUID newLeaderUuid = newLeader.getUniqueId();

        UUID partyLeader = playerParty.get(leaderUuid);
        if (partyLeader == null || !partyLeader.equals(leaderUuid)) return false;

        Party party = parties.get(leaderUuid);
        if (party == null || !party.hasMember(newLeaderUuid)) return false;

        party.removeMember(newLeaderUuid);
        Set<UUID> oldMembers = new HashSet<>(party.getMembers());
        oldMembers.add(leaderUuid);

        parties.remove(leaderUuid);

        Party newParty = new Party(newLeaderUuid, oldMembers);
        parties.put(newLeaderUuid, newParty);

        playerParty.put(newLeaderUuid, newLeaderUuid);
        for (UUID m : oldMembers) {
            playerParty.put(m, newLeaderUuid);
        }

        save();
        return true;
    }

    public Party getParty(UUID playerUuid) {
        UUID leader = playerParty.get(playerUuid);
        if (leader == null) return null;
        return parties.get(leader);
    }

    public Party getPartyByLeader(UUID leaderUuid) {
        return parties.get(leaderUuid);
    }

    public UUID getPartyLeader(UUID playerUuid) {
        return playerParty.get(playerUuid);
    }

    public boolean isInParty(UUID playerUuid) {
        return playerParty.containsKey(playerUuid);
    }

    public boolean isLeader(UUID playerUuid) {
        return playerParty.containsKey(playerUuid) && playerParty.get(playerUuid).equals(playerUuid);
    }

    public UUID getPendingInvite(UUID playerUuid) {
        return pendingInvites.get(playerUuid);
    }

    public List<Party> getAllParties() {
        return new ArrayList<>(parties.values());
    }

    private void save() {
        partyConfig.set("parties", null);
        for (Map.Entry<UUID, Party> entry : parties.entrySet()) {
            String path = "parties." + entry.getKey().toString();
            partyConfig.set(path + ".leader", entry.getValue().getLeader().toString());
            List<String> memberStrings = new ArrayList<>();
            for (UUID m : entry.getValue().getMembers()) {
                memberStrings.add(m.toString());
            }
            partyConfig.set(path + ".members", memberStrings);
        }
        try {
            partyConfig.save(partyFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save parties.yml");
        }
    }

    public static class Party {
        private final UUID leader;
        private final Set<UUID> members;

        public Party(UUID leader, Set<UUID> members) {
            this.leader = leader;
            this.members = new HashSet<>(members);
        }

        public UUID getLeader() { return leader; }
        public Set<UUID> getMembers() { return Collections.unmodifiableSet(members); }
        public int getSize() { return members.size() + 1; }

        public boolean isFull(Player leader) {
            int maxSize = (leader != null && leader.hasPermission("duelplugin.vip")) ? MAX_SIZE_VIP : MAX_SIZE_NORMAL;
            return getSize() >= maxSize;
        }

        public boolean hasMember(UUID uuid) {
            return uuid.equals(leader) || members.contains(uuid);
        }

        public void addMember(UUID uuid) {
            if (!uuid.equals(leader)) members.add(uuid);
        }

        public void removeMember(UUID uuid) {
            members.remove(uuid);
        }
    }
}
