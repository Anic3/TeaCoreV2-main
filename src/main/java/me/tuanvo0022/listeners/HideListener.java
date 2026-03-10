package me.tuanvo0022.listeners;

import me.tuanvo0022.Main;
import me.tuanvo0022.database.DatabaseManager.ToggleType;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class HideListener implements Listener {
    private final Main plugin;
    
    public HideListener(Main plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        plugin.getDatabaseManager().getHideToggleAsync(player.getUniqueId()).thenAccept(toggle -> {
            if (toggle) {
                event.setDeathMessage(null);
            }
        });
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        UUID uuid = player.getUniqueId();
        
        for (Player hidden : plugin.getDatabaseManager().getEnabledPlayers(ToggleType.HIDE)) {
            player.unlistPlayer(hidden);
        }
        
        plugin.getDatabaseManager().getHideToggleAsync(uuid).thenAccept(toggle -> {
            if (toggle) {
                event.joinMessage(null);
                
                Main.getScheduler().runLaterAsync(() -> {
                    TabPlayer tabplayer = TabAPI.getInstance().getPlayer(uuid);
                    TabAPI.getInstance().getNameTagManager().setPrefix(tabplayer, "&k");
                    
                    if (!player.isListed(player)) return;
                    
                    for (Player viewer : plugin.getServer().getOnlinePlayers()) {
                        viewer.unlistPlayer(player);
                    }
                }, 3L);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        plugin.getDatabaseManager().getHideToggleAsync(player.getUniqueId()).thenAccept(toggle -> {
            if (toggle) {
                event.quitMessage(null);
            }
        });
    }
}
