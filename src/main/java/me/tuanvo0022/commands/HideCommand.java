package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HideCommand extends Command {
    private final Main plugin;
    
    public HideCommand(Main plugin) {
        super("hide");
        this.plugin = plugin;
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }
        
        if (!player.hasPermission("teacore.hide")) {
            MessageUtil.sendMessage(player, "error_permission");
            return true;
        }
        
        
        UUID uuid = player.getUniqueId();
        
        plugin.getDatabaseManager().getHideToggleAsync(uuid).thenAccept(toggle -> {
            TabPlayer tabplayer = TabAPI.getInstance().getPlayer(uuid);
            if (!toggle) {
                plugin.getDatabaseManager().setHideToggleAsync(uuid, true);
                
                //TabAPI.getInstance().getTabListFormatManager().setPrefix(tabplayer, "&k");
                
                TabAPI.getInstance().getNameTagManager().setPrefix(tabplayer, "&k");
                for (Player viewer : plugin.getServer().getOnlinePlayers()) {
                    viewer.unlistPlayer(player);
                }
                
                MessageUtil.sendMessage(player, "hide_enabled");
            } else {
                plugin.getDatabaseManager().setHideToggleAsync(uuid, false);
                
                //TabAPI.getInstance().getTabListFormatManager().setPrefix(tabplayer, TabAPI.getInstance().getTabListFormatManager().getOriginalPrefix(tabplayer));
                
                TabAPI.getInstance().getNameTagManager().setPrefix(tabplayer, TabAPI.getInstance().getNameTagManager().getOriginalPrefix(tabplayer));
                for (Player viewer : plugin.getServer().getOnlinePlayers()) {
                    viewer.listPlayer(player);
                }
                
                MessageUtil.sendMessage(player, "hide_disabled");
            }
        });
        return true;
    }
}
