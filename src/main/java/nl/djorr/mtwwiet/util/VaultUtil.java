package nl.djorr.mtwwiet.util;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Utility class voor Vault economy integratie.
 */
public class VaultUtil {
    private static Economy economy = null;
    
    /**
     * Initialiseer de economy service.
     */
    public static boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
    
    /**
     * Haal de economy service op.
     */
    public static Economy getEconomy() {
        return economy;
    }
    
    /**
     * Check of economy beschikbaar is.
     */
    public static boolean isEconomyAvailable() {
        return economy != null;
    }
} 