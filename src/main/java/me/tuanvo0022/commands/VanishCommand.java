package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class VanishCommand extends Command {
    private final Main plugin;

    public VanishCommand(Main plugin) {
        super("vanish");
        this.plugin = plugin;
        setDescription("Ẩn/hiện bản thân với người chơi thường.");
        setUsage("/<command>");
        setAliases(List.of("v"));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player staff)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }

        if (!staff.hasPermission("teacore.vanish")) {
            MessageUtil.sendMessage(staff, "error_permission");
            return true;
        }

        UUID uuid = staff.getUniqueId();

        Main.getScheduler().runAtEntity(staff, task -> {
            if (plugin.getVanishedPlayers().contains(uuid)) {
                plugin.getVanishedPlayers().remove(uuid);
                for (Player viewer : plugin.getServer().getOnlinePlayers()) {
                    viewer.showPlayer(plugin, staff);
                }
                MessageUtil.sendMessage(staff, "vanish_disabled");
            } else {
                plugin.getVanishedPlayers().add(uuid);
                for (Player viewer : plugin.getServer().getOnlinePlayers()) {
                    if (!viewer.hasPermission("teacore.vanish.see")) {
                        viewer.hidePlayer(plugin, staff);
                    }
                }
                MessageUtil.sendMessage(staff, "vanish_enabled");
            }
        });

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return Collections.emptyList();
    }
}
