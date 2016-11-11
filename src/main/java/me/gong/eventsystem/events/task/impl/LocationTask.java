package me.gong.eventsystem.events.task.impl;

import me.gong.eventsystem.events.task.Task;
import me.gong.eventsystem.util.CancellableCallback;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class LocationTask extends Task<Location> {

    public LocationTask(String id, String event, UUID creating, String name, String help, CancellableCallback<Location> callback, Logic<Location> logic) {
        super(id, event, creating, name, help, callback, logic);
    }

    @Override
    public void beginTask() {
        getPlayer().sendMessage(StringUtils.info("Right click a block to set a location for &b'" + name + "'&7. Help: &e" + help));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if (ev.getAction() == Action.RIGHT_CLICK_BLOCK && isCreating(ev.getPlayer())) {
            ev.setCancelled(true);
            Location l = ev.getClickedBlock().getLocation();
            if (checkWithLogic(l, ev.getPlayer())) callback.onComplete(l);
        }
    }

    public static Class<?> getCreating() {
        return Location.class;
    }
}
