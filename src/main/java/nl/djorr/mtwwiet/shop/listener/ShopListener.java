package nl.djorr.mtwwiet.shop.listener;

import nl.djorr.mtwwiet.core.PluginContext;
import nl.djorr.mtwwiet.item.CustomItems;
import nl.djorr.mtwwiet.shop.ShopModule;
import nl.djorr.mtwwiet.util.MessageUtil;
import nl.djorr.mtwwiet.util.VaultUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

/**
 * Beheert de winkel GUI voor het kopen van wietzaadjes.
 */
public class ShopListener implements Listener {
    
    private final Plugin plugin;
    
    public ShopListener(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onChestClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.CHEST) return;
        
        Player player = event.getPlayer();
        if (!player.hasPermission("weedplugin.winkel") && !player.isOp()) return;
        
        // Check of dit een shop chest is (kan uitgebreid worden met metadata)
        if (isShopChest(block.getLocation())) {
            event.setCancelled(true);
            ShopModule shopModule = PluginContext.getInstance(plugin).getService(ShopModule.class).orElse(null);
            shopModule.openShop(player);
        }
    }
    
    private boolean isShopChest(org.bukkit.Location location) {
        // Voor nu, alle chests zijn shop chests
        // Later kan dit uitgebreid worden met metadata of config
        return true;
    }
    
    @EventHandler
    public void onShopClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        if (!event.getView().getTitle().equals(MessageUtil.getMessage("shop.title"))) return;
        
        event.setCancelled(true);
        
        if (event.getCurrentItem() == null) return;
        
        // Check of het een wietzaadje is (check op materiaal + displayName)
        ItemStack clicked = event.getCurrentItem();
        if (clicked.getType() != Material.SEEDS) return;
        if (clicked.getItemMeta() == null || !clicked.getItemMeta().hasDisplayName()) return;
        String displayName = clicked.getItemMeta().getDisplayName();
        CustomItems customItems = PluginContext.getInstance(plugin).getService(CustomItems.class).orElse(null);
        String expectedName = getWeedSeedNameFromConfig();
        if (!displayName.equals(expectedName)) return;
        
        // Check of speler genoeg geld heeft
        double price = 50.0; // Wietzaadje prijs
        double balance = VaultUtil.getEconomy().getBalance(player);
        if (balance < price) {
            player.sendMessage(MessageUtil.getMessage("shop.not-enough-money"));
            return;
        }
        
        // Koop wietzaadje: geef altijd een nieuw custom zaadje met NBT
        VaultUtil.getEconomy().withdrawPlayer(player, price);
        if (customItems != null) {
            player.getInventory().addItem(customItems.createWeedSeed());
        }
        player.sendMessage(MessageUtil.getMessage("shop.purchase-success").replace("{price}", String.valueOf(price)));
        player.updateInventory();
    }
    
    private String getWeedSeedNameFromConfig() {
        nl.djorr.mtwwiet.config.ConfigManager configManager = PluginContext.getInstance(plugin).getService(nl.djorr.mtwwiet.config.ConfigManager.class).orElse(null);
        if (configManager != null) {
            return configManager.getConfig().getString("custom-items.weed-seed.name", "§aWietzaadje");
        }
        return "§aWietzaadje";
    }
} 