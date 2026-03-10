package me.tuanvo0022.economy;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public interface ShopEconomy {

    public String getName();

    public Type getType();

    /** Withdraw money from a player (online/offline) */
    public void withdraw(@NotNull OfflinePlayer player, double amount);

    /** Deposit money to a player (online/offline) */
    public void deposit(@NotNull OfflinePlayer player, double amount);

    /** Get the balance of a player (online/offline) */
    public double getBalance(@NotNull OfflinePlayer player);

    /** Check if a player has at least the given amount */
    public boolean has(@NotNull OfflinePlayer player, double amount);

    public static enum Type {
        VAULT,
        PLAYER_POINTS,
        COINSENGINE;

        @NotNull
        public static Type fromName(String name, Type defaultValue) {
            for (Type type : values()) {
                if (name.equalsIgnoreCase(type.name())) return type;
            }
            return defaultValue;
        }
    }
}