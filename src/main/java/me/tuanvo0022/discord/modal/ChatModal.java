package me.tuanvo0022.discord.modal;

import me.tuanvo0022.Main;

import net.dv8tion.jda.api.modals.Modal;

public final class ChatModal extends BaseModal {

    public ChatModal() {
        super("discord/modal/chat-modal");
    }

    public Modal create(Main plugin) {
        return build(plugin, "panel:chat_modal");
    }
}