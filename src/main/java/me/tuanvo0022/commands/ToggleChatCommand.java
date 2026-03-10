package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ToggleChatCommand extends Command {
    private final Main plugin;
    
    public ToggleChatCommand(Main plugin) {
        super("togglechat");
        this.plugin = plugin;
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }
        
        if (!player.hasPermission("teacore.togglechat")) {
            MessageUtil.sendMessage(player, "error_permission");
            return true;
        }
        
        UUID uuid = player.getUniqueId();
        
        plugin.getDatabaseManager().getChatToggleAsync(uuid).thenAccept(toggle -> {
            if (toggle) {
                plugin.getDatabaseManager().setChatToggleAsync(uuid, false);
                MessageUtil.sendMessage(player, "chattoggle_disabled");
                return;
            }
            
            plugin.getDatabaseManager().setChatToggleAsync(uuid, true);
            MessageUtil.sendMessage(player, "chattoggle_enabled");
        });
        return true;
    }
}
