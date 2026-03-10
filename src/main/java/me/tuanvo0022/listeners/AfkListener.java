package me.tuanvo0022.listeners;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;
import me.tuanvo0022.utils.WorldUtil;
import me.tuanvo0022.utils.TeleportUtil;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import io.canvasmc.canvas.event.EntityTeleportAsyncEvent;
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AfkListener implements Listener {
    private final Main plugin;

    private final Map<UUID, Location> pendingAfk = new ConcurrentHashMap<>();

    public AfkListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAsyncSpawn(AsyncPlayerSpawnLocationEvent event) {
        UUID uuid = event.getConnection().getProfile().getId();

        Location spawnLocation = event.getSpawnLocation();
        if (!isInAfk(spawnLocation)) return;

        Location loc = null;

        if (plugin.config().AFK_TELEPORT_TO_JOIN || isAfkFull(spawnLocation, getMaxPlayers())) {
            loc = getAvailableAfk();
        }

        if (loc != null) {
            event.setSpawnLocation(loc);
            pendingAfk.put(uuid, loc);
        }
    }

    
    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Location loc = pendingAfk.remove(player.getUniqueId());

        if (loc != null) {
            MessageUtil.sendMessage(player, "teleport_complete_afk");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pendingAfk.remove(event.getPlayer().getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onTeleportAsync(EntityTeleportAsyncEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        int max = getMaxPlayers();

        if (isAfkFull(event.getTo(), max)) {
            event.setCancelled(true);
            MessageUtil.sendMessage(player, "afk_is_full", "%max%", String.valueOf(max));
        }
    }
    
    private Location getAvailableAfk() {
        Collection<Location> afks = plugin.getDatabaseManager().getAllAfkLocations();
        return WorldUtil.findAvailableLocation(afks);
    }
    
    private int getMaxPlayers() {
        return plugin.config().AFK_MAX_PLAYERS;
    }
    
    private boolean isAfkFull(Location to, int maxPlayers) {
        String worldName = to.getWorld().getName();

        for (Location afk : plugin.getDatabaseManager().getAllAfkLocations()) {
            if (!afk.getWorld().getName().equals(worldName)) continue;

            return to.getWorld().getPlayerCount() >= maxPlayers;
        }

        return false;
    }

    private boolean isInAfk(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;

        String playerWorld = loc.getWorld().getName();

        for (Location afk : plugin.getDatabaseManager().getAllAfkLocations()) {
            if (afk.getWorld().getName().equals(playerWorld)) {
                return true;
            }
        }
        return false;
    }
}