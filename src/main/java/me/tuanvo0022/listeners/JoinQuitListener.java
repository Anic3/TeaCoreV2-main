package me.tuanvo0022.listeners;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.ColorUtil;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {
    private final Main plugin;
    
    public JoinQuitListener(Main plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String joinMsg = plugin.config().JOIN_MESSAGE;

        if (joinMsg == null || joinMsg.isEmpty()) {
            return;
        }
        
        String msg = joinMsg.replace("%player%", event.getPlayer().getName());
        
        event.joinMessage(ColorUtil.miniHex(msg));
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        String quitMsg = plugin.config().QUIT_MESSAGE;

        if (quitMsg == null || quitMsg.isEmpty()) {
            return;
        }
        
        String msg = quitMsg.replace("%player%", event.getPlayer().getName());
        
        event.quitMessage(ColorUtil.miniHex(msg));
    }
}