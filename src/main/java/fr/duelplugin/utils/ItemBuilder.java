package fr.duelplugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ItemBuilder {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    private Component parseComponent(String text) {
        return LEGACY.deserialize(text.replace('&', '\u00A7'));
    }

    public ItemBuilder name(String name) {
        if (meta != null) meta.displayName(parseComponent(name));
        return this;
    }

    public ItemBuilder lore(String... lore) {
        if (meta != null) {
            List<Component> components = Arrays.stream(lore)
                    .map(this::parseComponent)
                    .collect(Collectors.toList());
            meta.lore(components);
        }
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
