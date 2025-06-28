package nl.djorr.mtwwiet.plant.factory;

import nl.djorr.mtwwiet.plant.model.PlantData;
import org.bukkit.Location;
import java.util.UUID;

/**
 * Factory voor het aanmaken van nieuwe PlantData instanties.
 */
public class PlantFactory {
    public static PlantData createPlant(UUID owner, Location location, long growTime) {
        PlantData plant = new PlantData();
        plant.owner = owner;
        plant.plantBlockLocation = location;
        plant.growTime = growTime;
        plant.plantedAt = System.currentTimeMillis();
        plant.ready = false;
        return plant;
    }
} 