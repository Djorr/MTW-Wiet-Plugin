package nl.djorr.mtwwiet.plant.model;

import org.bukkit.entity.ArmorStand;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.UUID;

/**
 * Data class voor een geplante wietplant, inclusief groei, oogst en cleanup.
 */
public class PlantData {
    public UUID owner;
    public long plantedAt;
    public long growTime;
    public ArmorStand stand;
    public Hologram hologram; // HolographicDisplays hologram
    public boolean ready;
    public BukkitRunnable growthTask;
    public BukkitRunnable countdownTask; // Timer voor hologram countdown updates
    public UUID oogstSpeler;
    public Hologram oogstHologram;
    public org.bukkit.Location plantBlockLocation; // Locatie van het plantblok (waar de sapling wordt geplaatst)

    /**
     * Ruimt alle taken, hologrammen, minigame en locks netjes op.
     */
    public void cleanup() {
        if (growthTask != null) growthTask.cancel();
        if (countdownTask != null) countdownTask.cancel();
        if (hologram != null) hologram.delete();
        if (oogstHologram != null) oogstHologram.delete();
        oogstSpeler = null;
    }
} 