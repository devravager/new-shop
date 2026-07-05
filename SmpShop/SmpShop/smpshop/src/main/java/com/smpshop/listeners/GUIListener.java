package com.smpshop.listeners;

import com.smpshop.config.ShopConfig;
import com.smpshop.economy.EconomyService;
import com.smpshop.gui.CategoryMenuHolder;
import com.smpshop.gui.MainMenuHolder;
import com.smpshop.gui.QuantityMenuHolder;
import com.smpshop.gui.ShopGUIManager;
import com.smpshop.model.ShopCategory;
import com.smpshop.model.ShopItemDef;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final ShopConfig config;
    private final EconomyService economy;
    private final ShopGUIManager guiManager;

    public GUIListener(ShopConfig config, EconomyService economy, ShopGUIManager guiManager) {
        this.config = config;
        this.economy = economy;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        InventoryHolder holder = topInventory.getHolder();

        if (!(holder instanceof MainMenuHolder)
                && !(holder instanceof CategoryMenuHolder)
                && !(holder instanceof QuantityMenuHolder)) {
            return;
        }

        // Always cancel: these are display-only menus, nothing should be picked up or shifted.
        event.setCancelled(true);

        // Ignore clicks that land in the player's own inventory while a shop menu is open.
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(topInventory)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (holder instanceof MainMenuHolder) {
            handleMainMenuClick(player, event.getSlot());
        } else if (holder instanceof CategoryMenuHolder categoryHolder) {
            handleCategoryMenuClick(player, categoryHolder, event.getSlot(), event.getClick());
        } else {
            handleQuantityMenuClick(player, (QuantityMenuHolder) holder, event.getSlot());
        }
    }

    // ---------------------------------------------------------------
    private void handleMainMenuClick(Player player, int slot) {
        for (ShopCategory category : config.getCategories().values()) {
            if (category.getMainMenuSlot() == slot) {
                player.openInventory(guiManager.buildCategoryMenu(category));
                playClick(player);
                return;
            }
        }
    }

    // ---------------------------------------------------------------
    private void handleCategoryMenuClick(Player player, CategoryMenuHolder holder, int slot, ClickType click) {
        ShopCategory category = config.getCategory(holder.getCategoryKey());
        if (category == null) return;

        if (slot == 49) {
            player.openInventory(guiManager.buildMainMenu());
            playClick(player);
            return;
        }

        ShopItemDef target = null;
        for (ShopItemDef def : category.getItems().values()) {
            if (def.getSlot() == slot) {
                target = def;
                break;
            }
        }
        if (target == null) return;

        if (click.isRightClick()) {
            player.openInventory(guiManager.buildQuantityMenu(category, target, 1));
            playClick(player);
        } else {
            attemptPurchase(player, category, target, 1);
        }
    }

    // ---------------------------------------------------------------
    private void handleQuantityMenuClick(Player player, QuantityMenuHolder holder, int slot) {
        ShopCategory category = config.getCategory(holder.getCategoryKey());
        if (category == null) return;
        ShopItemDef item = category.getItems().get(holder.getItemKey());
        if (item == null) return;

        int quantity = holder.getQuantity();
        int max = config.getMaxQuantityPerPurchase();

        switch (slot) {
            case 10 -> quantity = Math.max(1, quantity - 10);
            case 11 -> quantity = Math.max(1, quantity - 1);
            case 15 -> quantity = Math.min(max, quantity + 1);
            case 16 -> quantity = Math.min(max, quantity + 10);
            case 18 -> {
                player.openInventory(guiManager.buildCategoryMenu(category));
                playClick(player);
                return;
            }
            case 22 -> {
                attemptPurchase(player, category, item, quantity);
                return;
            }
            default -> {
                return;
            }
        }

        playClick(player);
        player.openInventory(guiManager.buildQuantityMenu(category, item, quantity));
    }

    // ---------------------------------------------------------------
    private void attemptPurchase(Player player, ShopCategory category, ShopItemDef item, int amount) {
        if (!economy.isAvailable()) {
            player.sendMessage(config.message("vault-missing"));
            return;
        }

        double total = item.getPrice() * amount;

        if (!economy.has(player, total)) {
            player.sendMessage(config.message("not-enough-money"));
            player.playSound(player.getLocation(),
                    config.getSound("purchase-fail", Sound.ENTITY_VILLAGER_NO),
                    config.getSoundVolume(), config.getSoundPitch());
            return;
        }

        if (!hasInventorySpace(player, amount)) {
            player.sendMessage(config.message("inventory-full"));
            return;
        }

        economy.withdraw(player, total);
        giveItems(player, item, amount);

        player.sendMessage(config.message("purchase-success")
                .replace("%amount%", String.valueOf(amount))
                .replace("%item%", prettify(item.getKey()))
                .replace("%price%", economy.format(total)));
        player.playSound(player.getLocation(),
                config.getSound("purchase-success", Sound.ENTITY_EXPERIENCE_ORB_PICKUP),
                config.getSoundVolume(), config.getSoundPitch());

        // Return the player to the category menu so they can keep shopping.
        player.openInventory(guiManager.buildCategoryMenu(category));
    }

    private boolean hasInventorySpace(Player player, int amount) {
        int freeSlots = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (stack == null) freeSlots++;
        }
        // Rough check: at least one free slot, or amount fits within a max stack size buffer.
        return freeSlots > 0 || amount <= 64;
    }

    private void giveItems(Player player, ShopItemDef item, int amount) {
        int remaining = amount;
        int maxStack = item.getMaterial().getMaxStackSize();
        while (remaining > 0) {
            int give = Math.min(maxStack, remaining);
            player.getInventory().addItem(new ItemStack(item.getMaterial(), give));
            remaining -= give;
        }
    }

    private void playClick(Player player) {
        player.playSound(player.getLocation(),
                config.getSound("click", Sound.UI_BUTTON_CLICK),
                config.getSoundVolume(), config.getSoundPitch());
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
