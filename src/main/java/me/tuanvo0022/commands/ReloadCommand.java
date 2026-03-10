package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    private final Main plugin;
    
    public ReloadCommand(Main plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("teacore.admin")) {
            MessageUtil.sendMessage(sender, "error_permission");
            return true;
        } 
        
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            Main.getScheduler().runAsync(task -> {
                plugin.config().reload();
                MessageUtil.sendMessage(sender, "reload_complete");
            });
            return true;
        }
        return false;
    }
}
