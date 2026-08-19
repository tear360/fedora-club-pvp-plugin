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

        if (name.contains("§d§lDéfi")) {
            event.setCancelled(true);
            openQueueGUI(player);
        } else if (name.contains("§5§lKits")) {
            event.setCancelled(true);
            plugin.getKitEditorGUI().openModeSelector(player);
        }
    }

    private void openQueueGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45,
                plugin.colorize("&5Rejoindre une queue"));

        for (int i = 0; i < 45; i++) {
            if (i == 10 || i == 11 || i == 12 || i == 13 || i == 14 ||
                    i == 19 || i == 20 || i == 21 || i == 22 || i == 23) {
                continue;
            }
            inv.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());
        }

        DuelGameMode[] modes = DuelGameMode.values();
        int[] slots = {10, 11, 12, 13, 14, 19, 20, 21, 22, 23};

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
                                    (hasArena ? "&a&lArènes disponibles" : "&c&lAucune arène") :
                                    "&a&lMode libre",
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
            if (event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) return;
            if (event.getCurrentItem().getItemMeta() == null) return;

            String name = event.getCurrentItem().getItemMeta().getDisplayName();
            for (DuelGameMode mode : DuelGameMode.values()) {
                if (name.contains(mode.getDisplayName())) {
                    if (plugin.getQueueManager().isInAnyQueue(player)) {
                        player.sendMessage(plugin.getPrefix() + "§cVous êtes déjà en queue!");
                        return;
                    }
                    plugin.getQueueManager().joinQueue(player, mode);
                    player.sendMessage(plugin.getPrefix() + "§dQueue rejoinue pour §f" + mode.getDisplayName() + "§d! §7En attente d'un adversaire...");
                    player.closeInventory();
                    return;
                }
            }
            return;
        }

        if (title.contains("Sélection de mode") && !title.contains("queue")) {
            event.setCancelled(true);
            return;
        }

        if (title.contains("Éditeur de kits")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            if (event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) return;
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
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

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

            if (name.contains("Retour")) {
                plugin.getKitEditorGUI().removeEditingMode(player.getUniqueId());
                plugin.getKitEditorGUI().openModeSelector(player);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (title == null) return;
        if (title.contains("Kit ") || title.contains("Rejoindre une queue") || title.contains("Éditeur de kits")) {
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
        return name.contains("§d§lDéfi") || name.contains("§5§lKits");
    }

    private Material getModeIcon(DuelGameMode mode) {
        return switch (mode) {
            case SWORD -> Material.DIAMOND_SWORD;
            case AXE -> Material.DIAMOND_AXE;
            case UHC -> Material.GOLDEN_APPLE;
            case MACE -> Material.MACE;
            case VANILLA -> Material.END_CRYSTAL;
            case SMP -> Material.SHIELD;
            case DIASMP -> Material.CHORUS_FRUIT;
            case POT -> Material.SPLASH_POTION;
            case NETHPOT -> Material.NETHERITE_HELMET;
        };
    }
}
