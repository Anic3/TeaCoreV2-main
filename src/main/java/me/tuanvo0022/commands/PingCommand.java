package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PingCommand implements CommandExecutor {
    private final Main plugin;

    public PingCommand(Main plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("teacore.ping")) {
            MessageUtil.sendMessage(sender, "error_permission");
            return true;
        }
        
        if (args.length == 0) {
            if (sender instanceof Player player) {
                MessageUtil.sendMessage(player, "ping_self", "%ping%", String.valueOf(player.getPing()));
            } else {
                MessageUtil.sendMessage(sender, "ping_command");
            }
        } else if (args.length == 1) {
            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                MessageUtil.sendMessage(sender, "error_player_not_found");
                return true;
            }

            MessageUtil.sendMessage(sender, "ping_other", "%target%", target.getName(), "%ping%", String.valueOf(target.getPing()));
        } else {
            MessageUtil.sendMessage(sender, "ping_command");
        }

        return true;
    }
}