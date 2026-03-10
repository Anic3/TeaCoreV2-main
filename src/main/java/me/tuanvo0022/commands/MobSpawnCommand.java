package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


import java.util.Arrays;
import java.util.UUID;

public class MobSpawnCommand extends Command {
    private final Main plugin;
    
    public MobSpawnCommand(Main plugin) {
        super("mobspawn");
        this.plugin = plugin;
        this.setAliases(Arrays.asList("disablemobspawn"));
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }
        
        if (!player.hasPermission("teacore.mobspawn")) {
            MessageUtil.sendMessage(player, "error_permission");
            return true;
        }
        
        UUID uuid = player.getUniqueId();
        
        plugin.getDatabaseManager().getMobSpawnToggleAsync(uuid).thenAccept(toggle -> {
            if (toggle) {
                plugin.getDatabaseManager().setMobSpawnToggleAsync(uuid, false);
                MessageUtil.sendMessage(player, "mobspawn_disabled");
                return;
            }
            
            plugin.getDatabaseManager().setMobSpawnToggleAsync(uuid, true);
            MessageUtil.sendMessage(player, "mobspawn_enabled");
        });
        return true;
    }
}
