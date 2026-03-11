package me.tuanvo0022.config;

import me.tuanvo0022.Main;
import me.tuanvo0022.managers.ConfigManager;

import java.util.List;

public final class ConfigValues {
    private final Main plugin;

    public ConfigValues(Main plugin) {
        this.plugin = plugin;
    }

    // ===== SPAWN =====
    public boolean SPAWN_ON_FIRST_JOIN;
    public boolean SPAWN_ON_DEATH;
    public boolean SPAWN_BED_PRIORITY;
    public boolean SPAWN_DISABLE_PORTAL;
    public int SPAWN_MAX_PLAYERS;
    public boolean SPAWN_TELEPORT_TO_JOIN;

    // ===== AFK =====
    public int AFK_MAX_PLAYERS;
    public boolean AFK_TELEPORT_TO_JOIN;

    // ===== PHANTOM =====
    public int PHANTOM_CHECK_RADIUS;

    // ===== MOB SPAWN =====
    public int MOBSPAWN_CHECK_RADIUS;

    // ===== DOUBLE JUMP =====
    public int DOUBLEJUMP_POWER;
    public int DOUBLEJUMP_Y;
    public int DOUBLEJUMP_CHECKDEPTH;
    public long DOUBLEJUMP_COOLDOWN;
    public List<String> DOUBLEJUMP_ALLOWED_WORLDS;
    public List<String> DOUBLEJUMP_ALLOWED_REGIONS;

    // ===== FLY =====
    public List<String> FLY_ALLOWED_WORLDS;
    public List<String> FLY_ALLOWED_REGIONS;

    // ===== SHARD =====
    public double SHARD_AFK_MULTIPLIER;
    public double SHARD_KILL_MULTIPLIER;

    // ===== TELEPORT =====
    public int TELEPORT_DELAY;
    public boolean TELEPORT_CANCEL_ON_MOVE;
    public boolean TELEPORT_AUTO_BALANCE_ENABLED;
    public double TELEPORT_MAX_MOVE_DISTANCE;

    // ===== COMBAT =====
    public boolean LIGHTNING_ON_DEATH;

    // ===== JOIN QUIT =====
    public String JOIN_MESSAGE;
    public String QUIT_MESSAGE;

    // ===== HOOKS =====
    public String HOOKS_CURRENCYNAME;

    private final String spawnKey = "spawn/config";
    private final String afkKey = "afk/config";
    private final String phantomKey = "phantom/config";
    private final String mobSpawnKey = "mobspawn/config";
    private final String doubleJumpKey = "doublejump/config";
    private final String flyKey = "fly/config";
    private final String shardKey = "shard/config";
    private final String teleportKey = "teleport/config";
    private final String combatKey = "combat/config";
    private final String joinQuitKey = "joinquit/config";
    private final String hooksKey = "hooks";

    public void reload() {
        load(plugin.getConfigManager());
    }

    public void load(ConfigManager config) {
        // Reload all config
        plugin.getFileManager().reloadConfig("all");

        // SPAWN
        SPAWN_ON_FIRST_JOIN =
            config.getBoolean(spawnKey, "on-first-join");

        SPAWN_ON_DEATH =
            config.getBoolean(spawnKey, "on-death");

        SPAWN_BED_PRIORITY =
            config.getBoolean(spawnKey, "bed-priority");

        SPAWN_DISABLE_PORTAL =
            config.getBoolean(spawnKey, "disable-portal");

        SPAWN_MAX_PLAYERS =
            config.getInt(spawnKey, "max-players");

        SPAWN_TELEPORT_TO_JOIN =
            config.getBoolean(spawnKey, "teleport-to-join");

        // AFK
        AFK_MAX_PLAYERS =
            config.getInt(afkKey, "max-players");
        AFK_TELEPORT_TO_JOIN =
            config.getBoolean(afkKey, "teleport-to-join");

        // PHANTOM
        PHANTOM_CHECK_RADIUS =
            config.getInt(phantomKey, "check-radius");

        // MOB SPAWN
        MOBSPAWN_CHECK_RADIUS =
            config.getInt(mobSpawnKey, "check-radius");

        // DOUBLE JUMP
        DOUBLEJUMP_POWER =
            config.getInt(doubleJumpKey, "jump-power");

        DOUBLEJUMP_Y =
            config.getInt(doubleJumpKey, "jump-y");

        DOUBLEJUMP_COOLDOWN =
            config.getLong(doubleJumpKey, "cooldown");

        DOUBLEJUMP_CHECKDEPTH =
            config.getInt(doubleJumpKey, "ground-check-depth");

        DOUBLEJUMP_ALLOWED_WORLDS =
            config.getStringList(doubleJumpKey, "allowed-worlds");

        DOUBLEJUMP_ALLOWED_REGIONS =
            config.getStringList(doubleJumpKey, "allowed-regions");

        // FLY
        FLY_ALLOWED_WORLDS =
            config.getStringList(flyKey, "allowed-worlds");

        FLY_ALLOWED_REGIONS =
            config.getStringList(flyKey, "allowed-regions");

        // SHARD
        SHARD_AFK_MULTIPLIER =
            config.getDouble(shardKey, "afk-shard-percent");

        SHARD_KILL_MULTIPLIER =
            config.getDouble(shardKey, "kill-shard-percent");

        // TELEPORT
        TELEPORT_DELAY =
            config.getInt(teleportKey, "teleport-delay");

        TELEPORT_CANCEL_ON_MOVE =
            config.getBoolean(teleportKey, "movement-cancel.enabled");

        TELEPORT_MAX_MOVE_DISTANCE =
            config.getDouble(teleportKey, "movement-cancel.max-distance");

        TELEPORT_AUTO_BALANCE_ENABLED =
            config.getBoolean(teleportKey, "auto-balance-enabled");

        // COMBAT
        LIGHTNING_ON_DEATH =
            config.getBoolean(combatKey, "lightning-on-death.enabled");

        // JOIN QUIT
        JOIN_MESSAGE =
            config.getString(joinQuitKey, "join_message.message");

        QUIT_MESSAGE =
            config.getString(joinQuitKey, "quit_message.message");

        // HOOKS
        HOOKS_CURRENCYNAME =
            config.getString(hooksKey, "coinsengine");
    }
}
