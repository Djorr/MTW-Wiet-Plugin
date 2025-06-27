package nl.djorr.mtwwiet;

import nl.djorr.mtwwiet.command.WietCommand;
import nl.djorr.mtwwiet.listener.*;
import nl.djorr.mtwwiet.manager.NPCManager;
import nl.djorr.mtwwiet.manager.PlantManager;
import nl.djorr.mtwwiet.manager.PlantManagerUtil;
import nl.djorr.mtwwiet.util.VaultUtil;
import org.bukkit.plugin.java.JavaPlugin;
import nl.djorr.mtwwiet.util.MessageUtil;

/**
 * Hoofdklasse van de WeedPlugin.
 * Registreert alle managers, listeners en commando's.
 */
public class MTWWiet extends JavaPlugin {
    private SellListener sellListener;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info(MessageUtil.getMessage("plugin.enabled"));
        
        // Laad messages
        MessageUtil.loadMessages(this);
        
        // Init managers
        NPCManager.getInstance().init(this);
        PlantManager.getInstance().init(this);
        
        // Registreer commando
        this.getCommand("wiet").setExecutor(new WietCommand());
        
        // Vault setup
        if (!VaultUtil.setupEconomy()) {
            getLogger().severe(MessageUtil.getMessage("plugin.vault-not-found"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Registreer listeners
        PlantListener plantListener = new PlantListener();
        MinigameListener minigameListener = new MinigameListener();
        ShopListener shopListener = new ShopListener();
        sellListener = new SellListener();
        sellListener.init(this);
        NPCListener npcListener = new NPCListener();
        
        getServer().getPluginManager().registerEvents(plantListener, this);
        getServer().getPluginManager().registerEvents(minigameListener, this);
        getServer().getPluginManager().registerEvents(shopListener, this);
        getServer().getPluginManager().registerEvents(sellListener, this);
        getServer().getPluginManager().registerEvents(npcListener, this);
        
        // Physics protection voor plugin-planten
        getServer().getPluginManager().registerEvents(new PlantManagerUtil.PlantPhysicsListener(), this);
        
        // Plant break protection
        getServer().getPluginManager().registerEvents(new PlantManagerUtil.PlantBreakListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        NPCManager.getInstance().saveNPCs();
        PlantManager.getInstance().savePlants();
        if (sellListener != null) sellListener.saveDoorCooldowns();
        getLogger().info(MessageUtil.getMessage("plugin.disabled"));
    }
} 