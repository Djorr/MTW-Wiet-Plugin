package nl.djorr.mtwwiet.npc;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.CitizensAPI;
import nl.djorr.mtwwiet.core.PluginModule;
import nl.djorr.mtwwiet.npc.conversation.DealerConversation;
import nl.djorr.mtwwiet.item.CustomItems;
import nl.djorr.mtwwiet.util.MessageUtil;
import nl.djorr.mtwwiet.util.VaultUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Module voor het beheren van NPC dealers met conversatie systeem.
 */
public class NPCModule implements PluginModule {
    private static NPCModule instance;
    private final Map<UUID, NPC> weedNPCs = new HashMap<>();
    private final Map<UUID, Location> npcLocations = new HashMap<>();
    private File npcFile;
    private YamlConfiguration npcConfig;
    private final Plugin plugin;
    private final DealerConversation dealerConversation;
    private final Map<UUID, Long> lastDealerSpawn = new ConcurrentHashMap<>();
    private final Random random = new Random();
    
    // Dealer data
    private int weedSeedStock = 100;
    private long lastRestock = System.currentTimeMillis();

    public static NPCModule getInstance(Plugin plugin) {
        if (instance == null) instance = new NPCModule(plugin);
        return instance;
    }

    public NPCModule(Plugin plugin) {
        this.plugin = plugin;
        this.dealerConversation = new DealerConversation(plugin);
        npcFile = new File(plugin.getDataFolder(), "npcs.yml");
        if (!npcFile.exists()) {
            try { npcFile.createNewFile(); } catch (IOException ignored) {}
        }
        npcConfig = YamlConfiguration.loadConfiguration(npcFile);
        loadNPCs();
        
        // Start restock timer
        startRestockTimer();
    }

    @Override
    public void init(Plugin plugin) {
        // Register listeners
        plugin.getServer().getPluginManager().registerEvents(new nl.djorr.mtwwiet.npc.listener.NPCListener(plugin), plugin);
        
        // Start restock timer
        startRestockTimer();
    }

    @Override
    public void shutdown(Plugin plugin) {
        saveNPCs();
        // Cleanup conversations
        dealerConversation.endAllConversations();
    }

    @Override
    public String getName() {
        return "NPCModule";
    }

    public void saveNPCs() {
        npcConfig.set("npcs", null);
        for (Map.Entry<UUID, Location> entry : npcLocations.entrySet()) {
            UUID id = entry.getKey();
            Location loc = entry.getValue();
            String path = "npcs." + id;
            npcConfig.set(path + ".world", loc.getWorld().getName());
            npcConfig.set(path + ".x", loc.getX());
            npcConfig.set(path + ".y", loc.getY());
            npcConfig.set(path + ".z", loc.getZ());
            npcConfig.set(path + ".yaw", loc.getYaw());
            npcConfig.set(path + ".pitch", loc.getPitch());
        }
        try { npcConfig.save(npcFile); } catch (IOException ignored) {}
    }

