package nl.djorr.mtwwiet.util;

import nl.djorr.mtwwiet.core.PluginModule;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.inventory.ItemStack;
import nl.djorr.mtwwiet.item.CustomItems;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Anti-dupe systeem dat spelers monitort voor verdachte activiteiten.
 */
public class AntiDupeManager implements PluginModule {
    private final Plugin plugin;
    private final CustomItems customItems;
    private final Map<UUID, PlayerInventorySnapshot> playerSnapshots = new HashMap<>();
    private final Map<UUID, Integer> suspiciousActivityCount = new HashMap<>();
    
    public AntiDupeManager(Plugin plugin, CustomItems customItems) {
        this.plugin = plugin;
        this.customItems = customItems;
    }
    
    @Override
    public void init(Plugin plugin) {
        // Already initialized in constructor
    }
    
    @Override
    public void shutdown(Plugin plugin) {
        // Cleanup snapshots
        playerSnapshots.clear();
        suspiciousActivityCount.clear();
    }
    
    @Override
    public String getName() {
        return "AntiDupeManager";
    }
    
    /**
     * Maakt een snapshot van de huidige inventory van een speler.
     */
    public void takeSnapshot(Player player) {
        PlayerInventorySnapshot snapshot = new PlayerInventorySnapshot(player);
        playerSnapshots.put(player.getUniqueId(), snapshot);
    }
    
    /**
     * Checkt voor verdachte activiteiten in de inventory van een speler.
     */
    public void checkForSuspiciousActivity(Player player) {
        PlayerInventorySnapshot oldSnapshot = playerSnapshots.get(player.getUniqueId());
        if (oldSnapshot == null) {
            takeSnapshot(player);
            return;
        }
        
        PlayerInventorySnapshot newSnapshot = new PlayerInventorySnapshot(player);
        int oldWeedCount = oldSnapshot.getWeedItemCount();
        int newWeedCount = newSnapshot.getWeedItemCount();
        
        // Check voor verdachte toename van wiet items
        if (newWeedCount > oldWeedCount + 5) { // Meer dan 5 items extra
            reportSuspiciousActivity(player, "Verdachte toename van wiet items", 
                "Van " + oldWeedCount + " naar " + newWeedCount + " items");
        }
        
        // Check voor creative mode met wiet items
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE && newWeedCount > 0) {
            reportSuspiciousActivity(player, "Creative mode met wiet items", 
                "Heeft " + newWeedCount + " wiet items in creative mode");
        }
        
        // Update snapshot
        playerSnapshots.put(player.getUniqueId(), newSnapshot);
    }
    
    /**
     * Rapporteert verdachte activiteit aan admins.
     */
    private void reportSuspiciousActivity(Player player, String reason, String details) {
        UUID playerId = player.getUniqueId();
        int count = suspiciousActivityCount.getOrDefault(playerId, 0) + 1;
        suspiciousActivityCount.put(playerId, count);
        
        String message = "§c§l[ANTI-DUPE] §fVerdachte activiteit gedetecteerd!\n" +
                        "§7Speler: §e" + player.getName() + "\n" +
                        "§7Reden: §e" + reason + "\n" +
                        "§7Details: §e" + details + "\n" +
                        "§7Verdachte activiteiten: §e" + count + "\n" +
                        "§7Klik hier om inventory te bekijken: §a/invcheck " + player.getName();
        
        // Stuur naar alle admins
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("weedplugin.admin") || onlinePlayer.isOp()) {
                onlinePlayer.sendMessage(message);
            }
        }
        
        // Log naar console
        plugin.getLogger().warning("[ANTI-DUPE] Verdachte activiteit van " + player.getName() + ": " + reason + " - " + details);
    }
    
    /**
     * Toont de inventory van een speler aan een admin.
     */
    public void showPlayerInventory(Player admin, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            admin.sendMessage("§cSpeler " + targetName + " is niet online!");
            return;
        }
        
        if (!admin.hasPermission("weedplugin.admin") && !admin.isOp()) {
            admin.sendMessage("§cJe hebt geen permissie om dit commando te gebruiken!");
            return;
        }
        
        // Open inventory viewer
        admin.openInventory(target.getInventory());
        admin.sendMessage("§aJe bekijkt nu de inventory van " + target.getName());
    }
    
    /**
     * Snapshot van een speler inventory voor vergelijking.
     */
    private class PlayerInventorySnapshot {
        private final UUID playerId;
        private final int weedItemCount;
        private final long timestamp;
        
        public PlayerInventorySnapshot(Player player) {
            this.playerId = player.getUniqueId();
            this.weedItemCount = customItems.countWeedItems(player);
            this.timestamp = System.currentTimeMillis();
        }
        
        public int getWeedItemCount() {
            return weedItemCount;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
} 