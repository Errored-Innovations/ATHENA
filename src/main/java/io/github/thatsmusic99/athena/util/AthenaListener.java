package io.github.thatsmusic99.athena.util;

import io.github.thatsmusic99.athena.AthenaCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.*;

import java.util.*;

public class AthenaListener implements Listener {

    private final Set<CommandSender> senders;

    public AthenaListener() {
        this.senders = Collections.newSetFromMap(new WeakHashMap<>());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEvent(Event event) {
        // Gather the execution source
        List<String> classLoaders = new ArrayList<>();
        TextComponent hoverText = Component.text().build();
        Exception ex = new Exception("Boo");
        try {
            // >:)
            throw ex;
        } catch (Exception e) {
            for (StackTraceElement element : e.getStackTrace()) {
                hoverText = hoverText.append(Component.text(element.toString()).color(AthenaCore.getSuccessColour())).append(Component.text("\n"));
                if (element.getClassLoaderName() == null) continue;
                if (element.getClassLoaderName().contains("ATHENA")) continue;
                if (classLoaders.contains(element.getClassLoaderName())) continue;
                classLoaders.add(element.getClassLoaderName());
            }
        }
        // Send the notice to each sender
        for (CommandSender sender : senders) {
            if (sender == null) return;

            // "Event has been executed"
            AthenaCore.sendNoticeMessage(sender, Component.text(event.getClass().getSimpleName(), AthenaCore.getNoticeColourDark())
                    .append(Component.text(" has been executed!", AthenaCore.getNoticeColour())));
            // List classloaders found
            if (!classLoaders.isEmpty()) {
                AthenaCore.sendNoticeMessage(sender, Component.text("Classloaders: ", AthenaCore.getNoticeColour())
                        .append(Component.text(String.join(", ", classLoaders), AthenaCore.getNoticeColourDark()).hoverEvent(hoverText)));
            }
        }
    }

    public void addSender(CommandSender sender) {
        senders.add(sender);
    }

    public void removeSender(CommandSender sender) {
        if (!senders.contains(sender)) return;
        senders.remove(sender);
        if (senders.isEmpty()) HandlerList.unregisterAll(this);
    }
}