    public void loadNPCs() {
        weedNPCs.clear();
        npcLocations.clear();
        if (npcConfig.getConfigurationSection("npcs") == null) return;
        
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        
        for (String idStr : npcConfig.getConfigurationSection("npcs").getKeys(false)) {
            try {
                UUID id = UUID.fromString(idStr);
                String world = npcConfig.getString("npcs." + idStr + ".world");
                double x = npcConfig.getDouble("npcs." + idStr + ".x");
                double y = npcConfig.getDouble("npcs." + idStr + ".y");
                double z = npcConfig.getDouble("npcs." + idStr + ".z");
                float yaw = (float) npcConfig.getDouble("npcs." + idStr + ".yaw");
                float pitch = (float) npcConfig.getDouble("npcs." + idStr + ".pitch");
                World w = Bukkit.getWorld(world);
                if (w == null) continue;
                Location loc = new Location(w, x, y, z, yaw, pitch);
                
                // Check if NPC already exists at this location
                NPC existingNPC = findExistingNPCAtLocation(loc);
                if (existingNPC != null) {
                    // NPC already exists, just register it
                    weedNPCs.put(existingNPC.getUniqueId(), existingNPC);
                    npcLocations.put(existingNPC.getUniqueId(), loc);
                    plugin.getLogger().info("Registered existing NPC at " + loc.toString());
                } else {
                    // Create new NPC
                    NPC npc = registry.createNPC(EntityType.PLAYER, "§cDealer");
                    npc.spawn(loc);
                    weedNPCs.put(npc.getUniqueId(), npc);
                    npcLocations.put(npc.getUniqueId(), loc);
                    plugin.getLogger().info("Spawned new NPC at " + loc.toString());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load NPC: " + e.getMessage());
            }
        }
    }
    
    /**
     * Find existing NPC at location (within 1 block radius)
     */
    private NPC findExistingNPCAtLocation(Location location) {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        for (NPC npc : registry) {
            if (npc.isSpawned() && npc.getEntity().getLocation().distance(location) < 1.0) {
                return npc;
            }
        }
        return null;
    }

    /**
     * Spawn een dealer NPC op de gegeven locatie.
     */
    public NPC spawnWeedNPC(Location location) {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC npc = registry.createNPC(EntityType.PLAYER, "§cDealer");
        npc.spawn(location);
        weedNPCs.put(npc.getUniqueId(), npc);
        npcLocations.put(npc.getUniqueId(), location);
        saveNPCs();
        return npc;
    }
    
    /**
     * Verwijder een dealer NPC.
     */
    public boolean removeWeedNPC(UUID npcId) {
        NPC npc = weedNPCs.get(npcId);
        if (npc != null) {
            npc.destroy();
            weedNPCs.remove(npcId);
            npcLocations.remove(npcId);
            saveNPCs();
            return true;
        }
        return false;
    }
    
    /**
     * Check of een NPC een dealer NPC is.
     */
    public boolean isWeedNPC(UUID npcId) {
        return weedNPCs.containsKey(npcId);
    }
    
    /**
     * Haal alle wiet NPCs op.
     */
    public Map<UUID, NPC> getAllWeedNPCs() {
        return new HashMap<>(weedNPCs);
    }

    /**
     * Spawn een dealer NPC op de locatie van de speler.
     */
    public void spawnDealer(Player player) {
        Location spawnLocation = player.getLocation();
        
        // Check of er al een dealer NPC in de buurt staat
        for (Entity entity : spawnLocation.getWorld().getEntities()) {
            if (entity.getType() == EntityType.VILLAGER && 
                "§aDealer".equals(entity.getCustomName()) &&
                entity.getLocation().distance(spawnLocation) < 10) { // 10 blokken radius
                player.sendMessage("§cEr staat al een dealer NPC in de buurt!");
                return;
            }
        }
        
        // Spawn de NPC
        Entity dealer = spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.VILLAGER);
        dealer.setCustomName("§aDealer");
        dealer.setCustomNameVisible(true);
        
        // Store spawn time
        lastDealerSpawn.put(dealer.getUniqueId(), System.currentTimeMillis());
        
        // Auto despawn after 5 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                if (dealer.isValid()) {
                    dealer.remove();
                    lastDealerSpawn.remove(dealer.getUniqueId());
                }
            }
        }.runTaskLater(plugin, 20 * 60 * 5); // 5 minutes
        
        player.sendMessage(MessageUtil.getMessage("npc.spawn"));
    }
    
    /**
     * Despawn alle dealer NPCs.
     */
    public void despawnDealers() {
        int removed = 0;
        for (org.bukkit.World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getType() == EntityType.VILLAGER && 
                    "§aDealer".equals(entity.getCustomName())) {
                    entity.remove();
                    lastDealerSpawn.remove(entity.getUniqueId());
                    removed++;
                }
            }
        }
        plugin.getLogger().info("Removed " + removed + " dealer NPCs");
    }
    
    /**
     * Start een conversatie met een dealer.
     */
    public void startConversation(Player player, Entity dealer) {
        dealerConversation.startConversation(player, dealer);
    }
    
    /**
     * Handle player chat during conversation.
     */
    public void handlePlayerChat(Player player, String message) {
        dealerConversation.handleChatInput(player, message);
    }
    
    /**
     * Send random dealer message.
     */
    private void sendRandomDealerMessage(Player player, String messageType) {
        dealerConversation.sendRandomDealerMessage(player, messageType);
    }
    
    /**
     * Send random player message.
     */
    private void sendRandomPlayerMessage(Player player, String messageType) {
        dealerConversation.sendRandomPlayerMessage(player, messageType);
    }
    
    /**
     * Start restock timer.
     */
    private void startRestockTimer() {
        int restockInterval = plugin.getConfig().getInt("npc-dealer.stock.restock-interval-minutes", 10);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                restockWeedSeeds();
            }
        }.runTaskTimer(plugin, 20 * 60 * restockInterval, 20 * 60 * restockInterval);
    }
    
    /**
     * Restock weed seeds.
     */
    private void restockWeedSeeds() {
        int maxStock = plugin.getConfig().getInt("npc-dealer.stock.max-weed-seeds", 100);
        weedSeedStock = Math.min(weedSeedStock + 20, maxStock);
        lastRestock = System.currentTimeMillis();
    }
    
    /**
     * Get weed seed price.
     */
    private int getWeedSeedPrice() {
        return plugin.getConfig().getInt("npc-dealer.prices.weed-seed", 50);
    }
    
    /**
     * Check if player is in conversation.
     */
    public boolean isInConversation(Player player) {
        return dealerConversation.isInConversation(player);
    }
} 