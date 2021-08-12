package io.github.thatsmusic99.athena.commands;

import io.github.thatsmusic99.athena.AthenaCore;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AthenaCommand implements IAthenaCommand {

    private final HashMap<String, IAthenaCommand> subcommands;

    public AthenaCommand() {
        subcommands = new HashMap<>();
        subcommands.put("listen", new ListenCommand());
        subcommands.put("stop", new StopCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            AthenaCore.sendFailMessage(sender, "Not enough arguments are included! Please add a subcommand.");
            return true;
        }
        IAthenaCommand command = subcommands.get(args[0]);
        if (command == null) {
            AthenaCore.sendFailMessage(sender, args[0] + " is not a valid command!");
            return true;
        }
        // Get metadata
        Command commandInfo = command.getClass().getAnnotation(Command.class);

        // Check for my stupidity
        if (commandInfo == null) {
            AthenaCore.sendFailMessage(sender, "The " + args[0] + " command is not configured properly! Please let the ATHENA developer know.");
            return true;
        }
        // If no permission
        if (!sender.hasPermission(commandInfo.permission())) {
            AthenaCore.sendFailMessage(sender, "You do not have access to this command!");
            return true;
        }

        if (!command.onCommand(sender, cmd, s, args)) {
            AthenaCore.sendFailMessage(sender, "Usage: " + commandInfo.usage());
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] args) {
        List<String> results = new ArrayList<>();
        if (args.length == 1) {
            for (String subcommand : subcommands.keySet()) {
                if (!sender.hasPermission("athena.command." + subcommand.toLowerCase())) continue;
                if (!subcommand.toLowerCase().startsWith(args[0].toLowerCase())) continue;
                results.add(subcommand);
            }
        }
        if (args.length == 2) {
            if (subcommands.get(args[0]) != null) {
                Command commandInfo = subcommands.get(args[0]).getClass().getAnnotation(Command.class);
                if (commandInfo == null) return results;
                if (!sender.hasPermission(commandInfo.permission())) return results;
                List<String> commandTB = subcommands.get(args[0]).onTabComplete(sender, command, s, args);
                if (commandTB == null) commandTB = new ArrayList<>();
                StringUtil.copyPartialMatches(args[1], commandTB, results);
            }
        }
        return results;
    }
}
