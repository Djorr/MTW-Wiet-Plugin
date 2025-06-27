package nl.yourname.weedplugin.item;

import nl.yourname.weedplugin.util.MessageUtil;
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
        meta.setDisplayName(MessageUtil.getMessage("items.wiet-top-name", "kwaliteit", String.valueOf(kwaliteit)));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtil.getMessage("items.wiet-top-lore"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getZakje() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtil.getMessage("items.zakje-name"));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtil.getMessage("items.zakje-lore"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getGevuldeWietzak() {
        ItemStack item = new ItemStack(Material.SUGAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtil.getMessage("items.gevulde-wietzak-name"));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtil.getMessage("items.gevulde-wietzak-lore"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
} 