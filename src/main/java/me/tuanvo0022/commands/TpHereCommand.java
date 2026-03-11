package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TpHereCommand extends Command {
    private final Main plugin;

    public TpHereCommand(Main plugin) {
        super("tphere");
        this.plugin = plugin;
        setDescription("Teleport người chơi đến vị trí của bạn.");
        setUsage("/<command> <player>");
        setAliases(List.of("tph"));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player staff)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }

        if (!staff.hasPermission("teacore.tphere")) {
            MessageUtil.sendMessage(staff, "error_permission");
            return true;
        }

        if (args.length < 1) {
            MessageUtil.sendMessage(staff, "tphere_command");
            return true;
        }

        Player target = plugin.getServer().getPlayerExact(args[0]);
        if (target == null) {
            MessageUtil.sendMessage(staff, "error_player_not_found");
            return true;
        }

        if (target.equals(staff)) {
            MessageUtil.sendMessage(staff, "tphere_self");
            return true;
        }

        Main.getScheduler().runAtEntity(target, task -> {
            target.teleport(staff.getLocation());
            MessageUtil.sendMessage(staff, "tphere_success", "%target%", target.getName());
            MessageUtil.sendMessage(target, "tphere_teleported", "%staff%", staff.getName());
        });

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(partial)) {
                    suggestions.add(p.getName());
                }
            }
        }
        return suggestions;
    }
}
