package fr.duelplugin.listeners;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.utils.ItemBuilder;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerListener implements Listener {

    private final DuelPlugin plugin;

    public PlayerListener(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerManager().getDuelPlayer(player.getUniqueId());

        if (plugin.getLobbyManager().isLobbySet()) {
            plugin.getLobbyManager().teleportToLobby(player);
            plugin.getScoreboardManager().createLobbyScoreboard(player, null, null);
        }

        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setFlying(false);
        player.setAllowFlight(false);

        giveLobbyItems(player);

        event.setJoinMessage(plugin.colorize("&5+ &d" + player.getName() + " &7a rejoint le serveur"));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getQueueManager().leaveQueue(player);
        plugin.getDuelManager().handleDisconnect(player);
        plugin.getScoreboardManager().removeScoreboard(player);
        event.setQuitMessage(plugin.colorize("&5- &d" + player.getName() + " &7a quitté le serveur"));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!plugin.getDuelManager().isInDuel(player)) return;

        event.getDrops().clear();
        event.setDroppedExp(0);
        event.setDeathMessage(null);

        var duel = plugin.getDuelManager().getDuel(player.getUniqueId());
        if (duel == null) return;

        Player killer = player.getKiller();
        if (killer == null) {
            killer = plugin.getServer().getPlayer(duel.getOpponent(player.getUniqueId()));
        }

        if (killer != null) {
            plugin.getDuelManager().endDuel(killer.getUniqueId(), killer.getUniqueId(), player.getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (plugin.getDuelManager().isInDuel(player)) return;

        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory() == player.getInventory()) {
            ItemStack item = event.getCurrentItem();
            if (isLobbyItem(item) || isVipLobbyItem(item)) {
                event.setCancelled(true);
            }
            if (event.getSlot() == 38 && plugin.getVipManager().isVip(player.getUniqueId())) {
                event.setCancelled(true);
            }
            if (event.getSlot() == 40 && plugin.getVipManager().isVip(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
        if (event.getSlot() == 38 && plugin.getVipManager().isVip(player.getUniqueId())) {
            event.setCancelled(true);
        }
        if (event.getSlot() == 40 && plugin.getVipManager().isVip(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWindChargeUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDuelManager().isInDuel(player)) return;

        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand.getType() == Material.WIND_CHARGE && plugin.getVipManager().isVip(player.getUniqueId())) {
            if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR || event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline() && plugin.getLobbyManager().isLobbySet() && !plugin.getDuelManager().isInDuel(player)) {
                        player.getInventory().setItemInOffHand(new ItemStack(Material.WIND_CHARGE, 64));
                    }
                }, 1L);
            }
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
        if (isLobbyItem(dropped) || isVipLobbyItem(dropped)) {
            event.setCancelled(true);
        }
        if (dropped.getType() == Material.WIND_CHARGE && plugin.getVipManager().isVip(player.getUniqueId())) {
            event.setCancelled(true);
        }
        if (dropped.getType() == Material.ELYTRA && plugin.getVipManager().isVip(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    private boolean isLobbyItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null) return false;
        String name = item.getItemMeta().getDisplayName();
        return name.contains("§d§lQueue") || name.contains("§5§lKits") || name.contains("§d§lParty");
    }

    private boolean isVipLobbyItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (item.getType() == Material.WIND_CHARGE) return true;
        if (item.getType() == Material.ELYTRA) return true;
        return false;
    }

    public static void giveLobbyItems(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);

        player.getInventory().setItem(0, new ItemBuilder(Material.NETHERITE_SWORD)
                .name("§d§lQueue")
                .lore("", "§7Rejoindre une queue de duel", "").build());

        player.getInventory().setItem(4, new ItemBuilder(Material.CRAFTING_TABLE)
                .name("§5§lKits")
                .lore("", "§7Éditez vos kits", "").build());

        player.getInventory().setItem(8, new ItemBuilder(Material.NETHER_STAR)
                .name("§d§lParty")
                .lore("", "§7Gérez votre party", "").build());

        DuelPlugin plugin = DuelPlugin.getInstance();
        if (plugin != null && plugin.getVipManager().isVip(player.getUniqueId())) {
            ItemStack windCharge = new ItemStack(Material.WIND_CHARGE, 64);
            player.getInventory().setItemInOffHand(windCharge);

            ItemStack elytra = new ItemStack(Material.ELYTRA);
            ItemMeta elytraMeta = elytra.getItemMeta();
            if (elytraMeta != null) {
                elytraMeta.displayName(net.kyori.adventure.text.Component.text("§d§lElytra VIP", net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE));
                elytraMeta.lore(java.util.Arrays.asList(
                        net.kyori.adventure.text.Component.text("§7Élytra exclusive VIP"),
                        net.kyori.adventure.text.Component.text("§7Incassable")
                ));
                elytraMeta.setUnbreakable(true);
                elytra.setItemMeta(elytraMeta);
            }
            player.getInventory().setChestplate(elytra);
        }
    }
}
