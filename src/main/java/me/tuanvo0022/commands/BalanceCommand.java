package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.economy.ShopEconomy;
import me.tuanvo0022.utils.MessageUtil;
import me.tuanvo0022.utils.NumberUtil;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BalanceCommand extends Command {
    private final Main plugin;
    
    public BalanceCommand(Main plugin) {
        super("balance");
        this.plugin = plugin;;
        this.setAliases(Arrays.asList("money", "bal"));
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }
        
        if (!player.hasPermission("teacore.balance")) {
            MessageUtil.sendMessage(player, "error_permission");
            return true;
        }
        
        ShopEconomy economy = plugin.getEconomies().get(ShopEconomy.Type.VAULT);
        
        if (economy == null) return true;
        
        if (args.length == 0) {
            MessageUtil.sendMessage(player, "balance_self", "%amount%", NumberUtil.formatShort(economy.getBalance(player)));
            return true;
        } 
        
        if (args.length == 1) {
            OfflinePlayer target = plugin.getServer().getOfflinePlayerIfCached(args[0]);
            if (target == null) {
                MessageUtil.sendMessage(player, "error_player_not_found");
                return true;
            }
            
            MessageUtil.sendMessage(player, "balance_other", "%target%", target.getName(), "%amount%", NumberUtil.formatShort(economy.getBalance(target)));
            return true;
        }
        
        MessageUtil.sendMessage(player, "balance_command");
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                String name = player.getName();
                if (name != null && name.toLowerCase().startsWith(partial)) {
                    suggestions.add(name);
                }
            }
        }
        return suggestions;
    }
}
