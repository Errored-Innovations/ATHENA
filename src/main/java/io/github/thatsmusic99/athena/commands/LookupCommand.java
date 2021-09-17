package io.github.thatsmusic99.athena.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.thatsmusic99.athena.AthenaCore;

@io.github.thatsmusic99.athena.commands.Command(
    name = "lookup",
    permission = "athena.command.lookup",
    description = "Looks up the plugin belonging to a given command.",
    usage = "/athena lookup <command>"
    )
  public class LookupCommand implements IAthenaCommand {
  
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
    if (args.length > 1) {
      PluginCommand command = Bukkit.getPluginCommand(args[1]);
      if (command == null) {
        AthenaCore.sendSuccessMessage(sender, "The Command \"" + args[1] + "\" does not exist");
        return true;
      }
      AthenaCore.sendSuccessMessage(sender, "Information on the Command " + args[1] + ":");
      AthenaCore.sendSuccessMessage(sender, "Registering Plugin: " + command.getPlugin().getName());
      AthenaCore.sendSuccessMessage(sender, "Permission in Plugin.yml: " + command.getPermission());
	    return true;
    }
    return false;
  }
  
  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
    return null;
  }

}
