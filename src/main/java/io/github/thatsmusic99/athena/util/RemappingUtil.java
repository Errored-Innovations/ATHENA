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

    private static class AthenaExecutor implements EventExecutor {

        private final List<CommandSender> senders;
        private final RegisteredListener listener;
        private final EventExecutor executor;
        private final Field executorField;
        private final String name;

        public AthenaExecutor(CommandSender sender, RegisteredListener listener, String name) throws NoSuchFieldException, IllegalAccessException {
            senders = new ArrayList<>();
            senders.add(sender);
            this.listener = listener;
            this.name = name;
            executorField = listener.getClass().getDeclaredField("executor");
            executorField.setAccessible(true);
            executor = (EventExecutor) executorField.get(listener);
        }

        @Override
        public void execute(@NotNull Listener listener, @NotNull Event event) {
            boolean executed = false;
            try {
                HashMap<String, Object> details = getEventDetails(event);
                long currentMillis = System.nanoTime();
                try {
                    executor.execute(listener, event);
                } catch (Throwable ex) {
                    senders.forEach(sender -> AthenaCore.sendFailMessage(sender,
                            "An error occurred internally within " + this.listener.getPlugin().getName() + ", " +
                                    "please report it to the developer."));
                    ex.printStackTrace();
                    return;
                }
                executed = true;
                long finish = System.nanoTime();
                HashMap<String, Object> newDetails = getEventDetails(event);
                HashMap<String, Change> differences = new HashMap<>();
                for (String key : details.keySet()) {

                    Change change = new Change(details.get(key), newDetails.get(key));
                    if (details.get(key) == null ^ newDetails.get(key) == null) {
                        differences.put(key, change);
                        continue;
                    }

                    if (change.getOldObject().equals(change.getNewObject())) continue;
                    differences.put(key, new Change(details.get(key), newDetails.get(key)));
                }
                dumpData(finish - currentMillis, differences);
            } catch (Throwable ex) {
                senders.forEach(sender -> AthenaCore.sendFailMessage(sender,
                        "An error occurred internally within ATHENA, please report it to the developer."));
                ex.printStackTrace();
                if (!executed) {
                    try {
                        AthenaCore.get().getLogger().info("Executing listener since ATHENA failed before it could.");
                        executor.execute(listener, event);
                    } catch (Throwable ex2) {
                        senders.forEach(sender -> AthenaCore.sendFailMessage(sender,
                                "An error occurred internally within " + this.listener.getPlugin().getName() + ", " +
                                        "please report it to the developer."));
                        ex2.printStackTrace();
                    }
                }
            }
        }

        private void remapExecutor() throws IllegalAccessException {
            executorField.set(listener, this);
        }

        private void unmapExecutor() throws IllegalAccessException {
            executorField.set(listener, executor);
            HashSet<AthenaExecutor> executors = RemappingUtil.get().registeredEvents.get(name);
            executors.remove(this);
            RemappingUtil.get().registeredEvents.put(name, executors);
        }

        private HashMap<String, Object> getEventDetails(Event event) throws IllegalAccessException {
            // Create the map
            HashMap<String, Object> map = new HashMap<>();
            // Get all the fields
            Class<?> checkedClass = event.getClass();
            while (checkedClass != null) {
                for (Field field : checkedClass.getDeclaredFields()) {
                    // Don't bother if it's static
                    if (Modifier.isStatic(field.getModifiers())) continue;
                    // Make it accessible
                    field.setAccessible(true);
                    // Get the field
                    map.put(field.getName(), field.get(event));
                }
                checkedClass = checkedClass.getSuperclass();
            }

            return map;
        }

        private void dumpData(long completionTime, HashMap<String, Change> differences) {
            TextComponent infoDump;
            TextComponent hoverText = Component.text().build();
            if (!differences.isEmpty()) {
                infoDump = Component.text(listener.getPlugin().getName() + " made some changes to the event!",
                        AthenaCore.getSuccessColour());

                for (String key : differences.keySet()) {
                    hoverText = hoverText.append(Component.text(key, AthenaCore.getInfoColour()))
                            .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                            .append(Component.text(differences.get(key).getOldObject(), AthenaCore.getSuccessColour()))
                            .append(Component.text(" to ", NamedTextColor.GRAY))
                            .append(Component.text(differences.get(key).getNewObject(), AthenaCore.getSuccessColour()))
                            .append(Component.text("\n"));
                }
            } else {
                infoDump = Component.text(listener.getPlugin().getName() + " didn't make any changes.",
                        AthenaCore.getSuccessColour());

            }
            Class<?> listenerClass = listener.getListener().getClass();
            hoverText = hoverText.append(Component.text("Completion time ", AthenaCore.getInfoColour()))
                    .append(Component.text("» ", NamedTextColor.DARK_GRAY))
                    .append(Component.text((completionTime / 100000) + "ms", AthenaCore.getSuccessColour()))
                    .append(Component.text("\n"))
                    .append(Component.text("Listener Class ", AthenaCore.getInfoColour()))
                    .append(Component.text("» ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(listenerClass.getSimpleName() + " (" + listenerClass.getName() + ")",
                            AthenaCore.getSuccessColour()))
                    .append(Component.text("\n"))
                    .append(Component.text("Event Priority", AthenaCore.getInfoColour()))
                    .append(Component.text("» ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(listener.getPriority().name(), AthenaCore.getSuccessColour()));
            infoDump = infoDump.hoverEvent(hoverText);

            Component result = AthenaCore.getPrefix().append(infoDump);

            senders.forEach(sender -> sender.sendMessage(result));
        }

        protected void addSender(CommandSender sender) {
            senders.add(sender);
        }

        protected void removeSender(CommandSender sender) throws IllegalAccessException {
            senders.remove(sender);
            if (senders.size() == 0) unmapExecutor();
        }
    }

    private record Change(Object oldObj, Object newObj) {

        public String getNewObject() {
            return newObj == null ? "null" : newObj.toString();
        }

        public String getOldObject() {
            return oldObj == null ? "null" : oldObj.toString();
        }
    }
}
