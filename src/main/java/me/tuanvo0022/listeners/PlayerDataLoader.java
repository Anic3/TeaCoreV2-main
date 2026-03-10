package me.tuanvo0022.listeners;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;
import me.tuanvo0022.utils.NumberUtil;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class PlayerDataLoader implements Listener {
    private final Main plugin;

    public PlayerDataLoader(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        plugin.getDatabaseManager().loadAllToggles(playerId);

        plugin.getDatabaseManager().getPendingPayments(playerId).thenAccept(list -> {
            for (Map<String, Object> payment : list) {
                double amount = (double) payment.get("amount");
                String sender = (String) payment.get("sender");

                MessageUtil.sendMessage(player, "pay_received_offline",
                        "%sender%", sender,
                        "%amount%", NumberUtil.formatShort(amount));
            }

            plugin.getDatabaseManager().removePendingPayments(playerId);
        });
    }
}