package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.economy.ShopEconomy;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class KillshardCommand extends Command {
    private final Main plugin;
    
    public KillshardCommand(Main plugin) {
        super("killshard");
        this.plugin = plugin;
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("teacore.killshard")) {
            MessageUtil.sendMessage(sender, "error_permission");
            return true;
        }
        
        if (args.length != 1) {
            MessageUtil.sendMessage(sender, "killshard_command");
            return true;
        }
        
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            MessageUtil.sendMessage(sender, "error_player_not_found");
            return true;
        }
        
        ShopEconomy economy = plugin.getEconomies().get(ShopEconomy.Type.COINSENGINE);
        if (economy == null) return true;

        double killRate = plugin.config().SHARD_KILL_MULTIPLIER;

        DecimalFormat df = new DecimalFormat("#.##");
        
        for (int i = 20; i >= 1; i--) {
            String permission = "killshard.x" + i;
            if (target.hasPermission(permission)) {
                double amount = i * killRate;

                economy.deposit(target, amount);
                
                MessageUtil.sendMessage(target, "killshard", "%amount%", df.format(amount));
                return true;
            }
        }
        MessageUtil.sendMessage(target, "error_permission");
        return true;
    }
}
