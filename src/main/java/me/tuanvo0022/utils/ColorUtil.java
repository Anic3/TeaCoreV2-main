package me.tuanvo0022.utils;

import me.tuanvo0022.Main;

import org.bukkit.ChatColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {
    
    private static Main plugin;

    public ColorUtil(Main plugin) {
        this.plugin = plugin;
    }
    
    private static final Pattern pattern = Pattern.compile("(?<!\\\\)(#[a-fA-F0-9]{6})");
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static Component miniHex(String input) {
        return miniMessage.deserialize(input);
    }
    
    public static String legacyHex(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder("");
            for (char c : ch) {
                builder.append("&" + c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * Retrieves a string from the plugin config, applies replacements, and converts it to a MiniMessage Component.
     *
     * @param path The config path to retrieve the string from.
     * @param replacements Optional key-value pairs for replacements (key1, value1, key2, value2, ...).
     * @return Component with MiniMessage formatting and applied replacements.
     */
    public static Component getComponent(String file, String path, String... replacements) {
        // Get the string from the plugin config
        String msg = plugin.getConfigManager().getString(file, path);
        if (msg == null) return Component.empty();

        // Apply replacements if given and valid
        if (replacements.length % 2 == 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                String target = replacements[i];
                String replacement = replacements[i + 1];
                if (target != null && replacement != null) {
                    msg = msg.replace(target, replacement);
                }
            }
        } else {
            plugin.getLogger().warning("Invalid replacements for message: " + path);
        }

        // Convert to MiniMessage Component
        return miniHex(msg);
    }
}