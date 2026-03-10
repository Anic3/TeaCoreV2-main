package me.tuanvo0022.listeners;

import me.tuanvo0022.Main;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathLightningListener implements Listener {
    private final Main plugin;

    public DeathLightningListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        if (!plugin.config().LIGHTNING_ON_DEATH) return;

        Player player = event.getEntity();
        Location location = player.getLocation();
        World world = player.getWorld();

        world.strikeLightningEffect(location);

        // Play a dramatic thunder sound
        world.playSound(
                location,
                Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
                1.0F,
                1.0F
        );
    }
}