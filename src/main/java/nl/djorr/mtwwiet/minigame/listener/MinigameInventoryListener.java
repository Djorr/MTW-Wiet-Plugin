package nl.djorr.mtwwiet.minigame.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import nl.djorr.mtwwiet.core.PluginContext;
import nl.djorr.mtwwiet.minigame.HarvestMinigameModule;
import nl.djorr.mtwwiet.minigame.model.IHarvestMinigame;
import nl.djorr.mtwwiet.MTWWiet;

/**
 * Luistert naar clicks in de minigame GUI en stuurt deze door naar de juiste minigame instance.
 */
public class MinigameInventoryListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        // Get minigame service
        HarvestMinigameModule minigameService = PluginContext.getInstance(MTWWiet.getPlugin(MTWWiet.class)).getService(HarvestMinigameModule.class).orElse(null);
        if (minigameService == null) return;
        
        // Get active minigame for player
        IHarvestMinigame minigame = minigameService.getActiveMinigame(player);
        if (minigame != null && minigame.isActive()) {
            // Cancel the event first to prevent item movement
            event.setCancelled(true);
            
            // Handle the click in the minigame
            minigame.handleClick(event);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        
        // Get minigame service
        HarvestMinigameModule minigameService = PluginContext.getInstance(MTWWiet.getPlugin(MTWWiet.class)).getService(HarvestMinigameModule.class).orElse(null);
        if (minigameService == null) return;
        
        // Get active minigame for player
        IHarvestMinigame minigame = minigameService.getActiveMinigame(player);
        if (minigame != null && minigame.isActive()) {
            // Fail the minigame when inventory is closed
            minigame.forceFail();
        }
    }
} 