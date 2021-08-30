package io.github.thatsmusic99.athena.util;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EventUtilities {

    public static HandlerList getHandlers(Class<? extends Event> eventClass) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getHandlerList;
        try {
            getHandlerList = eventClass.getDeclaredMethod("getHandlerList");
        } catch (NoSuchMethodException ex) {
            getHandlerList = eventClass.getMethod("getHandlerList");
        }

        getHandlerList.setAccessible(true);
        return (HandlerList) getHandlerList.invoke(null);
    }
}
