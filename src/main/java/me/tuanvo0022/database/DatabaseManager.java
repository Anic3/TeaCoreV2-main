package me.tuanvo0022.database;

import me.tuanvo0022.Main;
import me.tuanvo0022.discord.link.LinkedAccount;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Objects;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseManager {

    /* ======================
    CORE
    ====================== */
    private final Main plugin;
    private final int threads;
    private final ExecutorService dbExecutor;
    private HikariDataSource dataSource;

    /* ======================
       TOGGLE ENUM
       ====================== */
    public enum ToggleType {
        PAY("pay_toggle", true),
        MESSAGE("message_toggle", true),
        CHAT("chat_toggle", true),
        HIDE("hide_toggle", false),
        PHANTOM("phantom_toggle", false),
        MOBSPAWN("mobspawn_toggle", false);

        public final String table;
        public final boolean defaultValue;

        ToggleType(String table, boolean defaultValue) {
            this.table = table;
            this.defaultValue = defaultValue;
        }
    }

    public enum LocationType {
        SPAWN("spawn_location", true),
        AFK("afk_location", true),
        WARP("warp_location", false);

        public final String table;
        public final boolean hasWorld;

        LocationType(String table, boolean hasWorld) {
            this.table = table;
            this.hasWorld = hasWorld;
        }
    }

    /* ======================
       GENERIC TOGGLE CACHE
       ====================== */
    private final Map<ToggleType, Map<UUID, Boolean>> toggleCaches = new ConcurrentHashMap<>();

    /* ======================
       LOCATION CACHES
       ====================== */
    public final Map<String, Location> spawnCache = new ConcurrentHashMap<>();
    public final Map<String, Location> afkCache   = new ConcurrentHashMap<>();
    public final Map<String, Location> warpCache  = new ConcurrentHashMap<>();

    /* ======================
       DISCORD LINK CACHE
       ====================== */
    private final Map<UUID, LinkedAccount> accountCache = new ConcurrentHashMap<>();
    private final Map<Long, UUID> discordIndex = new ConcurrentHashMap<>();


    /* ======================
    CONSTRUCTOR
    ====================== */
    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
        this.threads = plugin.getConfig().getInt("database.async-threads", 2);
        this.dbExecutor = Executors.newFixedThreadPool(threads);

        for (ToggleType type : ToggleType.values()) {
            toggleCaches.put(type, new ConcurrentHashMap<>());
        }

        setupDatabase();
    }

    /* ======================
       DATABASE SETUP
       ====================== */
    private void setupDatabase() {
        String type = plugin.getConfig().getString("database.type", "sqlite").toLowerCase();
        HikariConfig config = new HikariConfig();

        if (type.equals("mysql")) {
            setupMySQL(config);
        } else {
            setupSQLite(config);
        }

        this.dataSource = new HikariDataSource(config);
        createTables();
    }

    private void setupSQLite(HikariConfig config) {
        String databaseUrl = plugin.getDataFolder().getAbsolutePath() + "/database.db";

        config.setJdbcUrl("jdbc:sqlite:" + databaseUrl);
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);
    }

    private void setupMySQL(HikariConfig config) {
        ConfigurationSection mysql = plugin.getConfig().getConfigurationSection("database.mysql");

        String host = mysql.getString("host");
        int port = mysql.getInt("port");
        String database = mysql.getString("database");
        String username = mysql.getString("username");
        String password = mysql.getString("password");

        // Build JDBC URL
        StringBuilder jdbc = new StringBuilder("jdbc:mysql://")
                .append(host).append(":").append(port).append("/")
                .append(database).append("?");

        // Add properties from config
        ConfigurationSection props = mysql.getConfigurationSection("properties");
        if (props != null) {
            for (String key : props.getKeys(false)) {
                Object val = props.get(key);
                jdbc.append(key).append("=").append(val).append("&");
            }
        }

        config.setJdbcUrl(jdbc.toString());
        config.setUsername(username);
        config.setPassword(password);

        // Pool settings
        config.setMaximumPoolSize(mysql.getInt("pool-size", 10));
        config.setMinimumIdle(mysql.getInt("minimum-idle", 2));
        config.setMaxLifetime(mysql.getLong("maximum-lifetime", 1800000));
        config.setConnectionTimeout(mysql.getLong("connection-timeout", 30000));
        config.setIdleTimeout(mysql.getLong("idle-timeout", 600000));
    }

    private void createTables() {
        try (Connection conn = getConnection()) {
            try (PreparedStatement stmtMessage = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS message_toggle (" +
                            "playerUUID TEXT PRIMARY KEY NOT NULL," +
                            "enabled BOOLEAN NOT NULL)")) {
                stmtMessage.executeUpdate();
            }

            try (PreparedStatement stmtPay = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS pay_toggle (" +
                            "playerUUID TEXT PRIMARY KEY NOT NULL," +
                            "enabled BOOLEAN NOT NULL)")) {
                stmtPay.executeUpdate();
            }

            try (PreparedStatement stmtPhantom = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS phantom_toggle (" +
                            "playerUUID TEXT PRIMARY KEY NOT NULL," +
                            "enabled BOOLEAN NOT NULL)")) {
                stmtPhantom.executeUpdate();
            }

            try (PreparedStatement stmtHide = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS hide_toggle (" +
                            "playerUUID TEXT PRIMARY KEY NOT NULL," +
                            "enabled BOOLEAN NOT NULL)")) {
                stmtHide.executeUpdate();
            }
            
            try (PreparedStatement stmtMobSpawn = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS mobspawn_toggle (" +
                            "playerUUID TEXT PRIMARY KEY NOT NULL," +
                            "enabled BOOLEAN NOT NULL)")) {
                stmtMobSpawn.executeUpdate();
            }

            try (PreparedStatement stmtChat = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS chat_toggle (" +
                            "playerUUID TEXT PRIMARY KEY NOT NULL," +
                            "enabled BOOLEAN NOT NULL)")) {
                stmtChat.executeUpdate();
            }
            
            try (PreparedStatement stmtPending = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS pending_payments (" +
                            "playerUUID TEXT NOT NULL," +
                            "amount DOUBLE NOT NULL," +
                            "senderName TEXT NOT NULL)")) {
                stmtPending.executeUpdate();
            }
            
            try (PreparedStatement stmtSpawn = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS spawn_location (" +
                            "name TEXT PRIMARY KEY NOT NULL," +
                            "world TEXT NOT NULL," +
                            "x DOUBLE NOT NULL," +
                            "y DOUBLE NOT NULL," +
                            "z DOUBLE NOT NULL," +
                            "yaw FLOAT NOT NULL," +
                            "pitch FLOAT NOT NULL)")) {
                stmtSpawn.executeUpdate();
            }
            
            try (PreparedStatement stmtAfk = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS afk_location (" +
                            "name TEXT PRIMARY KEY NOT NULL," +
                            "world TEXT NOT NULL," +
                            "x DOUBLE NOT NULL," +
                            "y DOUBLE NOT NULL," +
                            "z DOUBLE NOT NULL," +
                            "yaw FLOAT NOT NULL," +
                            "pitch FLOAT NOT NULL)")) {
                stmtAfk.executeUpdate();
            }
            
            try (PreparedStatement stmtWarp = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS warp_location (" +
                            "name TEXT PRIMARY KEY NOT NULL," +
                            "x DOUBLE NOT NULL," +
                            "y DOUBLE NOT NULL," +
                            "z DOUBLE NOT NULL," +
                            "yaw FLOAT NOT NULL," +
                            "pitch FLOAT NOT NULL)")) {
                stmtWarp.executeUpdate();
            }

            try (PreparedStatement stmtLinked = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS linked_accounts (" +
                             "playerUUID TEXT PRIMARY KEY NOT NULL," +
                             "discordId INTEGER NOT NULL UNIQUE," +
                             "locked BOOLEAN NOT NULL," +
                             "linked_at BIGINT NOT NULL)")) {
                stmtLinked.executeUpdate();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ======================
       CONNECTION
       ====================== */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        dbExecutor.shutdownNow();

        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }

        toggleCaches.values().forEach(Map::clear);
        spawnCache.clear();
        afkCache.clear();
        warpCache.clear();
        accountCache.clear();
        discordIndex.clear();
    }

    /* ======================
       TOGGLE LOAD API
       ====================== */
    public void loadAllToggles(UUID playerId) {
        getChatToggleAsync(playerId);      // cache chat
        getHideToggleAsync(playerId);      // cache hide
        getPayToggleAsync(playerId);       // cache pay
        getPhantomToggleAsync(playerId);   // cache phantom
        getMobSpawnToggleAsync(playerId);   // cache phantom
        getMsgToggleAsync(playerId);       // cache message
        getAccountAsync(playerId);       // cache account
    }

    /* ======================
       GENERIC TOGGLE API
       ====================== */
    public CompletableFuture<Boolean> getToggleAsync(UUID playerId, ToggleType type) {
        Map<UUID, Boolean> cache = toggleCaches.get(type);

        if (cache.containsKey(playerId)) {
            return CompletableFuture.completedFuture(cache.get(playerId));
        }

        return CompletableFuture.supplyAsync(() -> {
            boolean enabled = type.defaultValue;

            try (Connection conn = getConnection();
                 PreparedStatement statement = conn.prepareStatement(
                         "SELECT enabled FROM " + type.table + " WHERE playerUUID = ?")) {

                statement.setString(1, playerId.toString());

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        enabled = rs.getBoolean("enabled");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            cache.put(playerId, enabled);
            return enabled;
        }, dbExecutor);
    }

    public CompletableFuture<Void> setToggleAsync(UUID playerId, ToggleType type, boolean enabled) {
        toggleCaches.get(type).put(playerId, enabled);

        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement statement = conn.prepareStatement(
                         "INSERT OR REPLACE INTO " + type.table + "(playerUUID, enabled) VALUES(?, ?)")) {

                statement.setString(1, playerId.toString());
                statement.setBoolean(2, enabled);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, dbExecutor);
    }

    // Chat toggle
    public CompletableFuture<Boolean> getChatToggleAsync(UUID id) {
        return getToggleAsync(id, ToggleType.CHAT);
    }

    public CompletableFuture<Void> setChatToggleAsync(UUID id, boolean v) {
        return setToggleAsync(id, ToggleType.CHAT, v);
    }

    // Hide toggle
    public CompletableFuture<Boolean> getHideToggleAsync(UUID id) {
        return getToggleAsync(id, ToggleType.HIDE);
    }

    public CompletableFuture<Void> setHideToggleAsync(UUID id, boolean v) {
        return setToggleAsync(id, ToggleType.HIDE, v);
    }

    // Pay toggle
    public CompletableFuture<Boolean> getPayToggleAsync(UUID id) {
        return getToggleAsync(id, ToggleType.PAY);
    }

    public CompletableFuture<Void> setPayToggleAsync(UUID id, boolean v) {
        return setToggleAsync(id, ToggleType.PAY, v);
    }

    // Msg toggle
    public CompletableFuture<Boolean> getMsgToggleAsync(UUID id) {
        return getToggleAsync(id, ToggleType.MESSAGE);
    }

    public CompletableFuture<Void> setMsgToggleAsync(UUID id, boolean v) {
        return setToggleAsync(id, ToggleType.MESSAGE, v);
    }

    // Phantom toggle
    public CompletableFuture<Boolean> getPhantomToggleAsync(UUID id) {
        return getToggleAsync(id, ToggleType.PHANTOM);
    }

    public CompletableFuture<Void> setPhantomToggleAsync(UUID id, boolean v) {
        return setToggleAsync(id, ToggleType.PHANTOM, v);
    }

    // Mobspawn toggle
    public CompletableFuture<Boolean> getMobSpawnToggleAsync(UUID id) {
        return getToggleAsync(id, ToggleType.MOBSPAWN);
    }

    public CompletableFuture<Void> setMobSpawnToggleAsync(UUID id, boolean v) {
        return setToggleAsync(id, ToggleType.MOBSPAWN, v);
    }
    
    public Boolean getCachedToggle(UUID playerId, ToggleType type) {
        return toggleCaches.get(type).get(playerId);
    }

    public Map<String, Location> getLocationCache(LocationType type) {
        return switch (type) {
            case SPAWN -> spawnCache;
            case AFK   -> afkCache;
            case WARP  -> warpCache;
        };
    }

    public CompletableFuture<Void> saveLocation(String name, Location loc, LocationType type) {
        getLocationCache(type).put(name, loc);

        return CompletableFuture.runAsync(() -> {
            String sql = type.hasWorld
                    ? "INSERT OR REPLACE INTO " + type.table +
                    "(name, world, x, y, z, yaw, pitch) VALUES(?, ?, ?, ?, ?, ?, ?)"
                    : "INSERT OR REPLACE INTO " + type.table +
                    "(name, x, y, z, yaw, pitch) VALUES(?, ?, ?, ?, ?, ?)";

            try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, name);

                int i = 2;
                if (type.hasWorld) {
                    stmt.setString(i++, loc.getWorld().getName());
                }

                stmt.setDouble(i++, loc.getX());
                stmt.setDouble(i++, loc.getY());
                stmt.setDouble(i++, loc.getZ());
                stmt.setFloat(i++, loc.getYaw());
                stmt.setFloat(i,   loc.getPitch());

                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, dbExecutor);
    }

    public CompletableFuture<Location> getLocation(String name, LocationType type) {
        Map<String, Location> cache = getLocationCache(type);
        if (cache.containsKey(name)) {
            return CompletableFuture.completedFuture(cache.get(name));
        }

        return CompletableFuture.supplyAsync(() -> {
            Location loc = null;

            String sql = type.hasWorld
                    ? "SELECT world, x, y, z, yaw, pitch FROM " + type.table + " WHERE name = ?"
                    : "SELECT x, y, z, yaw, pitch FROM " + type.table + " WHERE name = ?";

            try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, name);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        World world = null;
                        if (type.hasWorld) {
                            world = plugin.getServer().getWorld(rs.getString("world"));
                            if (world == null) return null;
                        }

                        loc = new Location(
                                world,
                                rs.getDouble("x"),
                                rs.getDouble("y"),
                                rs.getDouble("z"),
                                rs.getFloat("yaw"),
                                rs.getFloat("pitch")
                        );

                        cache.put(name, loc);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return loc;
        }, dbExecutor);
    }

    public CompletableFuture<Void> deleteLocation(String name, LocationType type) {
        getLocationCache(type).remove(name);

        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM " + type.table + " WHERE name = ?")) {
                stmt.setString(1, name);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, dbExecutor);
    }

    public CompletableFuture<Void> loadAllLocations(LocationType type) {
        return CompletableFuture.runAsync(() -> {
            Map<String, Location> cache = getLocationCache(type);
            cache.clear();

            String sql = type.hasWorld
                    ? "SELECT name, world, x, y, z, yaw, pitch FROM " + type.table
                    : "SELECT name, x, y, z, yaw, pitch FROM " + type.table;

            try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    World world = null;
                    if (type.hasWorld) {
                        world = plugin.getServer().getWorld(rs.getString("world"));
                        if (world == null) continue;
                    }

                    Location loc = new Location(
                            world,
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("z"),
                            rs.getFloat("yaw"),
                            rs.getFloat("pitch")
                    );

                    cache.put(rs.getString("name"), loc);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, dbExecutor);
    }

    // Spawn
    public CompletableFuture<Void> setSpawnLocation(String n, Location l) {
        return saveLocation(n, l, LocationType.SPAWN);
    }
    public CompletableFuture<Location> getSpawnLocation(String n) {
        return getLocation(n, LocationType.SPAWN);
    }
    public CompletableFuture<Void> deleteSpawnLocation(String n) {
        return deleteLocation(n, LocationType.SPAWN);
    }
    public CompletableFuture<Void> loadAllSpawnLocations() {
        return loadAllLocations(LocationType.SPAWN);
    }

    // AFK
    public CompletableFuture<Void> setAfkLocation(String n, Location l) {
        return saveLocation(n, l, LocationType.AFK);
    }
    public CompletableFuture<Location> getAfkLocation(String n) {
        return getLocation(n, LocationType.AFK);
    }
    public CompletableFuture<Void> deleteAfkLocation(String n) {
        return deleteLocation(n, LocationType.AFK);
    }
    public CompletableFuture<Void> loadAllAfkLocations() {
        return loadAllLocations(LocationType.AFK);
    }

    // Warp
    public CompletableFuture<Void> setWarpLocation(String n, Location l) {
        return saveLocation(n, l, LocationType.WARP);
    }
    public CompletableFuture<Location> getWarpLocation(String n) {
        return getLocation(n, LocationType.WARP);
    }
    public CompletableFuture<Void> deleteWarpLocation(String n) {
        return deleteLocation(n, LocationType.WARP);
    }
    public CompletableFuture<Void> loadAllWarpLocations() {
        return loadAllLocations(LocationType.WARP);
    }
    
    // Get all spawn location
    public Collection<Location> getAllSpawnLocations() {
        return spawnCache.values();
    }

    // Get all afk location
    public Collection<Location> getAllAfkLocations() {
        return afkCache.values();
    }
    
    // Get all warp location
    public Collection<Location> getAllWarpLocations() {
        return warpCache.values();
    }

    // Get all spawn name
    public Set<String> getAllSpawnNames() {
        return spawnCache.keySet();
    }

    // Get all afk name
    public Set<String> getAllAfkNames() {
        return afkCache.keySet();
    }
    
    // Get all warp name
    public Set<String> getAllWarpNames() {
        return warpCache.keySet();
    }
    
    /* ======================
       PLAYER UTIL
       ====================== */
    public List<Player> getEnabledPlayers(ToggleType type) {
        return toggleCaches.get(type).entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(e -> plugin.getServer().getPlayer(e.getKey()))
                .filter(Objects::nonNull)
                .toList();
    }
    
    public CompletableFuture<Void> addPendingPayment(UUID playerUUID, double amount, String senderName) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO pending_payments(playerUUID, amount, senderName) VALUES(?, ?, ?)")) {
                stmt.setString(1, playerUUID.toString());
                stmt.setDouble(2, amount);
                stmt.setString(3, senderName);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, dbExecutor);
    }

    public CompletableFuture<List<Map<String, Object>>> getPendingPayments(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            List<Map<String, Object>> list = new ArrayList<>();
                try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT amount, senderName FROM pending_payments WHERE playerUUID = ?")) {
                stmt.setString(1, playerUUID.toString());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("amount", rs.getDouble("amount"));
                    data.put("sender", rs.getString("senderName"));
                    list.add(data);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return list;
        }, dbExecutor);
    }

    public CompletableFuture<Void> removePendingPayments(UUID playerUUID) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM pending_payments WHERE playerUUID = ?")) {
                stmt.setString(1, playerUUID.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, dbExecutor);
    }

    public CompletableFuture<Void> linkAccountAsync(UUID playerId, long discordId) {
        long now = System.currentTimeMillis();

        LinkedAccount account = new LinkedAccount(playerId, discordId, false, now);

        accountCache.put(playerId, account);
        discordIndex.put(discordId, playerId);

        return CompletableFuture.runAsync(() -> {
            try (Connection c = getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "INSERT OR REPLACE INTO linked_accounts(playerUUID, discordId, locked, linked_at) VALUES(?,?,?,?)")) {
                ps.setString(1, playerId.toString());
                ps.setLong(2, discordId);
                ps.setBoolean(3, false);
                ps.setLong(4, now);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, dbExecutor);
    }

    public CompletableFuture<Boolean> hasLinkedAccountAsync(UUID playerUUID) {
        if (accountCache.containsKey(playerUUID)) {
            return CompletableFuture.completedFuture(true);
        }
        return hasLinkedAccountFromDatabase(playerUUID);
    }

    public CompletableFuture<Boolean> hasLinkedAccountAsync(long discordId) {
        UUID uuid = discordIndex.get(discordId);

        if (uuid != null && accountCache.containsKey(uuid)) {
            return CompletableFuture.completedFuture(true);
        }

        return hasLinkedAccountFromDatabase(discordId);
    }

    private CompletableFuture<Boolean> hasLinkedAccountFromDatabase(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection();
                PreparedStatement statement = conn.prepareStatement(
                        "SELECT 1 FROM linked_accounts WHERE playerUUID=?")) {

                statement.setString(1, playerUUID.toString());
                return statement.executeQuery().next();

            } catch (SQLException e) {
                return false;
            }
        }, dbExecutor);
    }

    private CompletableFuture<Boolean> hasLinkedAccountFromDatabase(long discordId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection();
                PreparedStatement statement = conn.prepareStatement(
                        "SELECT 1 FROM linked_accounts WHERE discordId=?")) {

                statement.setLong(1, discordId);
                return statement.executeQuery().next();

            } catch (SQLException e) {
                return false;
            }
        }, dbExecutor);
    }

    public CompletableFuture<LinkedAccount> getAccountAsync(UUID playerUUID) {
        LinkedAccount cached = accountCache.get(playerUUID);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return CompletableFuture.supplyAsync(() -> loadAccountByUUID(playerUUID), dbExecutor);
    }

    public CompletableFuture<LinkedAccount> getAccountAsync(long discordId) {
        UUID uuid = discordIndex.get(discordId);

        if (uuid != null) {
            LinkedAccount cached = accountCache.get(uuid);
            if (cached != null) {
                return CompletableFuture.completedFuture(cached);
            }
        }
        return CompletableFuture.supplyAsync(() -> loadAccountByDiscord(discordId), dbExecutor);
    }

    private LinkedAccount loadAccountByUUID(UUID uuid) {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT discordId, locked, linked_at FROM linked_accounts WHERE playerUUID=?")) {
            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long discordId = rs.getLong("discordId");
                    boolean locked = rs.getBoolean("locked");
                    long linkedAt = rs.getLong("linked_at");

                    LinkedAccount acc = new LinkedAccount(uuid, discordId, locked, linkedAt);
                    accountCache.put(uuid, acc);
                    discordIndex.put(discordId, uuid);
                    return acc;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private LinkedAccount loadAccountByDiscord(long discordId) {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT playerUUID, locked, linked_at FROM linked_accounts WHERE discordId=?")) {
            ps.setLong(1, discordId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("playerUUID"));
                    boolean locked = rs.getBoolean("locked");
                    long linkedAt = rs.getLong("linked_at");

                    LinkedAccount acc = new LinkedAccount(uuid, discordId, locked, linkedAt);
                    accountCache.put(uuid, acc);
                    discordIndex.put(discordId, uuid);
                    return acc;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CompletableFuture<Void> unlinkAccountAsync(UUID playerUUID) {
        LinkedAccount acc = accountCache.remove(playerUUID);
        if (acc != null) {
            discordIndex.remove(acc.getDiscordId());
        }
        return deleteLinkFromDatabase("playerUUID", playerUUID.toString());
    }

    public CompletableFuture<Void> unlinkAccountAsync(long discordId) {
        UUID uuid = discordIndex.remove(discordId);
        if (uuid != null) {
            accountCache.remove(uuid);
        }
        return deleteLinkFromDatabase("discordId", String.valueOf(discordId));
    }

    private CompletableFuture<Void> deleteLinkFromDatabase(String column, String value) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                PreparedStatement statement = conn.prepareStatement(
                        "DELETE FROM linked_accounts WHERE " + column + "=?")) {

                statement.setString(1, value);
                statement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, dbExecutor);
    }

    public CompletableFuture<Void> setAccountLockedAsync(UUID playerUUID, boolean locked) {
        LinkedAccount acc = accountCache.get(playerUUID);
        if (acc != null) acc.setLocked(locked);

        return updateLockState("playerUUID", playerUUID.toString(), locked);
    }

    public CompletableFuture<Void> setAccountLockedAsync(long discordId, boolean locked) {
        UUID uuid = discordIndex.get(discordId);
        if (uuid != null) {
            LinkedAccount acc = accountCache.get(uuid);
            if (acc != null) acc.setLocked(locked);
        }
        return updateLockState("discordId", String.valueOf(discordId), locked);
    }

    public CompletableFuture<Boolean> isAccountLockedAsync(UUID playerId) {
        LinkedAccount cached = accountCache.get(playerId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached.isLocked());
        }

        return getAccountAsync(playerId).thenApply(acc -> acc != null && acc.isLocked());
    }

    private CompletableFuture<Void> updateLockState(String column, String value, boolean locked) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                PreparedStatement statement = conn.prepareStatement(
                        "UPDATE linked_accounts SET locked=? WHERE " + column + "=?")) {

                statement.setBoolean(1, locked);
                statement.setString(2, value);
                statement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, dbExecutor);
    }


    /**
     * Retrieves a cached account by uuid.
     * @param uuid the uuid of the account to retrieve
     * @return the cached account if it exists, null otherwise
     */
    public LinkedAccount getCachedAccount(UUID uuid) {
        return accountCache.get(uuid);
    }
}