package me.tuanvo0022.listeners;

import me.tuanvo0022.Main;

import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class MobSpawnListener implements Listener {
    private final Main plugin;
    
    public MobSpawnListener(Main plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Monster) {
            event.getLocation().getNearbyPlayers(plugin.config().MOBSPAWN_CHECK_RADIUS).forEach(player -> {
                plugin.getDatabaseManager().getMobSpawnToggleAsync(player.getUniqueId()).thenAccept(toggle -> {
                    if (toggle) {
                        event.setCancelled(true);
                    }
                });
            });
        }
    }
}
