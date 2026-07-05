package com.smpshop.model;

import org.bukkit.Material;

/**
 * Immutable definition of one shop item, as read from config.yml.
 */
public class ShopItemDef {

    private final String key;
    private final String categoryKey;
    private final Material material;
    private final int slot;
    private final double price;
    private final double sellPrice;

    public ShopItemDef(String key, String categoryKey, Material material, int slot, double price, double sellPrice) {
        this.key = key;
        this.categoryKey = categoryKey;
        this.material = material;
        this.slot = slot;
        this.price = price;
        this.sellPrice = sellPrice;
    }

    public String getKey() {
        return key;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    public Material getMaterial() {
        return material;
    }

    public int getSlot() {
        return slot;
    }

    public double getPrice() {
        return price;
    }

    public double getSellPrice() {
        return sellPrice;
    }
}
