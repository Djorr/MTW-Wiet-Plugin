package nl.yourname.weedplugin.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility voor het genereren van custom plugin-items.
 */
public class CustomItems {
    public static ItemStack getWietTop(int kwaliteit) {
        ItemStack item = new ItemStack(Material.DOUBLE_PLANT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§aWiet Top (kwaliteit " + kwaliteit + ")");
        List<String> lore = new ArrayList<>();
        lore.add("§7Vers geoogst");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getZakje() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§fZakje");
        List<String> lore = new ArrayList<>();
        lore.add("§7Leeg zakje voor wiet");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getGevuldeWietzak() {
        ItemStack item = new ItemStack(Material.SUGAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§aGevulde Wietzak");
        List<String> lore = new ArrayList<>();
        lore.add("§7Bevat wiet van topkwaliteit");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
} 