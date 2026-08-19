package fr.duelplugin.gui;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.managers.PartyManager;
import fr.duelplugin.utils.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map;
import java.util.UUID;

public class PartyGUI {

    private final DuelPlugin plugin;

    public PartyGUI(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    public void openPartyMenu(Player player) {
        UUID uuid = player.getUniqueId();
        boolean inParty = plugin.getPartyManager().isInParty(uuid);
        boolean isLeader = plugin.getPartyManager().isLeader(uuid);

        PartyManager.Party party = plugin.getPartyManager().getParty(uuid);

        if (!inParty) {
            openNoPartyGUI(player);
        } else if (isLeader) {
            openLeaderGUI(player, party);
        } else {
            openMemberGUI(player, party);
        }
    }

    private void openNoPartyGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text("Party", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));

        fillGlass(inv, 27);

        inv.setItem(13, new ItemBuilder(Material.NETHER_STAR)
                .name("§a§lCréer une party")
                .lore("", "§7Créez votre propre party", "§7pour jouer avec vos amis.", "", "§a&lCliquez pour créer")
                .build());

        UUID pendingInvite = plugin.getPartyManager().getPendingInvite(player.getUniqueId());
        if (pendingInvite != null) {
            PartyManager.Party party = plugin.getPartyManager().getPartyByLeader(pendingInvite);
            if (party != null) {
                Player leader = Bukkit.getPlayer(pendingInvite);
                String leaderName = leader != null ? leader.getName() : "???";
                inv.setItem(11, new ItemBuilder(Material.GREEN_WOOL)
                        .name("§a§lRejoindre la party")
                        .lore("", "§7Invité par §d" + leaderName, "§7Membres: §f" + party.getSize(), "", "§a&lCliquez pour accepter")
                        .build());
                inv.setItem(15, new ItemBuilder(Material.RED_WOOL)
                        .name("§c§lRefuser l'invitation")
                        .lore("", "§7Refuser l'invitation de §d" + leaderName, "", "§c&lCliquez pour refuser")
                        .build());
            }
        }

        player.openInventory(inv);
    }

    private void openLeaderGUI(Player player, PartyManager.Party party) {
        Inventory inv = Bukkit.createInventory(null, 54,
                Component.text().append(Component.text("Party (Leader)", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD)).build());

        fillGlass(inv, 54);

        inv.setItem(10, new ItemBuilder(Material.PLAYER_HEAD)
                .name("§d§lInviter un joueur")
                .lore("", "§7Invitez un joueur en ligne", "§7dans votre party", "", "§d&lCliquez pour inviter")
                .build());

        inv.setItem(12, new ItemBuilder(Material.TRIDENT)
                .name("§d§lTransférer le leadership")
                .lore("", "§7Transférez la direction", "§7de la party à un membre", "", "§d&lCliquez pour transférer")
                .build());

        inv.setItem(14, new ItemBuilder(Material.RED_WOOL)
                .name("§c§lDissoudre la party")
                .lore("", "§7Dissout la party et", "§7expulse tous les membres", "", "§c&lCliquez pour dissoudre")
                .build());

        inv.setItem(16, new ItemBuilder(Material.BARRIER)
                .name("§c§lQuitter la party")
                .lore("", "§7Quittez votre propre party", "§7(Transfert au 1er membre)", "", "§c&lCliquez pour quitter")
                .build());

        int memberSlot = 28;
        Player leader = Bukkit.getPlayer(party.getLeader());
        if (leader != null) {
            inv.setItem(4, createPlayerHead(leader, "§6👑 §f" + leader.getName() + " §7(Leader)"));
        }
        for (UUID m : party.getMembers()) {
            if (memberSlot >= 34) break;
            Player member = Bukkit.getPlayer(m);
            if (member != null) {
                inv.setItem(memberSlot, createPlayerHead(member, "§7» §f" + member.getName()));
                memberSlot++;
            }
        }

        inv.setItem(49, new ItemBuilder(Material.ARROW).name("§d§lRetour").lore("", "§7Retour au lobby").build());

        player.openInventory(inv);
    }

    private void openMemberGUI(Player player, PartyManager.Party party) {
        Inventory inv = Bukkit.createInventory(null, 45,
                Component.text("Party", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));

        fillGlass(inv, 45);

        inv.setItem(13, new ItemBuilder(Material.BARRIER)
                .name("§c§lQuitter la party")
                .lore("", "§7Quittez la party", "", "§c&lCliquez pour quitter")
                .build());

        Player leader = Bukkit.getPlayer(party.getLeader());
        if (leader != null) {
            inv.setItem(4, createPlayerHead(leader, "§6👑 §f" + leader.getName() + " §7(Leader)"));
        }

        int memberSlot = 19;
        for (UUID m : party.getMembers()) {
            if (memberSlot >= 25) break;
            Player member = Bukkit.getPlayer(m);
            if (member != null) {
                inv.setItem(memberSlot, createPlayerHead(member, "§7» §f" + member.getName()));
                memberSlot++;
            }
        }

        inv.setItem(40, new ItemBuilder(Material.ARROW).name("§d§lRetour").lore("", "§7Retour au lobby").build());

        player.openInventory(inv);
    }

    public void openKickSelector(Player player) {
        PartyManager.Party party = plugin.getPartyManager().getParty(player.getUniqueId());
        if (party == null) return;

        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text("Kick un membre", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));

        fillGlass(inv, 27);

        int slot = 10;
        for (UUID m : party.getMembers()) {
            if (slot >= 17) break;
            Player member = Bukkit.getPlayer(m);
            if (member != null) {
                inv.setItem(slot, createPlayerHead(member, "§c§lKick §f" + member.getName()));
                slot++;
            }
        }

        inv.setItem(22, new ItemBuilder(Material.ARROW).name("§d§lRetour").lore("", "§7Retour à la party").build());

        player.openInventory(inv);
    }

    public void openTransferSelector(Player player) {
        PartyManager.Party party = plugin.getPartyManager().getParty(player.getUniqueId());
        if (party == null) return;

        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text("Transférer le leadership", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));

        fillGlass(inv, 27);

        int slot = 10;
        for (UUID m : party.getMembers()) {
            if (slot >= 17) break;
            Player member = Bukkit.getPlayer(m);
            if (member != null) {
                inv.setItem(slot, createPlayerHead(member, "§d§lTransférer à §f" + member.getName()));
                slot++;
            }
        }

        inv.setItem(22, new ItemBuilder(Material.ARROW).name("§d§lRetour").lore("", "§7Retour à la party").build());

        player.openInventory(inv);
    }

    private ItemStack createPlayerHead(Player player, String name) {
        ItemStack head = new ItemBuilder(Material.PLAYER_HEAD).name(name).build();
        if (head.getItemMeta() instanceof SkullMeta meta) {
            meta.setOwningPlayer(player);
            head.setItemMeta(meta);
        }
        return head;
    }

    private void fillGlass(Inventory inv, int size) {
        for (int i = 0; i < size; i++) {
            inv.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());
        }
    }
}
