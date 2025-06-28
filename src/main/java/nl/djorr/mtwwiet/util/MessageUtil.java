package nl.djorr.mtwwiet.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Utility class voor het ophalen en formatteren van berichten.
 * Laadt messages.yml direct zonder afhankelijkheid van PluginContext.
 */
public class MessageUtil {
    
    private static FileConfiguration messagesConfig = null;
    private static File messagesFile = null;
    
    /**
     * Initialiseer MessageUtil met de plugin instance.
     * 
     * @param plugin De plugin instance
     */
    public static void init(Plugin plugin) {
        try {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
            if (!messagesFile.exists()) {
                plugin.saveResource("messages.yml", false);
                plugin.getLogger().info("MessageUtil: Created messages.yml from resources");
            }
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            
            // Verify that messages are loaded
            String testMessage = messagesConfig.getString("planting.success");
            if (testMessage == null) {
                plugin.getLogger().severe("MessageUtil: Failed to load messages from messages.yml!");
            } else {
                plugin.getLogger().info("MessageUtil initialized successfully with " + messagesConfig.getKeys(true).size() + " messages");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize MessageUtil: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Haal een bericht op uit de configuratie.
     * 
     * @param path Het config pad
     * @return Het bericht, of het pad als fallback
     */
    public static String getMessage(String path) {
        return getMessage(path, path);
    }
    
    /**
     * Haal een bericht op uit de configuratie met fallback.
     * 
     * @param path Het config pad
     * @param fallback De fallback waarde als niet gevonden
     * @return Het bericht
     */
    public static String getMessage(String path, String fallback) {
        try {
            if (messagesConfig == null) {
                // Try to initialize if not done yet
                Plugin plugin = getPlugin();
                if (plugin != null) {
                    init(plugin);
                }
                
                if (messagesConfig == null) {
                    plugin.getLogger().warning("MessageUtil: messagesConfig is null for path '" + path + "'");
                    return formatColors(fallback);
                }
            }
            
            String message = messagesConfig.getString(path, fallback);
            if (message == null || message.equals(path)) {
                // Log that the message was not found
                Plugin plugin = getPlugin();
                if (plugin != null) {
                    plugin.getLogger().warning("MessageUtil: Message not found for path '" + path + "', using fallback: '" + fallback + "'");
                }
                return formatColors(fallback);
            }
            
            return formatColors(message);
        } catch (Exception e) {
            // Log error but don't crash
            try {
                Plugin plugin = getPlugin();
                if (plugin != null) {
                    plugin.getLogger().severe("MessageUtil error for path '" + path + "': " + e.getMessage());
                }
            } catch (Exception ignored) {}
            return formatColors(fallback);
        }
    }
    
    /**
     * Formatteer kleurcodes in een bericht.
     * Converteert & kleurcodes naar § kleurcodes.
     * 
     * @param message Het bericht om te formatteren
     * @return Het geformatteerde bericht
     */
    private static String formatColors(String message) {
        if (message == null) return null;
        return message.replace('&', '§');
    }
    
    /**
     * Haal een bericht op en vervang placeholders.
     * 
     * @param path Het config pad
     * @param replacements Map van placeholder -> vervanging
     * @return Het geformatteerde bericht
     */
    public static String getMessage(String path, Map<String, String> replacements) {
        String message = getMessage(path);
        message = replacePlaceholders(message, replacements);
        return formatColors(message);
    }
    
    /**
     * Haal een bericht op en vervang enkele placeholders.
     * 
     * @param path Het config pad
     * @param key De placeholder key
     * @param value De vervangende waarde
     * @return Het geformatteerde bericht
     */
    public static String getMessage(String path, String key, String value) {
        String message = getMessage(path);
        message = message.replace("{" + key + "}", value);
        return formatColors(message);
    }
    
    /**
     * Haal een bericht op en vervang meerdere placeholders.
     * 
     * @param path Het config pad
     * @param key1 De eerste placeholder key
     * @param value1 De eerste vervangende waarde
     * @param key2 De tweede placeholder key
     * @param value2 De tweede vervangende waarde
     * @return Het geformatteerde bericht
     */
    public static String getMessage(String path, String key1, String value1, String key2, String value2) {
        String message = getMessage(path);
        message = message.replace("{" + key1 + "}", value1);
        message = message.replace("{" + key2 + "}", value2);
        return formatColors(message);
    }
    
    /**
     * Haal een bericht op en vervang drie placeholders.
     * 
     * @param path Het config pad
     * @param key1 De eerste placeholder key
     * @param value1 De eerste vervangende waarde
     * @param key2 De tweede placeholder key
     * @param value2 De tweede vervangende waarde
     * @param key3 De derde placeholder key
     * @param value3 De derde vervangende waarde
     * @return Het geformatteerde bericht
     */
    public static String getMessage(String path, String key1, String value1, String key2, String value2, String key3, String value3) {
        String message = getMessage(path);
        message = message.replace("{" + key1 + "}", value1);
        message = message.replace("{" + key2 + "}", value2);
        message = message.replace("{" + key3 + "}", value3);
        return formatColors(message);
    }
    
    /**
     * Haal een bericht op en vervang vier placeholders.
     * 
     * @param path Het config pad
     * @param key1 De eerste placeholder key
     * @param value1 De eerste vervangende waarde
     * @param key2 De tweede placeholder key
     * @param value2 De tweede vervangende waarde
     * @param key3 De derde placeholder key
     * @param value3 De derde vervangende waarde
     * @param key4 De vierde placeholder key
     * @param value4 De vierde vervangende waarde
     * @return Het geformatteerde bericht
     */
    public static String getMessage(String path, String key1, String value1, String key2, String value2, String key3, String value3, String key4, String value4) {
        String message = getMessage(path);
        message = message.replace("{" + key1 + "}", value1);
        message = message.replace("{" + key2 + "}", value2);
        message = message.replace("{" + key3 + "}", value3);
        message = message.replace("{" + key4 + "}", value4);
        return formatColors(message);
    }
    
    /**
     * Vervang placeholders in een bericht.
     * 
     * @param message Het originele bericht
     * @param replacements Map van placeholder -> vervanging
     * @return Het bericht met vervangen placeholders
     */
    private static String replacePlaceholders(String message, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return formatColors(message);
    }
    
    /**
     * Haal de plugin instance op.
     * 
     * @return De plugin instance, of null als niet beschikbaar
     */
    private static Plugin getPlugin() {
        try {
            // Probeer de plugin te vinden via Bukkit
            return org.bukkit.Bukkit.getPluginManager().getPlugin("MTWWiet");
        } catch (Exception e) {
            return null;
        }
    }
} 