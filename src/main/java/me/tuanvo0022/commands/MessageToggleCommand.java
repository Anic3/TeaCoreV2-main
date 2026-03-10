package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MessageToggleCommand extends Command {
    private final Main plugin;
    
    public MessageToggleCommand(Main plugin) {
        super("msgtoggle");
        this.plugin = plugin;
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }
        
        if (!player.hasPermission("teacore.msgtoggle")) {
            MessageUtil.sendMessage(player, "error_permission");
            return true;
        }
        
        UUID uuid = player.getUniqueId();
        
        plugin.getDatabaseManager().getMsgToggleAsync(uuid).thenAccept(toggle -> {
            if (toggle) {
                plugin.getDatabaseManager().setMsgToggleAsync(uuid, false);
                MessageUtil.sendMessage(player, "msg_disabled");
                return;
            }
            
            plugin.getDatabaseManager().setMsgToggleAsync(uuid, true);
            MessageUtil.sendMessage(player, "msg_enabled");
        });
        return true;
    }
}
