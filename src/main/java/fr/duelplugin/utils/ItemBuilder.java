package fr.duelplugin.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        if (meta != null) meta.setDisplayName(name);
        return this;
    }

    public ItemBuilder lore(String... lore) {
        if (meta != null) meta.setLore(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder glowing(boolean glowing) {
        if (meta != null) {
            meta.setEnchantmentGlintOverride(glowing);
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) item.setItemMeta(meta);
        return item;
    }
}
