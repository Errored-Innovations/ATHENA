package io.github.thatsmusic99.athena.util;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

/**
 * Used to store events that are actively being listened to by the server.
 */
public class EventCache {

    private final HashMap<String, Class<? extends Event>> registeredEvents;
    private static EventCache instance;

    /**
     * Initiates the event cache.
     */
    public EventCache() {
        // Add the instance.
        instance = this;
        // Create the hashmap tracking events being listened to.
        registeredEvents = new HashMap<>();
        // Get the handler lists for listener classes.
        for (HandlerList list : HandlerList.getHandlerLists()) {
            // Get all registered listeners in a given class.
            for (RegisteredListener listener : list.getRegisteredListeners()) {
                Listener actualListener = listener.getListener();
                // Get all the methods in the listener.
                for (Method method : actualListener.getClass().getDeclaredMethods()) {
                    // If they aren't annotated with @EventHandler, ignore them
                    if (!method.isAnnotationPresent(EventHandler.class)) continue;
                    // Get the class of each parameter specified.
                    for (Class<?> clazz : method.getParameterTypes()) {
                        // If it's not an event, ignore it.
                        if (!Event.class.isAssignableFrom(clazz)) continue;
                        registeredEvents.put(clazz.getSimpleName(), (Class<? extends Event>) clazz);
                    }
                }
            }
        }
    }

    public Set<String> getEventNames() {
        return registeredEvents.keySet();
    }

    public Class<? extends Event> getEventClass(String key) {
        return registeredEvents.get(key);
    }

    public void addEvent(Class<? extends Event> event) {
        registeredEvents.put(event.getSimpleName(), event);
    }

    public static EventCache get() {
        return instance;
    }
}
