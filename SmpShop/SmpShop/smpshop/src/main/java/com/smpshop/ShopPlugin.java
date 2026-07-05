package com.smpshop;

import com.smpshop.commands.SellCommand;
import com.smpshop.commands.ShopAdminCommand;
import com.smpshop.commands.ShopCommand;
import com.smpshop.config.ShopConfig;
import com.smpshop.economy.EconomyService;
import com.smpshop.gui.ShopGUIManager;
import com.smpshop.listeners.GUIListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class ShopPlugin extends JavaPlugin {

    private ShopConfig shopConfig;
    private EconomyService economyService;
    private ShopGUIManager guiManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        shopConfig = new ShopConfig(this);
        shopConfig.load();

        economyService = new EconomyService(this);
        if (!economyService.setup()) {
            getLogger().warning("Vault (or a linked economy plugin) was not found. "
                    + "Buying and selling will be disabled until one is installed.");
        }

        guiManager = new ShopGUIManager(shopConfig, economyService);

        getServer().getPluginManager().registerEvents(
                new GUIListener(shopConfig, economyService, guiManager), this);

        getCommand("shop").setExecutor(new ShopCommand(shopConfig, guiManager));
        getCommand("sell").setExecutor(new SellCommand(shopConfig, economyService));
        getCommand("sellall").setExecutor(new SellCommand(shopConfig, economyService));
        getCommand("smpshop").setExecutor(new ShopAdminCommand(shopConfig));

        getLogger().info("SMPShop enabled with " + shopConfig.getCategories().size() + " categories.");
    }

    @Override
    public void onDisable() {
        getLogger().info("SMPShop disabled.");
    }

    public ShopConfig getShopConfig() {
        return shopConfig;
    }

    public EconomyService getEconomyService() {
        return economyService;
    }

    public ShopGUIManager getGuiManager() {
        return guiManager;
    }
}
