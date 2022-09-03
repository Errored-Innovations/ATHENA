package io.github.thatsmusic99.athena.util;

import io.github.thatsmusic99.athena.AthenaCore;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;

public class RemappingUtil {

    private static RemappingUtil instance;
    // A HashMap containing command senders listening to a set of events.
    private final HashMap<CommandSender, HashSet<AthenaExecutor>> listeningUsers;
    private final HashMap<String, HashSet<AthenaExecutor>> registeredEvents;
    private final HashMap<String, AthenaListener> listeners;

    public RemappingUtil() {
        instance = this;
        listeningUsers = new HashMap<>();
        registeredEvents = new HashMap<>();
        listeners = new HashMap<>();
    }

    public static RemappingUtil get() {
        return instance;
    }

    public void remapEvent(Class<? extends Event> clazz, CommandSender sender) {
        HashSet<AthenaExecutor> listeners = listeningUsers.getOrDefault(sender, new HashSet<>());
        if (registeredEvents.containsKey(clazz.getSimpleName()) && !registeredEvents.get(clazz.getSimpleName()).isEmpty()) {
            for (AthenaExecutor executor : registeredEvents.get(clazz.getSimpleName())) {
                // If the player is already listening, tell them to shove off
                if (executor.hasSender(sender)) {
                    AthenaCore.sendFailMessage(sender, "You're already listening to event " + clazz.getSimpleName() + "!");
                    return;
                }
                executor.addSender(sender);
                listeners.add(executor);
            }
            listeningUsers.put(sender, listeners);
            this.listeners.get(clazz.getSimpleName()).addSender(sender);
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
        AthenaListener listener = new AthenaListener();
        listener.addSender(sender);
        Bukkit.getPluginManager().registerEvent(clazz, listener, EventPriority.LOWEST, ((listener1, event) -> {
            if (!(listener1 instanceof AthenaListener)) return;
            if (event.getClass() != clazz) return;
            ((AthenaListener) listener1).onEvent(event);
        }), AthenaCore.get());
        this.listeners.put(clazz.getSimpleName(), listener);
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
                if (!executor.getName().equals(event) && !event.isEmpty()) return;
                listeners.get(executor.getName()).removeSender(sender);
                executor.removeSender(sender);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        // Check if player still has events that they are listening to
        if (event.isEmpty()
                || (executors.size() == 1 && executors.iterator().next().getName().equals(event))) {
            listeningUsers.remove(sender);
        }

        AthenaCore.sendSuccessMessage(sender, event.isEmpty() ? "Successfully stopped listening to all events!"
                : "Successfully stopped listening to all " + event + " events!");
    }

    public HashSet<AthenaExecutor> getRegisteredListeners(CommandSender sender) {
        return listeningUsers.get(sender);
    }

    public HashMap<String, HashSet<AthenaExecutor>> getRegisteredEvents() {
        return registeredEvents;
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
