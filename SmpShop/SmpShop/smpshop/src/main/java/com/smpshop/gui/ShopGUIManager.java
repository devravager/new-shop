package com.smpshop.gui;

import com.smpshop.config.ShopConfig;
import com.smpshop.economy.EconomyService;
import com.smpshop.model.ShopCategory;
import com.smpshop.model.ShopItemDef;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopGUIManager {

    private final ShopConfig config;
    private final EconomyService economy;

    public ShopGUIManager(ShopConfig config, EconomyService economy) {
        this.config = config;
        this.economy = economy;
    }

    // ---------------------------------------------------------------
    // Main categories menu
    // ---------------------------------------------------------------
    public Inventory buildMainMenu() {
        MainMenuHolder holder = new MainMenuHolder();
        Inventory inv = org.bukkit.Bukkit.createInventory(holder, config.getMainMenuSize(), config.getMainMenuTitle());
        holder.setInventory(inv);

        fillBorder(inv);

        for (ShopCategory category : config.getCategories().values()) {
            ItemStack icon = namedItem(category.getIcon(), category.getDisplayName(), category.getLore());
            if (category.getMainMenuSlot() >= 0 && category.getMainMenuSlot() < inv.getSize()) {
                inv.setItem(category.getMainMenuSlot(), icon);
            }
        }
        return inv;
    }

    // ---------------------------------------------------------------
    // Category shop menu (lists every item in that category)
    // ---------------------------------------------------------------
    public Inventory buildCategoryMenu(ShopCategory category) {
        CategoryMenuHolder holder = new CategoryMenuHolder(category.getKey());
        Inventory inv = org.bukkit.Bukkit.createInventory(holder, 54, category.getMenuTitle());
        holder.setInventory(inv);

        fillBorder(inv);

        for (ShopItemDef item : category.getItems().values()) {
            inv.setItem(item.getSlot(), buildShopItemIcon(item));
        }

        // Back button
        ItemStack back = namedItem(Material.ARROW, ChatColor.YELLOW + "Back to Categories", List.of());
        inv.setItem(49, back);

        return inv;
    }

    private ItemStack buildShopItemIcon(ShopItemDef item) {
        String display = ChatColor.WHITE + prettify(item.getKey());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Price: " + ChatColor.GREEN + economy.format(item.getPrice()));
        lore.add(ChatColor.GRAY + "Sell:  " + ChatColor.GOLD + economy.format(item.getSellPrice()));
        lore.add("");
        lore.add(ChatColor.YELLOW + "Left-click " + ChatColor.GRAY + "to buy 1");
        lore.add(ChatColor.YELLOW + "Right-click " + ChatColor.GRAY + "to choose a quantity");
        return namedItem(item.getMaterial(), display, lore);
    }

    // ---------------------------------------------------------------
    // Quantity selector menu
    // ---------------------------------------------------------------
    public Inventory buildQuantityMenu(ShopCategory category, ShopItemDef item, int quantity) {
        QuantityMenuHolder holder = new QuantityMenuHolder(category.getKey(), item.getKey(), quantity);
        Inventory inv = org.bukkit.Bukkit.createInventory(holder, 27,
                ChatColor.DARK_GRAY + "Buy " + prettify(item.getKey()));
        holder.setInventory(inv);
        fillBorder(inv);

        inv.setItem(10, namedItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "-10", List.of()));
        inv.setItem(11, namedItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "-1", List.of()));

        List<String> centerLore = new ArrayList<>();
        centerLore.add(ChatColor.GRAY + "Quantity: " + ChatColor.WHITE + quantity);
        centerLore.add(ChatColor.GRAY + "Total: " + ChatColor.GREEN + economy.format(item.getPrice() * quantity));
        inv.setItem(13, namedItem(item.getMaterial(), ChatColor.WHITE + prettify(item.getKey()), centerLore));

        inv.setItem(15, namedItem(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "+1", List.of()));
        inv.setItem(16, namedItem(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "+10", List.of()));

        inv.setItem(22, namedItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "Confirm Purchase", List.of()));
        inv.setItem(18, namedItem(Material.BARRIER, ChatColor.RED + "Cancel", List.of()));

        return inv;
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private void fillBorder(Inventory inv) {
        ItemStack pane = namedItem(config.getBorderMaterial(), config.getBorderName(), List.of());
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, pane);
            }
        }
    }

    private ItemStack namedItem(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material == null ? Material.STONE : material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private String prettify(String key) {
        String[] parts = key.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }
}
