package me.tuanvo0022;

import me.tuanvo0022.commands.BalanceCommand;
import me.tuanvo0022.commands.DiscordCommand;
import me.tuanvo0022.commands.HideCommand;
import me.tuanvo0022.commands.MessageCommand;
import me.tuanvo0022.commands.MessageToggleCommand;
import me.tuanvo0022.commands.NightVisionCommand;
import me.tuanvo0022.commands.PayCommand;
import me.tuanvo0022.commands.PayToggleCommand;
import me.tuanvo0022.commands.PhantomCommand;
import me.tuanvo0022.commands.MobSpawnCommand;
import me.tuanvo0022.commands.PingCommand;
import me.tuanvo0022.commands.ReloadCommand;
import me.tuanvo0022.commands.ReplyCommand;
import me.tuanvo0022.commands.StoreCommand;
import me.tuanvo0022.commands.ToggleChatCommand;
import me.tuanvo0022.commands.AfkshardCommand;
import me.tuanvo0022.commands.KillshardCommand;
import me.tuanvo0022.commands.ShardCommand;
import me.tuanvo0022.commands.SetSpawnCommand;
import me.tuanvo0022.commands.DelSpawnCommand;
import me.tuanvo0022.commands.SpawnCommand;
import me.tuanvo0022.commands.SetAfkCommand;
import me.tuanvo0022.commands.DelAfkCommand;
import me.tuanvo0022.commands.AfkCommand;
import me.tuanvo0022.commands.SetWarpCommand;
import me.tuanvo0022.commands.DelWarpCommand;
import me.tuanvo0022.commands.WarpCommand;
import me.tuanvo0022.commands.RestartCommand;
import me.tuanvo0022.commands.LinkCommand;
import me.tuanvo0022.commands.UnlinkCommand;
import me.tuanvo0022.commands.EnderchestCommand;

import me.tuanvo0022.database.DatabaseManager;

import me.tuanvo0022.economy.ShopEconomy;
import me.tuanvo0022.economy.impl.CoinsEngineEconomy;
import me.tuanvo0022.economy.impl.PlayerPointsEconomy;
import me.tuanvo0022.economy.impl.VaultEconomy;

import me.tuanvo0022.listeners.JumpAndFlyListener;
import me.tuanvo0022.listeners.HideListener;
import me.tuanvo0022.listeners.PhantomListener;
import me.tuanvo0022.listeners.MobSpawnListener;
import me.tuanvo0022.listeners.PlayerDataLoader;
import me.tuanvo0022.listeners.ToggleChatListener;
import me.tuanvo0022.listeners.JoinQuitListener;
import me.tuanvo0022.listeners.SpawnListener;
import me.tuanvo0022.listeners.AfkListener;
import me.tuanvo0022.listeners.DeathLightningListener;
import me.tuanvo0022.listeners.DiscordListener;

import me.tuanvo0022.menus.ListSpawnView;
import me.tuanvo0022.menus.ListAfkView;

import me.tuanvo0022.discord.DiscordBotManager;
import me.tuanvo0022.discord.link.LinkCodeManager;

import me.tuanvo0022.managers.ConfigManager;
import me.tuanvo0022.managers.FileManager;

import me.tuanvo0022.service.EconomyService;
import me.tuanvo0022.service.DiscordService;

import me.tuanvo0022.papi.TeaCoreExpansion;

import me.tuanvo0022.utils.ColorUtil;
import me.tuanvo0022.utils.MessageUtil;
import me.tuanvo0022.utils.TeleportUtil;
import me.tuanvo0022.utils.WorldUtil;
import me.tuanvo0022.utils.EmbedUtil;

import me.tuanvo0022.config.ConfigValues;

import com.github.retrooper.packetevents.PacketEvents;

import net.luckperms.api.LuckPerms;

import me.devnatan.inventoryframework.ViewFrame;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;

import dev.respark.licensegate.LicenseGate;

