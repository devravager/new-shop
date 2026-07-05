package com.smpshop.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CategoryMenuHolder implements InventoryHolder {

    private final String categoryKey;
    private Inventory inventory;

    public CategoryMenuHolder(String categoryKey) {
        this.categoryKey = categoryKey;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
