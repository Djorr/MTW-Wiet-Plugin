package nl.yourname.weedplugin.manager;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import nl.yourname.weedplugin.util.MessageUtil;
import nl.yourname.weedplugin.item.CustomItems;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Beheert wiet NPCs voor de shop functionaliteit.
 */
public class NPCManager {
    private static NPCManager instance;
    private final Map<UUID, NPC> weedNPCs = new HashMap<>();
    private final Map<UUID, Location> npcLocations = new HashMap<>();
    private File npcFile;
    private YamlConfiguration npcConfig;
    private Plugin plugin;

    public static NPCManager getInstance() {
        if (instance == null) instance = new NPCManager();
        return instance;
    }

    public void init(Plugin plugin) {
        this.plugin = plugin;
        npcFile = new File(plugin.getDataFolder(), "npcs.yml");
        if (!npcFile.exists()) {
            try { npcFile.createNewFile(); } catch (IOException ignored) {}
        }
        npcConfig = YamlConfiguration.loadConfiguration(npcFile);
        loadNPCs();
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
                NPCRegistry registry = CitizensAPI.getNPCRegistry();
                NPC npc = registry.createNPC(EntityType.PLAYER, "§aWiet Verkoper");
                npc.spawn(loc);
                weedNPCs.put(npc.getUniqueId(), npc);
                npcLocations.put(npc.getUniqueId(), loc);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Spawn een nieuwe wiet NPC op de gegeven locatie.
     */
    public NPC spawnWeedNPC(Location location) {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC npc = registry.createNPC(EntityType.PLAYER, "§aWiet Verkoper");
        npc.spawn(location);
        weedNPCs.put(npc.getUniqueId(), npc);
        npcLocations.put(npc.getUniqueId(), location);
        saveNPCs();
        return npc;
    }
    
    /**
     * Verwijder een wiet NPC.
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
     * Check of een NPC een wiet NPC is.
     */
    public boolean isWeedNPC(UUID npcId) {
        return weedNPCs.containsKey(npcId);
    }
    
    /**
     * Open de shop GUI voor een speler.
     */
    public void openShop(Player player) {
        Inventory shop = org.bukkit.Bukkit.createInventory(null, 9, "§aGrowshop");
        Plugin plugin = player.getServer().getPluginManager().getPlugin("WeedPlugin");
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfig();
        
        // Wietzaadje (normale seeds)
        ItemStack zaad = new ItemStack(org.bukkit.Material.SEEDS);
        ItemMeta zaadMeta = zaad.getItemMeta();
        zaadMeta.setDisplayName("§aWietzaadje");
        java.util.List<String> zaadLore = new java.util.ArrayList<>();
        zaadLore.add("§7Prijs: §e" + config.getInt("shop.zaad"));
        zaadMeta.setLore(zaadLore);
        zaad.setItemMeta(zaadMeta);
        shop.setItem(0, zaad);
        
        // Zakje (papier)
        ItemStack zakje = CustomItems.getZakje();
        ItemMeta zakjeMeta = zakje.getItemMeta();
        java.util.List<String> zakjeLore = new java.util.ArrayList<>();
        zakjeLore.add("§7Prijs: §e" + config.getInt("shop.zakje"));
        zakjeMeta.setLore(zakjeLore);
        zakje.setItemMeta(zakjeMeta);
        shop.setItem(1, zakje);
        
        player.openInventory(shop);
    }
    
    /**
     * Haal alle wiet NPCs op.
     */
    public Map<UUID, NPC> getAllWeedNPCs() {
        return new HashMap<>(weedNPCs);
    }
} 