package nl.yourname.weedplugin;

import org.bukkit.plugin.java.JavaPlugin;
import nl.yourname.weedplugin.util.MessageUtil;

/**
 * Hoofdklasse van de WeedPlugin.
 * Registreert alle managers, listeners en commando's.
 */
public class WeedPlugin extends JavaPlugin {
    private nl.yourname.weedplugin.listener.SellListener sellListener;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info(MessageUtil.getMessage("plugin.enabled"));
        
        // Laad messages
        MessageUtil.loadMessages(this);
        
        // Init managers
        nl.yourname.weedplugin.manager.NPCManager.getInstance().init(this);
        nl.yourname.weedplugin.manager.PlantManager.getInstance().init(this);
        
        // Registreer commando
        this.getCommand("wiet").setExecutor(new nl.yourname.weedplugin.command.WietCommand());
        
        // Vault setup
        if (!nl.yourname.weedplugin.util.VaultUtil.setupEconomy()) {
            getLogger().severe(MessageUtil.getMessage("plugin.vault-not-found"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Registreer listeners
        nl.yourname.weedplugin.listener.PlantListener plantListener = new nl.yourname.weedplugin.listener.PlantListener();
        nl.yourname.weedplugin.listener.MinigameListener minigameListener = new nl.yourname.weedplugin.listener.MinigameListener();
        nl.yourname.weedplugin.listener.ShopListener shopListener = new nl.yourname.weedplugin.listener.ShopListener();
        nl.yourname.weedplugin.listener.BagListener bagListener = new nl.yourname.weedplugin.listener.BagListener();
        sellListener = new nl.yourname.weedplugin.listener.SellListener();
        sellListener.init(this);
        nl.yourname.weedplugin.listener.NPCListener npcListener = new nl.yourname.weedplugin.listener.NPCListener();
        
        getServer().getPluginManager().registerEvents(plantListener, this);
        getServer().getPluginManager().registerEvents(minigameListener, this);
        getServer().getPluginManager().registerEvents(shopListener, this);
        getServer().getPluginManager().registerEvents(bagListener, this);
        getServer().getPluginManager().registerEvents(sellListener, this);
        getServer().getPluginManager().registerEvents(npcListener, this);
        
        // Physics protection voor plugin-planten
        getServer().getPluginManager().registerEvents(new nl.yourname.weedplugin.manager.PlantManagerUtil.PlantPhysicsListener(), this);
        
        // Plant break protection
        getServer().getPluginManager().registerEvents(new nl.yourname.weedplugin.manager.PlantManagerUtil.PlantBreakListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        nl.yourname.weedplugin.manager.NPCManager.getInstance().saveNPCs();
        nl.yourname.weedplugin.manager.PlantManager.getInstance().savePlants();
        if (sellListener != null) sellListener.saveDoorCooldowns();
        getLogger().info(MessageUtil.getMessage("plugin.disabled"));
    }
} 