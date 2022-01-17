package io.github.thatsmusic99.athena.util;

import io.github.thatsmusic99.athena.AthenaCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class RemappingUtil {

    private static RemappingUtil instance;
    private final HashMap<CommandSender, HashSet<AthenaExecutor>> listeningUsers;
    private final HashMap<String, HashSet<AthenaExecutor>> registeredEvents;

    public RemappingUtil() {
        instance = this;
        listeningUsers = new HashMap<>();
        registeredEvents = new HashMap<>();
    }

    public static RemappingUtil get() {
        return instance;
    }

    public void remapEvent(Class<? extends Event> clazz, CommandSender sender) {
        HashSet<AthenaExecutor> listeners = listeningUsers.getOrDefault(sender, new HashSet<>());
        if (registeredEvents.containsKey(clazz.getSimpleName())) {
            for (AthenaExecutor executor : registeredEvents.get(clazz.getSimpleName())) {
                executor.addSender(sender);
                listeners.add(executor);
            }
            listeningUsers.put(sender, listeners);
            AthenaCore.sendSuccessMessage(sender,
                    "Successfully started listening to event " + clazz.getSimpleName() + "!");
            return;
        }


        HandlerList handlerList;
        try {
            // why is it not getHandlers? bukket explane!!!
            handlerList = EventUtilities.getHandlers(clazz);
        } catch (NoSuchMethodException e) {
            AthenaCore.sendFailMessage(sender, "Event class " + clazz.getSimpleName() + " does not have the " +
                    "getHandlerList method, nag the hell out of the plugin author about this!");
            return;
        } catch (InvocationTargetException e) {
            AthenaCore.sendFailMessage(sender, "Couldn't access getHandlerList for " + clazz.getSimpleName() + " due " +
                    "to an internal error!");
            e.printStackTrace();
            return;
        } catch (IllegalAccessException e) {
            AthenaCore.sendFailMessage(sender, "Couldn't access getHandlerList for " + clazz.getSimpleName() + " due " +
                    "to the lack of access!");
            return;
        }

        if (handlerList == null) {
            AthenaCore.sendFailMessage(sender, "The handler list for " + clazz.getSimpleName() + " is returning null!" +
                    " That's... not how it works?");
            return;
        }

        int currentSize = listeners.size();
        HashSet<AthenaExecutor> eventExecutors = new HashSet<>();
        for (RegisteredListener listener : handlerList.getRegisteredListeners()) {
            try {
                AthenaExecutor executor = new AthenaExecutor(sender, listener, clazz.getSimpleName());
                executor.remapExecutor();
                listeners.add(executor);
                eventExecutors.add(executor);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                AthenaCore.sendFailMessage(sender, "Failed to listen to event " + clazz.getSimpleName() + ", please " +
                        "report this to the ATHENA developer.");
                e.printStackTrace();
            }
        }
        if (currentSize == listeners.size()) {
            AthenaCore.sendFailMessage(sender, "Event " + clazz.getSimpleName() + " hasn't got any listeners!");
        }

        listeningUsers.put(sender, listeners);
        registeredEvents.put(clazz.getSimpleName(), eventExecutors);
        AthenaCore.sendSuccessMessage(sender, "Successfully started listening to event " + clazz.getSimpleName() + "!");
    }

    public void unmapEvent(CommandSender sender) {
        unmapEvent(sender, "");
    }

    public void unmapEvent(CommandSender sender, String event) {
        HashSet<AthenaExecutor> executors = listeningUsers.getOrDefault(sender, new HashSet<>());
        if (executors.isEmpty()) {
            AthenaCore.sendSuccessMessage(sender, "You aren't listening to any events!");
            return;
        }
        executors.forEach(executor -> {
            try {
                if (!executor.name.equals(event) && !event.isEmpty()) return;
                executor.removeSender(sender);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        // Check if player still has events that they are listening to
        if (event.isEmpty()
                || (executors.size() == 1 && executors.iterator().next().name.equals(event))) {
            listeningUsers.remove(sender);
        }

        AthenaCore.sendSuccessMessage(sender, event.isEmpty() ? "Successfully stopped listening to all events!"
                : "Successfully stopped listening to all " + event + " events!");
    }

    public HashSet<AthenaExecutor> getRegisteredListeners(CommandSender sender) {
        return listeningUsers.get(sender);
    }

    public static record Change(Object oldObj, Object newObj) {

        public String getNewObject() {
            return newObj == null ? "null" : newObj.toString();
        }

        public String getOldObject() {
            return oldObj == null ? "null" : oldObj.toString();
        }
    }
}
