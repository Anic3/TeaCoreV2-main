package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PayToggleCommand extends Command {
    private final Main plugin;
    
    public PayToggleCommand(Main plugin) {
        super("paytoggle");
        this.plugin = plugin;
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }
        
        if (!player.hasPermission("teacore.paytoggle")) {
            MessageUtil.sendMessage(player, "error_permission");
            return true;
        }
        
        UUID uuid = player.getUniqueId();
        
        plugin.getDatabaseManager().getPayToggleAsync(uuid).thenAccept(toggle -> {
            if (toggle) {
                plugin.getDatabaseManager().setPayToggleAsync(uuid, false);
                MessageUtil.sendMessage(player, "paytoggle_disabled");
                return;
            }
            
            plugin.getDatabaseManager().setPayToggleAsync(uuid, true);
            MessageUtil.sendMessage(player, "paytoggle_enabled");
        });
        return true;
    }
}
