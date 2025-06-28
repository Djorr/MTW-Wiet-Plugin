package nl.djorr.mtwwiet.item;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import nl.djorr.mtwwiet.core.PluginModule;
import nl.djorr.mtwwiet.config.ConfigManager;
import nl.djorr.mtwwiet.util.ItemBuilderUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Beheert alle custom items met NBT metadata voor de wiet plugin.
 */
public class CustomItems implements PluginModule {
    private final Plugin plugin;
    private static final String WEED_SEED_NBT = "mtw_weed_seed";
    private static final String WEED_NBT = "mtw_weed";
    private static final String UNIQUE_ID_NBT = "mtw_unique_id";
    
    public CustomItems(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void init(Plugin plugin) {
        // Already initialized in constructor
    }
    
    @Override
    public void shutdown(Plugin plugin) {
        // No cleanup needed
    }
    
    @Override
    public String getName() {
        return "CustomItems";
    }
    
    /**
     * Maakt een wietzaadje met NBT metadata en configureerbare lore.
     */
    public ItemStack createWeedSeed() {

        final ItemBuilderUtil weedSeedItem = new ItemBuilderUtil(Material.SEEDS);
        weedSeedItem.setName(getWeedSeedName());
        weedSeedItem.setLore(getWeedLore());

        weedSeedItem.setNBT(WEED_SEED_NBT, WEED_SEED_NBT);

        return weedSeedItem.toItemStack();
    }
    
    /**
     * Maakt wiet met NBT metadata en configureerbare lore.
     */
    public ItemStack createWeed() {
        final ItemBuilderUtil weedItem = new ItemBuilderUtil(Material.WATER_LILY);
        weedItem.setName(getWeedName());
        weedItem.setLore(getWeedLore());

        weedItem.addEnchant(Enchantment.DEPTH_STRIDER, 1);
        weedItem.setItemFlag(ItemFlag.HIDE_ENCHANTS);

        weedItem.setNBT(WEED_NBT, WEED_NBT);

        return weedItem.toItemStack();
    }
    

    /**
     * Haalt de naam voor wietzaadjes op uit config.
     */
    private String getWeedSeedName() {
        ConfigManager configManager = nl.djorr.mtwwiet.core.PluginContext.getInstance(plugin).getService(ConfigManager.class).orElse(null);
        if (configManager != null) {
            return configManager.getConfig().getString("custom-items.weed-seed.name", "§aWietzaadje");
        }
        return "§aWietzaadje";
    }
    
    /**
     * Haalt de naam voor wiet op uit config.
     */
    private String getWeedName() {
        ConfigManager configManager = nl.djorr.mtwwiet.core.PluginContext.getInstance(plugin).getService(ConfigManager.class).orElse(null);
        if (configManager != null) {
            return configManager.getConfig().getString("custom-items.weed.name", "§aWiet");
        }
        return "§aWiet";
    }
    
    /**
     * Haalt de lore voor wietzaadjes op uit config en vervangt placeholders.
     */
    private List<String> getWeedSeedLore(String uniqueId) {
        ConfigManager configManager = nl.djorr.mtwwiet.core.PluginContext.getInstance(plugin).getService(ConfigManager.class).orElse(null);
        if (configManager != null) {
            List<String> configLore = configManager.getConfig().getStringList("custom-items.weed-seed.lore");
            return configLore; // Geen placeholder vervanging meer nodig
        }
        
        // Fallback lore
        return Arrays.asList(
            "§7Dit zijn wietzaadjes die je kunt planten op gras",
            "§7Plant ze om wiet te laten groeien",
            "",
            "§c⚠ Illegaal item",
            "§7Officieel item van MT Wars"
        );
    }
    
    /**
     * Haalt de lore voor wiet op uit config en vervangt placeholders.
     */
    private List<String> getWeedLore() {
        ConfigManager configManager = nl.djorr.mtwwiet.core.PluginContext.getInstance(plugin).getService(ConfigManager.class).orElse(null);
        if (configManager != null) {
            List<String> configLore = configManager.getConfig().getStringList("custom-items.weed.lore");
            return configLore; // Geen placeholder vervanging meer nodig
        }
        
        // Fallback lore
        return Arrays.asList(
            "§7Vers geoogste wiet van hoge kwaliteit",
            "§7Je kunt dit verkopen aan deuren",
            "",
            "§c⚠ Illegaal item",
            "§7Officieel item van MT Wars"
        );
    }
    
    /**
     * Check of een item een wietzaadje is via NBT.
     */
    public boolean isWeedSeed(ItemStack item) {
        if (item == null) return false;
        try {
            return NBTEditor.contains(item, WEED_SEED_NBT);
        } catch (Exception e) {
            // Fallback to display name check if NBT fails
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                return item.getItemMeta().getDisplayName().equals(getWeedSeedName());
            }
            return false;
        }
    }
    
    /**
     * Check of een item wiet is via NBT.
     */
    public boolean isWeed(ItemStack item) {
        if (item == null) return false;
        try {
            return NBTEditor.contains(item, WEED_NBT);
        } catch (Exception e) {
            // Fallback to display name check if NBT fails
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                return item.getItemMeta().getDisplayName().equals(getWeedName());
            }
            return false;
        }
    }

    /**
     * Check of een blok geschikt is voor het planten van wiet.
     */
    public boolean canPlantOnBlock(Block block) {
        return block.getType() == Material.GRASS;
    }
    
    /**
     * Check of een speler custom wiet items heeft in zijn inventory.
     */
    public boolean hasWeedItems(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isWeed(item)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Telt het aantal wiet items in een inventory.
     */
    public int countWeedItems(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (isWeed(item)) {
                count += item.getAmount();
            }
        }
        return count;
    }
} 