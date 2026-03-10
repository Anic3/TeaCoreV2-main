package me.tuanvo0022.listeners;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import io.canvasmc.canvas.event.EntityPostTeleportAsyncEvent;
import io.canvasmc.canvas.event.EntityPostPortalAsyncEvent;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JumpAndFlyListener implements Listener {
    private final Main plugin;
    
    // Double jump cooldown
    private final ConcurrentHashMap<UUID, Long> doubleJumpCooldowns = new ConcurrentHashMap<>();
    
    public JumpAndFlyListener(Main plugin) {
        this.plugin = plugin;
    }
    
    public enum Type {
        DOUBLE_JUMP,
        FLY
    }
    
    // Double Jump - PlayerToggleFlightEvent
    private void handleDoubleJump(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        
        if (isAdmin(player)) {
            return;
        }
        
        if (!canUseDoubleJump(player)) {
            disableFlight(player);
            return;
        }
        
        if (!isAllowed(Type.DOUBLE_JUMP, player)) {
            disableFlight(player);
            return;
        }
        
        event.setCancelled(true);
        disableFlight(player);
            
        long cooldown = plugin.config().DOUBLEJUMP_COOLDOWN;
        long currentTime = System.currentTimeMillis();
        UUID playerId = player.getUniqueId();
        Long lastJumpTime = this.doubleJumpCooldowns.get(playerId);

        if (lastJumpTime != null && currentTime - lastJumpTime < cooldown) {
            return;
        }
            
        double jumpPower = plugin.config().DOUBLEJUMP_POWER;
        double jumpY = plugin.config().DOUBLEJUMP_Y;
            
        player.setVelocity(player.getLocation().getDirection().setY(jumpY).multiply(jumpPower));
        MessageUtil.sendMessage(player, "doublejump_used");
            
        this.doubleJumpCooldowns.put(playerId, currentTime);
    }
    
    // Double Jump - PlayerMoveEvent
    private void handleDoubleJump(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (isAdmin(player)) {
            return;
        }
        
        if (!canUseDoubleJump(player)) {
            disableFlight(player);
            return;
        }
        
        if (isAllowed(Type.DOUBLE_JUMP, player) && isOnGround(player)) {
            player.setAllowFlight(true);
        } else {
            disableFlight(player);
        }
    }
    
    // Fly - PlayerToggleFlightEvent
    private boolean handleFly(PlayerToggleFlightEvent event) {
        return checkAndUpdateFly(event.getPlayer(), false);
    }
    
    // Fly - PlayerMoveEvent
    private boolean handleFly(PlayerMoveEvent event) {
        return checkAndUpdateFly(event.getPlayer());
    }
    
    // Fly - PlayerJoinEvent
    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        player.getScheduler().execute(plugin, () -> checkAndUpdateFly(player), null, 3L);
    }
    
    // Fly - PlayerPostRespawnEvent
    @EventHandler(priority = EventPriority.LOW)
    public void onPostRespawn(PlayerPostRespawnEvent event) {
        Player player = event.getPlayer();
        
        checkAndUpdateFly(player);
    }
    
    // Fly - PlayerPostPortalAsyncEvent
    @EventHandler(priority = EventPriority.LOW)
    public void onPostPortalAsync(EntityPostPortalAsyncEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        checkAndUpdateFly(player);
    }
    
    // Fly - EntityPostTeleportAsyncEvent
    @EventHandler(priority = EventPriority.LOW)
    public void onPostTeleportAsync(EntityPostTeleportAsyncEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        checkAndUpdateFly(player);
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        
        if (handleFly(event)) {
            return;
        }
        
        handleDoubleJump(event);
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        if (handleFly(event)) {
            return;
        }
        
        handleDoubleJump(event);
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Double Jump
        this.doubleJumpCooldowns.remove(player.getUniqueId());
    }
    
    private boolean isAllowed(Type type, Player player) {
        Location loc = player.getLocation();
        String worldName = loc.getWorld().getName();

        // ===== 1. WORLD CHECK (no WorldGuard) =====
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            return isWorldAllowed(type, worldName);
        }

        // ===== 2. WORLD CHECK (fallback) =====
        boolean worldAllowed = isWorldAllowed(type, worldName);

        // ===== 3. REGION CHECK =====
        RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(loc.getWorld()));

        if (regionManager != null) {
            ApplicableRegionSet regions = regionManager.getApplicableRegions(
                    BukkitAdapter.asBlockVector(loc));

            for (ProtectedRegion region : regions) {
                if (isRegionAllowed(type, region.getId())) {
                    return true;
                }
            }
        }

        // ===== 4. fallback to world =====
        return worldAllowed;
    }

    private boolean isWorldAllowed(Type type, String worldName) {
        switch (type) {
            case DOUBLE_JUMP:
                return plugin.config()
                    .DOUBLEJUMP_ALLOWED_WORLDS
                    .contains(worldName);

            case FLY:
                return plugin.config()
                    .FLY_ALLOWED_WORLDS
                    .contains(worldName);

            default:
                return false;
        }
    }

    private boolean isRegionAllowed(Type type, String regionId) {
        switch (type) {
            case DOUBLE_JUMP:
                return plugin.config()
                    .DOUBLEJUMP_ALLOWED_REGIONS
                    .contains(regionId);

            case FLY:
                return plugin.config()
                    .FLY_ALLOWED_REGIONS
                    .contains(regionId);

            default:
                return false;
        }
    }

    
    private void disableFlight(Player player) {
        player.setAllowFlight(false);
        player.setFlying(false);
    }
    
    private boolean checkAndUpdateFly(Player player) {
        return checkAndUpdateFly(player, true);
    }

    private boolean checkAndUpdateFly(Player player, boolean allowFly) {
        if (isAdmin(player)) return false;

        if (!canUseFly(player) || !isAllowed(Type.FLY, player)) {
            disableFlight(player);
            return false;
        }
        
        if (allowFly && !player.isFlying()) {
            player.setAllowFlight(true);
        }

        return true;
    }
    
    private boolean canUseDoubleJump(Player player) {
        if (isCreativeOrSpectator(player)) return false;
        
        return player.hasPermission("teadoublejump.use");
    }
    
    private boolean canUseFly(Player player) {
        if (isCreativeOrSpectator(player)) return true;
        
        return player.hasPermission("teafly.use");
    }
    
    private boolean isAdmin(Player player) {
        if (isCreativeOrSpectator(player)) return true;
        
        return player.hasPermission("teacore.admin");
    }
    
    private boolean isCreativeOrSpectator(Player player) {
        return player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
    }
    
    private boolean isOnGround(Player player) {
        int depth = plugin.config().DOUBLEJUMP_CHECKDEPTH;
        for (int i = 1; i <= depth; i++) {
            Block blockBelow = player.getLocation().getBlock().getRelative(0, -i, 0);
            if (blockBelow.getType().isSolid()) {
                return true;
            }
        }
        return false;
    }
}