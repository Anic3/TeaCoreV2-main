package me.tuanvo0022.discord.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.managers.ConfigManager;
import me.tuanvo0022.utils.EmbedUtil;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PanelSlashCommand extends ListenerAdapter {

    private final Main plugin;
    private final String path = "discord/panel/panel-embed";

    public PanelSlashCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("panel")) return;
        if (event.isAcknowledged()) return;

        ConfigManager cfg = plugin.getConfigManager();

        String base = "commands.panel";

        // ===== ENABLE CHECK =====
        if (!cfg.getBoolean("discord/config", base + ".enabled", true)) {
            event.reply("Lệnh này đã bị tắt.")
                .setEphemeral(true)
                .queue(sc -> {}, er -> {});
            return;
        }

        // ===== ROLE CHECK =====
        List<String> allowedRoles = cfg.getStringList("discord/config", base + ".allowed-roles");
        if (!plugin.getDiscordService().isAllowedRole(event, allowedRoles)) {
            event.reply("Bạn không có quyền để dùng lệnh này.")
                .setEphemeral(true)
                .queue(sc -> {}, er -> {});
            return;
        }

        // ===== CHANNEL CHECK =====
        List<String> allowedChannels = cfg.getStringList("discord/config", base + ".allowed-channels");
        if (!plugin.getDiscordService().isAllowedChannel(event, allowedChannels)) {
            event.reply("Lệnh này không được phép dùng trong kênh này.")
                .setEphemeral(true)
                .queue(sc -> {}, er -> {});
            return;
        }

        event.deferReply(false).queue(success -> {
            // ===== EMBED =====
            EmbedBuilder embed = EmbedUtil.builder(path);

            // ===== BUTTONS =====
            List<Button> buttons = new ArrayList<>();
            List<Map<?, ?>> buttonList =
                cfg.getConfig(path).getMapList("buttons.row-1");

            for (Map<?, ?> btn : buttonList) {
                buttons.add(
                    Button.of(
                        ButtonStyle.valueOf(((String) btn.get("style")).toUpperCase()),
                        (String) btn.get("id"),
                        (String) btn.get("text")
                    )
                );
            }

            // ===== DROPDOWN =====
            ConfigurationSection dropdown =
                cfg.getConfigurationSection(path, "dropdown");

            StringSelectMenu.Builder menu =
                StringSelectMenu.create(dropdown.getString("id"))
                    .setPlaceholder(dropdown.getString("placeholder"))
                    .setMinValues(dropdown.getInt("min-select"))
                    .setMaxValues(dropdown.getInt("max-select"));

            for (Map<?, ?> opt : cfg.getConfig(path).getMapList("dropdown.options")) {
                menu.addOption(
                    (String) opt.get("label"),
                    (String) opt.get("value"),
                    (String) opt.get("description")
                );
            }

            // ===== SEND =====
            event.getHook().sendMessageEmbeds(embed.build())
                .addComponents(
                    ActionRow.of(buttons),
                    ActionRow.of(menu.build())
                )
                // .setEphemeral(false)
                .queue(sc -> {}, er -> {});
        }, error -> {});
    }
}
