package com.smpshop.commands;

import com.smpshop.config.ShopConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ShopAdminCommand implements CommandExecutor {

    private final ShopConfig config;

    public ShopAdminCommand(ShopConfig config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("smpshop.admin")) {
            sender.sendMessage(config.message("no-permission"));
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("Usage: /smpshop reload");
            return true;
        }

        config.load();
        sender.sendMessage(config.message("config-reloaded"));
        return true;
    }
}
