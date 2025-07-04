package nl.djorr.mtwwiet.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import nl.djorr.mtwwiet.manager.PlantManager;
import nl.djorr.mtwwiet.model.PlantData;

/**
 * Luistert naar clicks in de minigame GUI en stuurt deze door naar de juiste minigame instance.
 */
public class MinigameListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        // Zoek de plant van deze speler (optioneel: via locatie, of via alle planten itereren)
        for (PlantData data : PlantManager.getInstance().getAllPlants()) {
            if (data.oogstSpeler != null && data.oogstSpeler.equals(player.getUniqueId())) {
                if (data.getMinigame() != null) {
                    data.getMinigame().handleClick(event);
                }
                break;
            }
        }
    }
} 