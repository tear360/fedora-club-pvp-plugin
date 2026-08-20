package fr.duelplugin.listeners;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.gui.DuelGUI;
import fr.duelplugin.models.DuelGameMode;
import fr.duelplugin.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
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
        if (item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null) return;

        String name = item.getItemMeta().getDisplayName();

        if (name.contains("§d§lQueue")) {
            event.setCancelled(true);
            openQueueGUI(player);
        } else if (name.contains("§5§lKits")) {
            event.setCancelled(true);
            plugin.getKitEditorGUI().openModeSelector(player);
        } else if (name.contains("§d§lParty")) {
            event.setCancelled(true);
            plugin.getPartyGUI().openPartyMenu(player);
        }
    }

    private void openQueueGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45,
                net.kyori.adventure.text.Component.text("Rejoindre une queue",
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
                                    (hasArena ? "&aArènes disponibles" : "&cAucune arène") :
                                    "&aMode libre",
                            "&7Blocs: " + (mode.canBreakBlocks() ? "&aCassables" : "&cNon cassables"),
                            "&dQueue: &f" + queueSize + " joueur" + (queueSize != 1 ? "s" : ""),
                            "",
                            "&d&lCliquez pour rejoindre"
                    ).build());
        }

        queueGUIs.put(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (title == null) return;

        if (title.contains("Rejoindre une queue")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            if (event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE || event.getCurrentItem().getType() == Material.PURPLE_STAINED_GLASS_PANE) return;
            if (event.getCurrentItem().getItemMeta() == null) return;

            String name = event.getCurrentItem().getItemMeta().getDisplayName();
            for (DuelGameMode mode : DuelGameMode.values()) {
                if (name.contains(mode.getDisplayName())) {
                    if (plugin.getQueueManager().isInQueue(player, mode)) {
                        plugin.getQueueManager().leaveQueue(player, mode);
                        player.sendMessage(plugin.getPrefix() + "§cQueue §f" + mode.getDisplayName() + " §cquittée.");
                        openQueueGUI(player);
                        return;
                    }
                    if (mode.isArenaRestricted() && plugin.getArenaManager().getAvailableArena(mode) == null) {
                        player.sendMessage(plugin.getPrefix() + "§cAucune arène disponible pour §f" + mode.getDisplayName() + "§c!");
                        return;
                    }
                    if (plugin.getQueueManager().isInAnyQueue(player)) {
                        plugin.getQueueManager().leaveQueue(player);
                    }
                    plugin.getQueueManager().joinQueue(player, mode);
                    player.sendMessage(plugin.getPrefix() + "§dQueue rejoinue pour §f" + mode.getDisplayName() + "§d! §7En attente d'un adversaire...");
                    player.closeInventory();
                    return;
                }
            }
            return;
        }

        if (title.contains("Sélection de mode") || title.contains("Défi →")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            if (event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE || event.getCurrentItem().getType() == Material.PURPLE_STAINED_GLASS_PANE) return;
            if (event.getCurrentItem().getItemMeta() == null) return;

            String name = event.getCurrentItem().getItemMeta().getDisplayName();
            for (DuelGameMode mode : DuelGameMode.values()) {
                if (name.contains(mode.getDisplayName())) {
                    if (title.contains("Défi →")) {
                        UUID targetUuid = plugin.getDuelGUI().peekPendingTarget(player.getUniqueId());
                        if (targetUuid != null) {
                            Player target = org.bukkit.Bukkit.getPlayer(targetUuid);
                            if (target != null) {
                                plugin.getDuelGUI().getPendingTarget(player.getUniqueId());
                                if (mode.isArenaRestricted() && plugin.getArenaManager().getAvailableArena(mode) == null) {
                                    player.sendMessage(plugin.getPrefix() + "§cAucune arène disponible pour §f" + mode.getDisplayName() + "§c!");
                                    player.closeInventory();
                                    return;
                                }
                                if (plugin.getDuelManager().isInDuel(player) || plugin.getDuelManager().isInDuel(target)) {
                                    player.sendMessage(plugin.getPrefix() + "§cUn des joueurs est déjà en duel!");
                                    player.closeInventory();
                                    return;
                                }
                                if (plugin.getDuelManager().sendRequest(player, target, mode)) {
                                    player.sendMessage(plugin.getPrefix() + "§dDemande de duel envoyée à §f" + target.getName() + " §d[" + mode.getDisplayName() + "§d]");
                                } else {
                                    player.sendMessage(plugin.getPrefix() + "§cImpossible d'envoyer la demande.");
                                }
                            } else {
                                player.sendMessage(plugin.getPrefix() + "§cCe joueur n'est plus en ligne.");
                            }
                        }
                        player.closeInventory();
                    }
                    break;
                }
            }
            return;
        }

        if (title.contains("Éditeur de kits")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            if (event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE || event.getCurrentItem().getType() == Material.PURPLE_STAINED_GLASS_PANE) return;
            if (event.getCurrentItem().getItemMeta() == null) return;

            String name = event.getCurrentItem().getItemMeta().getDisplayName();
            for (DuelGameMode mode : DuelGameMode.values()) {
                if (name.contains(mode.getDisplayName())) {
                    plugin.getKitEditorGUI().openKitEditor(player, mode);
                    break;
                }
            }
            return;
        }

        if (title.contains("Kit ")) {
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

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
            String name = event.getCurrentItem().getItemMeta() != null ? event.getCurrentItem().getItemMeta().getDisplayName() : "";

            if (name.contains("Sauvegarder")) {
                DuelGameMode mode = plugin.getKitEditorGUI().getEditingMode(player.getUniqueId());
                if (mode != null) {
                    plugin.getKitEditorGUI().saveKit(player, mode, event.getInventory());
                }
                plugin.getKitEditorGUI().removeEditingMode(player.getUniqueId());
                plugin.getKitEditorGUI().openModeSelector(player);
                return;
            }

            if (name.contains("Réinitialiser")) {
                DuelGameMode mode = plugin.getKitEditorGUI().getEditingMode(player.getUniqueId());
                if (mode != null) {
                    plugin.getKitEditorGUI().resetKit(player, mode);
                }
                plugin.getKitEditorGUI().removeEditingMode(player.getUniqueId());
                plugin.getKitEditorGUI().openModeSelector(player);
                return;
            }

            if (name.contains("Trims VIP")) {
                if (!plugin.getVipManager().isVip(player.getUniqueId())) {
                    player.sendMessage(plugin.getPrefix() + "§cFonctionnalité VIP uniquement!");
                    return;
                }
                plugin.getKitEditorGUI().openArmorPieceSelector(player);
                return;
            }

            if (name.contains("Retour")) {
                plugin.getKitEditorGUI().removeEditingMode(player.getUniqueId());
                plugin.getKitEditorGUI().openModeSelector(player);
                return;
            }
        }

        if (title.contains("Choisir une pièce")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            if (event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) return;

            String name = event.getCurrentItem().getItemMeta() != null ? event.getCurrentItem().getItemMeta().getDisplayName() : "";

            if (name.contains("Retour")) {
                DuelGameMode mode = plugin.getKitEditorGUI().getEditingMode(player.getUniqueId());
                if (mode != null) {
                    plugin.getKitEditorGUI().openKitEditor(player, mode);
                }
                return;
            }

            int armorSlot = -1;
            if (name.contains("Casque")) armorSlot = 3;
            else if (name.contains("Plastron")) armorSlot = 2;
            else if (name.contains("Jambières")) armorSlot = 1;
            else if (name.contains("Bottes")) armorSlot = 0;

            if (armorSlot >= 0) {
                plugin.getKitEditorGUI().openPatternSelector(player, armorSlot);
            }
            return;
        }

        if (title.contains("Choisir un pattern")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            if (event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) return;

            String name = event.getCurrentItem().getItemMeta() != null ? event.getCurrentItem().getItemMeta().getDisplayName() : "";

            if (name.contains("Retour")) {
                plugin.getKitEditorGUI().openArmorPieceSelector(player);
                return;
            }

            TrimPattern[] patterns = {
                    TrimPattern.SENTRY, TrimPattern.DUNE, TrimPattern.COAST, TrimPattern.WILD,
                    TrimPattern.WARD, TrimPattern.EYE, TrimPattern.VEX, TrimPattern.TIDE,
                    TrimPattern.SNOUT, TrimPattern.RIB, TrimPattern.SPIRE, TrimPattern.WAYFINDER,
                    TrimPattern.SHAPER, TrimPattern.SILENCE, TrimPattern.RAISER, TrimPattern.HOST,
                    TrimPattern.FLOW, TrimPattern.BOLT
            };
            for (TrimPattern p : patterns) {
                String patternName = formatTrimName(p.getKey().getKey());
                if (name.contains(patternName)) {
                    plugin.getKitEditorGUI().openMaterialSelector(player, p);
                    return;
                }
            }
            return;
        }

        if (title.contains("Choisir un matériau")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            if (event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) return;

            String name = event.getCurrentItem().getItemMeta() != null ? event.getCurrentItem().getItemMeta().getDisplayName() : "";

            if (name.contains("Retour")) {
                int armorSlot = plugin.getKitEditorGUI().getEditingArmorSlot(player.getUniqueId());
                plugin.getKitEditorGUI().openPatternSelector(player, armorSlot);
                return;
            }

            TrimMaterial[] materials = {
                    TrimMaterial.IRON, TrimMaterial.COPPER, TrimMaterial.GOLD,
                    TrimMaterial.DIAMOND, TrimMaterial.EMERALD, TrimMaterial.LAPIS,
                    TrimMaterial.NETHERITE, TrimMaterial.AMETHYST
            };
            for (TrimMaterial m : materials) {
                String matName = formatTrimName(m.getKey().getKey());
                if (name.contains(matName)) {
                    TrimPattern pattern = plugin.getKitEditorGUI().getSelectedPattern(player.getUniqueId());
                    if (pattern != null) {
                        plugin.getKitEditorGUI().applyTrimToSlot(player, pattern, m);
                        int armorSlot = plugin.getKitEditorGUI().getEditingArmorSlot(player.getUniqueId());
                        String slotName = switch (armorSlot) {
                            case 0 -> "Bottes";
                            case 1 -> "Jambières";
                            case 2 -> "Plastron";
                            case 3 -> "Casque";
                            default -> "???";
                        };
                        player.sendMessage(plugin.getPrefix() + "§dTrim appliqué sur §f" + slotName + "§d: §f" + formatTrimName(pattern.getKey().getKey()) + " §7/ §f" + matName);
                    }
                    plugin.getKitEditorGUI().openArmorPieceSelector(player);
                    return;
                }
            }
        }

        if (title.contains("Party") || title.equals("Kick un membre") || title.equals("§5§lKick un membre") || title.equals("Transférer le leadership") || title.equals("§5§lTransférer le leadership") || title.equals("Choisir un mode FFA") || title.equals("§5§lChoisir un mode FFA") || title.contains("Quitter la party")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            if (event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE || event.getCurrentItem().getType() == Material.PURPLE_STAINED_GLASS_PANE) return;
            if (event.getCurrentItem().getItemMeta() == null) return;
            String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
            String cleanTitle = title.replaceAll("§[0-9a-fk-or]", "");

            if (cleanTitle.equals("Party") && !cleanTitle.contains("Leader") && !cleanTitle.contains("FFA")) {
                if (itemName.contains("Créer une party")) {
                    if (plugin.getPartyManager().createParty(player)) {
                        player.closeInventory();
                        player.sendMessage(plugin.getPrefix() + "§aParty créée! §7Invitez des joueurs avec §d/party invite <joueur>");
                    } else {
                        player.sendMessage(plugin.getPrefix() + "§cImpossible de créer la party.");
                    }
                    return;
                }
                if (itemName.contains("Rejoindre la party")) {
                    UUID leaderUuid = plugin.getPartyManager().getPendingInvite(player.getUniqueId());
                    if (leaderUuid != null) {
                        plugin.getPartyManager().acceptInvite(player);
                        player.closeInventory();
                        player.sendMessage(plugin.getPrefix() + "§aParty rejointe!");
                    }
                    return;
                }
                if (itemName.contains("Refuser l'invitation")) {
                    plugin.getPartyManager().declineInvite(player);
                    player.closeInventory();
                    player.sendMessage(plugin.getPrefix() + "§cInvitation refusée.");
                    return;
                }
                if (itemName.contains("Quitter la party")) {
                    plugin.getPartyManager().leaveParty(player);
                    player.closeInventory();
                    player.sendMessage(plugin.getPrefix() + "§cVous avez quitté la party.");
                    return;
                }
                if (itemName.contains("Retour")) {
                    player.closeInventory();
                    return;
                }
            }

            if (cleanTitle.equals("Party (Leader)")) {
                if (itemName.contains("Inviter un joueur")) {
                    player.closeInventory();
                    player.sendMessage(plugin.getPrefix() + "§dUtilisez §f/party invite <joueur> §dpour inviter.");
                    return;
                }
                if (itemName.contains("Lancer une FFA")) {
                    plugin.getPartyGUI().openFFASelector(player);
                    return;
                }
                if (itemName.contains("Transférer")) {
                    plugin.getPartyGUI().openTransferSelector(player);
                    return;
                }
                if (itemName.contains("Dissoudre")) {
                    plugin.getPartyManager().disbandParty(player);
                    player.closeInventory();
                    player.sendMessage(plugin.getPrefix() + "§cParty dissoute.");
                    return;
                }
                if (itemName.contains("Quitter la party")) {
                    plugin.getPartyManager().leaveParty(player);
                    player.closeInventory();
                    player.sendMessage(plugin.getPrefix() + "§cVous avez quitté la party.");
                    return;
                }
                if (itemName.contains("Retour")) {
                    player.closeInventory();
                    return;
                }
            }

            if (cleanTitle.equals("Kick un membre")) {
                if (itemName.contains("Retour")) {
                    plugin.getPartyGUI().openPartyMenu(player);
                    return;
                }
                if (itemName.contains("Kick")) {
                    for (java.util.UUID m : plugin.getPartyManager().getParty(player.getUniqueId()).getMembers()) {
                        Player member = org.bukkit.Bukkit.getPlayer(m);
                        if (member != null && itemName.contains(member.getName())) {
                            plugin.getPartyManager().kickPlayer(player, member);
                            player.sendMessage(plugin.getPrefix() + "§c" + member.getName() + " §7a été kické.");
                            plugin.getPartyGUI().openPartyMenu(player);
                            return;
                        }
                    }
                    return;
                }
            }

            if (cleanTitle.equals("Transférer le leadership")) {
                if (itemName.contains("Retour")) {
                    plugin.getPartyGUI().openPartyMenu(player);
                    return;
                }
                if (itemName.contains("Transférer")) {
                    for (java.util.UUID m : plugin.getPartyManager().getParty(player.getUniqueId()).getMembers()) {
                        Player member = org.bukkit.Bukkit.getPlayer(m);
                        if (member != null && itemName.contains(member.getName())) {
                            plugin.getPartyManager().transferLeadership(player, member);
                            player.closeInventory();
                            player.sendMessage(plugin.getPrefix() + "§dLeadership transféré à §f" + member.getName() + "§d!");
                            return;
                        }
                    }
                    return;
                }
            }

            if (cleanTitle.equals("Choisir un mode FFA")) {
                if (itemName.contains("Retour")) {
                    plugin.getPartyGUI().openPartyMenu(player);
                    return;
                }
                for (DuelGameMode mode : DuelGameMode.values()) {
                    if (itemName.contains(mode.getDisplayName())) {
                        if (mode.isArenaRestricted() && plugin.getArenaManager().getAvailableArena(mode) == null) {
                            player.sendMessage(plugin.getPrefix() + "§cAucune arène disponible pour §f" + mode.getDisplayName() + "§c!");
                            return;
                        }
                        plugin.getDuelManager().startPartyFFA(player, mode);
                        player.closeInventory();
                        return;
                    }
                }
                return;
            }

            return;
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
        String title = event.getView().getTitle();
        if (title == null) return;
        if (title.contains("Rejoindre une queue") || title.contains("Éditeur de kits") || title.contains("Kit ") || title.contains("Choisir") || title.contains("Party") || title.contains("FFA") || title.contains("Sélection de mode") || title.contains("Défi") || title.contains("Kick") || title.contains("Transférer")) {
            event.setCancelled(true);
        }
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
        if (item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null) return false;
        String name = item.getItemMeta().getDisplayName();
        return name.contains("§d§lQueue") || name.contains("§5§lKits") || name.contains("§d§lParty");
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
