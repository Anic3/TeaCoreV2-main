package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MessageCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    
    public MessageCommand(Main plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }
        
        if (!player.hasPermission("teacore.msg")) {
            MessageUtil.sendMessage(player, "error_permission");
            return true;
        }
        
        if (args.length < 2) {
            MessageUtil.sendMessage(sender, "msg_command");
            return true;
        }
        
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            MessageUtil.sendMessage(player, "error_player_not_found");
            return true;
        }
        
        UUID playerUUID = player.getUniqueId();
        UUID targetUUID = target.getUniqueId();
        
        if (playerUUID.equals(targetUUID)) {
            MessageUtil.sendMessage(player, "msg_yourself");
            return true;
        }
        
        plugin.getDatabaseManager().getMsgToggleAsync(targetUUID).thenAccept(toggle -> {
            if (!toggle) {
                MessageUtil.sendMessage(player, "msg_disabled_other");
                return;
            }
            
            String message = String.join(" ", Arrays.asList(args).subList(1, args.length));
                
            MessageUtil.sendMessage(player, "msg_send", "%sender%", player.getName(), "%receiver%", target.getName(), "%message%", message);
            MessageUtil.sendMessage(target, "msg_receive", "%sender%", player.getName(), "%receiver%", target.getName(), "%message%", message);
            
            plugin.getLastMessaged().put(player, target);
            plugin.getLastMessaged().put(target, player);
        });
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                String name = player.getName();
                if (name != null && name.toLowerCase().startsWith(partial)) {
                    suggestions.add(name);
                }
            }
        }

        return suggestions;
    }
}
