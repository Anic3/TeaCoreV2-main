package me.tuanvo0022.listeners;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;
import me.tuanvo0022.utils.ColorUtil;

import net.kyori.adventure.text.Component;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class DiscordListener implements Listener {
    private final Main plugin;

    public DiscordListener(Main plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();

        plugin.getDatabaseManager().isAccountLockedAsync(uuid).thenAccept(locked -> {
            if (!locked) return;
            
            Component kickMessage = ColorUtil.miniHex(plugin.getConfigManager().getString("messages", "lock_message"));
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, kickMessage);
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        plugin.getDatabaseManager()
            .getAccountAsync(uuid)
            .thenAccept(acc -> {
                if (acc != null) {
                    plugin.getDiscordService().syncMcToDiscord(player);
                    plugin.getDiscordService().syncDiscordToMc(acc.getDiscordId());
                    return;
                }

                if (plugin.config().ROLE_SYNC_DISCORD_TO_MC_ENFORCE_UNLINKED_CLEANUP) {
                    plugin.getDiscordService().forceRemoveMcRanksOnUnlink(uuid);
                }

                MessageUtil.sendMessage(player, "link_reminder");
            });
    }
}
