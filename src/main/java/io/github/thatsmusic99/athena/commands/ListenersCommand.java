package io.github.thatsmusic99.athena.commands;

import io.github.thatsmusic99.athena.AthenaCore;
import io.github.thatsmusic99.athena.util.EventCache;
import io.github.thatsmusic99.athena.util.EventUtilities;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@io.github.thatsmusic99.athena.commands.Command(
        name = "listeners",
        permission = "athena.commands.listeners",
        description = "Dumps all the listeners currently listening to an event.",
        usage = "/athena listeners <Event|Plugin>"
)
public class ListenersCommand implements IAthenaCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s,
                             @NotNull String[] args) {
        if (args.length == 1) {
            AthenaCore.sendFailMessage(sender, "You need to specify an event or plugin to dump the listeners for!");
            return false;
        }
        String key = args[1];
        Plugin plugin = Bukkit.getPluginManager().getPlugin(key);
        // Looking up a plugin
        if (plugin != null) {
            // TODO - condense into a paged list
            List<RegisteredListener> listeners = HandlerList.getRegisteredListeners(plugin);

            if (listeners.size() > 0) {
                for (RegisteredListener listener : listeners) {
                    AthenaCore.sendSuccessMessage(sender, listener.getListener().getClass().getSimpleName() + " " +
                            "(Priority " + listener.getPriority().name() + ")");
                }
                return true;
            }
        }
        // Looking up an event
        Class<? extends Event> eventClass;
        if (EventCache.get().getEventClass(key) != null) {
            eventClass = EventCache.get().getEventClass(key);
        } else {
            try {
                Class<?> tempClass = Class.forName(args[1]);
                if (!Event.class.isAssignableFrom(tempClass)) {
                    AthenaCore.sendFailMessage(sender, "You need to specify an event to listen to!");
                    return true;
                }
                eventClass = (Class<? extends Event>) tempClass;
                EventCache.get().addEvent(eventClass);
            } catch (ClassNotFoundException e) {
                AthenaCore.sendFailMessage(sender, "There is no such event with this name! Please include the package" +
                        " name as well.");
                return true;
            }
        }

        HandlerList handlerList;
        try {
            handlerList = EventUtilities.getHandlers(eventClass);
        } catch (NoSuchMethodException e) {
            AthenaCore.sendFailMessage(sender, "Event class " + eventClass.getSimpleName() + " does not have the " +
                    "getHandlerList method, nag the hell out of the plugin author about this!");
            return true;
        } catch (InvocationTargetException e) {
            AthenaCore.sendFailMessage(sender, "Couldn't access getHandlerList for " + eventClass.getSimpleName() +
                    " due to an internal error!");
            e.printStackTrace();
            return true;
        } catch (IllegalAccessException e) {
            AthenaCore.sendFailMessage(sender, "Couldn't access getHandlerList for " + eventClass.getSimpleName() +
                    " due to the lack of access!");
            return true;
        }

        if (handlerList.getRegisteredListeners().length > 0) {
            for (RegisteredListener listener : handlerList.getRegisteredListeners()) {
                AthenaCore.sendSuccessMessage(sender,
                        listener.getPlugin().getName() + " - " + listener.getListener().getClass().getSimpleName() +
                                " (Priority " + listener.getPriority().name() + ")");
            }
            return true;
        } else {
            AthenaCore.sendSuccessMessage(sender, "There are no listeners for " + eventClass.getSimpleName());
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String s, @NotNull String[] args) {
        List<String> results = new ArrayList<>();
        if (args.length == 2) {
            List<String> collection = new ArrayList<>(EventCache.get().getEventNames());
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                collection.add(plugin.getName());
            }
            StringUtil.copyPartialMatches(args[1], collection, results);
        }
        return results;
    }
}
