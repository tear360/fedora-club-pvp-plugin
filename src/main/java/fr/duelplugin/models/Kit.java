package fr.duelplugin.models;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Kit {

    public static void giveKit(Player player, DuelGameMode mode) {
        PlayerInventory inv = player.getInventory();
        inv.clear();
        inv.setArmorContents(null);
        inv.setItemInOffHand(null);
        player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));

        switch (mode) {
            case SWORD -> giveSwordKit(inv);
            case AXE -> giveAxeKit(inv);
            case UHC -> giveUHCKit(inv);
            case POT -> givePotKit(inv);
            case NETHPOT -> giveNethPotKit(inv);
            case MACE -> giveMaceKit(inv);
            case VANILLA -> giveVanillaKit(inv);
            case SMP -> giveSMPKit(inv);
            case DIASMP -> giveDiaSMPKit(inv);
            case SPEAR_MACE -> giveSpearMaceKit(inv);
        }
    }

    private static void giveSwordKit(PlayerInventory inv) {
        inv.setItem(0, ench(Material.DIAMOND_SWORD, Enchantment.SWEEPING_EDGE, 3));
        inv.setArmorContents(new ItemStack[]{
                ench(Material.DIAMOND_BOOTS, Enchantment.PROTECTION, 3),
                ench(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION, 3),
                ench(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION, 3),
                ench(Material.DIAMOND_HELMET, Enchantment.PROTECTION, 3)
        });
    }

    private static void giveAxeKit(PlayerInventory inv) {
        inv.setItem(0, ench(Material.DIAMOND_SWORD));
        inv.setItem(1, ench(Material.DIAMOND_AXE));
        inv.setItem(2, new ItemStack(Material.BOW));
        inv.setItem(3, new ItemStack(Material.CROSSBOW));
        inv.setItem(8, new ItemStack(Material.ARROW, 6));
        inv.setArmorContents(new ItemStack[]{
                new ItemStack(Material.DIAMOND_BOOTS),
                new ItemStack(Material.DIAMOND_LEGGINGS),
                new ItemStack(Material.DIAMOND_CHESTPLATE),
                new ItemStack(Material.DIAMOND_HELMET)
        });
        inv.setItemInOffHand(new ItemStack(Material.SHIELD));
    }

    private static void giveUHCKit(PlayerInventory inv) {
        inv.setItem(0, ench(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 3));
        inv.setItem(1, ench(Material.DIAMOND_AXE, Enchantment.EFFICIENCY, 3));
        inv.setItem(2, ench(Material.BOW, Enchantment.POWER, 1));
        inv.setItem(3, ench(Material.CROSSBOW, Enchantment.PIERCING, 1));
        inv.setItem(4, ench(Material.DIAMOND_PICKAXE, Enchantment.EFFICIENCY, 3));
        inv.setItem(5, new ItemStack(Material.ARROW, 10));
        inv.setItem(6, new ItemStack(Material.GOLDEN_APPLE, 8));
        inv.setItem(7, new ItemStack(Material.PLAYER_HEAD, 2));
        inv.setItem(8, new ItemStack(Material.WATER_BUCKET, 4));
        inv.setItem(9, new ItemStack(Material.LAVA_BUCKET, 2));
        inv.setItem(10, new ItemStack(Material.OAK_PLANKS, 128));
        inv.setArmorContents(new ItemStack[]{
                ench(Material.DIAMOND_BOOTS, Enchantment.PROTECTION, 3),
                ench(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION, 2),
                ench(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION, 2),
                ench(Material.DIAMOND_HELMET, Enchantment.PROTECTION, 3)
        });
        inv.setItemInOffHand(new ItemStack(Material.SHIELD));
    }

    private static void givePotKit(PlayerInventory inv) {
        inv.setItem(0, ench(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3));
        inv.setItem(1, new ItemStack(Material.COOKED_BEEF, 5));
        for (int i = 2; i <= 27; i++) {
            inv.setItem(i, splash(PotionEffectType.INSTANT_HEALTH, 1));
        }
        inv.setItem(28, splash(PotionEffectType.STRENGTH, 1));
        inv.setItem(29, splash(PotionEffectType.STRENGTH, 1));
        inv.setItem(30, splash(PotionEffectType.STRENGTH, 1));
        inv.setItem(31, splash(PotionEffectType.SPEED, 1));
        inv.setItem(32, splash(PotionEffectType.SPEED, 1));
        inv.setItem(33, splash(PotionEffectType.SPEED, 1));
        inv.setItem(34, splash(PotionEffectType.REGENERATION, 1));
        inv.setItem(35, splash(PotionEffectType.REGENERATION, 1));
        inv.setArmorContents(new ItemStack[]{
                ench(Material.DIAMOND_BOOTS, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3),
                ench(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3),
                ench(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3),
                ench(Material.DIAMOND_HELMET, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3)
        });
    }

    private static void giveNethPotKit(PlayerInventory inv) {
        inv.setItem(0, ench(Material.NETHERITE_SWORD, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3));
        inv.setItem(1, new ItemStack(Material.GOLDEN_APPLE, 64));
        inv.setItem(2, new ItemStack(Material.EXPERIENCE_BOTTLE, 64));
        for (int i = 3; i <= 7; i++) {
            inv.setItem(i, splash(PotionEffectType.STRENGTH, 1));
            inv.setItem(i + 5, splash(PotionEffectType.SPEED, 1));
        }
        inv.setArmorContents(new ItemStack[]{
                ench(Material.NETHERITE_BOOTS, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                ench(Material.NETHERITE_LEGGINGS, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                ench(Material.NETHERITE_CHESTPLATE, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                ench(Material.NETHERITE_HELMET, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1)
        });
        inv.setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
        inv.setItem(12, new ItemStack(Material.TOTEM_OF_UNDYING));
    }

    private static void giveMaceKit(PlayerInventory inv) {
        inv.setItem(0, ench(Material.NETHERITE_SWORD, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3));
        inv.setItem(1, ench(Material.NETHERITE_AXE, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3));
        inv.setItem(2, ench(Material.MACE, Enchantment.BREACH, 4, Enchantment.UNBREAKING, 3));
        inv.setItem(3, ench(Material.MACE, Enchantment.DENSITY, 5, Enchantment.WIND_BURST, 1, Enchantment.UNBREAKING, 3));
        inv.setItem(4, new ItemStack(Material.GOLDEN_APPLE, 128));
        inv.setItem(5, new ItemStack(Material.ENDER_PEARL, 256));
        inv.setItem(6, new ItemStack(Material.WIND_CHARGE, 128));
        inv.setItem(7, new ItemStack(Material.ELYTRA));
        inv.setItem(8, ench(Material.SHIELD, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        inv.setArmorContents(new ItemStack[]{
                ench(Material.NETHERITE_BOOTS, Enchantment.PROTECTION, 4, Enchantment.FEATHER_FALLING, 4),
                ench(Material.NETHERITE_LEGGINGS, Enchantment.PROTECTION, 4),
                ench(Material.NETHERITE_CHESTPLATE, Enchantment.PROTECTION, 4),
                ench(Material.NETHERITE_HELMET, Enchantment.PROTECTION, 4)
        });
        inv.setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
        for (int i = 9; i <= 19; i++) {
            inv.setItem(i, splash(PotionEffectType.STRENGTH, 1));
        }
        for (int i = 20; i <= 29; i++) {
            inv.setItem(i, splash(PotionEffectType.SPEED, 1));
        }

        ItemStack shulker = new ItemStack(Material.SHULKER_BOX);
        ItemMeta shulkerMeta = shulker.getItemMeta();
        if (shulkerMeta instanceof BlockStateMeta bsm) {
            ShulkerBox shulkerBox = (ShulkerBox) bsm.getBlockState();
            for (int i = 0; i < 14; i++) {
                shulkerBox.getInventory().setItem(i, splash(PotionEffectType.SPEED, 1));
            }
            for (int i = 14; i < 27; i++) {
                shulkerBox.getInventory().setItem(i, splash(PotionEffectType.STRENGTH, 1));
            }
            bsm.setBlockState(shulkerBox);
            shulker.setItemMeta(bsm);
        }
        inv.setItem(30, shulker);
    }

    private static void giveVanillaKit(PlayerInventory inv) {
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

    private static void giveSMPKit(PlayerInventory inv) {
        inv.setItem(0, ench(Material.NETHERITE_SWORD, Enchantment.SHARPNESS, 5, Enchantment.FIRE_ASPECT, 2, Enchantment.UNBREAKING, 3));
        inv.setItem(1, ench(Material.NETHERITE_SWORD, Enchantment.SHARPNESS, 5, Enchantment.FIRE_ASPECT, 2, Enchantment.KNOCKBACK, 1, Enchantment.UNBREAKING, 3));
        inv.setItem(2, ench(Material.NETHERITE_AXE, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3));
        inv.setItem(3, new ItemStack(Material.GOLDEN_APPLE, 128));
        inv.setItem(4, new ItemStack(Material.EXPERIENCE_BOTTLE, 64));
        inv.setItem(5, new ItemStack(Material.ENDER_PEARL, 128));
        inv.setItem(6, new ItemStack(Material.TOTEM_OF_UNDYING));
        inv.setArmorContents(new ItemStack[]{
                ench(Material.NETHERITE_BOOTS, Enchantment.PROTECTION, 4, Enchantment.FEATHER_FALLING, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                ench(Material.NETHERITE_LEGGINGS, Enchantment.PROTECTION, 4, Enchantment.SWIFT_SNEAK, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                ench(Material.NETHERITE_CHESTPLATE, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                ench(Material.NETHERITE_HELMET, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1)
        });
        inv.setItemInOffHand(ench(Material.SHIELD, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        for (int i = 7; i <= 18; i++) {
            inv.setItem(i, splashWithTwo(PotionEffectType.STRENGTH, 1, PotionEffectType.SPEED, 1));
        }
        inv.setItem(19, splash(PotionEffectType.FIRE_RESISTANCE, 0));
        inv.setItem(20, splash(PotionEffectType.FIRE_RESISTANCE, 0));
        inv.setItem(21, splash(PotionEffectType.FIRE_RESISTANCE, 0));
    }

    private static void giveDiaSMPKit(PlayerInventory inv) {
        inv.setItem(0, ench(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 5, Enchantment.FIRE_ASPECT, 2, Enchantment.UNBREAKING, 3));
        inv.setItem(1, ench(Material.DIAMOND_AXE, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3));
        inv.setItem(2, ench(Material.NETHERITE_PICKAXE, Enchantment.EFFICIENCY, 5, Enchantment.SILK_TOUCH, 1, Enchantment.MENDING, 1, Enchantment.UNBREAKING, 3));
        inv.setItem(3, new ItemStack(Material.GOLDEN_APPLE, 128));
        inv.setItem(4, new ItemStack(Material.EXPERIENCE_BOTTLE, 64));
        inv.setItem(5, new ItemStack(Material.ENDER_PEARL, 128));
        inv.setItem(6, new ItemStack(Material.TOTEM_OF_UNDYING));
        inv.setItem(7, new ItemStack(Material.OAK_LOG, 64));
        inv.setItem(8, new ItemStack(Material.COBWEB, 64));
        inv.setItem(9, new ItemStack(Material.CHORUS_FRUIT, 64));
        inv.setArmorContents(new ItemStack[]{
                ench(Material.DIAMOND_BOOTS, Enchantment.PROTECTION, 4, Enchantment.FEATHER_FALLING, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                ench(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION, 4, Enchantment.SWIFT_SNEAK, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                ench(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                ench(Material.DIAMOND_HELMET, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1)
        });
        inv.setItemInOffHand(ench(Material.SHIELD, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        for (int i = 10; i <= 25; i++) {
            inv.setItem(i, splashWithTwo(PotionEffectType.STRENGTH, 1, PotionEffectType.SPEED, 1));
        }
        inv.setItem(26, splash(PotionEffectType.SPEED, 1));
        inv.setItem(27, splash(PotionEffectType.SPEED, 1));
        inv.setItem(28, splash(PotionEffectType.SPEED, 1));
        inv.setItem(29, splash(PotionEffectType.FIRE_RESISTANCE, 0));
        inv.setItem(30, splash(PotionEffectType.FIRE_RESISTANCE, 0));
        inv.setItem(31, splash(PotionEffectType.FIRE_RESISTANCE, 0));
    }

    private static void giveSpearMaceKit(PlayerInventory inv) {
        inv.setItem(0, ench(Material.NETHERITE_SWORD, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3));
        inv.setItem(1, ench(Material.NETHERITE_AXE, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3));
        inv.setItem(2, ench(Material.TRIDENT, Enchantment.IMPALING, 5, Enchantment.LOYALTY, 3, Enchantment.UNBREAKING, 3));
        inv.setItem(3, ench(Material.MACE, Enchantment.DENSITY, 5, Enchantment.WIND_BURST, 1));
        inv.setItem(4, new ItemStack(Material.WIND_CHARGE, 192));
        inv.setItem(5, new ItemStack(Material.ENDER_PEARL, 48));
        inv.setArmorContents(new ItemStack[]{
                ench(Material.NETHERITE_BOOTS, Enchantment.PROTECTION, 4, Enchantment.FEATHER_FALLING, 4, Enchantment.UNBREAKING, 3),
                ench(Material.NETHERITE_LEGGINGS, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3),
                ench(Material.NETHERITE_CHESTPLATE, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3),
                ench(Material.NETHERITE_HELMET, Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3)
        });
        inv.setItemInOffHand(ench(Material.SHIELD, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        for (int i = 6; i <= 16; i++) {
            inv.setItem(i, splashWithDuration(PotionEffectType.STRENGTH, 1, 1800));
        }
        for (int i = 17; i <= 27; i++) {
            inv.setItem(i, splashWithDuration(PotionEffectType.SPEED, 1, 1800));
        }
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

    private static ItemStack splashWithTwo(PotionEffectType type1, int amp1, PotionEffectType type2, int amp2) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof org.bukkit.inventory.meta.PotionMeta pm) {
            pm.addCustomEffect(new PotionEffect(type1, 1, amp1), true);
            pm.addCustomEffect(new PotionEffect(type2, 1, amp2), true);
            item.setItemMeta(pm);
        }
        return item;
    }

    private static ItemStack splashWithDuration(PotionEffectType type, int amplifier, int durationTicks) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof org.bukkit.inventory.meta.PotionMeta pm) {
            pm.addCustomEffect(new PotionEffect(type, durationTicks, amplifier), true);
            item.setItemMeta(pm);
        }
        return item;
    }
}
