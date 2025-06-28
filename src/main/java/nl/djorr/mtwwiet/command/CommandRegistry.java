package nl.djorr.mtwwiet.command;

import nl.djorr.mtwwiet.core.PluginModule;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry voor het beheren en registreren van alle plugin commando's.
 * Implementeert automatische registratie van commando's en tab completion.
 */
public class CommandRegistry implements PluginModule {
    
    private JavaPlugin plugin;
    private final Map<String, CommandExecutor> commands = new HashMap<>();
    private final Map<String, TabCompleter> tabCompleters = new HashMap<>();
    
    @Override
    public void init(Plugin plugin) {
        this.plugin = (JavaPlugin) plugin;
        
        // Register commands
        this.plugin.getCommand("wiet").setExecutor(new WietCommand(plugin));
        this.plugin.getCommand("wiet").setTabCompleter(new WietTabCompleter());
    }
    
    @Override
    public void shutdown(Plugin plugin) {
        // Commando's worden automatisch unregistered door Bukkit
    }
    
    @Override
    public String getName() {
        return "CommandRegistry";
    }
    
    /**
     * Registreer een commando.
     * 
     * @param name De commando naam
     * @param executor De command executor
     */
    public void registerCommand(String name, CommandExecutor executor) {
        registerCommand(name, executor, null);
    }
    
    /**
     * Registreer een commando met tab completion.
     * 
     * @param name De commando naam
     * @param executor De command executor
     * @param tabCompleter De tab completer
     */
    public void registerCommand(String name, CommandExecutor executor, TabCompleter tabCompleter) {
        commands.put(name, executor);
        if (tabCompleter != null) {
            tabCompleters.put(name, tabCompleter);
        }
        
        // Registreer bij Bukkit
        org.bukkit.command.PluginCommand pluginCommand = plugin.getServer().getPluginCommand(name);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(executor);
            if (tabCompleter != null) {
                pluginCommand.setTabCompleter(tabCompleter);
            }
        }
    }
    
    /**
     * Haal een geregistreerd commando op.
     * 
     * @param name De commando naam
     * @return De command executor, of null als niet gevonden
     */
    public CommandExecutor getCommand(String name) {
        return commands.get(name);
    }
    
    /**
     * Haal een tab completer op.
     * 
     * @param name De commando naam
     * @return De tab completer, of null als niet gevonden
     */
    public TabCompleter getTabCompleter(String name) {
        return tabCompleters.get(name);
    }
    
    /**
     * Check of een commando is geregistreerd.
     * 
     * @param name De commando naam
     * @return true als geregistreerd
     */
    public boolean isCommandRegistered(String name) {
        return commands.containsKey(name);
    }
} 