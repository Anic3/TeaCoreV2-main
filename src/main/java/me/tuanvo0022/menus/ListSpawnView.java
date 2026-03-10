package me.tuanvo0022.menus;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.MessageUtil;
import me.tuanvo0022.utils.TeleportUtil;
import me.tuanvo0022.utils.WorldUtil;
import me.tuanvo0022.utils.ItemBuilder;

import me.devnatan.inventoryframework.component.Pagination;
import me.devnatan.inventoryframework.context.Context;
import me.devnatan.inventoryframework.context.OpenContext;
import me.devnatan.inventoryframework.context.RenderContext;
import me.devnatan.inventoryframework.context.SlotClickContext;
import me.devnatan.inventoryframework.state.State;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class ListSpawnView extends BaseMenu {
    public ListSpawnView(Main plugin) {
        super(plugin, "menus/list-spawn-menu");
    }

    private final State<Pagination> state = buildComputedPaginationState(this::fetchSpawnList)
        .elementFactory((ctx, builder, index, spawnName) -> {
            builder.onRender(render -> {
                Player player = ctx.getPlayer();

                ConfigurationSection section = config.getConfigurationSection(configPath, "pagination_items.spawn_item");
                if (section == null) {
                    debug("Missing config section: pagination_items.spawn_item in " + configPath);
                    render.setItem(null);
                    return;
                }

                ItemStack item = ItemBuilder.fromConfig(
                    section,
                    "%player%", player.getName(),
                    "%name%", spawnName,
                    "%max%", String.valueOf(getMaxPlayers("spawn")),
                    "%players%", String.valueOf(getCurrentPlayers("spawn", spawnName)),
                    "%player_ping%", String.valueOf(player.getPing())
                );
                render.setItem(item);
            })
            .onClick(click -> {
                if (click == null) {
                    click.setCancelled(true);
                    return;
                }
                playSound(click, config.getString(configPath, "pagination_items.spawn_item.sound"));
                handleAction(click, spawnName);
            });
        })
        .build();

    @Override
    public void onFirstRender(RenderContext render) {
        Pagination pagination = state.get(render);
        
        forEachSection("items", configPath, (key, section) -> {
            ItemStack itemStack = ItemBuilder.fromConfig(section, "%player%", render.getPlayer().getName());
                
            String layout = section.getString("layout");
            if (layout == null || layout.isEmpty()) return;
            
            String soundName = section.getString("sound");
            
            switch (layout) {
                case "<" -> render.layoutSlot('<', itemStack)
                    .displayIf(() -> pagination.currentPageIndex() != 0)
                    .updateOnStateChange(state)
                    .onClick(ctx -> {
                        playSound(ctx, soundName);
                        state.get(ctx).back();
                    });

                case ">" -> render.layoutSlot('>', itemStack)
                    .displayIf(() -> pagination.currentPageIndex() < pagination.lastPageIndex())
                    .updateOnStateChange(state)
                    .onClick(ctx -> {
                        playSound(ctx, soundName);
                        state.get(ctx).advance();
                    });

                case "V" -> render.layoutSlot('V', itemStack)
                    .onClick(ctx -> {
                        playSound(ctx, soundName);
                        ctx.closeForPlayer();
                    });

                case "X" -> render.layoutSlot('X', itemStack)
                    .onClick(ctx -> {
                        playSound(ctx, soundName);
                    }); 
                    
                case "R" -> render.layoutSlot('R', itemStack)
                    .onClick(ctx -> {
                        playSound(ctx, soundName);
                        handleAction(ctx, null);
                    });        
            }
        });
    }    

    private void handleAction(Context ctx, String spawnName) {
        ctx.closeForPlayer();

        Player player = ctx.getPlayer();

        if (spawnName == null) {
            Location loc = WorldUtil.findAvailableLocation(plugin.getDatabaseManager().getAllSpawnLocations());

            if (loc == null) {
                MessageUtil.sendMessage(player, "spawn_none_exist");
                return;
            }

            TeleportUtil.teleportCountdown(player, loc).thenAccept(success -> {
                if (success) {
                    MessageUtil.sendMessage(player, "teleport_complete_spawn");
                }
            });
            return;
        }

        plugin.getDatabaseManager().getSpawnLocation(spawnName).thenAccept(loc -> {
            if (loc == null) {
                MessageUtil.sendMessage(player, "spawn_not_found", "%name%", spawnName);
                return;
            }

            TeleportUtil.teleportCountdown(player, loc).thenAccept(success -> {
                if (success) {
                    MessageUtil.sendMessage(player, "teleport_complete_spawn", "%name%", spawnName);
                }
            });
        });
    }

    private List<String> fetchSpawnList(Context ctx) {
        return new ArrayList<>(plugin.getDatabaseManager().getAllSpawnNames());
    }

}
