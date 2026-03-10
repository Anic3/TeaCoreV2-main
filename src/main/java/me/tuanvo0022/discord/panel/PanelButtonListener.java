package me.tuanvo0022.discord.panel;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.ColorUtil;
import me.tuanvo0022.utils.EmbedUtil;
import me.tuanvo0022.utils.NumberUtil;
import me.tuanvo0022.economy.ShopEconomy;

import org.bukkit.entity.Player;

import java.util.List;

import org.bukkit.OfflinePlayer;

import net.luckperms.api.model.user.User;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import org.jetbrains.annotations.NotNull;

public class PanelButtonListener extends BasePanel {

    public PanelButtonListener(Main plugin) {
        super(plugin);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String id = event.getComponentId();

        switch (id) {
            case "panel:link" -> handleLink(event);
            case "panel:unlink" -> handleUnlink(event);
            case "panel:status" -> handleStatus(event);
            case "panel:lock" -> handleLock(event);
            case "panel:unlock" -> handleUnlock(event);
        }
    }

    private void handleLink(ButtonInteractionEvent event) {
        event.deferReply(true).queue(sc -> {}, er -> {});

        long discordId = event.getUser().getIdLong();

        plugin.getDatabaseManager()
            .getAccountAsync(discordId)
            .thenAccept(existing -> {
                if (existing != null) {
                    event.getHook()
                        .editOriginal(config.getString(path, "already_linked"))
                        .queue(sc -> {}, er -> {});
                    return;
                }

                event.getHook()
                    .editOriginal(
                        config.getString(path, "link_generated")
                            .replace("%code%", plugin.getLinkCodeManager().create(discordId))
                    )
                    .queue(sc -> {}, er -> {});
            });
    }

    private void handleUnlink(ButtonInteractionEvent event) {
        event.deferReply(true).queue(sc -> {}, er -> {});

        withLinkedAccount(event, acc -> {
            long cooldownMillis = plugin.config().UNLINK_COOLDOWN * 1000L;

            if (cooldownMillis > 0) {
                long now = System.currentTimeMillis();
                long linkedAt = acc.getLinkedAt();
                long passed = now - linkedAt;

                if (passed < cooldownMillis) {
                    long remaining = (cooldownMillis - passed) / 1000;

                    event.getHook()
                        .editOriginal(
                            config.getString(path, "unlink_cooldown")
                                .replace("%time%", NumberUtil.formatDuration(remaining))
                        )
                        .queue(sc -> {}, er -> {});
                    return;
                }
            }

            Player player = plugin.getServer().getPlayer(acc.getPlayerId());
            if (player == null) {
                event.getHook()
                    .editOriginal(config.getString(path, "player_not_online"))
                    .queue(sc -> {}, er -> {});
                return;
            }

            plugin.getDiscordService().forceRemoveMcRanksOnUnlink(acc.getPlayerId());

            plugin.getDatabaseManager()
                .unlinkAccountAsync(acc.getPlayerId())
                .thenRun(() -> {
                    event.getHook()
                        .editOriginal(config.getString(path, "unlink_success"))
                        .queue(sc -> {}, er -> {});

                    if (plugin.config().UNLINK_NICKNAME_RESET) {
                        plugin.getDiscordService().resetDiscordNickname(acc.getDiscordId());
                    }

                    if (!plugin.config().UNLINK_EXECUTE_COMMANDS) return;

                    String playerName = player.getName();

                    String uuid = acc.getPlayerId().toString();
                    String discordId = String.valueOf(acc.getDiscordId());

                    List<String> commands = plugin.config().UNLINK_SERVER_COMMANDS;
                    Main.getScheduler().runNextTick(task -> {
                        for (String raw : commands) {
                            String command = raw
                                    .replace("%player%", playerName)
                                    .replace("%uuid%", uuid)
                                    .replace("%discord%", discordId);

                            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                        }
                    });
                });
        });
    }

    private void handleStatus(ButtonInteractionEvent event) {
        event.deferReply(true).queue(sc -> {}, er -> {});

        long discordId = event.getUser().getIdLong();

        withLinkedAccount(event, acc -> {
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(acc.getPlayerId());
            if (player == null) {
                event.getHook()
                    .editOriginal(config.getString(path, "player_not_found"))
                    .queue(sc -> {}, er -> {});
                return;
            }

            ShopEconomy economy = plugin.getEconomies().get(ShopEconomy.Type.VAULT);
            double moneyBalance = economy != null ? economy.getBalance(player) : 0;

            ShopEconomy shard = plugin.getEconomies().get(ShopEconomy.Type.COINSENGINE);
            double shardBalance = shard != null ? shard.getBalance(player) : 0;

            String path = "discord/panel/panel-status-embed";
            String key = "placeholders.";

            String onlineText = config.getString(path, key + "player_online");
            String offlineText = config.getString(path, key + "player_offline");

            String lockedText = config.getString(
                    path,
                    key + (acc.isLocked() ? "account_locked" : "account_unlocked")
            );

            String linkedText = config.getString(
                    path,
                    key + (acc != null ? "linked_yes" : "linked_no")
            );

            String defaultRank = config.getString(path, key + "default_rank");
            
            User user = plugin.getLuckPerms()
                    .getUserManager()
                    .getUser(player.getUniqueId());

            String playerRank = (user == null || "default".equalsIgnoreCase(user.getPrimaryGroup()))
                    ? defaultRank
                    : user.getPrimaryGroup();

            EmbedBuilder embed = EmbedUtil.builder(
                    path,
                    "%player_name%", player.getName(),
                    "%player_uuid%", player.getUniqueId().toString(),
                    "%player_online%", player.isOnline() ? onlineText : offlineText,
                    "%discord_id%", String.valueOf(discordId),
                    "%discord_tag%", event.getUser().getAsTag(),
                    "%locked%", lockedText,
                    "%linked%", linkedText,
                    "%player_money%", NumberUtil.formatShort(moneyBalance),
                    "%player_shard%", String.valueOf(shardBalance),
                    "%player_rank%", playerRank
            );

            event.getHook()
                .editOriginalEmbeds(embed.build())
                .queue(sc -> {}, er -> {});
        });
    }

    private void handleLock(ButtonInteractionEvent event) {
        event.deferReply(true).queue(sc -> {}, er -> {});

        withLinkedAccount(event, acc -> {
            plugin.getDatabaseManager()
                .setAccountLockedAsync(acc.getPlayerId(), true)
                .thenRun(() -> event.getHook()
                    .editOriginal(config.getString(path, "lock_success"))
                    .queue(sc -> {}, er -> {}));

            Player player = plugin.getServer().getPlayer(acc.getPlayerId());
            if (player != null) {
                player.kick(ColorUtil.miniHex(config.getString("messages", "lock_message")));
            }
        });
    }

    private void handleUnlock(ButtonInteractionEvent event) {
        event.deferReply(true).queue(sc -> {}, er -> {});

        withLinkedAccount(event, acc -> {
            plugin.getDatabaseManager()
                .setAccountLockedAsync(acc.getPlayerId(), false)
                .thenRun(() -> event.getHook()
                    .editOriginal(config.getString(path, "unlock_success"))
                    .queue(sc -> {}, er -> {}));
        });
    }
}