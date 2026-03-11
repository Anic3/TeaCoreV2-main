package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EnderSeeCommand extends Command {
    private final Main plugin;

    public EnderSeeCommand(Main plugin) {
        super("endersee");
        this.plugin = plugin;
        setDescription("Xem Ender Chest của người chơi khác.");
        setUsage("/<command> <player>");
        setAliases(List.of("esee"));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player staff)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }

        if (!staff.hasPermission("teacore.endersee")) {
            MessageUtil.sendMessage(staff, "error_permission");
            return true;
        }

        if (args.length < 1) {
            MessageUtil.sendMessage(staff, "endersee_command");
            return true;
        }

        Player target = plugin.getServer().getPlayerExact(args[0]);
        if (target == null) {
            MessageUtil.sendMessage(staff, "error_player_not_found");
            return true;
        }

        if (target.equals(staff)) {
            MessageUtil.sendMessage(staff, "endersee_self");
            return true;
        }

        boolean canEdit = staff.hasPermission("teacore.endersee.edit");

        Main.getScheduler().runAtEntity(staff, task -> {
            if (canEdit) {
                staff.openInventory(target.getEnderChest());
            } else {
                Inventory copy = Bukkit.createInventory(null, 27, "Ender Chest của " + target.getName());
                ItemStack[] contents = target.getEnderChest().getContents();
                for (int i = 0; i < Math.min(contents.length, 27); i++) {
                    copy.setItem(i, contents[i]);
                }
                staff.openInventory(copy);
            }
            MessageUtil.sendMessage(staff, "endersee_open", "%target%", target.getName());
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
