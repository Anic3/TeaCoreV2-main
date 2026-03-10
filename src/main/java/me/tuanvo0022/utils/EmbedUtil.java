package me.tuanvo0022.utils;

import me.tuanvo0022.Main;
import me.tuanvo0022.managers.ConfigManager;
import net.dv8tion.jda.api.EmbedBuilder;
import org.bukkit.configuration.ConfigurationSection;

import java.awt.Color;

public final class EmbedUtil {

    private static Main plugin;

    public EmbedUtil(Main plugin) {
        EmbedUtil.plugin = plugin;
    }

    public static EmbedBuilder builder(String path, String... replacements) {
        ConfigManager cfg = plugin.getConfigManager();

        String title = cfg.getString(path, "title");
        String description = cfg.getString(path, "description");

        ConfigurationSection footerSec =
                cfg.getConfigurationSection(path, "footer");

        String footerText = footerSec != null
                ? footerSec.getString("text")
                : null;

        String footerIcon = footerSec != null
                ? footerSec.getString("icon_url")
                : null;

        String thumbnail = cfg.getString(path, "thumbnail");

        // ===== APPLY REPLACEMENTS =====
        if (replacements.length % 2 == 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                String target = replacements[i];
                String replacement = replacements[i + 1];

                if (target == null || replacement == null) continue;

                if (title != null) title = title.replace(target, replacement);
                if (description != null) description = description.replace(target, replacement);
                if (footerText != null) footerText = footerText.replace(target, replacement);
                if (thumbnail != null) thumbnail = thumbnail.replace(target, replacement);
            }
        } else {
            plugin.getLogger().warning(
                "[EmbedUtil] Invalid replacements for: " + path
            );
        }

        // ===== BUILD EMBED =====
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(parseColor(cfg.getString(path, "color", "#2ECC71")));
                
        if (thumbnail != null && !thumbnail.isEmpty()) {
            embed.setThumbnail(thumbnail);
        }

        if (footerText != null) {
            embed.setFooter(footerText, footerIcon);
        }

        return embed;
    }

    private static Color parseColor(String hex) {
        try {
            return Color.decode(hex);
        } catch (Exception e) {
            return Color.decode("#2ECC71");
        }
    }
}
