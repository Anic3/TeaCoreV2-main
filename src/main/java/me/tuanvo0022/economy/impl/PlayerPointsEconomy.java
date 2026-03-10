package me.tuanvo0022.economy.impl;

import me.tuanvo0022.economy.ShopEconomy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.manager.DataManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerPointsEconomy implements ShopEconomy {

    private final DataManager dataManager = PlayerPoints.getInstance().getManager(DataManager.class);

    @Override
    public String getName() {
        return "PlayerPoints";
    }

    @Override
    public ShopEconomy.Type getType() {
        return ShopEconomy.Type.PLAYER_POINTS;
    }

    @Override
    public void withdraw(@NotNull OfflinePlayer player, double amount) {
        int newAmount = (int) (getBalance(player) - amount);
        dataManager.setPoints(player.getUniqueId(), newAmount);
    }

    @Override
    public void deposit(@NotNull OfflinePlayer player, double amount) {
        int newAmount = (int) (getBalance(player) + amount);
        dataManager.setPoints(player.getUniqueId(), newAmount);
    }

    @Override
    public double getBalance(@NotNull OfflinePlayer player) {
        return dataManager.getEffectivePoints(player.getUniqueId());
    }

    @Override
    public boolean has(@NotNull OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }
}