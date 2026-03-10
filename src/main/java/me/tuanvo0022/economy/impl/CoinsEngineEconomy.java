package me.tuanvo0022.economy.impl;

import me.tuanvo0022.Main;
import me.tuanvo0022.economy.ShopEconomy;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

import java.util.UUID;

public class CoinsEngineEconomy implements ShopEconomy {
    private final Currency currency;
    
    public CoinsEngineEconomy(Main plugin) {
        String currencyName = plugin.config().HOOKS_CURRENCYNAME;
        currency = CoinsEngineAPI.getCurrency(currencyName);
        
        if (currency == null) {
            plugin.getLogger().severe("CoinsEngine currency '" + currencyName + "' not found. Please check your config.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            throw new IllegalStateException("Missing CoinsEngine currency: " + currencyName);
        }
    }

    @Override
    public String getName() {
        return "CoinsEngine";
    }

    @Override
    public ShopEconomy.Type getType() {
        return ShopEconomy.Type.COINSENGINE;
    }

    @Override
    public void withdraw(@NotNull OfflinePlayer player, double amount) {
        CoinsEngineAPI.removeBalance(player.getUniqueId(), currency, amount);
    }

    @Override
    public void deposit(@NotNull OfflinePlayer player, double amount) {
        CoinsEngineAPI.addBalance(player.getUniqueId(), currency, amount);
    }

    @Override
    public double getBalance(@NotNull OfflinePlayer player) {
        return CoinsEngineAPI.getBalance(player.getUniqueId(), currency);
    }

    @Override
    public boolean has(@NotNull OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }
}