package me.tuanvo0022.managers;

import me.tuanvo0022.Main;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    private final Main plugin;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
    }

    // === String ===
    public String getString(String file, String path) {
        return getString(file, path, null);
    }

    public String getString(String file, String path, String defaultValue) {
        return getConfig(file).getString(path, defaultValue);
    }

    // === String List ===
    public List<String> getStringList(String file, String path) {
        return getStringList(file, path, null);
    }

    public List<String> getStringList(String file, String path, List<String> defaultValue) {
        List<String> result = getConfig(file).getStringList(path);
        return (result != null && !result.isEmpty()) ? result : defaultValue;
    }

    // === Integer ===
    public int getInt(String file, String path) {
        return getInt(file, path, 0);
    }

    public int getInt(String file, String path, int defaultValue) {
        return getConfig(file).getInt(path, defaultValue);
    }

    // === Long ===
    public long getLong(String file, String path) {
        return getLong(file, path, 0L);
    }

    public long getLong(String file, String path, long defaultValue) {
        return getConfig(file).getLong(path, defaultValue);
    }

    // === Double ===
    public double getDouble(String file, String path) {
        return getDouble(file, path, 0.0);
    }

    public double getDouble(String file, String path, double defaultValue) {
        return getConfig(file).getDouble(path, defaultValue);
    }

    // === Boolean ===
    public boolean getBoolean(String file, String path) {
        return getBoolean(file, path, false);
    }

    public boolean getBoolean(String file, String path, boolean defaultValue) {
        return getConfig(file).getBoolean(path, defaultValue);
    }

    // === Integer List ===
    public List<Integer> getIntegerList(String file, String path) {
        return getConfig(file).getIntegerList(path);
    }

    // === Double List ===
    public List<Double> getDoubleList(String file, String path) {
        return getConfig(file).getDoubleList(path);
    }

    // === String Map ===
    public Map<String, String> getStringMap(String file, String path) {
        ConfigurationSection sec = getConfigurationSection(file, path);
        if (sec == null) return new HashMap<>();

        Map<String, String> map = new HashMap<>();
        for (String key : sec.getKeys(false)) {
            map.put(key, sec.getString(key));
        }
        return map;
    }


    // === Section ===
    public ConfigurationSection getConfigurationSection(String file, String path) {
        return getConfig(file).getConfigurationSection(path);
    }

    public ConfigurationSection getConfigurationSection(String file) {
        return getConfig(file).getRoot();
    }
    
    // === Section (alias) ===
    public ConfigurationSection getSection(String file, String path) {
        return getConfigurationSection(file, path);
    }

    public ConfigurationSection getSection(String file) {
        return getConfigurationSection(file);
    }

    // === Core ===
    public FileConfiguration getConfig(String file) {
        return plugin.getFileManager().getConfig(file);
    }
}