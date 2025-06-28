package nl.djorr.mtwwiet;

import nl.djorr.mtwwiet.command.CommandRegistry;
import nl.djorr.mtwwiet.config.ConfigManager;
import nl.djorr.mtwwiet.core.PluginContext;
import nl.djorr.mtwwiet.core.PluginModule;
import nl.djorr.mtwwiet.npc.NPCModule;
import nl.djorr.mtwwiet.util.VaultUtil;
import org.bukkit.plugin.java.JavaPlugin;
import nl.djorr.mtwwiet.plant.PlantModule;
import nl.djorr.mtwwiet.minigame.HarvestMinigameModule;
import nl.djorr.mtwwiet.minigame.MinigameInventoryModule;
import nl.djorr.mtwwiet.shop.ShopModule;
import nl.djorr.mtwwiet.sell.SellModule;

import java.util.Arrays;
import java.util.List;

/**
 * Hoofdplugin class voor MTWWiet.
 * Gebruikt een modulaire architectuur met PluginContext voor dependency management.
 */
public class MTWWiet extends JavaPlugin {
    
    private PluginContext context;
    private List<PluginModule> modules;
    
    @Override
    public void onEnable() {
        // Initialiseer PluginContext
        context = PluginContext.getInstance(this);
        
        // Initialiseer MessageUtil eerst
        nl.djorr.mtwwiet.util.MessageUtil.init(this);
        
        // Registreer alle modules
        registerModules();
        
        // Initialiseer modules
        context.initModules();
        
        // Check dependencies
        checkDependencies();
        
        // Register additional commands
        getCommand("invcheck").setExecutor(new nl.djorr.mtwwiet.command.InvCheckCommand(
            context.getService(nl.djorr.mtwwiet.util.AntiDupeManager.class).orElse(null)));
        
        getLogger().info("MTWWiet plugin is ingeschakeld!");
    }
    
    @Override
    public void onDisable() {
        // Shutdown alle modules
        if (context != null) {
            context.shutdownModules();
        }
        
        getLogger().info("MTWWiet plugin is uitgeschakeld!");
    }
    
    /**
     * Registreer alle plugin modules.
     */
    private void registerModules() {
        // Create modules
        ConfigManager configManager = new ConfigManager();
        CommandRegistry commandRegistry = new CommandRegistry();
        nl.djorr.mtwwiet.item.CustomItems customItems = new nl.djorr.mtwwiet.item.CustomItems(this);
        nl.djorr.mtwwiet.util.AntiDupeManager antiDupeManager = new nl.djorr.mtwwiet.util.AntiDupeManager(this, customItems);
        PlantModule plantModule = new PlantModule();
        HarvestMinigameModule harvestMinigameModule = new HarvestMinigameModule();
        MinigameInventoryModule minigameInventoryModule = new MinigameInventoryModule();
        ShopModule shopModule = new ShopModule();
        SellModule sellModule = new SellModule();
        NPCModule npcModule = new NPCModule(this);
        
        // Create modules list
        modules = Arrays.asList(
            configManager,
            commandRegistry,
            customItems,
            antiDupeManager,
            plantModule,
            harvestMinigameModule,
            minigameInventoryModule,
            shopModule,
            sellModule,
            npcModule
        );
        
        // Register all modules
        for (PluginModule module : modules) {
            context.registerModule(module);
        }
        
        // Register services
        context.registerService(ConfigManager.class, configManager);
        context.registerService(CommandRegistry.class, commandRegistry);
        context.registerService(nl.djorr.mtwwiet.item.CustomItems.class, customItems);
        context.registerService(nl.djorr.mtwwiet.util.AntiDupeManager.class, antiDupeManager);
        context.registerService(PlantModule.class, plantModule);
        context.registerService(HarvestMinigameModule.class, harvestMinigameModule);
        context.registerService(MinigameInventoryModule.class, minigameInventoryModule);
        context.registerService(ShopModule.class, shopModule);
        context.registerService(SellModule.class, sellModule);
        context.registerService(NPCModule.class, npcModule);
    }
    
    /**
     * Check of alle benodigde dependencies aanwezig zijn.
     */
    private void checkDependencies() {
        // Check Vault
        if (!VaultUtil.setupEconomy()) {
            getLogger().severe("Vault niet gevonden! Plugin werkt niet zonder Vault.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Check Citizens (optioneel)
        if (getServer().getPluginManager().getPlugin("Citizens") == null) {
            getLogger().warning("Citizens niet gevonden! NPC functionaliteit is uitgeschakeld.");
        }
        
        // Check HolographicDisplays (optioneel)
        if (getServer().getPluginManager().getPlugin("HolographicDisplays") == null) {
            getLogger().warning("HolographicDisplays niet gevonden! Hologrammen zijn uitgeschakeld.");
        }
    }
    
    /**
     * Haal de PluginContext op.
     * 
     * @return De PluginContext instance
     */
    public static PluginContext getContext() {
        return PluginContext.getInstance(null);
    }
} 