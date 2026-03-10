package me.tuanvo0022.discord.panel;

import me.tuanvo0022.Main;
import me.tuanvo0022.managers.ConfigManager;
import me.tuanvo0022.discord.link.LinkedAccount;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.util.function.Consumer;

public abstract class BasePanel extends ListenerAdapter {
    protected final Main plugin;
    protected final ConfigManager config;
    protected final String path = "discord/panel/panel-messages";

    public BasePanel(Main plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    protected void withLinkedAccount(GenericInteractionCreateEvent event, Consumer<LinkedAccount> action) {
        long discordId = event.getUser().getIdLong();

        plugin.getDatabaseManager()
            .getAccountAsync(discordId)
            .thenAccept(account -> {
                if (account == null) {
                    reply(event, config.getString(path, "not_linked"));
                    return;
                }
                action.accept(account);
            });
    }

    private void reply(GenericInteractionCreateEvent event, String message) {
        if (event instanceof ButtonInteractionEvent e) {
            e.getHook().sendMessage(message).queue(sc -> {}, er -> {});
        } else if (event instanceof StringSelectInteractionEvent e) {
            e.getHook().sendMessage(message).queue(sc -> {}, er -> {});
        } else if (event instanceof ModalInteractionEvent e) {
            e.getHook().sendMessage(message).queue(sc -> {}, er -> {});
        }
    }
}