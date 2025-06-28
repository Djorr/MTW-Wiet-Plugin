package nl.djorr.mtwwiet.core;

import org.bukkit.plugin.Plugin;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Centrale context voor toegang tot alle plugin modules en services.
 * Implementeert het Service Locator pattern.
 */
public class PluginContext {
    
    private static PluginContext instance;
    private final Plugin plugin;
    private final Map<Class<?>, Object> services = new HashMap<>();
    private final Map<String, PluginModule> modules = new HashMap<>();
    
    private PluginContext(Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Haal de singleton instance op.
     * 
     * @param plugin De hoofdplugin instance
     * @return De PluginContext instance
     */
    public static PluginContext getInstance(Plugin plugin) {
        if (instance == null) {
            instance = new PluginContext(plugin);
        }
        return instance;
    }
    
    /**
     * Haal de hoofdplugin instance op.
     * 
     * @return De plugin instance
     */
    public Plugin getPlugin() {
        return plugin;
    }
    
    /**
     * Registreer een service.
     * 
     * @param serviceClass De service class
     * @param service De service instance
     * @param <T> De service type
     */
    public <T> void registerService(Class<T> serviceClass, T service) {
        services.put(serviceClass, service);
    }
    
    /**
     * Haal een service op.
     * 
     * @param serviceClass De service class
     * @param <T> De service type
     * @return Optional met de service, of empty als niet gevonden
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getService(Class<T> serviceClass) {
        return Optional.ofNullable((T) services.get(serviceClass));
    }
    
    /**
     * Registreer een HarvestMinigameService.java.
     * 
     * @param module De HarvestMinigameService.java instance
     */
    public void registerModule(PluginModule module) {
        modules.put(module.getName(), module);
    }
    
    /**
     * Haal een HarvestMinigameService.java op.
     * 
     * @param name De HarvestMinigameService.java naam
     * @return Optional met de HarvestMinigameService.java, of empty als niet gevonden
     */
    public Optional<PluginModule> getModule(String name) {
        return Optional.ofNullable(modules.get(name));
    }
    
    /**
     * Initialiseer alle geregistreerde modules.
     */
    public void initModules() {
        for (PluginModule module : modules.values()) {
            try {
                module.init(plugin);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to initialize HarvestMinigameService.java: " + module.getName());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Shutdown alle geregistreerde modules.
     */
    public void shutdownModules() {
        for (PluginModule module : modules.values()) {
            try {
                module.shutdown(plugin);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to shutdown HarvestMinigameService.java: " + module.getName());
                e.printStackTrace();
            }
        }
    }
} 