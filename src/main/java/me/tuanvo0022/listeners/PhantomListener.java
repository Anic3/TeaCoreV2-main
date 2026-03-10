package me.tuanvo0022.listeners;

import me.tuanvo0022.Main;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class PhantomListener implements Listener {
    private final Main plugin;
    
    public PhantomListener(Main plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPhantomSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.PHANTOM) {
            event.getLocation().getNearbyPlayers(plugin.config().PHANTOM_CHECK_RADIUS).forEach(player -> {
                plugin.getDatabaseManager().getPhantomToggleAsync(player.getUniqueId()).thenAccept(toggle -> {
                    if (toggle) {
                        event.setCancelled(true);
                    }
                });
            });
        }
    }
}
