package nl.djorr.mtwwiet.minigame;

import nl.djorr.mtwwiet.core.PluginModule;
import nl.djorr.mtwwiet.minigame.listener.MinigameInventoryListener;
import org.bukkit.plugin.Plugin;

/**
 * Service voor minigame inventory functionaliteit.
 */
public class MinigameInventoryModule implements PluginModule {
    private MinigameInventoryListener minigameInventoryListener;
    private Plugin plugin;

    @Override
    public void init(Plugin plugin) {
        this.plugin = plugin;
        this.minigameInventoryListener = new MinigameInventoryListener();
        plugin.getServer().getPluginManager().registerEvents(minigameInventoryListener, plugin);
    }

    @Override
    public void shutdown(Plugin plugin) {
        // MinigameInventoryListener heeft geen cleanup nodig
    }

    @Override
    public String getName() {
        return "MinigameInventoryService";
    }
} 