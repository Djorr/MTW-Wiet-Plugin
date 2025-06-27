package nl.djorr.mtwwiet.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class voor het laden en beheren van berichten uit het language bestand
 */
public class MessageUtil {
    
    private static FileConfiguration messagesConfig;
    private static File messagesFile;
    
    /**
     * Laad het messages.yml bestand
     */
    public static void loadMessages(Plugin plugin) {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    /**
     * Herlaad het messages.yml bestand
     */
    public static void reloadMessages(Plugin plugin) {
        loadMessages(plugin);
    }
    
    /**
     * Haal een bericht op uit het language bestand
     */
    public static String getMessage(String path) {
        if (messagesConfig == null) {
            return "§cMessages niet geladen!";
        }
        
        String message = messagesConfig.getString(path, "§cBericht niet gevonden: " + path);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * Haal een bericht op en vervang placeholders
     */
    public static String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);
        
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        
        return message;
    }
    
    /**
     * Haal een bericht op met enkele placeholder
     */
    public static String getMessage(String path, String placeholder, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder, value);
        return getMessage(path, placeholders);
    }
    
    /**
     * Haal een bericht op met meerdere placeholders
     */
    public static String getMessage(String path, String... placeholders) {
        if (placeholders.length % 2 != 0) {
            return getMessage(path);
        }
        
        Map<String, String> placeholderMap = new HashMap<>();
        for (int i = 0; i < placeholders.length; i += 2) {
            placeholderMap.put(placeholders[i], placeholders[i + 1]);
        }
        
        return getMessage(path, placeholderMap);
    }
} 