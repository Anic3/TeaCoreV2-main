package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PhantomCommand extends Command {
    private final Main plugin;
    
    public PhantomCommand(Main plugin) {
        super("phantom");
        this.plugin = plugin;
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }
        
        if (!player.hasPermission("teacore.phantom")) {
            MessageUtil.sendMessage(player, "error_permission");
            return true;
        }
        
        UUID uuid = player.getUniqueId();
        
        plugin.getDatabaseManager().getPhantomToggleAsync(uuid).thenAccept(toggle -> {
            if (toggle) {
                plugin.getDatabaseManager().setPhantomToggleAsync(uuid, false);
                MessageUtil.sendMessage(player, "phantom_disabled");
                return;
            }
            
            plugin.getDatabaseManager().setPhantomToggleAsync(uuid, true);
            MessageUtil.sendMessage(player, "phantom_enabled");
        });
        return true;
    }
}
