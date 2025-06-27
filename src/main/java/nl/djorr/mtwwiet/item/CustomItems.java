package nl.djorr.mtwwiet.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

/**
 * Beheert alle custom items in de plugin.
 */
public class CustomItems {
    
    /**
     * Maakt een wietzaadje item.
     */
    public static ItemStack getWietzaadje() {
        ItemStack seed = new ItemStack(Material.SEEDS);
        ItemMeta meta = seed.getItemMeta();
        meta.setDisplayName("§aWietzaadje");
        meta.setLore(Arrays.asList("§7Plant dit om wiet te laten groeien"));
        seed.setItemMeta(meta);
        return seed;
    }
    
    /**
     * Maakt een wiet item.
     */
    public static ItemStack getWiet() {
        ItemStack weed = new ItemStack(Material.DOUBLE_PLANT);
        weed.setDurability((short) 3);
        ItemMeta meta = weed.getItemMeta();
        meta.setDisplayName("§aWiet");
        meta.setLore(Arrays.asList("§7Vers geoogste wiet"));
        weed.setItemMeta(meta);
        return weed;
    }
    
    /**
     * Maakt een wiet top item met kwaliteit.
     */
    public static ItemStack getWietTop(int kwaliteit) {
        ItemStack top = new ItemStack(Material.DOUBLE_PLANT);
        top.setDurability((short) 3);
        ItemMeta meta = top.getItemMeta();
        meta.setDisplayName("§aWiet Top (kwaliteit " + kwaliteit + ")");
        meta.setLore(Arrays.asList("§7Vers geoogst"));
        top.setItemMeta(meta);
        return top;
    }
    
    /**
     * Controleert of een item een wietzaadje is.
     */
    public static boolean isWietzaadje(ItemStack item) {
        if (item == null || item.getType() != Material.SEEDS) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("§aWietzaadje");
    }
    
    /**
     * Controleert of een item wiet is.
     */
    public static boolean isWiet(ItemStack item) {
        if (item == null || item.getType() != Material.DOUBLE_PLANT) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().startsWith("§aWiet");
    }
} 