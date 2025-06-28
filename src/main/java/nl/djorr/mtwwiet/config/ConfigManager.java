package nl.djorr.mtwwiet.config;

import nl.djorr.mtwwiet.core.PluginModule;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Manager voor alle plugin configuratie bestanden.
 * Laadt en beheert config.yml en messages.yml.
 */
public class ConfigManager implements PluginModule {
    
    private Plugin plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private File configFile;
    private File messagesFile;
    
    @Override
    public void init(Plugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }
    
    @Override
    public void shutdown(Plugin plugin) {
        // Configs worden automatisch opgeslagen door Bukkit
    }
    
    @Override
    public String getName() {
        return "ConfigManager";
    }
    
    /**
     * Laad alle configuratie bestanden.
     */
    private void loadConfigs() {
        // Laad hoofdconfig
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        // Laad messages config
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        
        // Update messages van JAR als nodig
        updateMessagesFromJar();
    }
    
    /**
     * Update messages.yml van de JAR als er een nieuwere versie is.
     */
    private void updateMessagesFromJar() {
        try {
            InputStream jarMessages = plugin.getResource("messages.yml");
            if (jarMessages != null) {
                YamlConfiguration jarConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(jarMessages, StandardCharsets.UTF_8)
                );
                
                boolean updated = false;
                for (String key : jarConfig.getKeys(true)) {
                    if (!messages.contains(key)) {
                        messages.set(key, jarConfig.get(key));
                        updated = true;
                    }
                }
                
                if (updated) {
                    messages.save(messagesFile);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to update messages.yml from JAR: " + e.getMessage());
        }
    }
    
    /**
     * Herlaad alle configuraties.
     */
    public void reloadConfigs() {
        loadConfigs();
    }
    
    /**
     * Haal de hoofdconfig op.
     * 
     * @return De config.yml FileConfiguration
     */
    public FileConfiguration getConfig() {
        return config;
    }
    
    /**
     * Haal de messages config op.
     * 
     * @return De messages.yml FileConfiguration
     */
    public FileConfiguration getMessages() {
        return messages;
    }
    
    /**
     * Haal een string op uit de messages config.
     * 
     * @param path Het config pad
     * @param defaultValue De standaardwaarde als niet gevonden
     * @return De configuratie waarde
     */
    public String getMessage(String path, String defaultValue) {
        return messages.getString(path, defaultValue);
    }
    
    /**
     * Haal een string op uit de messages config.
     * 
     * @param path Het config pad
     * @return De configuratie waarde, of null als niet gevonden
     */
    public String getMessage(String path) {
        return messages.getString(path);
    }
    
    /**
     * Haal een integer op uit de hoofdconfig.
     * 
     * @param path Het config pad
     * @param defaultValue De standaardwaarde als niet gevonden
     * @return De configuratie waarde
     */
    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }
    
    /**
     * Haal een double op uit de hoofdconfig.
     * 
     * @param path Het config pad
     * @param defaultValue De standaardwaarde als niet gevonden
     * @return De configuratie waarde
     */
    public double getDouble(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }
    
    /**
     * Haal een boolean op uit de hoofdconfig.
     * 
     * @param path Het config pad
     * @param defaultValue De standaardwaarde als niet gevonden
     * @return De configuratie waarde
     */
    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }
} 