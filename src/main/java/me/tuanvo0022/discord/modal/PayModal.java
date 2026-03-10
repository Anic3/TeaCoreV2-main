package me.tuanvo0022.discord.modal;

import me.tuanvo0022.Main;

import net.dv8tion.jda.api.modals.Modal;

public final class PayModal extends BaseModal {

    public PayModal() {
        super("discord/modal/pay-modal");
    }

    public Modal create(Main plugin) {
        return build(plugin, "panel:pay_modal");
    }
}