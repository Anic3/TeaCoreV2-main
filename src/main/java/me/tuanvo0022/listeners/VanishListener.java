package me.tuanvo0022.listeners;

import me.tuanvo0022.Main;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class VanishListener implements Listener {
    private final Main plugin;

    public VanishListener(Main plugin) {
        this.plugin = plugin;
    }

    // When a new player joins, hide all currently vanished players from them
    // (unless they have permission to see vanished players).
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiningPlayer = event.getPlayer();

        for (UUID vanishedUUID : plugin.getVanishedPlayers()) {
            Player vanishedPlayer = plugin.getServer().getPlayer(vanishedUUID);
            if (vanishedPlayer == null || !vanishedPlayer.isOnline()) continue;

            if (!joiningPlayer.hasPermission("teacore.vanish.see")) {
                joiningPlayer.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }
}
