package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.economy.ShopEconomy;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class AfkshardCommand extends Command {
    private final Main plugin;
    
    public AfkshardCommand(Main plugin) {
        super("afkshard");
        this.plugin = plugin;
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("teacore.afkshard")) {
            MessageUtil.sendMessage(sender, "error_permission");
            return true;
        }
        
        if (args.length != 1) {
            MessageUtil.sendMessage(sender, "afkshard_command");
            return true;
        }
        
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            MessageUtil.sendMessage(sender, "error_player_not_found");
            return true;
        }
        
        ShopEconomy economy = plugin.getEconomies().get(ShopEconomy.Type.COINSENGINE);
        if (economy == null) return true;

        double afkRate = plugin.config().SHARD_AFK_MULTIPLIER;

        DecimalFormat df = new DecimalFormat("#.##");

        for (int i = 20; i >= 1; i--) {
            String permission = "afkshard.x" + i;
            if (target.hasPermission(permission)) {
                double amount = i * afkRate;

                economy.deposit(target, amount);
                
                MessageUtil.sendMessage(target, "afkshard", "%amount%", df.format(amount));
                return true;
            }
        }
        MessageUtil.sendMessage(target, "error_permission");
        return true;
    }
}