import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main extends JavaPlugin {
    private static FoliaLib foliaLib;
    
    private FileManager fileManager;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private ViewFrame viewFrame;
    private ConfigValues configValues;

    private DiscordBotManager discordBotManager;
    private LinkCodeManager linkCodeManager;
    
    private EconomyService economyService;
    private DiscordService discordService;

    private LuckPerms luckPerms;

    private final ConcurrentHashMap<ShopEconomy.Type, ShopEconomy> economies = new ConcurrentHashMap<>();
    private final Map<Player, Player> lastMessaged = new ConcurrentHashMap<>();
    
    @Override
    public void onEnable() {
        saveDefaultConfig();

        LicenseGate licenseGate = new LicenseGate("a1f5e");
        LicenseGate.ValidationType result = licenseGate.verify(getConfig().getString("license-key"), "TeaCoreV2");
        if (result != LicenseGate.ValidationType.VALID) {
            getLogger().severe("License key is invalid.");
            getLogger().severe("Please contact Discord: Tuanvo0022");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            economies.put(ShopEconomy.Type.VAULT, new VaultEconomy());
            getLogger().info("Vault detected.");
        } else {
            getLogger().warning("Vault not found, disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("PlaceholderAPI detected.");
        } else {
            getLogger().warning("PlaceholderAPI not found, disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        if (getServer().getPluginManager().getPlugin("TAB") != null) {
            getLogger().info("TAB detected.");
        } else {
            getLogger().warning("TAB not found, disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        if (getServer().getPluginManager().getPlugin("packetevents") != null) {
            PacketEvents.getAPI().load();
            getLogger().info("packetevents detected.");
        } else {
            getLogger().warning("packetevents not found, disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            this.luckPerms = provider.getProvider();
            getLogger().info("LuckPerms detected.");
        } else {
            getLogger().warning("LuckPerms not found, disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        foliaLib = new FoliaLib(this);
        
        databaseManager = new DatabaseManager(this);
        fileManager = new FileManager(this);
        configManager = new ConfigManager(this);
        configValues = new ConfigValues(this);
        economyService = new EconomyService(this);
        
        databaseManager.loadAllSpawnLocations().thenRun(() -> {
            for (String name : databaseManager.getAllSpawnNames()) {
                if (name == null) continue;
                
                getLogger().info("[Spawn] Loaded " + name);
            }
        });
        
        databaseManager.loadAllAfkLocations().thenRun(() -> {
            for (String name : databaseManager.getAllAfkNames()) {
                if (name == null) continue;
                
                getLogger().info("[AFK] Loaded " + name);
            }
        });
        
        databaseManager.loadAllWarpLocations().thenRun(() -> {
            for (String name : databaseManager.getAllWarpNames()) {
                if (name == null) continue;
                
                getLogger().info("[Warp] Loaded " + name);
            }
        });
        
        fileManager.loadFile("messages.yml");
        fileManager.loadFile("hooks.yml");
        fileManager.loadFile("menus/list-spawn-menu.yml");
        fileManager.loadFile("menus/list-afk-menu.yml");
        fileManager.loadFile("spawn/config.yml");
        fileManager.loadFile("afk/config.yml");
        fileManager.loadFile("phantom/config.yml");
        fileManager.loadFile("mobspawn/config.yml");
        fileManager.loadFile("doublejump/config.yml");
        fileManager.loadFile("fly/config.yml");
        fileManager.loadFile("shard/config.yml");
        fileManager.loadFile("teleport/config.yml");
        fileManager.loadFile("combat/config.yml");
        fileManager.loadFile("joinquit/config.yml");
        fileManager.loadFile("discord/config.yml");
        fileManager.loadFile("discord/panel/panel-embed.yml");
        fileManager.loadFile("discord/panel/panel-status-embed.yml");
        fileManager.loadFile("discord/panel/panel-messages.yml");
        fileManager.loadFile("discord/panel/profile-embed.yml");
        fileManager.loadFile("discord/modal/chat-modal.yml");
        fileManager.loadFile("discord/modal/pay-modal.yml");

        configValues.load(configManager);

        if (getServer().getPluginManager().getPlugin("PlayerPoints") != null) {
            economies.put(ShopEconomy.Type.PLAYER_POINTS, new PlayerPointsEconomy());
            getLogger().info("PlayerPoints detected.");
        }
        
        if (getServer().getPluginManager().getPlugin("CoinsEngine") != null) {
            economies.put(ShopEconomy.Type.COINSENGINE, new CoinsEngineEconomy(this));
            getLogger().info("CoinsEngine detected.");
        }

        discordBotManager = new DiscordBotManager(this);
        linkCodeManager = new LinkCodeManager(this);

        discordService = new DiscordService(this);
        
        new ColorUtil(this);
        new MessageUtil(this);
        new TeleportUtil(this);
        new WorldUtil(this);
        new EmbedUtil(this);
            
        new TeaCoreExpansion(this).register();
        
        getServer().getPluginManager().registerEvents(new PlayerDataLoader(this), this);
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathLightningListener(this), this);
        
        registerFeatures();
        
        registerInventoryFramework();
        
        getCommand("ping").setExecutor(new PingCommand(this));
        getCommand("restart").setExecutor(new RestartCommand(this));
        getCommand("teacore").setExecutor(new ReloadCommand(this));
    }
    
    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();

        if (linkCodeManager != null) {
            linkCodeManager.close();
        }

        if (discordBotManager != null) {
            discordBotManager.close();
        }
        
        if (databaseManager != null) {
            databaseManager.close();
        }
        
        if (foliaLib != null) {
            getScheduler().cancelAllTasks();
        }
    }
    
    private void registerFeatures() {
        String pluginName = getName().toLowerCase();
        
        String[] features = {
            "nightvision",
            "spawn",
            "afk",
            "discord",
            "store",
            "chat",
            "economy",
            "phantom",
            "mobspawn",
            "warp",
            "hide",
            "stats",
            "jumpandfly",
            "shard",
            "enderchest"

        };
        
        for (String feature : features) {
            if (!getConfig().getBoolean("features." + feature, true)) continue;
            
            switch (feature) {
                case "discord":
                    getCommandMap().register(pluginName, new DiscordCommand());
                    getCommandMap().register(pluginName, new LinkCommand(this));
                    getCommandMap().register(pluginName, new UnlinkCommand(this));
                    getServer().getPluginManager().registerEvents(new DiscordListener(this), this);

                    discordBotManager.init();
                    break;
                case "store":
                    getCommandMap().register(pluginName, new StoreCommand());
                    break;
                case "nightvision":
                    getCommandMap().register(pluginName, new NightVisionCommand(this));
                    break;
                case "hide":
                    getServer().getPluginManager().registerEvents(new HideListener(this), this);
                    getCommandMap().register(pluginName, new HideCommand(this));
                    break;
                case "economy":
                    getCommandMap().register(pluginName, new BalanceCommand(this));
                    getCommandMap().register(pluginName, new PayCommand(this));
                    getCommandMap().register(pluginName, new PayToggleCommand(this));
                    break;
                case "phantom":
                    getServer().getPluginManager().registerEvents(new PhantomListener(this), this);
                    getCommandMap().register(pluginName, new PhantomCommand(this));
                    break;
                case "mobspawn":
                    getServer().getPluginManager().registerEvents(new MobSpawnListener(this), this);
                    getCommandMap().register(pluginName, new MobSpawnCommand(this));
                    break;
                case "jumpandfly":
                    getServer().getPluginManager().registerEvents(new JumpAndFlyListener(this), this);
                    break;
                case "chat":
                    getServer().getPluginManager().registerEvents(new ToggleChatListener(this), this);
                    getCommandMap().register(pluginName, new ToggleChatCommand(this));
                    getCommandMap().register(pluginName, new MessageToggleCommand(this));
                    getCommand("msg").setExecutor(new MessageCommand(this));
                    getCommand("reply").setExecutor(new ReplyCommand(this));
                    break;
                case "afk":
                    getServer().getPluginManager().registerEvents(new AfkListener(this), this);
                    getCommandMap().register(pluginName, new SetAfkCommand(this));
                    getCommandMap().register(pluginName, new DelAfkCommand(this));
                    getCommandMap().register(pluginName, new AfkCommand(this));
                    getCommandMap().register(pluginName, new AfkshardCommand(this));
                    getCommandMap().register(pluginName, new KillshardCommand(this));
                    break;    
                case "shard":
                    getCommandMap().register(pluginName, new ShardCommand(this));
                    break;    
                case "spawn":
                    getServer().getPluginManager().registerEvents(new SpawnListener(this), this);
                    getCommandMap().register(pluginName, new SetSpawnCommand(this));
                    getCommandMap().register(pluginName, new DelSpawnCommand(this));
                    getCommandMap().register(pluginName, new SpawnCommand(this));
                    break;
                case "warp":
                    getCommandMap().register(pluginName, new SetWarpCommand(this));
                    getCommandMap().register(pluginName, new DelWarpCommand(this));
                    getCommandMap().register(pluginName, new WarpCommand(this));
                    break;
                case "enderchest":
                    getCommand("enderchest").setExecutor(new EnderchestCommand(this));
                    break;
            }
            getLogger().info(capitalize(feature) + " feature is enabled!");
        }
    }
    
    private void registerInventoryFramework() {
        viewFrame = ViewFrame.create(this)
            .with(
                new ListSpawnView(this),
                new ListAfkView(this)
            )
            .disableMetrics()
            .register();
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    private CommandMap getCommandMap() {
        CommandMap commandMap = null;
        
        try {
            if (getServer() != null) {
                Field bukkitCommandMap = getServer().getClass().getDeclaredField("commandMap");
                bukkitCommandMap.setAccessible(true);
                commandMap = (CommandMap)bukkitCommandMap.get(getServer());
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        
        return commandMap;
    }
    
    public static PlatformScheduler getScheduler() {
        return foliaLib.getScheduler();
    }
    
    public FileManager getFileManager() {
        return fileManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public DiscordBotManager getDiscordBotManager() {
        return discordBotManager;
    }

    public LinkCodeManager getLinkCodeManager() {
        return linkCodeManager;
    }
    
    public ViewFrame getViewFrame() {
        return viewFrame;
    }

    public ConfigValues config() {
        return configValues;
    }
    
    public ConcurrentHashMap<ShopEconomy.Type, ShopEconomy> getEconomies() {
        return economies;
    }

    public Map<Player, Player> getLastMessaged() {
        return lastMessaged;
    }

    public EconomyService getEconomyService() {
        return economyService;
    }

    public DiscordService getDiscordService() {
        return discordService;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }
}
