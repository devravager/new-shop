package com.smpshop.commands;

import com.smpshop.config.ShopConfig;
import com.smpshop.economy.EconomyService;
import com.smpshop.model.ShopItemDef;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SellCommand implements CommandExecutor {

    private final ShopConfig config;
    private final EconomyService economy;

    public SellCommand(ShopConfig config, EconomyService economy) {
        this.config = config;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can sell items.");
            return true;
        }

        if (!economy.isAvailable()) {
            player.sendMessage(config.message("vault-missing"));
            return true;
        }

        if (label.equalsIgnoreCase("sellall")) {
            sellAll(player);
            return true;
        }

        return sellHand(player, args);
    }

    private boolean sellHand(Player player, String[] args) {
        PlayerInventory inv = player.getInventory();
        ItemStack hand = inv.getItemInMainHand();

        if (hand == null || hand.getType() == Material.AIR) {
            player.sendMessage(config.message("nothing-to-sell"));
            return true;
        }

        ShopItemDef def = config.findItemByMaterial(hand.getType());
        if (def == null) {
            player.sendMessage(config.message("not-sellable"));
            return true;
        }

        int amount = hand.getAmount();
        if (args.length > 0) {
            try {
                amount = Math.min(amount, Math.max(1, Integer.parseInt(args[0])));
            } catch (NumberFormatException ignored) {
                // fall back to full stack
            }
        }

        double total = def.getSellPrice() * amount;
        hand.setAmount(hand.getAmount() - amount);
        economy.deposit(player, total);

        player.sendMessage(config.message("sell-success")
                .replace("%amount%", String.valueOf(amount))
                .replace("%item%", prettify(def.getKey()))
                .replace("%price%", economy.format(total)));
        player.playSound(player.getLocation(),
                config.getSound("purchase-success", Sound.ENTITY_EXPERIENCE_ORB_PICKUP),
                config.getSoundVolume(), config.getSoundPitch());
        return true;
    }

    private void sellAll(Player player) {
        PlayerInventory inv = player.getInventory();
        double totalEarned = 0;
        int totalItems = 0;

        for (int slot = 0; slot < inv.getStorageContents().length; slot++) {
            ItemStack stack = inv.getStorageContents()[slot];
            if (stack == null || stack.getType() == Material.AIR) continue;

            ShopItemDef def = config.findItemByMaterial(stack.getType());
            if (def == null || def.getSellPrice() <= 0) continue;

            totalEarned += def.getSellPrice() * stack.getAmount();
            totalItems += stack.getAmount();
            inv.setItem(slot, null);
        }

        if (totalItems == 0) {
            player.sendMessage(config.message("nothing-to-sell"));
            return;
        }

        economy.deposit(player, totalEarned);
        player.sendMessage(config.message("sell-success")
                .replace("%amount%", String.valueOf(totalItems))
                .replace("%item%", "items")
                .replace("%price%", economy.format(totalEarned)));
        player.playSound(player.getLocation(),
                config.getSound("purchase-success", Sound.ENTITY_EXPERIENCE_ORB_PICKUP),
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
