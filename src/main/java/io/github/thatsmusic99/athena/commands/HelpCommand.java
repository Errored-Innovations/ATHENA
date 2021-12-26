package io.github.thatsmusic99.athena.commands;

import io.github.thatsmusic99.athena.AthenaCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class HelpCommand implements IAthenaCommand {

    private final Component decoration = Component.text("                      ", TextColor.color(0x4EB4FF), TextDecoration.STRIKETHROUGH);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        sender.sendMessage(decoration.append(Component.text(" ATHENA HELP ", AthenaCore.getSuccessColour()).decoration(TextDecoration.STRIKETHROUGH, false)).append(decoration));
        boolean canSeeAny = false;
        for (Subcommand subcommand : Subcommand.values()) {
            String stringCommand = subcommand.name().toLowerCase(Locale.ROOT);
            if (!sender.hasPermission("athena.command." + stringCommand)) continue;
            io.github.thatsmusic99.athena.commands.Command commandInfo = subcommand.getExecutor().getClass().getAnnotation(io.github.thatsmusic99.athena.commands.Command.class);
            sender.sendMessage(commandEntry(sender, commandInfo));
            canSeeAny = true;
        }
        if (!canSeeAny) {
            AthenaCore.sendFailMessage(sender, "You do not have permission to execute any Athena commands.");
        }
        return true;
    }

    private Component commandEntry(CommandSender sender, io.github.thatsmusic99.athena.commands.Command commandInfo) {
        String prettyCommand = sender instanceof Player ? "/athena " + commandInfo.name() : "athena " + commandInfo.name();
        Component prettyCommandComponent = Component.text(prettyCommand, AthenaCore.getInfoColour());
        Component descriptionComponent = Component.text(" - "+commandInfo.description(), AthenaCore.getSuccessColour());
        return prettyCommandComponent.append(descriptionComponent);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
