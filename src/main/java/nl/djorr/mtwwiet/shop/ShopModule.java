package nl.djorr.mtwwiet.shop;

import nl.djorr.mtwwiet.core.PluginContext;
import nl.djorr.mtwwiet.core.PluginModule;
import nl.djorr.mtwwiet.item.CustomItems;
import nl.djorr.mtwwiet.shop.listener.ShopListener;
import nl.djorr.mtwwiet.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

/**
 * Service voor shop functionaliteit.
 */
public class ShopModule implements PluginModule {
    private ShopListener shopListener;
    private Plugin plugin;

    @Override
    public void init(Plugin plugin) {
        this.plugin = plugin;
        
        // Register shop listener
        this.shopListener = new nl.djorr.mtwwiet.shop.listener.ShopListener(plugin);
        plugin.getServer().getPluginManager().registerEvents(this.shopListener, plugin);
    }

    @Override
    public void shutdown(Plugin plugin) {
        // ShopListener.java heeft geen cleanup nodig
    }

    @Override
    public String getName() {
        return "ShopService";
    }

    /**
     * Open de shop voor een speler.
     */
    public void openShop(Player player) {
        CustomItems customItems = PluginContext.getInstance(plugin).getService(CustomItems.class).orElse(null);
        if (customItems == null) {
            player.sendMessage("§cShop is momenteel niet beschikbaar.");
            return;
        }

        // Maak shop inventory
        Inventory shopInventory = Bukkit.createInventory(null, 27, MessageUtil.getMessage("shop.title"));

        // Voeg wietzaadjes toe
        ItemStack weedSeed = customItems.createWeedSeed();
        shopInventory.setItem(10, weedSeed);

        // Voeg prijs informatie toe
        ItemStack priceInfo = new ItemStack(Material.PAPER);
        org.bukkit.inventory.meta.ItemMeta priceMeta = priceInfo.getItemMeta();
        priceMeta.setDisplayName("§ePrijzen");
        priceMeta.setLore(Arrays.asList(
                "§7Wietzaadje: §a50 euro",
                "§7Klik op een item om te kopen!"
        ));
        priceInfo.setItemMeta(priceMeta);
        shopInventory.setItem(16, priceInfo);

        player.openInventory(shopInventory);
    }
} 