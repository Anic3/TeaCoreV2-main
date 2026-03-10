package me.tuanvo0022.discord.panel;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.ColorUtil;
import me.tuanvo0022.utils.EmbedUtil;
import me.tuanvo0022.utils.NumberUtil;
import me.tuanvo0022.discord.modal.ChatModal;
import me.tuanvo0022.discord.modal.PayModal;
import me.tuanvo0022.economy.ShopEconomy;

import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;

import net.luckperms.api.model.user.User;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class PanelSelectListener extends BasePanel {
    
    public PanelSelectListener(Main plugin) {
        super(plugin);
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (!event.getComponentId().equals("panel:actions")) return;

        String value = event.getValues().get(0);

        switch (value) {
            case "link" -> handleLink(event);
            case "unlink" -> handleUnLink(event);

            case "lock" -> handleLock(event);
            case "unlock" -> handleUnlock(event);
            
            case "status" -> handleStatus(event);

            case "kick" -> handleKick(event);
            case "kill" -> handleKill(event);
            case "chat" -> handleChat(event);

            case "balance_money" -> handleBalanceMoney(event);
            case "balance_shard" -> handleBalanceShard(event);
            case "pay" -> handlePay(event);

            case "playerlist" -> handlePlayerList(event);

            default -> event.reply("Unknown action.")
                .setEphemeral(true)
                .queue(sc -> {}, er -> {});
        }
    }

    /* ======================
       HANDLERS
       ====================== */
    private void handleLink(StringSelectInteractionEvent event) {
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
    
    private void handleUnLink(StringSelectInteractionEvent event) {
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

    private void handleLock(StringSelectInteractionEvent event) {
        event.deferReply(true).queue(sc -> {}, er -> {});

        withLinkedAccount(event, acc -> {
            plugin.getDatabaseManager()
                .setAccountLockedAsync(acc.getPlayerId(), true)
                .thenRun(() -> event.getHook()
                    .editOriginal(config.getString(path, "lock_success"))
                    .queue());

            Player player = plugin.getServer().getPlayer(acc.getPlayerId());
            if (player != null) {
                player.kick(ColorUtil.miniHex(config.getString("messages", "lock_message")));
            }
        });
    }

    private void handleUnlock(StringSelectInteractionEvent event) {
        event.deferReply(true).queue(sc -> {}, er -> {});

        withLinkedAccount(event, acc -> {
            plugin.getDatabaseManager()
                .setAccountLockedAsync(acc.getPlayerId(), false)
                .thenRun(() -> event.getHook()
                    .editOriginal(config.getString(path, "unlock_success"))
                    .queue());
        });
    }

    private void handleStatus(StringSelectInteractionEvent event) {
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

    private void handleKick(StringSelectInteractionEvent event) {
        event.deferReply(true).queue(sc -> {}, er -> {});

        withLinkedAccount(event, acc -> {
            Player player = plugin.getServer().getPlayer(acc.getPlayerId());
            if (player == null) {
                event.getHook()
                    .editOriginal(config.getString(path, "player_not_online"))
                    .queue(sc -> {}, er -> {});
                return;
            }

            player.kick(ColorUtil.miniHex(
                config.getString(path, "kick_message")
            ));
            event.getHook()
                .editOriginal(config.getString(path, "kick_success"))
                .queue(sc -> {}, er -> {});
        });
    }

    private void handleKill(StringSelectInteractionEvent event) {
        event.deferReply(true).queue(sc -> {}, er -> {});

        withLinkedAccount(event, acc -> {
            Player player = plugin.getServer().getPlayer(acc.getPlayerId());
            if (player == null) {
                event.getHook()
                    .editOriginal(config.getString(path, "player_not_online"))
                    .queue(sc -> {}, er -> {});
                return;
            }

            Main.getScheduler().runAtEntity(player, task -> {
                player.setHealth(0);
                event.getHook()
                    .editOriginal(config.getString(path, "kill_success"))
                    .queue(sc -> {}, er -> {});
            });
        });
    }

    private void handleChat(StringSelectInteractionEvent event) {
        event.replyModal(new ChatModal().create(plugin)).queue(sc -> {}, er -> {});
    }

    private void handleBalanceMoney(StringSelectInteractionEvent event) {
        event.deferReply(true).queue(sc -> {}, er -> {});
        
        ShopEconomy economy = plugin.getEconomies().get(ShopEconomy.Type.VAULT);

        withLinkedAccount(event, acc -> {
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(acc.getPlayerId());
            
            double amount = economy != null ? economy.getBalance(player) : 0;
            String message = config.getString(path, "balance_money")
                .replace("%amount%", NumberUtil.formatShort(amount));

            event.getHook()
                .editOriginal(message)
                .queue(sc -> {}, er -> {});
        });
    }

    private void handleBalanceShard(StringSelectInteractionEvent event) {
        event.deferReply(true).queue(sc -> {}, er -> {});
        
        ShopEconomy economy = plugin.getEconomies().get(ShopEconomy.Type.COINSENGINE);

        withLinkedAccount(event, acc -> {
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(acc.getPlayerId());
            
            double amount = economy != null ? economy.getBalance(player) : 0;
            String message = config.getString(path, "balance_shard")
                .replace("%amount%", String.valueOf(amount));

            event.getHook()
                .editOriginal(message)
                .queue(sc -> {}, er -> {});
        });
    }

    private void handlePay(StringSelectInteractionEvent event) {
        event.replyModal(new PayModal().create(plugin)).queue(sc -> {}, er -> {});
    }

    private void handlePlayerList(StringSelectInteractionEvent event) {
        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();

        event.deferReply(true).queue(sc -> {}, er -> {});

        if (players.isEmpty()) {
            event.getHook()
                .editOriginal(config.getString(path, "no_players_online"))
                .queue(sc -> {}, er -> {});
            return;
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (Player p : players) {
            if (!first) sb.append(", ");
            sb.append(p.getName());
            first = false;
        }

        String playerList = sb.toString();

        String message = config.getString(path, "playerlist")
            .replace("%playerlist%", playerList).replace("%count%", String.valueOf(players.size()));

        event.getHook()
            .editOriginal(message)
            .queue(sc -> {}, er -> {});
    }
}
