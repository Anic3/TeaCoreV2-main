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
import org.bukkit.event.player.PlayerRespawnEvent;
import io.canvasmc.canvas.event.EntityTeleportAsyncEvent;
import io.canvasmc.canvas.event.EntityPortalAsyncEvent;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnListener implements Listener {
    private final Main plugin;

    private final Map<UUID, Location> pendingSpawn = new ConcurrentHashMap<>();

    public SpawnListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAsyncSpawn(AsyncPlayerSpawnLocationEvent event) {
        UUID uuid = event.getConnection().getProfile().getId();
        Location spawnLocation = event.getSpawnLocation();

        boolean shouldAssignSpawn = false;

        if (event.isNewPlayer() && plugin.config().SPAWN_ON_FIRST_JOIN) {
            shouldAssignSpawn = true;
        }
        
        else if (isInSpawn(spawnLocation)) {
            if (plugin.config().SPAWN_TELEPORT_TO_JOIN || isSpawnFull(spawnLocation, getMaxPlayers())) {
                shouldAssignSpawn = true;
            }
        }

        if (shouldAssignSpawn) {
            Location loc = getAvailableSpawn();
            if (loc != null) {
                event.setSpawnLocation(loc);
                pendingSpawn.put(uuid, loc);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Location loc = pendingSpawn.remove(player.getUniqueId());

        if (loc != null) {
            MessageUtil.sendMessage(player, "teleport_complete_spawn");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pendingSpawn.remove(event.getPlayer().getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // spawn.on-death must be enabled
        if (!plugin.config().SPAWN_ON_DEATH) return;

        boolean bedPriority = plugin.config().SPAWN_BED_PRIORITY;

        // If bed-priority is enabled → check bed first
        if (bedPriority) {
            Location bed = player.getBedSpawnLocation();
            if (bed != null) {
                event.setRespawnLocation(bed);
                return;
            }
            // No bed → fallback to spawn
        }

        // Bed-priority disabled OR no bed found → always spawn
        Location loc = this.getAvailableSpawn();

        if (loc != null) {
            event.setRespawnLocation(loc);
        } else {
            MessageUtil.sendMessage(player, "spawn_none_exist");
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPostRespawn(PlayerPostRespawnEvent event) {
        Player player = event.getPlayer();
        
        MessageUtil.sendMessage(player, "respawn_complete");
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onTeleportAsync(EntityTeleportAsyncEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        int max = getMaxPlayers();

        if (isSpawnFull(event.getTo(), max)) {
            event.setCancelled(true);
            MessageUtil.sendMessage(player, "spawn_is_full", "%max%", String.valueOf(max));
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPortalAsync(EntityPortalAsyncEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.config().SPAWN_DISABLE_PORTAL) return;
        
        String worldName = event.getFrom().getName();
        
        for (Location spawn : plugin.getDatabaseManager().getAllSpawnLocations()) {
            if (spawn.getWorld().getName().equals(worldName)) {
                event.setCancelled(true);
                MessageUtil.sendMessage(player, "portal_disabled");
                return;
            }
        }
    }
    
    private Location getAvailableSpawn() {
        Collection<Location> spawns = plugin.getDatabaseManager().getAllSpawnLocations();
        return WorldUtil.findAvailableLocation(spawns);
    }
    
    private int getMaxPlayers() {
        return plugin.config().SPAWN_MAX_PLAYERS;
    }
    
    private boolean isSpawnFull(Location to, int maxPlayers) {
        String worldName = to.getWorld().getName();

        for (Location spawn : plugin.getDatabaseManager().getAllSpawnLocations()) {
            if (!spawn.getWorld().getName().equals(worldName)) continue;

            return to.getWorld().getPlayerCount() >= maxPlayers;
        }

        return false;
    }

    private boolean isInSpawn(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;

        String playerWorld = loc.getWorld().getName();

        for (Location spawn : plugin.getDatabaseManager().getAllSpawnLocations()) {
            if (spawn.getWorld().getName().equals(playerWorld)) {
                return true;
            }
        }
        return false;
    }
}