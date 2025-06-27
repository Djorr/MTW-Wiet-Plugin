package nl.yourname.weedplugin;

import org.bukkit.plugin.java.JavaPlugin;
import nl.yourname.weedplugin.util.MessageUtil;

/**
 * Hoofdklasse van de WeedPlugin.
 * Registreert alle managers, listeners en commando's.
 */
public class WeedPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("WeedPlugin is ingeschakeld!");
        
        // Laad messages
        MessageUtil.loadMessages(this);
        
        // Registreer commando
        this.getCommand("wiet").setExecutor(new nl.yourname.weedplugin.command.WietCommand());
        
        // Vault setup
        if (!nl.yourname.weedplugin.util.VaultUtil.setupEconomy()) {
            getLogger().severe("Kan Vault niet vinden! Plugin werkt niet zonder Vault.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Registreer listeners
        getServer().getPluginManager().registerEvents(new nl.yourname.weedplugin.listener.PlantListener(), this);
        getServer().getPluginManager().registerEvents(new nl.yourname.weedplugin.listener.MinigameListener(), this);
        getServer().getPluginManager().registerEvents(new nl.yourname.weedplugin.listener.ShopListener(), this);
        getServer().getPluginManager().registerEvents(new nl.yourname.weedplugin.listener.BagListener(), this);
        getServer().getPluginManager().registerEvents(new nl.yourname.weedplugin.listener.SellListener(), this);
        
        // Physics protection voor plugin-planten
        getServer().getPluginManager().registerEvents(new nl.yourname.weedplugin.manager.PlantManagerUtil.PlantPhysicsListener(), this);
        
        // Plant break protection
        getServer().getPluginManager().registerEvents(new nl.yourname.weedplugin.manager.PlantManagerUtil.PlantBreakListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("WeedPlugin is uitgeschakeld!");
    }
} 