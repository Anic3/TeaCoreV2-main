package me.tuanvo0022.discord.panel;

import me.tuanvo0022.Main;
import me.tuanvo0022.service.PayResult;
import me.tuanvo0022.discord.modal.ChatModal;
import me.tuanvo0022.utils.NumberUtil;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import org.jetbrains.annotations.NotNull;

public class PanelModalListener extends BasePanel {

    public PanelModalListener(Main plugin) {
        super(plugin);
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String modalId = event.getModalId();

        switch (modalId) {
            case "panel:chat_modal" -> handleChat(event);
            case "panel:pay_modal" -> handlePay(event);
        }
    }

    private void handleChat(ModalInteractionEvent event) {
        event.deferReply(true).queue(sc -> {}, er -> {});

        withLinkedAccount(event, acc -> {
            Player player = plugin.getServer().getPlayer(acc.getPlayerId());
            if (player == null) {
                event.getHook()
                    .editOriginal(config.getString(path, "player_not_online"))
                    .queue(sc -> {}, er -> {});
                return;
            }

            String message = event.getValue("chat_message").getAsString();

            Main.getScheduler().runAtEntity(player, task -> {
                player.chat(message);

                event.getHook()
                    .editOriginal(config.getString(path, "chat_success"))
                    .queue(sc -> {}, er -> {});
            });
        });
    }

    private void handlePay(ModalInteractionEvent event) {
        event.deferReply(true).queue(sc -> {}, er -> {});

        withLinkedAccount(event, acc -> {
            OfflinePlayer sender = plugin.getServer().getOfflinePlayer(acc.getPlayerId());
            if (sender == null) {
                event.getHook()
                    .editOriginal(config.getString(path, "player_not_found"))
                    .queue(sc -> {}, er -> {});
                return;
            }

            String targetName = event.getValue("pay_target").getAsString();
            String amountStr = event.getValue("pay_amount").getAsString();

            OfflinePlayer target = plugin.getServer().getOfflinePlayerIfCached(targetName);
            if (target == null) {
                event.getHook()
                    .editOriginal(config.getString(path, "target_not_found"))
                    .queue(sc -> {}, er -> {});
                return;
            }

            UUID playerUUID = sender.getUniqueId();
            UUID targetUUID = target.getUniqueId();
                    
            if (playerUUID.equals(targetUUID)) {
                event.getHook()
                    .editOriginal(config.getString(path, "pay_self"))
                    .queue(sc -> {}, er -> {});
                return;
            }

            double amount;
            
            try {
                amount = NumberUtil.parseAmount(amountStr);
            } catch (NumberFormatException e) {
                event.getHook()
                    .editOriginal(config.getString(path, "pay_invalid_amount"))
                    .queue(sc -> {}, er -> {});
                return;
            }
            
            if (amount <= 0.0) {
                event.getHook()
                    .editOriginal(config.getString(path, "pay_invalid_amount"))
                    .queue(sc -> {}, er -> {});
                return;
            }

            plugin.getEconomyService()
                    .pay(sender, target, amount)
                    .thenAccept(result -> {
                        switch (result) {
                            case SUCCESS -> {
                                String message = config.getString(path, "pay_success")
                                    .replace("%target%", target.getName())
                                    .replace("%amount%", NumberUtil.formatShort(amount));

                                event.getHook()
                                    .editOriginal(message)
                                    .queue(sc -> {}, er -> {});
                            }

                            case NOT_ENOUGH_MONEY -> {
                                event.getHook()
                                    .editOriginal(config.getString(path, "pay_not_enough_money"))
                                    .queue(sc -> {}, er -> {});
                            }

                            case DISABLED_BY_TARGET -> {
                                event.getHook()
                                    .editOriginal(config.getString(path, "pay_disabled_by_target"))
                                    .queue(sc -> {}, er -> {});
                            }

                            case ERROR -> {
                                event.getHook()
                                    .editOriginal(config.getString(path, "pay_error"))
                                    .queue(sc -> {}, er -> {});
                            }
                        }
                    });
        });
    }
}