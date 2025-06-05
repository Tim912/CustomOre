package com.example.customitemsystem;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 * Provides tab completion for the /addability command.
 */
public class AbilityTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String lower = args[0].toLowerCase();
            return Arrays.stream(Ability.values())
                    .map(Enum::name)
                    .filter(n -> n.toLowerCase().startsWith(lower))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
