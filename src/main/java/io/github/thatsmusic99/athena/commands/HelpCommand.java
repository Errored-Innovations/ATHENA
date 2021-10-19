package io.github.thatsmusic99.athena.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class HelpCommand implements IAthenaCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        sender.sendMessage(ChatColor.STRIKETHROUGH+"----------"+ChatColor.RESET+ " ATHENA HELP " +ChatColor.STRIKETHROUGH+"----------");
        boolean canSeeAny = false;
        for (Subcommand subcommand : Subcommand.values()) {
            String stringCommand = subcommand.name().toLowerCase(Locale.ROOT);
            if (!sender.hasPermission("athena.command."+stringCommand)) continue;
            io.github.thatsmusic99.athena.commands.Command commandInfo = subcommand.getExecutor().getClass().getAnnotation(io.github.thatsmusic99.athena.commands.Command.class);
            String prettyCommand = sender instanceof Player ? "/athena "+stringCommand : "athena "+stringCommand;
            sender.sendMessage(prettyCommand+" - "+commandInfo.description());
            canSeeAny = true;
        }
        if (!canSeeAny) {
            sender.sendMessage("You do not have permission to execute any Athena commands.");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
