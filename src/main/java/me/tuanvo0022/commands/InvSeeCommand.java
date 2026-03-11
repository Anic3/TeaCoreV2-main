package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class InvSeeCommand extends Command {
    private final Main plugin;

    public InvSeeCommand(Main plugin) {
        super("invsee");
        this.plugin = plugin;
        setDescription("Xem túi đồ của người chơi khác.");
        setUsage("/<command> <player>");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player staff)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }

        if (!staff.hasPermission("teacore.invsee")) {
            MessageUtil.sendMessage(staff, "error_permission");
            return true;
        }

        if (args.length < 1) {
            MessageUtil.sendMessage(staff, "invsee_command");
            return true;
        }

        Player target = plugin.getServer().getPlayerExact(args[0]);
        if (target == null) {
            MessageUtil.sendMessage(staff, "error_player_not_found");
            return true;
        }

        if (target.equals(staff)) {
            MessageUtil.sendMessage(staff, "invsee_self");
            return true;
        }

        boolean canEdit = staff.hasPermission("teacore.invsee.edit");

        Main.getScheduler().runAtEntity(staff, task -> {
            if (canEdit) {
                staff.openInventory(target.getInventory());
            } else {
                Inventory copy = Bukkit.createInventory(null, 54, "Túi đồ của " + target.getName());
                ItemStack[] contents = target.getInventory().getContents();
                for (int i = 0; i < Math.min(contents.length, 54); i++) {
                    copy.setItem(i, contents[i]);
                }
                staff.openInventory(copy);
            }
            MessageUtil.sendMessage(staff, "invsee_open", "%target%", target.getName());
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
