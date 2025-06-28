package nl.djorr.mtwwiet.minigame;

import nl.djorr.mtwwiet.core.PluginModule;
import nl.djorr.mtwwiet.minigame.event.MinigameEndEvent;
import nl.djorr.mtwwiet.minigame.event.MinigameStartEvent;
import nl.djorr.mtwwiet.minigame.model.IHarvestMinigame;
import nl.djorr.mtwwiet.plant.model.PlantData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service voor het beheren van harvest minigames.
 */
public class HarvestMinigameModule implements PluginModule {
    private final Map<UUID, IHarvestMinigame> activeMinigames = new HashMap<>();
    private Plugin plugin;

    @Override
    public void init(Plugin plugin) {
        this.plugin = plugin;
        
        // Register minigame event SellListener.java
        plugin.getServer().getPluginManager().registerEvents(new nl.djorr.mtwwiet.minigame.listener.MinigameListener(plugin), plugin);
    }
    @Override
    public void shutdown(Plugin plugin) {
        for (IHarvestMinigame minigame : activeMinigames.values()) {
            minigame.forceCleanup();
        }
        activeMinigames.clear();
    }
    @Override
    public String getName() {
        return "HarvestMinigameService";
    }

    /**
     * Start een nieuwe minigame voor een speler en plant.
     */
    public void startMinigame(Player player, PlantData plant, IHarvestMinigame minigame) {
        activeMinigames.put(player.getUniqueId(), minigame);
        Bukkit.getPluginManager().callEvent(new MinigameStartEvent(minigame));
    }

    /**
     * Stop een actieve minigame.
     */
    public void stopMinigame(Player player) {
        IHarvestMinigame minigame = activeMinigames.remove(player.getUniqueId());
        if (minigame != null) {
            minigame.forceCleanup();
            // Don't trigger MinigameEndEvent here - let the minigame handle its own completion
        }
    }

    /**
     * End minigame with success/failure status.
     */
    public void endMinigame(Player player, boolean success) {
        IHarvestMinigame minigame = activeMinigames.remove(player.getUniqueId());
        if (minigame != null) {
            minigame.forceCleanup();
            // Only trigger MinigameEndEvent when minigame is actually completed
            if (success) {
                Bukkit.getPluginManager().callEvent(new MinigameEndEvent(minigame));
            }
        }
    }

    /**
     * Haal een actieve minigame op voor een speler.
     */
    public IHarvestMinigame getActiveMinigame(Player player) {
        return activeMinigames.get(player.getUniqueId());
    }
} 