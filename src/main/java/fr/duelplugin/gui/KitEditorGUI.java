package fr.duelplugin.gui;

import fr.duelplugin.DuelPlugin;
import fr.duelplugin.models.DuelGameMode;
import fr.duelplugin.models.Kit;
import fr.duelplugin.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;
import java.util.UUID;

public class KitEditorGUI {

    private final DuelPlugin plugin;
    private final Map<UUID, DuelGameMode> editingMode = new java.util.HashMap<>();

    public KitEditorGUI(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    public void openModeSelector(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45,
                plugin.colorize("&5Éditeur de kits"));

        for (int i = 0; i < 45; i++) {
            if (i == 10 || i == 11 || i == 12 || i == 13 || i == 14 ||
                    i == 19 || i == 20 || i == 21) {
                continue;
            }
            inv.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());
        }

        DuelGameMode[] modes = DuelGameMode.values();
        int[] slots = {10, 11, 12, 13, 14, 19, 20, 21};

        for (int i = 0; i < modes.length && i < slots.length; i++) {
            DuelGameMode mode = modes[i];
            boolean hasCustom = plugin.getKitManager().hasCustomKit(player.getUniqueId(), mode);

            Material icon = getModeIcon(mode);

            inv.setItem(slots[i], new ItemBuilder(icon)
                    .name(mode.getColoredName())
                    .lore(
                            "",
                            hasCustom ? "&a&lKit personnalisé" : "&7Kit par défaut",
                            "",
                            "&d&lCliquez pour éditer"
                    ).build());
        }

        player.openInventory(inv);
    }

    public void openKitEditor(Player player, DuelGameMode mode) {
        editingMode.put(player.getUniqueId(), mode);

        Inventory inv = Bukkit.createInventory(null, 54,
                plugin.colorize("&5Kit " + mode.getDisplayName()));

        Map<String, ItemStack[]> customKit = plugin.getKitManager().loadKit(player.getUniqueId(), mode);

        if (customKit != null) {
            ItemStack[] contents = customKit.get("contents");
            if (contents != null) {
                for (int i = 0; i < Math.min(36, contents.length); i++) {
                    if (contents[i] != null) {
                        inv.setItem(i, contents[i]);
                    }
                }
            }
            ItemStack[] armor = customKit.get("armor");
            if (armor != null) {
                if (armor.length > 3 && armor[3] != null) inv.setItem(50, armor[3]);
                if (armor.length > 2 && armor[2] != null) inv.setItem(51, armor[2]);
                if (armor.length > 1 && armor[1] != null) inv.setItem(52, armor[1]);
                if (armor.length > 0 && armor[0] != null) inv.setItem(53, armor[0]);
            }
            ItemStack[] offhand = customKit.get("offhand");
            if (offhand != null && offhand.length > 0 && offhand[0] != null) {
                inv.setItem(44, offhand[0]);
            }
        } else {
            fillDefaultKit(player, mode, inv);
        }

        inv.setItem(45, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("§a§lSauvegarder").lore("", "§7Cliquez pour sauvegarder").build());
        inv.setItem(46, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).name("§c§lRéinitialiser").lore("", "§7Cliquez pour réinitialiser").build());
        inv.setItem(49, new ItemBuilder(Material.ARROW).name("§d§lRetour").lore("", "§7Retour au menu").build());

        player.openInventory(inv);
    }

    private void fillDefaultKit(Player player, DuelGameMode mode, Inventory inv) {
        PlayerInventory tempInv = player.getInventory();
        ItemStack[] savedContents = tempInv.getContents().clone();
        ItemStack[] savedArmor = tempInv.getArmorContents().clone();
        ItemStack savedOffHand = tempInv.getItemInOffHand();

        Kit.giveKit(player, mode);

        for (int i = 0; i < 36; i++) {
            inv.setItem(i, tempInv.getItem(i));
        }

        ItemStack[] armor = tempInv.getArmorContents();
        if (armor.length >= 4) {
            if (armor[3] != null) inv.setItem(50, armor[3]);
            if (armor[2] != null) inv.setItem(51, armor[2]);
            if (armor[1] != null) inv.setItem(52, armor[1]);
            if (armor[0] != null) inv.setItem(53, armor[0]);
        }

        ItemStack offHand = tempInv.getItemInOffHand();
        if (offHand != null && offHand.getType() != Material.AIR) {
            inv.setItem(44, offHand);
        }

        tempInv.setContents(savedContents);
        tempInv.setArmorContents(savedArmor);
        tempInv.setItemInOffHand(savedOffHand);
    }

    public void saveKit(Player player, DuelGameMode mode, Inventory inv) {
        ItemStack[] contents = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            contents[i] = inv.getItem(i);
        }

        ItemStack[] armor = new ItemStack[4];
        armor[0] = inv.getItem(53);
        armor[1] = inv.getItem(52);
        armor[2] = inv.getItem(51);
        armor[3] = inv.getItem(50);

        ItemStack offHand = inv.getItem(44);

        plugin.getKitManager().saveKit(player.getUniqueId(), mode, contents, armor, offHand);
        player.sendMessage(plugin.getPrefix() + "§aKit §d" + mode.getDisplayName() + " §asauvegardé!");
    }

    public void resetKit(Player player, DuelGameMode mode) {
        plugin.getKitManager().deleteKit(player.getUniqueId(), mode);
        player.sendMessage(plugin.getPrefix() + "§dKit réinitialisé aux valeurs par défaut.");
    }

    public DuelGameMode getEditingMode(UUID uuid) {
        return editingMode.get(uuid);
    }

    public void removeEditingMode(UUID uuid) {
        editingMode.remove(uuid);
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
