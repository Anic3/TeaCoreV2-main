package me.tuanvo0022.utils;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class TeleportUtil {
    private static Main plugin;
    private static final Set<UUID> tpPlayers = ConcurrentHashMap.newKeySet();

    public TeleportUtil(Main plugin) {
        this.plugin = plugin;
    }
    
    public static CompletableFuture<Boolean> teleportCountdown(Player player, Location loc) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        UUID uuid = player.getUniqueId();
        
        /*if (player.hasPermission("teacore.bypass")) {
            player.teleportAsync(loc).thenAccept(success -> result.complete(success));
            return result;
        }*/
        
        if (tpPlayers.contains(uuid)) {
            result.complete(false);
            return result;
        }
        
        AtomicInteger countdown = new AtomicInteger(plugin.config().TELEPORT_DELAY);
        double maxDistance = plugin.config().TELEPORT_MAX_MOVE_DISTANCE;
        boolean cancelOnMove = plugin.config().TELEPORT_CANCEL_ON_MOVE;
        
        if (countdown.get() <= 0) {
            player.teleportAsync(loc).thenAccept(success -> result.complete(success));
            return result;
        }
        
        Location originLocation = cancelOnMove ? player.getLocation() : null;
        tpPlayers.add(uuid);
        
        Main.getScheduler().runTimerAsync(task -> {
            if (!player.isOnline()) {
                task.cancel();
                tpPlayers.remove(uuid);
                result.complete(false);
                return;
            }
            
            if (cancelOnMove) {
                if (!originLocation.getWorld().equals(player.getWorld()) || originLocation.distance(player.getLocation()) > maxDistance) {
                    task.cancel();
                    tpPlayers.remove(uuid);
                    MessageUtil.sendMessage(player, "teleport_cancel");
                    result.complete(false);
                    return;
                }
            }
            
            if (countdown.get() > 0) {
                String sec = String.valueOf(countdown.get());
                
                MessageUtil.sendMessage(player, "teleport_countdown", "%time%", sec);
                
                countdown.getAndDecrement();
            } else {
                task.cancel();
                tpPlayers.remove(uuid);
                
                player.teleportAsync(loc).thenAccept(success -> result.complete(success));
            }
        }, 1L, 20L);
        
        return result;
    }
    
    public static CompletableFuture<Boolean> teleportDelay(Player player, Location loc, long delay) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        Main.getScheduler().runLaterAsync(() -> {
            if (!player.isOnline()) {
                future.complete(false);
                return;
            }
            
            player.teleportAsync(loc).thenAccept(success -> {
                future.complete(success);
            });
        }, delay);
        
        return future;
    }
}