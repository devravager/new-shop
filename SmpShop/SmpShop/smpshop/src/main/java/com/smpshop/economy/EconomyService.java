package com.smpshop.economy;

import com.astra.econ.api.AstraEconomyAPI;
import com.astra.econ.api.EcoResponse;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Wraps whichever economy backend is available.
 *
 * AstraEconomy is hooked directly through its own service (AstraEconomyAPI),
 * so Vault is NOT required when AstraEconomy is installed. Vault is kept only
 * as a fallback for players who use a different economy plugin instead of
 * AstraEconomy.
 */
public class EconomyService {

    private enum Backend { ASTRA, VAULT, NONE }

    private final JavaPlugin plugin;
    private Backend backend = Backend.NONE;
    private AstraEconomyAPI astra;
    private Economy vaultEconomy;

    public EconomyService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /** @return true if an economy backend was found and hooked. */
    public boolean setup() {
        // 1. Prefer AstraEconomy directly - no Vault needed at all.
        RegisteredServiceProvider<AstraEconomyAPI> astraRsp =
                plugin.getServer().getServicesManager().getRegistration(AstraEconomyAPI.class);
        if (astraRsp != null && astraRsp.getProvider() != null) {
            astra = astraRsp.getProvider();
            backend = Backend.ASTRA;
            plugin.getLogger().info("Hooked into AstraEconomy directly (no Vault required).");
            return true;
        }

        // 2. Fall back to Vault, for any other economy plugin.
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> vaultRsp =
                    plugin.getServer().getServicesManager().getRegistration(Economy.class);
            if (vaultRsp != null && vaultRsp.getProvider() != null) {
                vaultEconomy = vaultRsp.getProvider();
                backend = Backend.VAULT;
                plugin.getLogger().info("Hooked into Vault economy provider: " + vaultEconomy.getName());
                return true;
            }
        }

        backend = Backend.NONE;
        return false;
    }

    public boolean isAvailable() {
        return backend != Backend.NONE;
    }

    public double getBalance(Player player) {
        return switch (backend) {
            case ASTRA -> astra.getBalance(player);
            case VAULT -> vaultEconomy.getBalance(player);
            case NONE -> 0;
        };
    }

    public boolean has(Player player, double amount) {
        return switch (backend) {
            case ASTRA -> astra.has(player, amount, astra.getDefaultCurrency().getId());
            case VAULT -> vaultEconomy.has(player, amount);
            case NONE -> false;
        };
    }

    public boolean withdraw(Player player, double amount) {
        return switch (backend) {
            case ASTRA -> {
                EcoResponse r = astra.withdraw(player, amount, astra.getDefaultCurrency().getId());
                yield r.isSuccess();
            }
            case VAULT -> vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
            case NONE -> false;
        };
    }

    public boolean deposit(Player player, double amount) {
        return switch (backend) {
            case ASTRA -> {
                EcoResponse r = astra.deposit(player, amount, astra.getDefaultCurrency().getId());
                yield r.isSuccess();
            }
            case VAULT -> vaultEconomy.depositPlayer(player, amount).transactionSuccess();
            case NONE -> false;
        };
    }

    public String format(double amount) {
        return switch (backend) {
            case ASTRA -> astra.getDefaultCurrency().format(amount);
            case VAULT -> vaultEconomy.format(amount);
            case NONE -> String.format("%.2f", amount);
        };
    }
}
