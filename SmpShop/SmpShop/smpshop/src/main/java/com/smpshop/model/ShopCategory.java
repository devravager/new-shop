package com.smpshop.model;

import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A category shown in the main shop menu (e.g. "nether", "end").
 * Holds the display icon/name for the main menu button plus the
 * ordered map of items that appear when the category is opened.
 */
public class ShopCategory {

    private final String key;
    private final Material icon;
    private final String displayName;
    private final java.util.List<String> lore;
    private final int mainMenuSlot;
    private final String menuTitle;
    private final Map<String, ShopItemDef> items = new LinkedHashMap<>();

    public ShopCategory(String key, Material icon, String displayName, java.util.List<String> lore,
                         int mainMenuSlot, String menuTitle) {
        this.key = key;
        this.icon = icon;
        this.displayName = displayName;
        this.lore = lore;
        this.mainMenuSlot = mainMenuSlot;
        this.menuTitle = menuTitle;
    }

    public void addItem(ShopItemDef item) {
        items.put(item.getKey(), item);
    }

    public String getKey() {
        return key;
    }

    public Material getIcon() {
        return icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public java.util.List<String> getLore() {
        return lore;
    }

    public int getMainMenuSlot() {
        return mainMenuSlot;
    }

    public String getMenuTitle() {
        return menuTitle;
    }

    public Map<String, ShopItemDef> getItems() {
        return items;
    }
}
