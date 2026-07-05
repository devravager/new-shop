package com.smpshop.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class QuantityMenuHolder implements InventoryHolder {

    private final String categoryKey;
    private final String itemKey;
    private int quantity;
    private Inventory inventory;

    public QuantityMenuHolder(String categoryKey, String itemKey, int quantity) {
        this.categoryKey = categoryKey;
        this.itemKey = itemKey;
        this.quantity = quantity;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    public String getItemKey() {
        return itemKey;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
