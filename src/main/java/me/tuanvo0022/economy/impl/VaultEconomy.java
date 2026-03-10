package me.tuanvo0022.economy.impl;

import me.tuanvo0022.economy.ShopEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VaultEconomy implements ShopEconomy {
    private final Economy economy = Bukkit.getServicesManager().load(Economy.class);

    @Override
    public String getName() {
        return "Vault";
    }

    @Override
    public ShopEconomy.Type getType() {
        return ShopEconomy.Type.VAULT;
    }

    @Override
    public void withdraw(@NotNull OfflinePlayer player, double amount) {
        this.economy.withdrawPlayer(player, amount);
    }

    @Override
    public void deposit(@NotNull OfflinePlayer player, double amount) {
        this.economy.depositPlayer(player, amount);
    }

    @Override
    public double getBalance(@NotNull OfflinePlayer player) {
        return this.economy.getBalance(player);
    }

    @Override
    public boolean has(@NotNull OfflinePlayer player, double amount) {
        return this.economy.has(player, amount);
    }
}

