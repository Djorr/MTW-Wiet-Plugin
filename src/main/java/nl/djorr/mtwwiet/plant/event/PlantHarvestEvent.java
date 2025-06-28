package nl.djorr.mtwwiet.plant.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import nl.djorr.mtwwiet.plant.model.PlantData;

/**
 * Event dat wordt getriggerd als een plant wordt geoogst.
 */
public class PlantHarvestEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final PlantData plant;
    public PlantHarvestEvent(PlantData plant) {
        this.plant = plant;
    }
    public PlantData getPlant() {
        return plant;
    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
} 