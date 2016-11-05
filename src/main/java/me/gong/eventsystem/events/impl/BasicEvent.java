package me.gong.eventsystem.events.impl;

import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.events.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BasicEvent extends Event {

    private Location spawnLocation;
    private Map<UUID, Location> originalLocations = new HashMap<>();

    @Override
    public void onBegin() {
        //begin logic
        spawnLocation = new Location(Bukkit.getWorld("world"), 292.5, 75, 265.5);
    }

    @Override
    public void onEnd(EventManager.ActionCause cause) {
        //ending logic
        spawnLocation = null;
        originalLocations.clear();
    }

    @Override
    public String getEventId() {
        return "basic-event";
    }

    @Override
    public boolean joinEvent(Player player, EventManager.ActionCause cause) {
        originalLocations.put(player.getUniqueId(), player.getLocation());
        player.teleport(spawnLocation);
        return true;
    }

    @Override
    public void quitEvent(Player player, EventManager.ActionCause cause) {
        Location l = originalLocations.get(player.getUniqueId());
        if(l != null) {
            player.teleport(l);
            originalLocations.remove(player.getUniqueId());
        }

    }
}
