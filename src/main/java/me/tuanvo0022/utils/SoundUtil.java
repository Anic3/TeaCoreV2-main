package me.tuanvo0022.utils;

import org.bukkit.Bukkit;

import org.bukkit.command.CommandSender;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class SoundUtil {
    
    public static void playSound(CommandSender sender, String soundName) {
        if (sender instanceof Player player) {
            if (player == null || !player.isOnline()) {
                return;
            }
            
            if (soundName == null || soundName.isEmpty()) {
                return;
            }
            
            try {
                player.playSound(player.getLocation(), Sound.valueOf(soundName.toUpperCase()), 1.0F, 1.0F);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Invalid sound " + soundName);
            }
        }
    }
}