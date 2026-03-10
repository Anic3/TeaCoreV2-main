package me.tuanvo0022.discord;

import me.tuanvo0022.Main;
import me.tuanvo0022.discord.panel.PanelButtonListener;
import me.tuanvo0022.discord.panel.PanelSelectListener;
import me.tuanvo0022.discord.panel.PanelModalListener;
import me.tuanvo0022.discord.commands.PanelSlashCommand;
import me.tuanvo0022.discord.commands.ProfileSlashCommand;

import com.tcoded.folialib.wrapper.task.WrappedTask;

import me.clip.placeholderapi.PlaceholderAPI;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class DiscordBotManager {

    private final Main plugin;
    private JDA jda;
    private WrappedTask activityTask;

    public DiscordBotManager(Main plugin) {
        this.plugin = plugin;
    }

    public void init() {
        String token = plugin.config().DISCORD_TOKEN;
        if (token == null || token.isBlank()) {
            plugin.getLogger().severe("[Discord] Bot token is missing!");
            return;
        }

        String guildId = plugin.config().DISCORD_GUILD_ID;
        if (guildId == null || guildId.isBlank()) {
            plugin.getLogger().severe("[Discord] Guild ID is missing!");
            return;
        }

        try {
            // Build JDA instance
            jda = JDABuilder.createDefault(token)
                .setStatus(parseStatus())
                .addEventListeners(
                    new PanelSlashCommand(plugin),
                    new PanelButtonListener(plugin),
                    new PanelSelectListener(plugin),
                    new PanelModalListener(plugin),
                    new ProfileSlashCommand(plugin)
                )
                .build();

            jda.awaitReady();
            plugin.getLogger().info("[Discord] Bot connected successfully.");

            Guild guild = jda.getGuildById(guildId);
            if (guild == null) {
                plugin.getLogger().severe("[Discord] Bot is not in guild: " + guildId);
                return;
            }

            registerCommands(guild);
            startActivityUpdater();

        } catch (Exception e) {
            plugin.getLogger().severe("[Discord] Failed to start bot!");
            e.printStackTrace();
        }
    }

    public void close() {
        if (activityTask != null) {
            activityTask.cancel();
            activityTask = null;
        }

        if (jda != null) {
            jda.shutdownNow();
            jda = null;
        }
    }

    public JDA getJda() {
        return jda;
    }

    /* ======================
       COMMAND REGISTRATION
       ====================== */

    private void registerCommands(Guild guild) {
        guild.updateCommands()
            .addCommands(
                Commands.slash("panel", "Tạo bảng điều khiển để quản lý tài khoản Minecraft của bạn"),
                
                Commands.slash(
                    "profile",
                    "Xem hồ sơ người chơi"
                ).addOption(
                    OptionType.STRING,
                    "player",
                    "Nhập tên người chơi cần tìm",
                    true
                )
            )
            .queue(
                success -> plugin.getLogger().info("[Discord] Slash commands registered."),
                error -> plugin.getLogger().severe(
                    "[Discord] Failed to register slash commands: " + error.getMessage()
                )
            );
    }

    /* ======================
       ACTIVITY UPDATER
       ====================== */

    private void startActivityUpdater() {
        long intervalTicks = plugin.config().DISCORD_ACTIVITY_UPDATE_INTERVAL * 20;

        activityTask = Main.getScheduler().runTimerAsync(() -> {
            if (jda == null) return;

            try {
                Activity activity = parseActivity();
                jda.getPresence().setActivity(activity);
            } catch (Exception e) {
                plugin.getLogger().warning(
                        "[Discord] Failed to update activity: " + e.getMessage()
                );
            }
        }, 5L * 20L, intervalTicks);
    }

    /* ======================
       INTERNAL HELPERS
       ====================== */

    private OnlineStatus parseStatus() {
        String raw = plugin.config().DISCORD_STATUS;

        try {
            return OnlineStatus.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return OnlineStatus.ONLINE;
        }
    }

    private Activity parseActivity() {
        String text = plugin.config().DISCORD_ACTIVITY_TEXT;

        text = replacePlaceholders(text);

        String type = plugin.config().DISCORD_ACTIVITY_TYPE;

        return switch (type.toUpperCase()) {
            case "WATCHING" -> Activity.watching(text);
            case "LISTENING" -> Activity.listening(text);
            case "COMPETING" -> Activity.competing(text);
            default -> Activity.playing(text);
        };
    }

    private String replacePlaceholders(String input) {
        return PlaceholderAPI.setPlaceholders(null, input);
    }
}
