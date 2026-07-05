package com.smpshop.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyService {

    private final JavaPlugin plugin;
    private Economy vaultEconomy;

    public EconomyService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null || rsp.getProvider() == null) {
            return false;
        }

        vaultEconomy = rsp.getProvider();
        plugin.getLogger().info("Hooked into Vault: " + vaultEconomy.getName());
        return true;
    }

    public boolean isAvailable() {
        return vaultEconomy != null;
    }

    public double getBalance(Player player) {
        if (vaultEconomy == null) return 0;
        return vaultEconomy.getBalance(player);
    }

    public boolean has(Player player, double amount) {
        return vaultEconomy != null && vaultEconomy.has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (vaultEconomy == null) return false;
        return vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean deposit(Player player, double amount) {
        if (vaultEconomy == null) return false;
        return vaultEconomy.depositPlayer(player, amount).transactionSuccess();
    }

    public String format(double amount) {
        if (vaultEconomy == null) {
            return String.format("%.2f", amount);
        }
        return vaultEconomy.format(amount);
    }
}
