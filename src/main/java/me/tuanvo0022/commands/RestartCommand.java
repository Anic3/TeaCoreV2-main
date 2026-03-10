package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RestartCommand implements CommandExecutor {
    private final Main plugin;

    public RestartCommand(Main plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("teacore.admin")) {
            MessageUtil.sendMessage(sender, "error_permission");
            return true;
        }
        
        Main.getScheduler().runNextTick(task -> {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "cc removeTag all");
            
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                MessageUtil.sendMessage(player, "restart_announcement");
            }
            
            Main.getScheduler().runLater(() -> {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "minecraft:stop");
            }, 20L);
        });

        return true;
    }
}