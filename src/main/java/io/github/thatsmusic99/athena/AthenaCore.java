package io.github.thatsmusic99.athena;

import io.github.thatsmusic99.athena.commands.AthenaCommand;
import io.github.thatsmusic99.athena.util.EventCache;
import io.github.thatsmusic99.athena.util.RemappingUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class AthenaCore extends JavaPlugin {

    private static AthenaCore instance;

    @Override
    public void onEnable() {
        instance = this;
        getCommand("athena").setExecutor(new AthenaCommand());
        new Metrics(this, 12408);
        new RemappingUtil();
        Bukkit.getScheduler().runTaskAsynchronously(this, EventCache::new);
    }

    public static AthenaCore get() {
        return instance;
    }

    public static Component getPrefix() {
        return Component.text("ATHENA ", TextColor.color(0x9ED0FF))
                .append(Component.text("» ", NamedTextColor.DARK_GRAY));
    }

    public static TextColor getInfoColour() {
        return TextColor.color(0x9ED0FF);
    }

    public static TextColor getSuccessColour() {
        return TextColor.color(0xD1E9FF);
    }

    public static TextColor getFailColour() {
        return TextColor.color(0xFCC0B3);
    }

    public static void sendSuccessMessage(CommandSender sender, String message) {
        sender.sendMessage(AthenaCore.getPrefix().append(Component.text(message, AthenaCore.getSuccessColour())));
    }

    public static void sendFailMessage(CommandSender sender, String message) {
        sender.sendMessage(AthenaCore.getPrefix().append(Component.text(message, AthenaCore.getFailColour())));
    }
}
