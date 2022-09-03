package io.github.thatsmusic99.athena.util;

import io.github.thatsmusic99.athena.AthenaCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class AthenaExecutor implements EventExecutor {

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
        if (!event.getClass().getSimpleName().equals(name)) return;
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
            HashMap<String, RemappingUtil.Change> differences = new HashMap<>();
            for (String key : details.keySet()) {

                RemappingUtil.Change change = new RemappingUtil.Change(details.get(key), newDetails.get(key));
                if (details.get(key) == null ^ newDetails.get(key) == null) {
                    differences.put(key, change);
                    continue;
                }

                if (change.getOldObject().equals(change.getNewObject())) continue;
                differences.put(key, new RemappingUtil.Change(details.get(key), newDetails.get(key)));
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

    public String getName() {
        return name;
    }

    void remapExecutor() throws IllegalAccessException {
        executorField.set(listener, this);
    }

    private void unmapExecutor() throws IllegalAccessException {
        executorField.set(listener, executor);
        HashSet<AthenaExecutor> executors = RemappingUtil.get().getRegisteredEvents().get(name);
        executors.remove(this);
        RemappingUtil.get().getRegisteredEvents().put(name, executors);
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

    private void dumpData(long completionTime, HashMap<String, RemappingUtil.Change> differences) {
        TextComponent infoDump;
        TextComponent hoverText = Component.text().build();
        if (!differences.isEmpty()) {
            infoDump = Component.text(listener.getPlugin().getName(), AthenaCore.getInfoColour())
                    .append(Component.text(" made some changes to the event!", AthenaCore.getSuccessColour()));

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
                    TextColor.color(0xA0B1B8), TextDecoration.ITALIC);

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

    protected boolean hasSender(CommandSender sender) {
        return senders.contains(sender);
    }
}
