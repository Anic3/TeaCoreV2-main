package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;
import me.tuanvo0022.utils.WorldUtil;
import me.tuanvo0022.utils.TeleportUtil;
import me.tuanvo0022.menus.ListSpawnView;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SpawnCommand extends Command {
    private final Main plugin;
    
    public SpawnCommand(Main plugin) {
        super("spawn");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }
        
        if (!player.hasPermission("teacore.spawn")) {
            MessageUtil.sendMessage(player, "error_permission");
            return true;
        }

        boolean autoBalance = plugin.config().TELEPORT_AUTO_BALANCE_ENABLED;

        // ==============================
        // AUTO BALANCE MODE
        // ==============================
        if (autoBalance) {
            Location loc = WorldUtil.findAvailableLocation(plugin.getDatabaseManager().getAllSpawnLocations());

            if (loc == null) {
                MessageUtil.sendMessage(player, "spawn_none_exist");
                return true;
            }

            TeleportUtil.teleportCountdown(player, loc).thenAccept(success -> {
                if (success) {
                    MessageUtil.sendMessage(player, "teleport_complete_spawn");
                }
            });
            return true;

        }
        // ==============================
        // MANUAL MODE (player must choose)
        // ==============================
        if (args.length != 1) {
            //MessageUtil.sendMessage(player, "spawn_command");
            plugin.getViewFrame().open(ListSpawnView.class, player);
            return true;
        }

        plugin.getDatabaseManager().getSpawnLocation(args[0]).thenAccept(loc -> {
            if (loc == null) {
                MessageUtil.sendMessage(player, "spawn_not_found", "%name%", args[0]);
                return;
            }

            TeleportUtil.teleportCountdown(player, loc).thenAccept(success -> {
                if (success) {
                    MessageUtil.sendMessage(player, "teleport_complete_spawn", "%name%", args[0]);
                }
            });
        });

        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        boolean autoBalance = plugin.config().TELEPORT_AUTO_BALANCE_ENABLED;

        if (autoBalance || args.length != 1) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();

        for (String name : plugin.getDatabaseManager().getAllSpawnNames()) {
            if (name != null) {
                result.add(name);
            }
        }

        return result;
    }
}
