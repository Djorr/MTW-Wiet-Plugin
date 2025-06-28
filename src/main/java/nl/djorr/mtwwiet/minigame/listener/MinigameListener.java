package nl.djorr.mtwwiet.minigame.listener;

import nl.djorr.mtwwiet.core.PluginContext;
import nl.djorr.mtwwiet.minigame.event.MinigameEndEvent;
import nl.djorr.mtwwiet.minigame.event.MinigameStartEvent;
import nl.djorr.mtwwiet.plant.PlantModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * Luistert naar minigame events en handelt ze af.
 */
public class MinigameListener implements Listener {
    private final Plugin plugin;
    
    public MinigameListener(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onMinigameStart(MinigameStartEvent event) {
        // Minigame started - could add logging or other start logic here
    }
    
    @EventHandler
    public void onMinigameEnd(MinigameEndEvent event) {
        // Delegate to PlantService for plant cleanup and rewards
        PlantModule plantModule = PluginContext.getInstance(plugin).getService(PlantModule.class).orElse(null);
        if (plantModule != null) {
            plantModule.onMinigameEnd(event);
        }
    }
} 