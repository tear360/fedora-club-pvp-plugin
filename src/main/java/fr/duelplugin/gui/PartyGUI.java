package fr.duelplugin.gui;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.managers.PartyManager;
import fr.duelplugin.models.DuelGameMode;
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
                .name(plugin.getLanguageManager().msgRaw(player, "gui_party_create"))
                .lore("", plugin.getLanguageManager().msgRaw(player, "gui_party_create_lore1"), plugin.getLanguageManager().msgRaw(player, "gui_party_create_lore2"), "", plugin.getLanguageManager().msgRaw(player, "gui_party_create_click"))
                .build());

        UUID pendingInvite = plugin.getPartyManager().getPendingInvite(player.getUniqueId());
        if (pendingInvite != null) {
            PartyManager.Party party = plugin.getPartyManager().getPartyByLeader(pendingInvite);
            if (party != null) {
                Player leader = Bukkit.getPlayer(pendingInvite);
                String leaderName = leader != null ? leader.getName() : "???";
                inv.setItem(11, new ItemBuilder(Material.GREEN_WOOL)
                        .name(plugin.getLanguageManager().msgRaw(player, "gui_party_join"))
                        .lore("", plugin.getLanguageManager().msgRaw(player, "gui_party_join_lore1") + " §d" + leaderName, plugin.getLanguageManager().msgRaw(player, "gui_party_join_lore2") + " §f" + party.getSize(), "", plugin.getLanguageManager().msgRaw(player, "gui_party_join_click"))
                        .build());
                inv.setItem(15, new ItemBuilder(Material.RED_WOOL)
                        .name(plugin.getLanguageManager().msgRaw(player, "gui_party_decline"))
                        .lore("", plugin.getLanguageManager().msgRaw(player, "gui_party_decline_lore1") + " §d" + leaderName, "", plugin.getLanguageManager().msgRaw(player, "gui_party_decline_click"))
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
                .name(plugin.getLanguageManager().msgRaw(player, "gui_party_leader_invite"))
                .lore("", plugin.getLanguageManager().msgRaw(player, "gui_party_leader_invite_lore1"), plugin.getLanguageManager().msgRaw(player, "gui_party_leader_invite_lore2"), "", plugin.getLanguageManager().msgRaw(player, "gui_party_leader_invite_click"))
                .build());

        inv.setItem(11, new ItemBuilder(Material.NETHERITE_SWORD)
                .name(plugin.getLanguageManager().msgRaw(player, "gui_party_leader_ffa"))
                .lore("", plugin.getLanguageManager().msgRaw(player, "gui_party_leader_ffa_lore1"), plugin.getLanguageManager().msgRaw(player, "gui_party_leader_ffa_lore2"), plugin.getLanguageManager().msgRaw(player, "gui_party_leader_ffa_lore3"), "", plugin.getLanguageManager().msgRaw(player, "gui_party_leader_ffa_click"))
                .build());

        inv.setItem(12, new ItemBuilder(Material.TRIDENT)
                .name(plugin.getLanguageManager().msgRaw(player, "gui_party_leader_transfer"))
                .lore("", plugin.getLanguageManager().msgRaw(player, "gui_party_leader_transfer_lore1"), plugin.getLanguageManager().msgRaw(player, "gui_party_leader_transfer_lore2"), "", plugin.getLanguageManager().msgRaw(player, "gui_party_leader_transfer_click"))
                .build());

        inv.setItem(14, new ItemBuilder(Material.RED_WOOL)
                .name(plugin.getLanguageManager().msgRaw(player, "gui_party_leader_disband"))
                .lore("", plugin.getLanguageManager().msgRaw(player, "gui_party_leader_disband_lore1"), plugin.getLanguageManager().msgRaw(player, "gui_party_leader_disband_lore2"), "", plugin.getLanguageManager().msgRaw(player, "gui_party_leader_disband_click"))
                .build());

        inv.setItem(16, new ItemBuilder(Material.BARRIER)
                .name(plugin.getLanguageManager().msgRaw(player, "gui_party_leader_leave"))
                .lore("", plugin.getLanguageManager().msgRaw(player, "gui_party_leader_leave_lore1"), plugin.getLanguageManager().msgRaw(player, "gui_party_leader_leave_lore2"), "", plugin.getLanguageManager().msgRaw(player, "gui_party_leader_leave_click"))
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

        inv.setItem(49, new ItemBuilder(Material.ARROW).name(plugin.getLanguageManager().msgRaw(player, "gui_back")).lore("", plugin.getLanguageManager().msgRaw(player, "gui_back_lobby")).build());

        player.openInventory(inv);
    }

    private void openMemberGUI(Player player, PartyManager.Party party) {
        Inventory inv = Bukkit.createInventory(null, 45,
                Component.text("Party", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));

        fillGlass(inv, 45);

        inv.setItem(13, new ItemBuilder(Material.BARRIER)
                .name(plugin.getLanguageManager().msgRaw(player, "gui_party_member_leave"))
                .lore("", plugin.getLanguageManager().msgRaw(player, "gui_party_member_leave_lore"), "", plugin.getLanguageManager().msgRaw(player, "gui_party_member_leave_click"))
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

        inv.setItem(40, new ItemBuilder(Material.ARROW).name(plugin.getLanguageManager().msgRaw(player, "gui_back")).lore("", plugin.getLanguageManager().msgRaw(player, "gui_back_lobby")).build());

        player.openInventory(inv);
    }

    public void openKickSelector(Player player) {
        PartyManager.Party party = plugin.getPartyManager().getParty(player.getUniqueId());
        if (party == null) return;

        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text(plugin.getLanguageManager().msgRaw(player, "gui_party_kick"), NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));

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

        inv.setItem(22, new ItemBuilder(Material.ARROW).name(plugin.getLanguageManager().msgRaw(player, "gui_back")).lore("", plugin.getLanguageManager().msgRaw(player, "gui_back_party")).build());

        player.openInventory(inv);
    }

    public void openFFASelector(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text(plugin.getLanguageManager().msgRaw(player, "gui_party_ffa"), NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));

        fillGlass(inv, 27);

        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        int[] icons = {0, 1, 2, 3, 4, 5, 6};
        DuelGameMode[] modes = DuelGameMode.values();

        for (int i = 0; i < modes.length && i < slots.length; i++) {
            DuelGameMode mode = modes[i];
            boolean hasArena = !mode.isArenaRestricted() || plugin.getArenaManager().getAvailableArena(mode) != null;
            Material icon = switch (mode) {
                case SWORD -> Material.DIAMOND_SWORD;
                case AXE -> Material.DIAMOND_AXE;
                case UHC -> Material.GOLDEN_APPLE;
                case MACE -> Material.MACE;
                case SMP -> Material.SHIELD;
                case DIASMP -> Material.CHORUS_FRUIT;
                case POT -> Material.SPLASH_POTION;
                case NETHPOT -> Material.NETHERITE_HELMET;
                case VANILLA -> Material.NETHERITE_SWORD;
            };
            inv.setItem(slots[i], new ItemBuilder(icon)
                    .name(mode.getColoredName())
                    .lore("",
                            mode.isArenaRestricted() ?
                                    (hasArena ? plugin.getLanguageManager().msgRaw(player, "gui_arenas_available") : plugin.getLanguageManager().msgRaw(player, "gui_no_arena")) :
                                    plugin.getLanguageManager().msgRaw(player, "gui_free_mode"),
                            plugin.getLanguageManager().msgRaw(player, "gui_party_ffa_launch"),
                            plugin.getLanguageManager().msgRaw(player, "gui_party_ffa_with_mode"), "",
                            hasArena ? plugin.getLanguageManager().msgRaw(player, "gui_party_ffa_click_launch") : plugin.getLanguageManager().msgRaw(player, "gui_unavailable"))
                    .build());
        }

        inv.setItem(22, new ItemBuilder(Material.ARROW).name(plugin.getLanguageManager().msgRaw(player, "gui_back")).lore("", plugin.getLanguageManager().msgRaw(player, "gui_back_party")).build());

        player.openInventory(inv);
    }

    public void openTransferSelector(Player player) {
        PartyManager.Party party = plugin.getPartyManager().getParty(player.getUniqueId());
        if (party == null) return;

        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text(plugin.getLanguageManager().msgRaw(player, "gui_party_transfer"), NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));

        fillGlass(inv, 27);

        int slot = 10;
        for (UUID m : party.getMembers()) {
            if (slot >= 17) break;
            Player member = Bukkit.getPlayer(m);
            if (member != null) {
                inv.setItem(slot, createPlayerHead(member, plugin.getLanguageManager().msgRaw(player, "gui_party_transfer_to") + " §f" + member.getName()));
                slot++;
            }
        }

        inv.setItem(22, new ItemBuilder(Material.ARROW).name(plugin.getLanguageManager().msgRaw(player, "gui_back")).lore("", plugin.getLanguageManager().msgRaw(player, "gui_back_party")).build());

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
