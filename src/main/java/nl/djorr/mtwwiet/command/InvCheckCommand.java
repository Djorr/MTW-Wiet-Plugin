package nl.djorr.mtwwiet.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import nl.djorr.mtwwiet.util.AntiDupeManager;

/**
 * Commando voor admins om spelers inventories te bekijken.
 */
public class InvCheckCommand implements CommandExecutor {
    private final AntiDupeManager antiDupeManager;
    
    public InvCheckCommand(AntiDupeManager antiDupeManager) {
        this.antiDupeManager = antiDupeManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cAlleen spelers kunnen dit commando gebruiken!");
            return true;
        }
        
        Player admin = (Player) sender;
        
        if (!admin.hasPermission("weedplugin.admin") && !admin.isOp()) {
            admin.sendMessage("§cJe hebt geen permissie om dit commando te gebruiken!");
            return true;
        }
        
        if (args.length != 1) {
            admin.sendMessage("§cGebruik: /invcheck <speler>");
            return true;
        }
        
        String targetName = args[0];
        antiDupeManager.showPlayerInventory(admin, targetName);
        
        return true;
    }
} 