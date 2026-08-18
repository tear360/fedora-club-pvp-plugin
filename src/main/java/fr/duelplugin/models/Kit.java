package fr.duelplugin.models;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Kit {

    public static void giveKit(Inventory inventory, DuelGameMode mode) {
        inventory.clear();
        inventory.setArmorContents(null);
        inventory.setItemInOffHand(null);

        switch (mode) {
            case SWORD -> giveSwordKit(inventory);
            case AXE -> giveAxeKit(inventory);
            case UHC -> giveUHCKit(inventory);
            case POT -> givePotKit(inventory);
            case NETHPOT -> giveNethPotKit(inventory);
            case MACE -> giveMaceKit(inventory);
            case VANILLA -> giveVanillaKit(inventory);
            case SMP -> giveSMPKit(inventory);
            case DIASMP -> giveDiaSMPKit(inventory);
            case SPEAR_MACE -> giveSpearMaceKit(inventory);
        }
    }

    private static void giveSwordKit(Inventory inv) {
        inv.setItem(0, ench(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 5, Enchantment.SWEEPING_EDGE, 3, Enchantment.UNBREAKING, 3));
        inv.setItem(1, ench(Material.BOW, Enchantment.POWER, 2, Enchantment.UNBREAKING, 3));
        inv.setItem(2, new ItemStack(Material.ARROW, 32));
        inv.setItem(3, new ItemStack(Material.ENDER_PEARL, 8));
        inv.setItem(4, new ItemStack(Material.GOLDEN_APPLE, 8));
        inv.setItem(8, ench(Material.SHIELD, Enchantment.UNBREAKING, 3));

        inv.setArmorContents(new ItemStack[]{
                ench(Material.DIAMOND_BOOTS, Enchantment.PROTECTION, 3, Enchantment.UNBREAKING, 3),
                ench(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION, 3, Enchantment.UNBREAKING, 3),
                ench(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION, 3, Enchantment.UNBREAKING, 3),
                ench(Material.DIAMOND_HELMET, Enchantment.PROTECTION, 3, Enchantment.UNBREAKING, 3)
        });
    }

    private static void giveAxeKit(Inventory inv) {
        inv.setItem(0, ench(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3));
        inv.setItem(1, ench(Material.DIAMOND_AXE, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3));
        inv.setItem(2, ench(Material.BOW, Enchantment.UNBREAKING, 3));
        inv.setItem(3, new ItemStack(Material.CROSSBOW));
        inv.setItem(5, new ItemStack(Material.GOLDEN_APPLE, 8));
        inv.setItem(8, new ItemStack(Material.ARROW, 6));

        inv.setArmorContents(new ItemStack[]{
                new ItemStack(Material.DIAMOND_BOOTS),
                new ItemStack(Material.DIAMOND_LEGGINGS),
                new ItemStack(Material.DIAMOND_CHESTPLATE),
                new ItemStack(Material.DIAMOND_HELMET)
        });
        inv.setItemInOffHand(ench(Material.SHIELD, Enchantment.UNBREAKING, 3));
    }

    private static void giveUHCKit(Inventory inv) {
        inv.setItem(0, ench(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 3));
        inv.setItem(1, ench(Material.DIAMOND_AXE, Enchantment.EFFICIENCY, 3));
        inv.setItem(2, ench(Material.BOW, Enchantment.POWER, 1));
        inv.setItem(3, ench(Material.CROSSBOW, Enchantment.PIERCING, 1));
        inv.setItem(4, new ItemStack(Material.GOLDEN_APPLE, 8));
        inv.setItem(5, new ItemStack(Material.GOLDEN_HEAD, 2));
        inv.setItem(6, new ItemStack(Material.OAK_PLANKS, 64));
        inv.setItem(7, new ItemStack(Material.OAK_PLANKS, 64));
        inv.setItem(8, ench(Material.DIAMOND_PICKAXE, Enchantment.EFFICIENCY, 3));

        inv.setArmorContents(new ItemStack[]{
                ench(Material.DIAMOND_BOOTS, Enchantment.PROTECTION, 3),
                ench(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION, 2),
                ench(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION, 2),
                ench(Material.DIAMOND_HELMET, Enchantment.PROTECTION, 3)
        });
        inv.setItemInOffHand(ench(Material.SHIELD));

        ItemStack extra = new ItemStack(Material.CHEST);
        inv.setItem(17, extra);
    }

    private static void givePotKit(Inventory inv) {
        inv.setItem(0, ench(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3));

        inv.setItem(1, splash(PotionEffectType.INSTANT_HEALTH, 1));
        inv.setItem(2, splash(PotionEffectType.INSTANT_HEALTH, 1));
        inv.setItem(3, splash(PotionEffectType.INSTANT_HEALTH, 1));
        inv.setItem(4, splash(PotionEffectType.STRENGTH, 1));
        inv.setItem(5, splash(PotionEffectType.SPEED, 1));
        inv.setItem(6, splash(PotionEffectType.REGENERATION, 1));
        inv.setItem(7, new ItemStack(Material.COOKED_BEEF, 5));
        inv.setItem(8, splash(PotionEffectType.INSTANT_HEALTH, 1));

        inv.setArmorContents(new ItemStack[]{
                ench(Material.DIAMOND_BOOTS, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3),
                ench(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3),
                ench(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3),
                ench(Material.DIAMOND_HELMET, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3)
        });
    }

    private static void giveNethPotKit(Inventory inv) {
        inv.setItem(0, ench(Material.NETHERITE_SWORD, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3));

        for (int i = 1; i <= 5; i++) {
            inv.setItem(i, splash(PotionEffectType.STRENGTH, 1));
            inv.setItem(i + 5, splash(PotionEffectType.SPEED, 1));
        }
        inv.setItem(11, new ItemStack(Material.GOLDEN_APPLE, 64));
        inv.setItem(12, new ItemStack(Material.XP_BOTTLE, 64));

        inv.setArmorContents(new ItemStack[]{
                ench(Material.NETHERITE_BOOTS, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                ench(Material.NETHERITE_LEGGINGS, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                ench(Material.NETHERITE_CHESTPLATE, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                ench(Material.NETHERITE_HELMET, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1)
        });
        inv.setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
        inv.setItem(13, new ItemStack(Material.TOTEM_OF_UNDYING));
    }

    private static void giveMaceKit(Inventory inv) {
        inv.setItem(0, ench(Material.NETHERITE_SWORD, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3));
        inv.setItem(1, ench(Material.NETHERITE_AXE, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3));
        inv.setItem(2, ench(Material.MACE, Enchantment.BREACH, 4, Enchantment.UNBREAKING, 3));
        inv.setItem(3, ench(Material.MACE, Enchantment.DENSITY, 5, Enchantment.WIND_BURST, 1, Enchantment.UNBREAKING, 3));
        inv.setItem(4, new ItemStack(Material.ENDER_PEARL, 64));
        inv.setItem(5, new ItemStack(Material.ENDER_PEARL, 64));
        inv.setItem(6, new ItemStack(Material.GOLDEN_APPLE, 64));
        inv.setItem(7, new ItemStack(Material.GOLDEN_APPLE, 64));
        inv.setItem(8, ench(Material.SHIELD, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));

        inv.setArmorContents(new ItemStack[]{
                ench(Material.NETHERITE_BOOTS, Enchantment.PROTECTION, 4, Enchantment.FEATHER_FALLING, 4),
                ench(Material.NETHERITE_LEGGINGS, Enchantment.PROTECTION, 4),
                ench(Material.NETHERITE_CHESTPLATE, Enchantment.PROTECTION, 4),
                ench(Material.NETHERITE_HELMET, Enchantment.PROTECTION, 4)
        });
        inv.setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
    }

    private static void giveVanillaKit(Inventory inv) {
        inv.setItem(0, ench(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3));
        inv.setItem(1, ench(Material.BOW, Enchantment.POWER, 2, Enchantment.UNBREAKING, 3));
        inv.setItem(2, new ItemStack(Material.ARROW, 32));
        inv.setItem(3, new ItemStack(Material.ENDER_PEARL, 8));
        inv.setItem(4, new ItemStack(Material.GOLDEN_APPLE, 8));
        inv.setItem(5, new ItemStack(Material.OAK_PLANKS, 64));
        inv.setItem(6, new ItemStack(Material.COBBLESTONE, 64));
        inv.setItem(7, new ItemStack(Material.WATER_BUCKET));
        inv.setItem(8, ench(Material.SHIELD, Enchantment.UNBREAKING, 3));

        inv.setArmorContents(new ItemStack[]{
                ench(Material.DIAMOND_BOOTS, Enchantment.PROTECTION, 3, Enchantment.UNBREAKING, 3),
                ench(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION, 3, Enchantment.UNBREAKING, 3),
                ench(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3),
                ench(Material.DIAMOND_HELMET, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3)
        });
    }

    private static void giveSMPKit(Inventory inv) {
        inv.setItem(0, ench(Material.NETHERITE_SWORD, Enchantment.SHARPNESS, 5, Enchantment.FIRE_ASPECT, 2, Enchantment.KNOCKBACK, 1, Enchantment.UNBREAKING, 3));
        inv.setItem(1, ench(Material.NETHERITE_SWORD, Enchantment.SHARPNESS, 5, Enchantment.FIRE_ASPECT, 2, Enchantment.UNBREAKING, 3));
        inv.setItem(2, ench(Material.NETHERITE_AXE, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3));
        inv.setItem(3, new ItemStack(Material.ENDER_PEARL, 64));
        inv.setItem(4, new ItemStack(Material.ENDER_PEARL, 64));
        inv.setItem(5, new ItemStack(Material.GOLDEN_APPLE, 64));
        inv.setItem(6, new ItemStack(Material.GOLDEN_APPLE, 64));
        inv.setItem(7, new ItemStack(Material.XP_BOTTLE, 64));
        inv.setItem(8, new ItemStack(Material.TOTEM_OF_UNDYING));

        inv.setArmorContents(new ItemStack[]{
                ench(Material.NETHERITE_BOOTS, Enchantment.PROTECTION, 4, Enchantment.FEATHER_FALLING, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                ench(Material.NETHERITE_LEGGINGS, Enchantment.PROTECTION, 4, Enchantment.SWIFT_SNEAK, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                ench(Material.NETHERITE_CHESTPLATE, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                ench(Material.NETHERITE_HELMET, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1)
        });
        inv.setItemInOffHand(ench(Material.SHIELD, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
    }

    private static void giveDiaSMPKit(Inventory inv) {
        inv.setItem(0, ench(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 5, Enchantment.FIRE_ASPECT, 2, Enchantment.UNBREAKING, 3));
        inv.setItem(1, ench(Material.DIAMOND_AXE, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3));
        inv.setItem(2, ench(Material.NETHERITE_PICKAXE, Enchantment.EFFICIENCY, 5, Enchantment.SILK_TOUCH, 1, Enchantment.MENDING, 1, Enchantment.UNBREAKING, 3));
        inv.setItem(3, new ItemStack(Material.ENDER_PEARL, 64));
        inv.setItem(4, new ItemStack(Material.GOLDEN_APPLE, 64));
        inv.setItem(5, new ItemStack(Material.GOLDEN_APPLE, 64));
        inv.setItem(6, new ItemStack(Material.XP_BOTTLE, 64));
        inv.setItem(7, new ItemStack(Material.OAK_LOG, 64));
        inv.setItem(8, new ItemStack(Material.TOTEM_OF_UNDYING));

        inv.setArmorContents(new ItemStack[]{
                ench(Material.DIAMOND_BOOTS, Enchantment.PROTECTION, 4, Enchantment.FEATHER_FALLING, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                ench(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION, 4, Enchantment.SWIFT_SNEAK, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                ench(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                ench(Material.DIAMOND_HELMET, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1)
        });
        inv.setItemInOffHand(ench(Material.SHIELD, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
    }

    private static void giveSpearMaceKit(Inventory inv) {
        inv.setItem(0, ench(Material.NETHERITE_SWORD, Enchantment.SHARPNESS, 5, Enchantment.SWEEPING_EDGE, 3, Enchantment.KNOCKBACK, 1, Enchantment.UNBREAKING, 3));
        inv.setItem(1, ench(Material.NETHERITE_AXE, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3));
        inv.setItem(2, ench(Material.TRIDENT, Enchantment.IMPALING, 5, Enchantment.UNBREAKING, 3));
        inv.setItem(3, ench(Material.MACE, Enchantment.DENSITY, 5, Enchantment.WIND_BURST, 1, Enchantment.UNBREAKING, 3));
        inv.setItem(4, new ItemStack(Material.ENDER_PEARL, 16));
        inv.setItem(5, new ItemStack(Material.GOLDEN_APPLE, 64));
        inv.setItem(6, new ItemStack(Material.WIND_CHARGE, 64));
        inv.setItem(7, new ItemStack(Material.ELYTRA));
        inv.setItem(8, ench(Material.SHIELD, Enchantment.MENDING, 1, Enchantment.UNBREAKING, 3));

        inv.setArmorContents(new ItemStack[]{
                ench(Material.NETHERITE_BOOTS, Enchantment.PROTECTION, 4, Enchantment.FEATHER_FALLING, 4),
                ench(Material.NETHERITE_LEGGINGS, Enchantment.PROTECTION, 4),
                ench(Material.NETHERITE_CHESTPLATE, Enchantment.PROTECTION, 4),
                ench(Material.NETHERITE_HELMET, Enchantment.PROTECTION, 4)
        });
        inv.setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
    }

    private static ItemStack ench(Material mat, Object... enchPairs) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            for (int i = 0; i < enchPairs.length; i += 2) {
                if (enchPairs[i] instanceof Enchantment e && enchPairs[i + 1] instanceof Integer l) {
                    meta.addEnchant(e, l, true);
                }
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack splash(PotionEffectType type, int amplifier) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof org.bukkit.inventory.meta.PotionMeta pm) {
            pm.addCustomEffect(new PotionEffect(type, 1, amplifier), true);
            item.setItemMeta(pm);
        }
        return item;
    }
}
