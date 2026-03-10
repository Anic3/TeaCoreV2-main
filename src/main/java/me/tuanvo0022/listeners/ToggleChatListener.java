package me.tuanvo0022.listeners;

import me.tuanvo0022.Main;
import me.tuanvo0022.database.DatabaseManager.ToggleType;

import io.papermc.paper.event.player.AsyncChatEvent;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;
import java.util.UUID;

public class ToggleChatListener implements Listener {
    private final Main plugin;
    
    public ToggleChatListener(Main plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Set<Audience> viewers = event.viewers();
        viewers.removeIf(audience -> {
            UUID uuid = audience.get(Identity.UUID).orElse(null);
            if (uuid == null) return false;
            
            Boolean enabled = plugin.getDatabaseManager().getCachedToggle(uuid, ToggleType.CHAT);
            return enabled != null && !enabled;
        });
    }
}
