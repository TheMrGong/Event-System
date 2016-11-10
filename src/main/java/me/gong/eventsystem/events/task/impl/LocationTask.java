package me.gong.eventsystem.events.task.impl;

import me.gong.eventsystem.events.task.meta.Task;
import me.gong.eventsystem.util.CancellableCallback;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class LocationTask extends Task<Location> {

    public LocationTask(String id, UUID creating, String help, CancellableCallback<Location> callback, Logic<Location> logic) {
        super(id, creating, help, callback, logic);
    }

    @Override
    public void beginTask() {
        getPlayer().sendMessage(StringUtils.info("Right click a block to set a location. Help: " + help));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if(ev.getAction() == Action.RIGHT_CLICK_BLOCK && isCreating(ev.getPlayer())) {
            ev.setCancelled(true);
            Location l = ev.getClickedBlock().getLocation();
            if(logic.check(l, ev.getPlayer())) callback.onComplete(l);
        }
    }

    public static Class<?> getCreating() {
        return Location.class;
    }
}
