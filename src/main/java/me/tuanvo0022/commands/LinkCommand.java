package me.tuanvo0022.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.discord.link.LinkCode;
import me.tuanvo0022.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class LinkCommand extends Command {
    private final Main plugin;

    public LinkCommand(Main plugin) {
        super("link");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "error_player_only");
            return true;
        }

        if (!player.hasPermission("teacore.link")) {
            MessageUtil.sendMessage(player, "error_permission");
            return true;
        }

        if (args.length != 1) {
            MessageUtil.sendMessage(player, "link_command");
            return true;
        }

        UUID playerId = player.getUniqueId();
        String code = args[0].toUpperCase();

        plugin.getDatabaseManager()
                .hasLinkedAccountAsync(playerId)
                .thenAccept(hasLinked -> {
                    if (hasLinked) {
                        MessageUtil.sendMessage(player, "link_already_linked");
                        return;
                    }

                    LinkCode linkCode = plugin.getLinkCodeManager().get(code);
                    if (linkCode == null) {
                        MessageUtil.sendMessage(player, "link_invalid_code");
                        return;
                    }

                    long discordId = linkCode.getDiscordId();

                    plugin.getDatabaseManager()
                            .hasLinkedAccountAsync(discordId)
                            .thenAccept(discordLinked -> {
                                if (discordLinked) {
                                    MessageUtil.sendMessage(player, "link_discord_already_linked");
                                    return;
                                }

                                LinkCode consumed = plugin.getLinkCodeManager().consume(code);
                                if (consumed == null) {
                                    MessageUtil.sendMessage(player, "link_invalid_code");
                                    return;
                                }

                                plugin.getDatabaseManager()
                                        .linkAccountAsync(playerId, discordId)
                                        .thenRun(() -> {

                                            MessageUtil.sendMessage(player, "link_success");

                                            if (!plugin.config().LINK_COMMANDS_ENABLED) return;

                                            List<String> commands = plugin.config().LINK_COMMANDS;
                                            Main.getScheduler().runNextTick(task -> {
                                                for (String raw : commands) {
                                                    String command = raw
                                                            .replace("%player%", player.getName())
                                                            .replace("%uuid%", player.getUniqueId().toString())
                                                            .replace("%discord%", String.valueOf(discordId));

                                                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                                                }
                                            });

                                            plugin.getDiscordService().syncMcToDiscord(player);
                                            plugin.getDiscordService().syncDiscordToMc(discordId);
                                        });
                            });
                });

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return Collections.emptyList();
    }
}
