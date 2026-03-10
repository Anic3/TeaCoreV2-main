package me.tuanvo0022.commands;

import me.tuanvo0022.Main;

import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SetAfkCommand extends Command {
    private final Main plugin;
    
    public SetAfkCommand(Main plugin) {
        super("setafk");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }
        
        if (!player.hasPermission("teacore.setafk")) {
            MessageUtil.sendMessage(player, "error_permission");
            return true;
        }
        
        if (args.length != 1) {
            MessageUtil.sendMessage(player, "setafk_command");
            return true;
        }
        
        String locationName = args[0];
        
        plugin.getDatabaseManager().setAfkLocation(locationName, player.getLocation()).thenRun(() -> {
            MessageUtil.sendMessage(player, "setafk_success", "%name%", locationName);
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
