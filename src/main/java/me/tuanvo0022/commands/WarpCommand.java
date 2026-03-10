package me.tuanvo0022.commands;

import me.tuanvo0022.Main;

import me.tuanvo0022.utils.MessageUtil;
import me.tuanvo0022.utils.WorldUtil;
import me.tuanvo0022.utils.TeleportUtil;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WarpCommand extends Command {
    private final Main plugin;
    
    public WarpCommand(Main plugin) {
        super("warp");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }
        
        if (!player.hasPermission("teacore.warp")) {
            MessageUtil.sendMessage(player, "error_permission");
            return true;
        }
        
        if (args.length != 1) {
            MessageUtil.sendMessage(player, "warp_command");
            return true;
        }
        
        Location loc = WorldUtil.findAvailableLocation(plugin.getDatabaseManager().getAllSpawnLocations());
        
        if (loc == null) {
            MessageUtil.sendMessage(player, "warp_none_exist");
            return true;
        }
        
        String arg = args[0];
        
        plugin.getDatabaseManager().getWarpLocation(arg).thenAccept(warpLoc -> {
            if (warpLoc == null) {
                MessageUtil.sendMessage(player, "warp_not_found", "%name%", arg);
                return;
            }
            
            warpLoc.setWorld(loc.getWorld());
            
            TeleportUtil.teleportCountdown(player, warpLoc).thenAccept(success -> {
                if (success) {
                    MessageUtil.sendMessage(player, "teleport_complete_warp", "%name%", arg);
                }
            });
        });
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        
        if (args.length == 1) {
            for (String name : plugin.getDatabaseManager().getAllWarpNames()) {
                if (name != null) suggestions.add(name);
            }
        }
        return suggestions;
    }
}
