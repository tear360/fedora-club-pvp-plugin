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
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.*;

public class KitEditorGUI {

    private final DuelPlugin plugin;
    private final Map<UUID, DuelGameMode> editingMode = new HashMap<>();
    private final Map<UUID, Map<Integer, ArmorTrim>> editingTrims = new HashMap<>();
    private final Map<UUID, Integer> editingArmorSlot = new HashMap<>();
    private final Map<UUID, TrimPattern> selectedPattern = new HashMap<>();

    public KitEditorGUI(DuelPlugin plugin) {
        this.plugin = plugin;
    }

    public void openModeSelector(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45,
                plugin.colorize("&5&lÉditeur de kits"));

        fillGlass(inv, 45);

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
                plugin.colorize("&5&lKit " + mode.getDisplayName()));

        Map<String, ItemStack[]> customKit = plugin.getKitManager().loadKit(player.getUniqueId(), mode);
        Map<Integer, ArmorTrim> trims = plugin.getKitManager().loadKitTrims(player.getUniqueId(), mode);

        if (trims != null) {
            editingTrims.put(player.getUniqueId(), new HashMap<>(trims));
        } else {
            editingTrims.put(player.getUniqueId(), new HashMap<>());
        }

        if (customKit != null) {
            ItemStack[] contents = customKit.get("contents");
            if (contents != null) {
                for (int i = 0; i < Math.min(36, contents.length); i++) {
                    if (contents[i] != null) inv.setItem(i, contents[i]);
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
        inv.setItem(47, new ItemBuilder(Material.ARMOR_STAND).name("§5§lTrims VIP").lore("", "§7Personnalisez les trims", "§7de votre armure", "").build());
        inv.setItem(49, new ItemBuilder(Material.ARROW).name("§d§lRetour").lore("", "§7Retour au menu").build());

        player.openInventory(inv);
    }

    public void openArmorPieceSelector(Player player) {
        UUID uuid = player.getUniqueId();
        DuelGameMode mode = editingMode.get(uuid);
        if (mode == null) return;

        Inventory inv = Bukkit.createInventory(null, 27,
                plugin.colorize("&5&lChoisir une pièce d'armure"));

        fillGlass(inv, 27);

        Map<String, ItemStack[]> customKit = plugin.getKitManager().loadKit(uuid, mode);
        ItemStack[] kitArmor = new ItemStack[4];

        if (customKit != null && customKit.get("armor") != null) {
            kitArmor = customKit.get("armor");
        } else {
            PlayerInventory tempInv = player.getInventory();
            ItemStack[] savedContents = tempInv.getContents().clone();
            ItemStack[] savedArmor = tempInv.getArmorContents().clone();
            ItemStack savedOffHand = tempInv.getItemInOffHand();
            Kit.giveKit(player, mode);
            kitArmor = tempInv.getArmorContents().clone();
            tempInv.setContents(savedContents);
            tempInv.setArmorContents(savedArmor);
            tempInv.setItemInOffHand(savedOffHand);
        }

        Map<Integer, ArmorTrim> trims = editingTrims.getOrDefault(uuid, new HashMap<>());

        String[] slotNames = {"Bottes", "Jambières", "Plastron", "Casque"};
        int[] invSlots = {16, 14, 12, 10};
        int[] armorIndices = {0, 1, 2, 3};

        for (int i = 0; i < 4; i++) {
            int armorIdx = armorIndices[i];
            ItemStack armorPiece = kitArmor != null && armorIdx < kitArmor.length ? kitArmor[armorIdx] : null;
            ArmorTrim trim = trims.get(armorIdx);

            if (armorPiece != null && armorPiece.getType() != Material.AIR) {
                ItemStack display = armorPiece.clone();
                if (trim != null && display.hasItemMeta() && display.getItemMeta() instanceof ArmorMeta) {
                    ArmorMeta meta = (ArmorMeta) display.getItemMeta();
                    meta.setTrim(trim);
                    display.setItemMeta(meta);
                }
                ItemBuilder builder = new ItemBuilder(display.getType());
                String trimInfo = trim != null ?
                        "§d" + formatTrimName(trim.getPattern().getKey().getKey()) + " §7/ §d" + formatTrimName(trim.getMaterial().getKey().getKey()) :
                        "§7Aucun";
                builder.name("§d" + slotNames[armorIdx]);
                builder.lore("", "§7Type: §f" + formatMaterialName(display.getType()), "§7Trim: " + trimInfo, "", "§d&lCliquez pour éditer");
                inv.setItem(invSlots[i], builder.build());
            } else {
                inv.setItem(invSlots[i], new ItemBuilder(Material.BARRIER)
                        .name("§c" + slotNames[armorIdx])
                        .lore("", "§7Aucune armure dans ce slot", "", "§d&lCliquez pour ajouter")
                        .build());
            }
        }

        inv.setItem(22, new ItemBuilder(Material.ARROW).name("§d§lRetour").lore("", "§7Retour à l'éditeur").build());

        player.openInventory(inv);
    }

    public void openPatternSelector(Player player, int armorSlot) {
        UUID uuid = player.getUniqueId();
        editingArmorSlot.put(uuid, armorSlot);
        Map<Integer, ArmorTrim> currentTrims = editingTrims.getOrDefault(uuid, new HashMap<>());
        ArmorTrim current = currentTrims.get(armorSlot);

        Inventory inv = Bukkit.createInventory(null, 54,
                plugin.colorize("&5&lChoisir un pattern"));

        fillGlass(inv, 54);

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

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26, 28, 29};
        for (int i = 0; i < patterns.length && i < slots.length; i++) {
            TrimPattern p = patterns[i];
            boolean isSelected = current != null && current.getPattern().getKey().equals(p.getKey());
            inv.setItem(slots[i], new ItemBuilder(patternIcons[i])
                    .name((isSelected ? "§a✓ " : "§d") + formatTrimName(p.getKey().getKey()))
                    .lore("", "§7Pattern de trim", isSelected ? "§aSélectionné actuellement" : "", "", "§d&lCliquez pour sélectionner")
                    .build());
        }

        inv.setItem(49, new ItemBuilder(Material.ARROW).name("§d§lRetour").lore("", "§7Retour à la sélection de pièce").build());

        player.openInventory(inv);
    }

    public void openMaterialSelector(Player player, TrimPattern pattern) {
        UUID uuid = player.getUniqueId();
        selectedPattern.put(uuid, pattern);
        Map<Integer, ArmorTrim> currentTrims = editingTrims.getOrDefault(uuid, new HashMap<>());
        int armorSlot = editingArmorSlot.getOrDefault(uuid, 3);
        ArmorTrim current = currentTrims.get(armorSlot);

        Inventory inv = Bukkit.createInventory(null, 27,
                plugin.colorize("&5&lChoisir un matériau"));

        fillGlass(inv, 27);

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

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 17};
        for (int i = 0; i < materials.length && i < slots.length; i++) {
            TrimMaterial m = materials[i];
            boolean isSelected = current != null && current.getMaterial().getKey().equals(m.getKey());
            inv.setItem(slots[i], new ItemBuilder(materialIcons[i])
                    .name((isSelected ? "§a✓ " : "§d") + formatTrimName(m.getKey().getKey()))
                    .lore("", "§7Matériau de trim", isSelected ? "§aSélectionné actuellement" : "", "", "§d&lCliquez pour sélectionner")
                    .build());
        }

        inv.setItem(22, new ItemBuilder(Material.ARROW).name("§d§lRetour").lore("", "§7Retour à la sélection de pattern").build());

        player.openInventory(inv);
    }

    public void applyTrimToSlot(Player player, TrimPattern pattern, TrimMaterial material) {
        UUID uuid = player.getUniqueId();
        int armorSlot = editingArmorSlot.getOrDefault(uuid, 3);
        editingTrims.computeIfAbsent(uuid, k -> new HashMap<>()).put(armorSlot, new ArmorTrim(material, pattern));
    }

    private void fillGlass(Inventory inv, int size) {
        for (int i = 0; i < size; i++) {
            inv.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());
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

    private String formatMaterialName(Material mat) {
        String name = mat.name().replace("_", " ").toLowerCase();
        StringBuilder sb = new StringBuilder();
        for (String part : name.split(" ")) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    public void applyTrimsToArmor(Player player, Inventory inv) {
        UUID uuid = player.getUniqueId();
        Map<Integer, ArmorTrim> trims = editingTrims.get(uuid);
        if (trims == null || trims.isEmpty()) return;

        int[] armorSlots = {53, 52, 51, 50};
        int[] trimSlots = {0, 1, 2, 3};

        for (int i = 0; i < armorSlots.length; i++) {
            int invSlot = armorSlots[i];
            int trimSlot = trimSlots[i];
            ItemStack item = inv.getItem(invSlot);
            ArmorTrim trim = trims.get(trimSlot);

            if (item != null && item.getType() != Material.AIR && item.hasItemMeta() && item.getItemMeta() instanceof ArmorMeta) {
                ArmorMeta meta = (ArmorMeta) item.getItemMeta();
                if (trim != null) {
                    meta.setTrim(trim);
                } else {
                    meta.setTrim(null);
                }
                item.setItemMeta(meta);
            }
        }
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
        applyTrimsToArmor(player, inv);

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

        Map<Integer, ArmorTrim> trims = editingTrims.remove(player.getUniqueId());
        if (trims != null && !trims.isEmpty()) {
            plugin.getKitManager().saveKitTrims(player.getUniqueId(), mode, trims);
        }

        player.sendMessage(plugin.getPrefix() + "§aKit §d" + mode.getDisplayName() + " §asauvegardé!");
    }

    public void resetKit(Player player, DuelGameMode mode) {
        plugin.getKitManager().deleteKit(player.getUniqueId(), mode);
        editingTrims.remove(player.getUniqueId());
        player.sendMessage(plugin.getPrefix() + "§dKit réinitialisé aux valeurs par défaut.");
    }

    public DuelGameMode getEditingMode(UUID uuid) {
        return editingMode.get(uuid);
    }

    public void removeEditingMode(UUID uuid) {
        editingMode.remove(uuid);
        editingTrims.remove(uuid);
        editingArmorSlot.remove(uuid);
        selectedPattern.remove(uuid);
    }

    public Map<Integer, ArmorTrim> getEditingTrims(UUID uuid) {
        return editingTrims.getOrDefault(uuid, new HashMap<>());
    }

    public int getEditingArmorSlot(UUID uuid) {
        return editingArmorSlot.getOrDefault(uuid, 3);
    }

    public TrimPattern getSelectedPattern(UUID uuid) {
        return selectedPattern.get(uuid);
    }

    public void clearTrims(UUID uuid) {
        editingTrims.put(uuid, new HashMap<>());
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
