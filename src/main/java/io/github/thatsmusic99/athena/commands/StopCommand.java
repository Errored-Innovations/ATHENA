package io.github.thatsmusic99.athena.commands;

import io.github.thatsmusic99.athena.util.RemappingUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@io.github.thatsmusic99.athena.commands.Command(
        name = "stop",
        permission = "athena.command.stop",
        description = "Stops listening to events.",
        usage = "/athena stop"
)
public class StopCommand implements IAthenaCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (args.length > 1) {
            RemappingUtil.get().unmapEvent(sender, args[1]);
            return true;
        }
        RemappingUtil.get().unmapEvent(sender);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return null;
    }
}
