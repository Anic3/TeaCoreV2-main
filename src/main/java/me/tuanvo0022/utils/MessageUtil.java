package me.tuanvo0022.utils;

import me.tuanvo0022.Main;
import me.clip.placeholderapi.PlaceholderAPI;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;

import java.time.Duration;

public final class MessageUtil {

    private static Main plugin;

    public MessageUtil(Main plugin) {
        this.plugin = plugin;
    }

    public static void sendMessage(CommandSender sender, String path, String... replacements) {
        ConfigurationSection section = plugin.getConfigManager().getConfigurationSection("messages", path);

        String msg = null;
        String title = null;
        String subtitle = null;
        String actionBar = null;
        String sound = null;
        boolean enabled = true;
        
        int fadeIn = 20;
        int stay = 60;
        int fadeOut = 20;

        if (section != null) {
            enabled = section.getBoolean("enabled", true);
            msg = section.getString("message", null);
            title = section.getString("title", null);
            subtitle = section.getString("subtitle", null);
            actionBar = section.getString("action_bar", null);
            sound = section.getString("sound", null);
            fadeIn = section.getInt("fade_in", fadeIn);
            stay = section.getInt("stay", stay);
            fadeOut = section.getInt("fade_out", fadeOut);
        }

        if (!enabled || (msg == null && title == null && subtitle == null && actionBar == null && sound == null)) {
            return;
        }

        Player player = sender instanceof Player ? (Player) sender : null;

        // PlaceholderAPI replacements
        if (player != null) {
            if (msg != null) {
                msg = PlaceholderAPI.setPlaceholders(player, msg).replace("%player%", player.getName());
            }
            if (title != null) {
                title = PlaceholderAPI.setPlaceholders(player, title).replace("%player%", player.getName());
            }
            if (subtitle != null) {
                subtitle = PlaceholderAPI.setPlaceholders(player, subtitle).replace("%player%", player.getName());
            }
            if (actionBar != null) {
                actionBar = PlaceholderAPI.setPlaceholders(player, actionBar).replace("%player%", player.getName());
            }
        }

        // Custom replacements
        if (replacements.length % 2 == 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                String target = replacements[i];
                String replacement = replacements[i + 1];
                if (target != null && replacement != null) {
                    if (msg != null) msg = msg.replace(target, replacement);
                    if (title != null) title = title.replace(target, replacement);
                    if (subtitle != null) subtitle = subtitle.replace(target, replacement);
                    if (actionBar != null) actionBar = actionBar.replace(target, replacement);
                }
            }
        } else {
            plugin.getLogger().warning("Invalid replacements for message: " + path);
        }

        // Send message
        if (msg != null) sender.sendMessage(ColorUtil.miniHex(msg));

        // Action bar
        if (actionBar != null && player != null) player.sendActionBar(ColorUtil.miniHex(actionBar));

        // Title & subtitle
        if ((title != null || subtitle != null) && player != null) {
            Times times = Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L)
            );
            player.showTitle(
                Title.title(
                    title != null ? ColorUtil.miniHex(title) : Component.empty(),
                    subtitle != null ? ColorUtil.miniHex(subtitle) : Component.empty(),
                    times
                )
            );
        }

        // Sound
        if (sound != null && player != null) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(sound.toUpperCase()), 1.0F, 1.0F);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound for message: " + path);
            }
        }
    }
}