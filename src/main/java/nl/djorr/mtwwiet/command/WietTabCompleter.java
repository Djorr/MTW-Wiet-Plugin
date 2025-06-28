package nl.djorr.mtwwiet.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tab completer voor het wiet commando.
 * Geeft suggesties voor subcommando's en parameters.
 */
public class WietTabCompleter implements TabCompleter {
    
    private static final List<String> SUBCOMMANDS = Arrays.asList(
        "plant", "balans", "geefgeld", "npc", "winkel", "reload", "cleanuparmorstands", "zaadje"
    );
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Eerste argument: subcommando
            String input = args[0].toLowerCase();
            for (String subcommand : SUBCOMMANDS) {
                if (subcommand.startsWith(input)) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            // Tweede argument: afhankelijk van subcommando
            String subcommand = args[0].toLowerCase();
            String input = args[1].toLowerCase();
            
            switch (subcommand) {
                case "geefgeld":
                    // Suggesties voor speler namen
                    completions.addAll(getOnlinePlayerNames(input));
                    break;
                case "npc":
                    if ("spawn".startsWith(input)) {
                        completions.add("spawn");
                    }
                    break;
            }
        } else if (args.length == 3) {
            // Derde argument: alleen voor geefgeld
            if ("geefgeld".equals(args[0].toLowerCase())) {
                String input = args[2].toLowerCase();
                if ("100".startsWith(input)) {
                    completions.add("100");
                }
                if ("1000".startsWith(input)) {
                    completions.add("1000");
                }
                if ("10000".startsWith(input)) {
                    completions.add("10000");
                }
            }
        }
        
        return completions;
    }
    
    /**
     * Haal online speler namen op die beginnen met de input.
     * 
     * @param input De input string
     * @return Lijst van speler namen
     */
    private List<String> getOnlinePlayerNames(String input) {
        List<String> names = new ArrayList<>();
        for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(input)) {
                names.add(player.getName());
            }
        }
        return names;
    }
} 