package com.smpshop.commands;

import com.smpshop.config.ShopConfig;
import com.smpshop.gui.ShopGUIManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {

    private final ShopConfig config;
    private final ShopGUIManager guiManager;

    public ShopCommand(ShopConfig config, ShopGUIManager guiManager) {
        this.config = config;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can open the shop.");
            return true;
        }
        player.openInventory(guiManager.buildMainMenu());
        return true;
    }
}
