package me.tuanvo0022.managers;

import me.tuanvo0022.Main;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Manages YAML configuration files for a Bukkit plugin.
 */
public final class FileManager {
    private final Main plugin;
    private final Map<String, File> files = new ConcurrentHashMap<>();
    private final Map<String, FileConfiguration> configs = new ConcurrentHashMap<>();

    /**
     * Constructs a FileManager instance for the specified plugin.
     *
     * @param plugin The JavaPlugin instance, must not be null.
     * @throws IllegalArgumentException if plugin is null.
     */
    public FileManager(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads a YAML configuration file, creating it from resources if it doesn't exist.
     *
     * @param fileName The name of the file (e.g., "config.yml").
     * @throws IllegalArgumentException if fileName is invalid or empty.
     * @throws IllegalStateException if file loading fails.
     */
    public void loadFile(String fileName) {
        validateFileName(fileName);

        String key = normalizeKey(fileName);
        File dataFolder = plugin.getDataFolder();
        
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IllegalStateException("Failed to create plugin data folder");
        }

        File file = new File(dataFolder, fileName);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw new IllegalStateException("Failed to create directory: " + parentDir.getPath());
        }
        if (!file.exists()) {
            try {
                plugin.saveResource(fileName, false);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Resource " + fileName + " not found, creating empty file");
                createEmptyFile(file);
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        files.put(key, file);
        configs.put(key, config);
    }

    /**
     * Retrieves the configuration for the specified key.
     *
     * @param key The configuration key (case-insensitive).
     * @return The FileConfiguration, or null if not found.
     */
    public FileConfiguration getConfig(String key) {
        if (key == null) {
            return null;
        }
        return configs.get(normalizeKey(key));
    }

    /**
     * Saves the configuration associated with the specified key to disk.
     *
     * @param key The configuration key (case-insensitive).
     * @throws IllegalStateException if saving fails.
     */
    public void saveFile(String key) {
        if (key == null) {
            return;
        }

        String normalizedKey = normalizeKey(key);
        File file = files.get(normalizedKey);
        FileConfiguration config = configs.get(normalizedKey);

        if (file == null || config == null) {
            plugin.getLogger().warning("No configuration found for key: " + normalizedKey);
            return;
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save configuration " + normalizedKey + ": " + e.getMessage());
            throw new IllegalStateException("Failed to save configuration: " + normalizedKey, e);
        }
    }
    
    /**
     * Reloads a specific configuration file or all configurations.
     * If name is "all", reloads all loaded configurations and the default plugin configuration.
     * Otherwise, reloads the configuration for the specified file name.
     *
     * @param name The name of the file (e.g., "gui/pay.yml") or "all" to reload all configurations.
     * @throws IllegalArgumentException if name is null, empty, or invalid (for specific file reload).
     */
     public void reloadConfig(String name) {
         if (name == null || name.trim().isEmpty()) {
             plugin.getLogger().warning("Configuration name cannot be null or empty");
             return;
         }
         if (name.equalsIgnoreCase("all")) {
             // Reload all configurations
             for (Map.Entry<String, File> entry : files.entrySet()) {
                 String key = entry.getKey();
                 File file = entry.getValue();
                 
                 if (file == null || !file.exists()) {
                     plugin.getLogger().warning("File not found for key: " + key + ", skipping reload");
                     configs.remove(key);
                     continue;
                     
                 }
                 
                 try {
                     FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                     configs.put(key, config);
                     plugin.getLogger().info("Loaded configuration: " + key);
                     
                 } catch (Exception e) {
                     plugin.getLogger().severe("Failed to reload configuration " + key + ": " + e.getMessage());
                 }
             }
             // Reload default plugin configuration
             plugin.reloadConfig();
             plugin.getLogger().info("Loaded default plugin configuration (config.yml)");
         } else {
             // Reload specific configuration
             try {
                 validateFileName(name);
             } catch (IllegalArgumentException e) {
                 plugin.getLogger().warning("Invalid file name: " + name + ". File name must end with .yml");
                 return;
             }
             String key = normalizeKey(name);
             File file = files.get(key);
             if (file == null || !file.exists()) {
                 plugin.getLogger().warning("Configuration file not found for: " + name);
                 return;
             }
             
             try {
                 FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                 configs.put(key, config);
                 plugin.getLogger().info("Reloaded configuration: " + name);
             } catch (Exception e) {
                 plugin.getLogger().severe("Failed to reload configuration " + name + ": " + e.getMessage());
             }
         }
     }
    
    /**
     * Normalizes a file name or key by removing ".yml" and converting to lowercase.
     *
     * @param fileName The file name or key to normalize.
     * @return The normalized key.
     */
    private String normalizeKey(String fileName) {
        return fileName.replace(".yml", "").toLowerCase();
    }

    /**
     * Validates the file name to ensure it is not null, empty, or invalid.
     *
     * @param fileName The file name to validate.
     * @throws IllegalArgumentException if the file name is invalid.
     */
    private void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        if (!fileName.endsWith(".yml")) {
            throw new IllegalArgumentException("File name must end with .yml");
        }
    }

    /**
     * Creates an empty file if it doesn't exist.
     *
     * @param file The file to create.
     */
    private void createEmptyFile(File file) {
        try {
            if (!file.createNewFile()) {
                plugin.getLogger().warning("Failed to create empty file: " + file.getName());
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Error creating empty file " + file.getName() + ": " + e.getMessage());
            throw new IllegalStateException("Failed to create empty file: " + file.getName(), e);
        }
    }
}