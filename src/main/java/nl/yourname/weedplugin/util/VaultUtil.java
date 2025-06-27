package nl.yourname.weedplugin.util;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Utility voor Vault economy integratie.
 */
public class VaultUtil {
    private static Economy econ = null;

    public static boolean setupEconomy() {
        if (econ != null) return true;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }
} 