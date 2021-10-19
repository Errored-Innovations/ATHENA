package io.github.thatsmusic99.athena.commands;

import io.github.thatsmusic99.athena.AthenaCore;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AthenaCommand implements IAthenaCommand {

    public static HelpCommand HELP_COMMAND = new HelpCommand();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            HELP_COMMAND.onCommand(sender, cmd, s, args);
            return true;
        }
        Subcommand command = Subcommand.match(args[0].toUpperCase(Locale.ROOT));

        if (command == null || command.getExecutor() == null) {
            HELP_COMMAND.onCommand(sender, cmd, s, args);
            return true;
        }
        // Get metadata
        Command commandInfo = command.getExecutor().getClass().getAnnotation(Command.class);

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

        if (!command.getExecutor().onCommand(sender, cmd, s, args)) {
            AthenaCore.sendFailMessage(sender, "Usage: " + commandInfo.usage());
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] args) {
        List<String> results = new ArrayList<>();
        if (args.length == 1) {
            for (Subcommand subcommand : Subcommand.values()) {
                String stringCommand = subcommand.name().toLowerCase();
                if (!sender.hasPermission("athena.command." + stringCommand)) continue;
                if (!stringCommand.startsWith(args[0].toLowerCase())) continue;
                results.add(stringCommand);
            }
            return results;
        }
        if (args.length == 2) {
            Subcommand subcommand = Subcommand.match(args[0].toLowerCase(Locale.ROOT));
            if (subcommand != null) {
                Command commandInfo = subcommand.getExecutor().getClass().getAnnotation(Command.class);
                if (commandInfo == null) return results;
                if (!sender.hasPermission(commandInfo.permission())) return results;
                List<String> commandTB = subcommand.getExecutor().onTabComplete(sender, command, s, args);
                if (commandTB == null) commandTB = new ArrayList<>();
                StringUtil.copyPartialMatches(args[1], commandTB, results);
            }
            return results;
        }
        return null;
    }
}
