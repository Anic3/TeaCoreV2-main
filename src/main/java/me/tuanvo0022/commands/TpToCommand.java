package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TpToCommand extends Command {
    private final Main plugin;

    public TpToCommand(Main plugin) {
        super("tpto");
        this.plugin = plugin;
        setDescription("Teleport đến vị trí của người chơi khác (không cần xác nhận).");
        setUsage("/<command> <player>");
        setAliases(List.of("tpo"));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player staff)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }

        if (!staff.hasPermission("teacore.tpto")) {
            MessageUtil.sendMessage(staff, "error_permission");
            return true;
        }

        if (args.length < 1) {
            MessageUtil.sendMessage(staff, "tpto_command");
            return true;
        }

        Player target = plugin.getServer().getPlayerExact(args[0]);
        if (target == null) {
            MessageUtil.sendMessage(staff, "error_player_not_found");
            return true;
        }

        if (target.equals(staff)) {
            MessageUtil.sendMessage(staff, "tpto_self");
            return true;
        }

        Main.getScheduler().runAtEntity(staff, task -> {
            staff.teleport(target.getLocation());
            MessageUtil.sendMessage(staff, "tpto_success", "%target%", target.getName());
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
