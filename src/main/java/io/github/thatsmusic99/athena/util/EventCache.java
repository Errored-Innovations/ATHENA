package io.github.thatsmusic99.athena.util;

import io.github.thatsmusic99.athena.AthenaCore;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

public class EventCache {

    private final HashMap<String, Class<? extends Event>> registeredEvents;
    private static EventCache instance;

    public EventCache() {
        instance = this;
        AthenaCore.get().getLogger().info("Initiated EventCache");
        registeredEvents = new HashMap<>();
        for (HandlerList list : HandlerList.getHandlerLists()) {
            for (RegisteredListener listener : list.getRegisteredListeners()) {
                AthenaCore.get().getLogger().info("Checking registered listener " + listener.getClass().getSimpleName());
                Listener actualListener = listener.getListener();
                for (Method method : actualListener.getClass().getDeclaredMethods()) {
                    if (!method.isAnnotationPresent(EventHandler.class)) continue;
                    for (Class<?> clazz : method.getParameterTypes()) {
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
