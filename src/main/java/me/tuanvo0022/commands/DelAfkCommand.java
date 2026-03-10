package me.tuanvo0022.commands;

import me.tuanvo0022.Main;

import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DelAfkCommand extends Command {
    private final Main plugin;
    
    public DelAfkCommand(Main plugin) {
        super("delafk");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }
        
        if (!player.hasPermission("teacore.delafk")) {
            MessageUtil.sendMessage(player, "error_permission");
            return true;
        }
        
        if (args.length != 1) {
            MessageUtil.sendMessage(player, "delafk_command");
            return true;
        }
        
        String locationName = args[0];
        
        plugin.getDatabaseManager().getAfkLocation(locationName).thenAccept(loc -> {
            if (loc == null) {
                MessageUtil.sendMessage(player, "afk_not_found", "%name%", locationName);
                return;
            }
            
            plugin.getDatabaseManager().deleteAfkLocation(locationName).thenRun(() -> {
                MessageUtil.sendMessage(player, "delafk_success", "%name%", locationName);
            });
        });
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        
        if (args.length == 1) {
            for (String name : plugin.getDatabaseManager().getAllAfkNames()) {
                if (name != null) suggestions.add(name);
            }
        }
        return suggestions;
    }
}
