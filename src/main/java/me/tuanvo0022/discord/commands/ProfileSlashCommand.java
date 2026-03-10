package me.tuanvo0022.discord.commands;

import me.tuanvo0022.Main;
import me.tuanvo0022.economy.ShopEconomy;
import me.tuanvo0022.managers.ConfigManager;
import me.tuanvo0022.utils.EmbedUtil;
import me.tuanvo0022.utils.NumberUtil;
import me.tuanvo0022.economy.ShopEconomy;

import org.bukkit.OfflinePlayer;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;

public class ProfileSlashCommand extends ListenerAdapter {

    private final Main plugin;
    private final String path = "discord/panel/profile-embed";

    public ProfileSlashCommand(Main plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("profile")) return;

        if (event.isAcknowledged()) return;

        event.deferReply(false).queue(success -> {
            ConfigManager cfg = plugin.getConfigManager();

            String playerName = event.getOption("player").getAsString();
            if (playerName == null) {
                event.getHook()
                    .editOriginal(cfg.getString(path, "target_not_found"))
                    .queue(sc -> {}, er -> {});
                return;
            }

            OfflinePlayer player = plugin.getServer().getOfflinePlayerIfCached(playerName);
            if (player == null) {
                event.getHook()
                    .editOriginal(cfg.getString(path, "target_not_found"))
                    .queue(sc -> {}, er -> {});
                return;
            }

            ShopEconomy economy = plugin.getEconomies().get(ShopEconomy.Type.VAULT);
            double moneyBalance = economy != null ? economy.getBalance(player) : 0;

            ShopEconomy shard = plugin.getEconomies().get(ShopEconomy.Type.COINSENGINE);
            double shardBalance = shard != null ? shard.getBalance(player) : 0;

            String key = "placeholders.";

            String onlineText = cfg.getString(path, key + "player_online");
            String offlineText = cfg.getString(path, key + "player_offline");

            plugin.getDatabaseManager()
                .getAccountAsync(player.getUniqueId())
                .thenAccept(acc -> {
                    String lockedText = acc == null ? "N/A"
                            : cfg.getString(path, key + (acc.isLocked() ? "account_locked" : "account_unlocked"));
                            
                    String linkedText = cfg.getString(
                            path,
                            key + (acc != null ? "linked_yes" : "linked_no")
                    );

                    String discordTag = "N/A";
                    String discordId = "0";

                    if (acc != null) {
                        discordId = String.valueOf(acc.getDiscordId());

                        User cached = event.getJDA().getUserById(acc.getDiscordId());
                        if (cached != null) {
                            discordTag = cached.getAsTag();
                        }
                    }

                    String defaultRank = cfg.getString(path, key + "default_rank");
            
                    net.luckperms.api.model.user.User user = plugin.getLuckPerms()
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
                            "%discord_id%", discordId,
                            "%discord_tag%", discordTag,
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
        }, error -> {});
    }
}
