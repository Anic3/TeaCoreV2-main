package me.tuanvo0022.utils;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import dev.s7a.base64.Base64ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ItemBuilder {
    private final ItemStack itemStack;
    private final ItemMeta itemMeta;
    
    public static ItemBuilder copyOf(@NotNull ItemStack item) {
        return new ItemBuilder(item.clone());
    }
    
    public ItemBuilder(@NotNull Material material) {
        this.itemStack = new ItemStack(material);
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder name(@NotNull String name) {
        itemMeta.setDisplayName(name);
        return this;
    }

    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder skull(@NotNull Player player) {
        SkullMeta skullMeta = (SkullMeta) itemMeta;
        skullMeta.setOwningPlayer(player);
        return this;
    }

    public ItemBuilder skull(@NotNull String playerName) {
        SkullMeta skullMeta = (SkullMeta) itemMeta;
        skullMeta.setOwner(playerName);
        return this;
    }

    public ItemBuilder customModelData(int customModelData) {
        itemMeta.setCustomModelData(customModelData);
        return this;
    }

    public ItemBuilder glow() {
        itemMeta.addEnchant(Enchantment.MENDING, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder lore(@NotNull List<String> lore) {
        itemMeta.setLore(lore);
        return this;
    }

    public ItemBuilder lore(@NotNull String... lines) {
        itemMeta.setLore(new ArrayList<>(Arrays.asList(lines)));
        return this;
    }

    public ItemBuilder addLore(@NotNull List<String> lines) {
        List<String> currentLore = itemMeta.getLore();
        if (currentLore == null) {
            currentLore = new ArrayList<>();
        }
        currentLore.addAll(lines);
        itemMeta.setLore(currentLore);
        return this;
    }

    public ItemBuilder addLore(@NotNull String... lines) {
        List<String> currentLore = itemMeta.getLore();
        if (currentLore == null) {
            currentLore = new ArrayList<>();
        }
        currentLore.addAll(Arrays.asList(lines));
        itemMeta.setLore(currentLore);
        return this;
    }

    public ItemBuilder color(@NotNull Color color) {
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) itemMeta;
        armorMeta.setColor(color);
        return this;
    }

    public ItemBuilder itemFlags(@NotNull ItemFlag... flags) {
        itemMeta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder enchantment(@NotNull Enchantment enchantment, int level) {
        itemMeta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder placeholders(@NotNull Map<String, String> placeholders) {
        if (itemMeta.hasDisplayName()) {
            String name = itemMeta.getDisplayName();
            itemMeta.setDisplayName(replacePlaceholders(name, placeholders));
        }
        if (itemMeta.hasLore()) {
            List<String> lore = itemMeta.getLore();
            lore.replaceAll(line -> replacePlaceholders(line, placeholders));
            itemMeta.setLore(lore);
        }
        return this;
    }

    private String replacePlaceholders(@NotNull String text, @NotNull Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return text;
    }

    public ItemBuilder effect(@NotNull PotionEffect effect) {
        PotionMeta potionMeta = (PotionMeta) itemMeta;
        potionMeta.addCustomEffect(effect, true);
        return this;
    }

    public ItemBuilder potionColor(@NotNull Color color) {
        PotionMeta potionMeta = (PotionMeta) itemMeta;
        potionMeta.setColor(color);
        return this;
    }
    
    public ItemBuilder trim(@NotNull TrimMaterial trimMaterial, TrimPattern trimPattern) {
        ArmorMeta armorMeta = (ArmorMeta) itemMeta;
        armorMeta.setTrim(new ArmorTrim(trimMaterial, trimPattern));
        return this;
    }

    public ItemStack build() {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
    
    public static ItemStack fromConfig(ConfigurationSection section, String... replacements) {
        return fromConfig(null, section, -1, replacements);
    }
    
    public static ItemStack fromConfig(ConfigurationSection section, int amount, String... replacements) {
        return fromConfig(null, section, amount, replacements);
    }
    
    public static ItemStack fromConfig(ItemStack itemStack, ConfigurationSection section, int amount, String... replacements) {
        if (section == null) {
            throw new IllegalArgumentException("Item config section is null!");
        }
        
        String base64 = section.getString("base64", "");
        if (base64 != null && !base64.isEmpty()) {
            ItemStack decoded = Base64ItemStack.decode(base64);
            return decoded;
        }
        
        Material material = null;
        if (itemStack == null) {
            String materialName = section.getString("material", "AIR");
            try {
                material = Material.valueOf(materialName.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid material type: " + materialName);
            }
        }
        
        if (amount == -1) {
            amount = section.getInt("amount", 1);
        }
        
        ItemBuilder item;
        if (itemStack != null) {
            item = new ItemBuilder(itemStack).amount(amount);
        } else {
            item = new ItemBuilder(material).amount(amount);
        }
        

        // Display name
        String displayName = section.getString("name");
        if (displayName != null && !displayName.isEmpty()) {
            displayName = ColorUtil.legacyHex(displayName);
            displayName = applyReplacements(displayName, replacements);
            item.name(displayName);
        }

        // Lore
        List<String> lore = section.getStringList("lore");
        if (!lore.isEmpty()) {
            lore.replaceAll(ColorUtil::legacyHex);
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, applyReplacements(lore.get(i), replacements));
            }
            item.lore(lore);
        }

        // Item Flags
        List<String> flagStrings = section.getStringList("flags");
        if (!flagStrings.isEmpty()) {
            ItemFlag[] itemFlags = flagStrings.stream()
                .map(ItemFlag::valueOf)
                .toArray(ItemFlag[]::new);
            item.itemFlags(itemFlags);
        }

        // Custom model data
        int customModelData = section.getInt("custom-model-data");
        if (customModelData > 0) {
            item.customModelData(customModelData);
        }

        // Potion Color
        String colorString = section.getString("potion-color");
        if (colorString != null) {
            try {
                Color color = Color.fromRGB(Integer.parseInt(colorString.replace("#", ""), 16));
                item.potionColor(color);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid potion color: " + colorString);
            }
        }

        // Potion Effects
        List<String> effects = section.getStringList("effects");
        for (String eff : effects) {
            String[] split = eff.split(":");
            if (split.length != 3) throw new IllegalArgumentException("Invalid effect format: " + eff);

            PotionEffectType type = PotionEffectType.getByName(split[0].toUpperCase());
            if (type == null) throw new IllegalArgumentException("Unknown potion effect: " + split[0]);

            int level, duration;
            try {
                level = Integer.parseInt(split[1]) - 1;
                duration = Integer.parseInt(split[2]) * 20;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid level/duration in effect: " + eff);
            }

            PotionEffect potionEffect = new PotionEffect(type, duration, level);
            item.effect(potionEffect);
        }

        // Trim
        String trimMaterialName = section.getString("trim.material");
        String trimPatternName = section.getString("trim.pattern");
        if (trimMaterialName != null && trimPatternName != null) {
            TrimMaterial trimMaterial = Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(trimMaterialName.toLowerCase()));
            TrimPattern trimPattern = Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(trimPatternName.toLowerCase()));

            if (trimMaterial == null) throw new IllegalArgumentException("Invalid trim material: " + trimMaterialName);
            if (trimPattern == null) throw new IllegalArgumentException("Invalid trim pattern: " + trimPatternName);

            item.trim(trimMaterial, trimPattern);
        }

        // Enchantments
        List<String> enchantments = section.getStringList("enchantments");
        for (String enchantment : enchantments) {
            String[] split = enchantment.split(":");
            if (split.length != 2) throw new IllegalArgumentException("Invalid enchantment: " + enchantment);

            Enchantment enchant = Enchantment.getByName(split[0].toUpperCase());
            if (enchant == null) throw new IllegalArgumentException("Unknown enchantment: " + split[0]);

            int level;
            try {
                level = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid enchantment level: " + split[1]);
            }

            item.enchantment(enchant, level);
        }

        return item.build();
    }

    private static String applyReplacements(String msg, String... replacements) {
        if (replacements == null || replacements.length == 0) return msg;

        if (replacements.length % 2 == 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                String target = replacements[i];
                String replacement = replacements[i + 1];
                if (target != null && replacement != null) {
                    msg = msg.replace(target, replacement);
                }
            }
        } else {
            Bukkit.getLogger().warning("Invalid replacements provided for item text: " + Arrays.toString(replacements));
        }
        return msg;
    }
    
    public static String getItemName(ItemStack itemStack) {
        if (itemStack == null) return "";

        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            return itemStack.getItemMeta().getDisplayName();
        }

        String name = itemStack.getType().name().replace("_", " ").toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}