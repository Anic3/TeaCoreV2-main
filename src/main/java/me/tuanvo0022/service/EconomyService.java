package me.tuanvo0022.service;

import me.tuanvo0022.Main;
import me.tuanvo0022.economy.ShopEconomy;

import org.bukkit.OfflinePlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EconomyService {

    private final Main plugin;
    private final ShopEconomy economy;

    public EconomyService(Main plugin) {
        this.plugin = plugin;
        this.economy = plugin.getEconomies().get(ShopEconomy.Type.VAULT);
    }

    public CompletableFuture<PayResult> pay(OfflinePlayer sender, OfflinePlayer target, double amount) {
        UUID senderId = sender.getUniqueId();
        UUID targetId = target.getUniqueId();

        return plugin.getDatabaseManager()
                .getPayToggleAsync(targetId)
                .thenApply(enabled -> {

                    if (!enabled)
                        return PayResult.DISABLED_BY_TARGET;

                    if (!economy.has(sender, amount))
                        return PayResult.NOT_ENOUGH_MONEY;

                    economy.withdraw(sender, amount);
                    economy.deposit(target, amount);

                    if (!target.isOnline()) {
                        plugin.getDatabaseManager()
                              .addPendingPayment(targetId, amount, sender.getName());
                    }

                    return PayResult.SUCCESS;
                });
    }
}
