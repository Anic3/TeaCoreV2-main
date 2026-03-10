package me.tuanvo0022.menus;

import me.tuanvo0022.Main;
import me.tuanvo0022.utils.SoundUtil;
import me.tuanvo0022.utils.ActionUtil;
import me.tuanvo0022.utils.ColorUtil;
import me.tuanvo0022.utils.ItemBuilder;
import me.tuanvo0022.managers.ConfigManager;

import me.devnatan.inventoryframework.View;
import me.devnatan.inventoryframework.ViewConfigBuilder;
import me.devnatan.inventoryframework.context.OpenContext;
import me.devnatan.inventoryframework.context.SlotClickContext;
import me.devnatan.inventoryframework.state.State;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * BaseMenu - the base class for all menus.
 * Provides the plugin and configManager fields for convenient use in subclasses.
 */
public abstract class BaseMenu extends View {

    protected final Main plugin;
    protected final ConfigManager config;
    
    protected String configPath;
    
    private final boolean debugEnabled;

    public BaseMenu(Main plugin, String configPath) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.configPath = configPath;
        this.debugEnabled = plugin.getConfig().getBoolean("debug");
    }
    
    public BaseMenu(Main plugin) {
        this(plugin, null);
    }
    
    protected void debug(String message) {
        if (debugEnabled) {
            plugin.getLogger().warning(message);
        }
    }

    @Override
    public void onInit(ViewConfigBuilder config) {
        config.scheduleUpdate(20L);
    }
    
    @Override
    public void onOpen(OpenContext ctx) {
        Player player = ctx.getPlayer();

        var cfg = ctx.modifyConfig()
            .title(ColorUtil.getComponent(configPath, "title", "%player%", player.getName()));

        try {
            cfg.layout(config.getStringList(configPath, "layout").toArray(new String[0]));
        } catch (Exception e) {
            debug("Error loading layout, fallback to size: " + e.getMessage());
            cfg.size(config.getInt(configPath, "size"));
        }
        
        ActionUtil.runAction(config.getStringList(configPath, "actions"), player);
    }
    
    @Override
    public void onClick(SlotClickContext ctx) {
        if (ctx.isOutsideClick()) {
            return;
        }
        
        if (!ctx.isOnEntityContainer() || ctx.isShiftClick()) {
            ctx.setCancelled(true);
        }
    }
    
    protected void playSound(SlotClickContext ctx, String soundName) {
        SoundUtil.playSound(ctx.getPlayer(), soundName);
    }
    
    protected void forEachSection(String sectionPath, String fallbackKey, BiConsumer<String, ConfigurationSection> action) {
        ConfigurationSection itemsSection = config.getConfigurationSection(configPath, sectionPath);
        if (itemsSection == null) {
            debug("Missing section for item key at " + configPath);
            return;
        }
        
        List<String> errors = new ArrayList<>();
        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection section = itemsSection.getConfigurationSection(key);
            if (section == null) {
                errors.add("Missing section for '" + key + "'");
                continue;
            }

            try {
                action.accept(key, section);
            } catch (Exception e) {
                errors.add("Failed to load item '" + key + "': " + e.getMessage());
                continue;
            }
        }
        if (!errors.isEmpty()) {
            debug("Errors at " + configPath + ": " + String.join(", ", errors));
        }
    }

    protected int getMaxPlayers(String path) {
        switch (path) {
            case "spawn":
                return plugin.config().SPAWN_MAX_PLAYERS;

            case "afk":
                return plugin.config().AFK_MAX_PLAYERS;

            default:
                return 0;
        }
    }
    
    protected int getCurrentPlayers(String type, String name) {
        Location loc;

        switch (type.toLowerCase()) {
            case "spawn" -> loc = plugin.getDatabaseManager().spawnCache.get(name);
            case "afk"   -> loc = plugin.getDatabaseManager().afkCache.get(name);
            default      -> { return 0; }
        }

        // CHECK NULL
        if (loc == null || loc.getWorld() == null) {
            return 0;
        }

        return loc.getWorld().getPlayerCount();
    }

}