package fr.duelplugin.listeners;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.DuelGameMode;
import fr.duelplugin.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LobbyItemListener implements Listener {

    private final DuelPlugin plugin;
    private final Map<UUID, Inventory> queueGUIs = new HashMap<>();

    public LobbyItemListener(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDuelManager().isInDuel(player)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.NETHERITE_SWORD) {
            event.setCancelled(true);
            openQueueGUI(player);
        } else if (item.getType() == Material.CRAFTING_TABLE) {
            event.setCancelled(true);
            plugin.getKitEditorGUI().openModeSelector(player);
        } else if (item.getType() == Material.NETHER_STAR) {
            event.setCancelled(true);
            plugin.getPartyGUI().openPartyMenu(player);
        }
    }

    private void openQueueGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45,
                net.kyori.adventure.text.Component.text(plugin.getLanguageManager().msgRaw(player, "queue_title"),
                        net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE, net.kyori.adventure.text.format.TextDecoration.BOLD));

        for (int i = 0; i < 45; i++) {
            inv.setItem(i, new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).name(" ").build());
        }

        int[] border = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,37,38,39,40,41,42,43,44};
        for (int slot : border) {
            inv.setItem(slot, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());
        }

        DuelGameMode[] modes = DuelGameMode.values();
        int[] slots = {10, 11, 12, 13, 14, 19, 20, 21};

        for (int i = 0; i < modes.length && i < slots.length; i++) {
            DuelGameMode mode = modes[i];
            int queueSize = plugin.getQueueManager().getQueueSize(mode);
            boolean hasArena = !mode.isArenaRestricted() || plugin.getArenaManager().getAvailableArena(mode) != null;

            Material icon = getModeIcon(mode);

            inv.setItem(slots[i], new ItemBuilder(icon)
                    .name(mode.getColoredName())
                    .lore(
                            "",
                            mode.isArenaRestricted() ?
                                    (hasArena ? plugin.getLanguageManager().msgRaw(player, "gui_arenas_available") : plugin.getLanguageManager().msgRaw(player, "gui_no_arena")) :
                                    plugin.getLanguageManager().msgRaw(player, "gui_free_mode"),
                            plugin.getLanguageManager().msgRaw(player, "gui_blocks") + (mode.canBreakBlocks() ? plugin.getLanguageManager().msgRaw(player, "gui_blocks_breakable") : plugin.getLanguageManager().msgRaw(player, "gui_blocks_unbreakable")),
                            "&dQueue: &f" + queueSize + " joueur" + (queueSize != 1 ? "s" : ""),
                            "",
                            plugin.getLanguageManager().msgRaw(player, "gui_click_to_play")
                    ).build());
        }

        queueGUIs.put(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        Material clickedType = event.getCurrentItem().getType();
        if (clickedType == Material.BLACK_STAINED_GLASS_PANE || clickedType == Material.PURPLE_STAINED_GLASS_PANE) return;

        Material topIcon = null;
        if (event.getView().getTopInventory() != null && event.getView().getTopInventory().getItem(10) != null) {
            topIcon = event.getView().getTopInventory().getItem(10).getType();
        }

        boolean isQueueGUI = topIcon != null && isModeIcon(topIcon);
        boolean isDuelGUI = plugin.getDuelGUI().peekPendingTarget(player.getUniqueId()) != null || hasDuelGUITitle(event);
        boolean isKitEditorModeSelect = !isQueueGUI && !isDuelGUI && clickedType != Material.AIR && isModeIcon(clickedType);

        if (queueGUIs.containsKey(player.getUniqueId()) && event.getView().getTopInventory() == queueGUIs.get(player.getUniqueId())) {
            handleQueueClick(event, player);
            return;
        }

        String cleanTitle = event.getView().getTitle().replaceAll("§[0-9a-fk-or]", "");

        if (cleanTitle.equals("Party") || cleanTitle.contains("Party (Leader)") || cleanTitle.equals("Party")) {
            handlePartyClick(event, player, cleanTitle);
            return;
        }

        if (cleanTitle.equals("Kick un membre")) {
            handleKickClick(event, player);
            return;
        }

        if (cleanTitle.equals("Transférer le leadership")) {
            handleTransferClick(event, player);
            return;
        }

        if (cleanTitle.equals("Choisir un mode FFA")) {
            handleFFAClick(event, player);
            return;
        }

        if (cleanTitle.contains("Kit Editor") || cleanTitle.contains("Éditeur de kits")) {
            handleKitEditorModeSelect(event, player);
            return;
        }

        if (cleanTitle.contains("Kit ")) {
            handleKitEditorClick(event, player);
            return;
        }

        if (cleanTitle.contains("Choisir une pièce") || cleanTitle.contains("Select Armor Piece")) {
            handleArmorPieceClick(event, player);
            return;
        }

        if (cleanTitle.equals("Choisir un pattern") || cleanTitle.equals("Select Pattern")) {
            handlePatternClick(event, player);
            return;
        }

        if (cleanTitle.equals("Choisir un matériau") || cleanTitle.equals("Select Material")) {
            handleMaterialClick(event, player);
            return;
        }

        if (cleanTitle.contains("Sélection de mode") || cleanTitle.contains("Mode Selection")
                || cleanTitle.contains("Défi →") || cleanTitle.contains("Challenge →")) {
            handleDuelGUIClick(event, player, cleanTitle);
            return;
        }
    }

    private void handleDuelGUIClick(InventoryClickEvent event, Player player, String cleanTitle) {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        if (item.getType() == Material.BLACK_STAINED_GLASS_PANE || item.getType() == Material.PURPLE_STAINED_GLASS_PANE) return;

        String name = item.getItemMeta() != null ? item.getItemMeta().getDisplayName() : "";
        for (DuelGameMode mode : DuelGameMode.values()) {
            if (name.contains(mode.getDisplayName())) {
                if (cleanTitle.contains("Défi →") || cleanTitle.contains("Challenge →")) {
                    UUID targetUuid = plugin.getDuelGUI().getPendingTarget(player.getUniqueId());
                    if (targetUuid != null) {
                        Player target = org.bukkit.Bukkit.getPlayer(targetUuid);
                        if (target != null) {
                            if (mode.isArenaRestricted() && plugin.getArenaManager().getAvailableArena(mode) == null) {
                                player.sendMessage(plugin.getLanguageManager().msg(player, "queue_no_arena", "%mode%", mode.getDisplayName()));
                                player.closeInventory();
                                return;
                            }
                            if (plugin.getDuelManager().isInDuel(player) || plugin.getDuelManager().isInDuel(target)) {
                                player.sendMessage(plugin.getLanguageManager().msg(player, "duel_both_in_duel"));
                                player.closeInventory();
                                return;
                            }
                            if (plugin.getDuelManager().sendRequest(player, target, mode)) {
                                player.sendMessage(plugin.getLanguageManager().msg(player, "duel_sent", "%player%", target.getName()));
                            } else {
                                player.sendMessage(plugin.getLanguageManager().msg(player, "duel_accept_fail"));
                            }
                        } else {
                            player.sendMessage(plugin.getLanguageManager().msg(player, "duel_target_online"));
                        }
                    }
                    player.closeInventory();
                }
                break;
            }
        }
    }

    private boolean hasDuelGUITitle(InventoryClickEvent event) {
        String cleanTitle = event.getView().getTitle().replaceAll("§[0-9a-fk-or]", "");
        return cleanTitle.contains("Sélection de mode") || cleanTitle.contains("Mode Selection")
                || cleanTitle.contains("Défi →") || cleanTitle.contains("Challenge →");
    }

    private void handleQueueClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        if (item.getType() == Material.BLACK_STAINED_GLASS_PANE || item.getType() == Material.PURPLE_STAINED_GLASS_PANE) return;

        String name = item.getItemMeta() != null ? item.getItemMeta().getDisplayName() : "";
        for (DuelGameMode mode : DuelGameMode.values()) {
            if (name.contains(mode.getDisplayName())) {
                if (plugin.getQueueManager().isInQueue(player, mode)) {
                    plugin.getQueueManager().leaveQueue(player, mode);
                    player.sendMessage(plugin.getLanguageManager().msg(player, "queue_left", "%mode%", mode.getDisplayName()));
                    openQueueGUI(player);
                    return;
                }
                if (mode.isArenaRestricted() && plugin.getArenaManager().getAvailableArena(mode) == null) {
                    player.sendMessage(plugin.getLanguageManager().msg(player, "queue_no_arena", "%mode%", mode.getDisplayName()));
                    return;
                }
                if (plugin.getQueueManager().isInAnyQueue(player)) {
                    plugin.getQueueManager().leaveQueue(player);
                }
                plugin.getQueueManager().joinQueue(player, mode);
                player.sendMessage(plugin.getLanguageManager().msg(player, "queue_joined", "%mode%", mode.getDisplayName()));
                player.closeInventory();
                return;
            }
        }
    }

    private void handlePartyClick(InventoryClickEvent event, Player player, String cleanTitle) {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        if (item.getType() == Material.BLACK_STAINED_GLASS_PANE || item.getType() == Material.PURPLE_STAINED_GLASS_PANE) return;

        Material type = item.getType();

        if (cleanTitle.equals("Party")) {
            if (type == Material.NETHER_STAR) {
                if (plugin.getPartyManager().createParty(player)) {
                    player.closeInventory();
                    player.sendMessage(plugin.getLanguageManager().msg(player, "party_created"));
                } else {
                    player.sendMessage(plugin.getLanguageManager().msg(player, "party_invite_expired"));
                }
                return;
            }
            if (type == Material.GREEN_WOOL) {
                UUID leaderUuid = plugin.getPartyManager().getPendingInvite(player.getUniqueId());
                if (leaderUuid != null) {
                    plugin.getPartyManager().acceptInvite(player);
                    player.closeInventory();
                    player.sendMessage(plugin.getLanguageManager().msg(player, "party_joined_broadcast", "%player%", player.getName()));
                }
                return;
            }
            if (type == Material.RED_WOOL) {
                plugin.getPartyManager().declineInvite(player);
                player.closeInventory();
                player.sendMessage(plugin.getLanguageManager().msg(player, "friend_deny_refused"));
                return;
            }
            if (type == Material.BARRIER) {
                plugin.getPartyManager().leaveParty(player);
                player.closeInventory();
                player.sendMessage(plugin.getLanguageManager().msg(player, "party_left"));
                return;
            }
            if (type == Material.ARROW) {
                player.closeInventory();
                return;
            }
            return;
        }

        if (cleanTitle.contains("Party (Leader)")) {
            if (type == Material.PLAYER_HEAD && event.getSlot() == 10) {
                player.closeInventory();
                player.sendMessage(plugin.getLanguageManager().msg(player, "party_help_invite"));
                return;
            }
            if (type == Material.NETHERITE_SWORD) {
                plugin.getPartyGUI().openFFASelector(player);
                return;
            }
            if (type == Material.TRIDENT) {
                plugin.getPartyGUI().openTransferSelector(player);
                return;
            }
            if (type == Material.RED_WOOL && event.getSlot() == 14) {
                plugin.getPartyManager().disbandParty(player);
                player.closeInventory();
                player.sendMessage(plugin.getLanguageManager().msg(player, "party_disbanded"));
                return;
            }
            if (type == Material.BARRIER) {
                plugin.getPartyManager().leaveParty(player);
                player.closeInventory();
                player.sendMessage(plugin.getLanguageManager().msg(player, "party_left"));
                return;
            }
            if (type == Material.ARROW) {
                player.closeInventory();
                return;
            }
            return;
        }
    }

    private void handleKickClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.ARROW) {
            plugin.getPartyGUI().openPartyMenu(player);
            return;
        }
        if (item.getType() == Material.PLAYER_HEAD) {
            for (UUID m : plugin.getPartyManager().getParty(player.getUniqueId()).getMembers()) {
                Player member = org.bukkit.Bukkit.getPlayer(m);
                if (member != null) {
                    plugin.getPartyManager().kickPlayer(player, member);
                    player.sendMessage(plugin.getLanguageManager().msg(player, "party_kicked_broadcast", "%player%", member.getName()));
                    plugin.getPartyGUI().openPartyMenu(player);
                    return;
                }
            }
        }
    }

    private void handleTransferClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.ARROW) {
            plugin.getPartyGUI().openPartyMenu(player);
            return;
        }
        if (item.getType() == Material.PLAYER_HEAD) {
            for (UUID m : plugin.getPartyManager().getParty(player.getUniqueId()).getMembers()) {
                Player member = org.bukkit.Bukkit.getPlayer(m);
                if (member != null) {
                    plugin.getPartyManager().transferLeadership(player, member);
                    player.closeInventory();
                    player.sendMessage(plugin.getLanguageManager().msg(player, "party_transfer", "%player%", member.getName()));
                    return;
                }
            }
        }
    }

    private void handleFFAClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.ARROW) {
            plugin.getPartyGUI().openPartyMenu(player);
            return;
        }
        for (DuelGameMode mode : DuelGameMode.values()) {
            if (item.getType() == getModeIcon(mode)) {
                if (mode.isArenaRestricted() && plugin.getArenaManager().getAvailableArena(mode) == null) {
                    player.sendMessage(plugin.getLanguageManager().msg(player, "queue_no_arena", "%mode%", mode.getDisplayName()));
                    return;
                }
                plugin.getDuelManager().startPartyFFA(player, mode);
                player.closeInventory();
                return;
            }
        }
    }

    private void handleKitEditorModeSelect(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        if (item.getType() == Material.BLACK_STAINED_GLASS_PANE || item.getType() == Material.PURPLE_STAINED_GLASS_PANE) return;

        String name = item.getItemMeta() != null ? item.getItemMeta().getDisplayName() : "";
        for (DuelGameMode mode : DuelGameMode.values()) {
            if (name.contains(mode.getDisplayName())) {
                plugin.getKitEditorGUI().openKitEditor(player, mode);
                break;
            }
        }
    }

    private void handleKitEditorClick(InventoryClickEvent event, Player player) {
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            event.setCancelled(true);
            return;
        }

        int slot = event.getSlot();
        if (slot >= 0 && slot < 45) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
            }
            return;
        }

        event.setCancelled(true);
        Material type = item.getType();

        if (type == Material.LIME_STAINED_GLASS_PANE) {
            DuelGameMode mode = plugin.getKitEditorGUI().getEditingMode(player.getUniqueId());
            if (mode != null) {
                plugin.getKitEditorGUI().saveKit(player, mode, event.getInventory());
            }
            plugin.getKitEditorGUI().removeEditingMode(player.getUniqueId());
            plugin.getKitEditorGUI().openModeSelector(player);
            return;
        }

        if (type == Material.RED_STAINED_GLASS_PANE) {
            DuelGameMode mode = plugin.getKitEditorGUI().getEditingMode(player.getUniqueId());
            if (mode != null) {
                plugin.getKitEditorGUI().resetKit(player, mode);
            }
            plugin.getKitEditorGUI().removeEditingMode(player.getUniqueId());
            plugin.getKitEditorGUI().openModeSelector(player);
            return;
        }

        if (type == Material.ARMOR_STAND) {
            if (!plugin.getVipManager().isVip(player.getUniqueId())) {
                player.sendMessage(plugin.getLanguageManager().msg(player, "party_vip_only"));
                return;
            }
            plugin.getKitEditorGUI().openArmorPieceSelector(player);
            return;
        }

        if (type == Material.ARROW) {
            plugin.getKitEditorGUI().removeEditingMode(player.getUniqueId());
            plugin.getKitEditorGUI().openModeSelector(player);
            return;
        }
    }

    private void handleArmorPieceClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        if (item.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        if (item.getType() == Material.ARROW) {
            DuelGameMode mode = plugin.getKitEditorGUI().getEditingMode(player.getUniqueId());
            if (mode != null) {
                plugin.getKitEditorGUI().openKitEditor(player, mode);
            }
            return;
        }

        int slot = event.getSlot();
        int armorSlot = -1;
        if (slot == 10) armorSlot = 3;
        else if (slot == 12) armorSlot = 2;
        else if (slot == 14) armorSlot = 1;
        else if (slot == 16) armorSlot = 0;

        if (armorSlot >= 0) {
            plugin.getKitEditorGUI().openPatternSelector(player, armorSlot);
        }
    }

    private void handlePatternClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        if (item.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        if (item.getType() == Material.ARROW) {
            plugin.getKitEditorGUI().openArmorPieceSelector(player);
            return;
        }

        if (item.getType() == Material.SMITHING_TABLE || item.getType() == Material.ANVIL) return;

        TrimPattern[] patterns = {
                TrimPattern.SENTRY, TrimPattern.DUNE, TrimPattern.COAST, TrimPattern.WILD,
                TrimPattern.WARD, TrimPattern.EYE, TrimPattern.VEX, TrimPattern.TIDE,
                TrimPattern.SNOUT, TrimPattern.RIB, TrimPattern.SPIRE, TrimPattern.WAYFINDER,
                TrimPattern.SHAPER, TrimPattern.SILENCE, TrimPattern.RAISER, TrimPattern.HOST,
                TrimPattern.FLOW, TrimPattern.BOLT
        };
        Material[] patternIcons = {
                Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.HOST_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE
        };

        for (int i = 0; i < patterns.length && i < patternIcons.length; i++) {
            if (item.getType() == patternIcons[i]) {
                plugin.getKitEditorGUI().openMaterialSelector(player, patterns[i]);
                return;
            }
        }
    }

    private void handleMaterialClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        if (item.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        if (item.getType() == Material.ARROW) {
            int armorSlot = plugin.getKitEditorGUI().getEditingArmorSlot(player.getUniqueId());
            plugin.getKitEditorGUI().openPatternSelector(player, armorSlot);
            return;
        }

        TrimMaterial[] materials = {
                TrimMaterial.IRON, TrimMaterial.COPPER, TrimMaterial.GOLD,
                TrimMaterial.DIAMOND, TrimMaterial.EMERALD, TrimMaterial.LAPIS,
                TrimMaterial.NETHERITE, TrimMaterial.AMETHYST
        };
        Material[] materialIcons = {
                Material.IRON_INGOT, Material.COPPER_INGOT, Material.GOLD_INGOT,
                Material.DIAMOND, Material.EMERALD, Material.LAPIS_LAZULI,
                Material.NETHERITE_INGOT, Material.AMETHYST_SHARD
        };

        for (int i = 0; i < materials.length && i < materialIcons.length; i++) {
            if (item.getType() == materialIcons[i]) {
                TrimPattern pattern = plugin.getKitEditorGUI().getSelectedPattern(player.getUniqueId());
                if (pattern != null) {
                    plugin.getKitEditorGUI().applyTrimToSlot(player, pattern, materials[i]);
                    int armorSlot = plugin.getKitEditorGUI().getEditingArmorSlot(player.getUniqueId());
                    String slotName = switch (armorSlot) {
                        case 0 -> plugin.getLanguageManager().msgRaw(player, "gui_armor_boots");
                        case 1 -> plugin.getLanguageManager().msgRaw(player, "gui_armor_leggings");
                        case 2 -> plugin.getLanguageManager().msgRaw(player, "gui_armor_chestplate");
                        case 3 -> plugin.getLanguageManager().msgRaw(player, "gui_armor_helmet");
                        default -> "???";
                    };
                    String matName = formatTrimName(materials[i].getKey().getKey());
                    player.sendMessage(plugin.getLanguageManager().msg(player, "gui_trim_applied", "%slot%", slotName, "%pattern%", formatTrimName(pattern.getKey().getKey()), "%material%", matName));
                }
                plugin.getKitEditorGUI().openArmorPieceSelector(player);
                return;
            }
        }
    }

    private String formatTrimName(String key) {
        if (key == null) return "???";
        String[] parts = key.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().getTitle() == null) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDuelManager().isInDuel(player)) return;
        if (plugin.getQueueManager().isInAnyQueue(player)) {
            event.setCancelled(true);
            return;
        }
        ItemStack dropped = event.getItemDrop().getItemStack();
        if (isLobbyItem(dropped)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (plugin.getDuelManager().isInDuel(player)) return;
        if (plugin.getQueueManager().isInAnyQueue(player)) {
            event.setCancelled(true);
        }
    }

    private boolean isLobbyItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        return item.getType() == Material.NETHERITE_SWORD || item.getType() == Material.CRAFTING_TABLE || item.getType() == Material.NETHER_STAR;
    }

    private boolean isModeIcon(Material type) {
        return type == Material.DIAMOND_SWORD || type == Material.DIAMOND_AXE || type == Material.GOLDEN_APPLE
                || type == Material.MACE || type == Material.SHIELD || type == Material.CHORUS_FRUIT
                || type == Material.SPLASH_POTION || type == Material.NETHERITE_HELMET;
    }

    private Material getModeIcon(DuelGameMode mode) {
        return switch (mode) {
            case SWORD -> Material.DIAMOND_SWORD;
            case AXE -> Material.DIAMOND_AXE;
            case UHC -> Material.GOLDEN_APPLE;
            case MACE -> Material.MACE;
            case SMP -> Material.SHIELD;
            case DIASMP -> Material.CHORUS_FRUIT;
            case POT -> Material.SPLASH_POTION;
            case NETHPOT -> Material.NETHERITE_HELMET;
        };
    }
}
