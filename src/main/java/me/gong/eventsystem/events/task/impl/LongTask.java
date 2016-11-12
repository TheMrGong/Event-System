package me.gong.eventsystem.events.task.impl;

import me.gong.eventsystem.events.task.Task;
import me.gong.eventsystem.util.CancellableCallback;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class LongTask extends Task<Long> {

    public LongTask(String id, String event, UUID creating, String name, String help, CancellableCallback<Long> callback, Logic<Long> logic) {
        super(id, event, creating, name, help, callback, logic);
    }

    @Override
    public void beginTask() {

        getPlayer().sendMessage(StringUtils.info("Type a number for &b'" + name + "'&7. Help: &e" + help));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent ev) {
        Player p = ev.getPlayer();
        if(isCreating(p)) {
            ev.setCancelled(true);
            try {
                Long l = Long.parseLong(ev.getMessage());
                if(checkWithLogic(l, p)) callback.onComplete(l);
            } catch (NumberFormatException ex) {
                p.sendMessage(StringUtils.info("'" + ev.getMessage()+"' is not a number. "));
            }
        }
    }

    public static Class<?> getCreating() {
        return Long.class;
    }
}
