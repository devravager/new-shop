package com.smpshop.config;

import com.smpshop.model.ShopCategory;
import com.smpshop.model.ShopItemDef;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Parses config.yml into in-memory model objects and exposes
 * convenience getters used by the GUI and commands.
 */
public class ShopConfig {

    private final JavaPlugin plugin;

    private double sellMultiplier;
    private int maxQuantityPerPurchase;

    private String mainMenuTitle;
    private int mainMenuSize;
    private Material borderMaterial;
    private String borderName;

    private final Map<String, ShopCategory> categories = new LinkedHashMap<>();

    public ShopConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();
        categories.clear();

        sellMultiplier = cfg.getDouble("settings.sell-multiplier", 0.5);
        maxQuantityPerPurchase = cfg.getInt("settings.max-quantity-per-purchase", 6400);

        mainMenuTitle = color(cfg.getString("gui.main-menu-title", "&8Shop"));
        mainMenuSize = cfg.getInt("gui.main-menu-size", 27);
        String borderMatName = cfg.getString("gui.border.material", "GRAY_STAINED_GLASS_PANE");
        borderMaterial = safeMaterial(borderMatName, Material.GRAY_STAINED_GLASS_PANE);
        borderName = color(cfg.getString("gui.border.name", " "));

        ConfigurationSection categoriesSection = cfg.getConfigurationSection("categories");
        ConfigurationSection categorySlots = cfg.getConfigurationSection("gui.category-slots");
        ConfigurationSection shopsSection = cfg.getConfigurationSection("shops");

        if (categoriesSection == null) {
            plugin.getLogger().warning("No 'categories' section found in config.yml!");
            return;
        }

        for (String catKey : categoriesSection.getKeys(false)) {
            ConfigurationSection catSec = categoriesSection.getConfigurationSection(catKey);
            if (catSec == null) continue;

            Material icon = safeMaterial(catSec.getString("icon", "CHEST"), Material.CHEST);
            String name = color(catSec.getString("name", catKey));
            java.util.List<String> lore = new java.util.ArrayList<>();
            for (String line : catSec.getStringList("lore")) {
                lore.add(color(line));
            }
            String menuTitle = color(catSec.getString("menu-title", name));
            int slot = categorySlots != null ? categorySlots.getInt(catKey, 0) : 0;

            ShopCategory category = new ShopCategory(catKey, icon, name, lore, slot, menuTitle);

            if (shopsSection != null) {
                ConfigurationSection shopSec = shopsSection.getConfigurationSection(catKey + ".items");
                if (shopSec != null) {
                    for (String itemKey : shopSec.getKeys(false)) {
                        ConfigurationSection itemSec = shopSec.getConfigurationSection(itemKey);
                        if (itemSec == null) continue;

                        Material material = safeMaterial(itemSec.getString("material", itemKey.toUpperCase()), null);
                        if (material == null) {
                            plugin.getLogger().log(Level.WARNING,
                                    "Skipping item '" + itemKey + "' in category '" + catKey + "': unknown material.");
                            continue;
                        }
                        int itemSlot = itemSec.getInt("slot", 0);
                        double price = itemSec.getDouble("price", 0);
                        double sell = itemSec.contains("sell")
                                ? itemSec.getDouble("sell")
                                : price * sellMultiplier;

                        category.addItem(new ShopItemDef(itemKey, catKey, material, itemSlot, price, sell));
                    }
                }
            }

            categories.put(catKey, category);
        }
    }

    private Material safeMaterial(String name, Material fallback) {
        if (name == null) return fallback;
        try {
            return Material.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Unknown material '" + name + "' in config.yml.");
            return fallback;
        }
    }

    public static String color(String input) {
        if (input == null) return "";
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', input);
    }

    public String message(String path) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        String msg = plugin.getConfig().getString("messages." + path, "");
        return color(prefix + msg);
    }

    public org.bukkit.Sound getSound(String path, org.bukkit.Sound fallback) {
        String name = plugin.getConfig().getString("sounds." + path);
        if (name == null) return fallback;
        try {
            return org.bukkit.Sound.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    public float getSoundVolume() {
        return (float) plugin.getConfig().getDouble("sounds.volume", 0.6);
    }

    public float getSoundPitch() {
        return (float) plugin.getConfig().getDouble("sounds.pitch", 1.0);
    }

    public double getSellMultiplier() {
        return sellMultiplier;
    }

    public int getMaxQuantityPerPurchase() {
        return maxQuantityPerPurchase;
    }

    public String getMainMenuTitle() {
        return mainMenuTitle;
    }

    public int getMainMenuSize() {
        return mainMenuSize;
    }

    public Material getBorderMaterial() {
        return borderMaterial;
    }

    public String getBorderName() {
        return borderName;
    }

    public Map<String, ShopCategory> getCategories() {
        return categories;
    }

    public ShopCategory getCategory(String key) {
        return categories.get(key);
    }

    /** Finds an item definition by scanning every category. */
    public ShopItemDef findItemByMaterial(Material material) {
        for (ShopCategory cat : categories.values()) {
            for (ShopItemDef def : cat.getItems().values()) {
                if (def.getMaterial() == material) {
                    return def;
                }
            }
        }
        return null;
    }
}
