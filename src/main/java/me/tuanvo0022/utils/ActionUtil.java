package me.tuanvo0022.utils;

import me.tuanvo0022.Main;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

public final class ActionUtil {
    private static Main plugin;

    public ActionUtil(Main plugin) {
        this.plugin = plugin;
    }
    
    public static void runAction(List<String> actions, Player player) {
        runAction(actions, player, null);
    }

    public static void runAction(List<String> actions, Player p1, Player p2) {
        if (actions == null || actions.isEmpty() || p1 == null) return;

        for (String raw : actions) {
            String command = raw
                    .replace("%player%", p1.getName())
                    .replace("%target%", p2 != null ? p2.getName() : "null");

            String lower = command.toLowerCase();

            try {
                if (lower.startsWith("console:")) {
                    String consoleCommand = command.substring("console:".length()).trim();
                    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () ->
                            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), consoleCommand)
                    );

                } else if (lower.startsWith("player:")) {
                    String playerCommand = command.substring("player:".length()).trim();
                    p1.getScheduler().execute(plugin, () -> p1.performCommand(playerCommand), null, 1L);

                } else if (lower.startsWith("p1:")) {
                    String p1Command = command.substring("p1:".length()).trim();
                    p1.getScheduler().execute(plugin, () -> p1.performCommand(p1Command), null, 1L);

                } else if (lower.startsWith("p2:")) {
                    if (p2 != null) {
                        String p2Command = command.substring("p2:".length()).trim();
                        p2.getScheduler().execute(plugin, () -> p2.performCommand(p2Command), null, 1L);
                    }
                    
                } else if (lower.startsWith("sound:")) {
                    String[] parts = command.substring("sound:".length()).trim().split(":");
                    if (parts.length >= 3) {
                        Sound sound = Sound.valueOf(parts[0].toUpperCase());
                        float volume = Float.parseFloat(parts[1]);
                        float pitch = Float.parseFloat(parts[2]);
                        p1.playSound(p1.getLocation(), sound, volume, pitch);
                    }

                } else if (lower.startsWith("sound:p1:")) {
                    String[] parts = command.substring("sound:p1:".length()).trim().split(":");
                    if (parts.length >= 3) {
                        Sound sound = Sound.valueOf(parts[0].toUpperCase());
                        float volume = Float.parseFloat(parts[1]);
                        float pitch = Float.parseFloat(parts[2]);
                        p1.playSound(p1.getLocation(), sound, volume, pitch);
                    }

                } else if (lower.startsWith("sound:p2:")) {
                    if (p2 != null) {
                        String[] parts = command.substring("sound:p2:".length()).trim().split(":");
                        if (parts.length >= 3) {
                            Sound sound = Sound.valueOf(parts[0].toUpperCase());
                            float volume = Float.parseFloat(parts[1]);
                            float pitch = Float.parseFloat(parts[2]);
                            p2.playSound(p2.getLocation(), sound, volume, pitch);
                        }
                    }

                } else {
                    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () ->
                            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command)
                    );
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error executing action: " + command + " | " + e.getMessage());
            }
        }
    }
}