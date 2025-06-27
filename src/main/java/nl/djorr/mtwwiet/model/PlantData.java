package nl.djorr.mtwwiet.model;

import org.bukkit.entity.ArmorStand;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.UUID;
import nl.djorr.mtwwiet.minigame.IHarvestMinigame;
import nl.djorr.mtwwiet.minigame.HarvestMinigame;
import org.bukkit.entity.Player;

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
    public UUID oogstSpeler;
    public Hologram oogstHologram;
    public org.bukkit.Location plantBlockLocation; // Locatie van het plantblok (waar de sapling wordt geplaatst)
    private IHarvestMinigame minigame;

    /**
     * Start een nieuwe minigame voor deze plant.
     */
    public void startMinigame(Player player) {
        oogstSpeler = player.getUniqueId();
        minigame = new HarvestMinigame(player, this);
    }

    /**
     * Ruimt alle taken, hologrammen, minigame en locks netjes op.
     */
    public void cleanup() {
        if (growthTask != null) growthTask.cancel();
        if (hologram != null) hologram.delete();
        if (oogstHologram != null) oogstHologram.delete();
        if (minigame != null) minigame.forceCleanup();
        oogstSpeler = null;
        minigame = null;
    }

    public IHarvestMinigame getMinigame() {
        return minigame;
    }
} 