package me.tuanvo0022.commands;

import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DiscordCommand extends Command {
    
    public DiscordCommand() {
        super("discord");
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }
        
        if (!player.hasPermission("teacore.discord")) {
            MessageUtil.sendMessage(player, "error_permission");
            return true;
        }
        
        MessageUtil.sendMessage(player, "discord");
        return true;
    }
}
