package nl.djorr.mtwwiet.core;

import org.bukkit.plugin.Plugin;

/**
 * Interface voor alle plugin modules.
 * Zorgt voor consistente initialisatie en lifecycle management.
 */
public interface PluginModule {
    
    /**
     * Initialiseer de HarvestMinigameService.java.
     * Wordt aangeroepen tijdens plugin startup.
     * 
     * @param plugin De hoofdplugin instance
     */
    void init(Plugin plugin);
    
    /**
     * Cleanup de HarvestMinigameService.java.
     * Wordt aangeroepen tijdens plugin shutdown.
     * 
     * @param plugin De hoofdplugin instance
     */
    void shutdown(Plugin plugin);
    
    /**
     * Haal de naam van deze HarvestMinigameService.java op.
     * 
     * @return De HarvestMinigameService.java naam
     */
    String getName();
} 