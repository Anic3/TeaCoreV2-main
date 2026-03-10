package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.service.PayResult;
import me.tuanvo0022.utils.MessageUtil;
import me.tuanvo0022.utils.NumberUtil;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PayCommand extends Command {
    private final Main plugin;
    
    public PayCommand(Main plugin) {
        super("pay");
        this.plugin = plugin;
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }
        
        if (!player.hasPermission("teacore.pay")) {
            MessageUtil.sendMessage(player, "error_permission");
            return true;
        }
        
        if (args.length != 2) {
            MessageUtil.sendMessage(player, "pay_command");
            return true;
        }
        
        OfflinePlayer target = plugin.getServer().getOfflinePlayerIfCached(args[0]);
        if (target == null) {
            MessageUtil.sendMessage(player, "error_player_not_found");
            return true;
        }
        
        UUID playerUUID = player.getUniqueId();
        UUID targetUUID = target.getUniqueId();
                  
        if (playerUUID.equals(targetUUID)) {
            MessageUtil.sendMessage(player, "pay_self");
            return true;
        }

        double amount;
            
        try {
            amount = NumberUtil.parseAmount(args[1]);
        } catch (NumberFormatException e) {
            MessageUtil.sendMessage(player, "pay_invalid_amount");
            return true;
        }
        
        if (amount <= 0.0) {
            MessageUtil.sendMessage(player, "pay_invalid_amount");
            return true;
        }

        plugin.getEconomyService()
                .pay(player, target, amount)
                .thenAccept(result -> {

                    switch (result) {
                        case SUCCESS -> {
                            MessageUtil.sendMessage(
                                    player,
                                    "pay_success",
                                    "%sender%", player.getName(),
                                    "%receiver%", target.getName(),
                                    "%amount%", NumberUtil.formatShort(amount)
                            );

                            if (target.isOnline()) {
                                MessageUtil.sendMessage(
                                        target.getPlayer(),
                                        "pay_received",
                                        "%sender%", player.getName(),
                                        "%receiver%", target.getName(),
                                        "%amount%", NumberUtil.formatShort(amount)
                                );
                            }
                        }

                        case DISABLED_BY_TARGET ->
                                MessageUtil.sendMessage(
                                        player,
                                        "pay_disabled_other"
                                );

                        case NOT_ENOUGH_MONEY ->
                                MessageUtil.sendMessage(
                                        player,
                                        "error_not_enough_money"
                                );
                    }
                });
        
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
        } else if (args.length == 2) {
            String partial = args[1];
            String[] amounts = {"100", "1000", "10000", "100000", "1000000"};

            for (String amt : amounts) {
                if (amt.startsWith(partial)) {
                    suggestions.add(amt);
                }
            }
        }

        return suggestions;
    }
}
