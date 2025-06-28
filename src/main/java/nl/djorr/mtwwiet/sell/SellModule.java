package nl.djorr.mtwwiet.sell;

import nl.djorr.mtwwiet.core.PluginModule;
import nl.djorr.mtwwiet.core.PluginContext;
import nl.djorr.mtwwiet.item.CustomItems;
import org.bukkit.plugin.Plugin;

/**
 * Service voor verkoop functionaliteit.
 */
public class SellModule implements PluginModule {
    private Plugin plugin;

    @Override
    public void init(Plugin plugin) {
        this.plugin = plugin;
        
        // Register sell listener
        CustomItems customItems = PluginContext.getInstance(plugin).getService(CustomItems.class).orElse(null);
        plugin.getServer().getPluginManager().registerEvents(new nl.djorr.mtwwiet.sell.listener.SellListener(plugin, customItems), plugin);
    }

    @Override
    public void shutdown(Plugin plugin) {
        // SellListener heeft geen cleanup nodig
    }

    @Override
    public String getName() {
        return "SellService";
    }
} 