package me.gong.eventsystem.events.impl;

import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.events.EventManager;
import me.gong.eventsystem.events.task.meta.Task;
import me.gong.eventsystem.config.meta.Configurable;
import me.gong.eventsystem.events.task.meta.Logic;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BasicEvent extends Event {

    @Configurable(name = "Spawn Location", description = "Location where players will spawn", id = "spawn-loc")
    private Location spawnLocation;

    @Configurable(name = "that other thing", description = "Other thing", id = "other-thing")
    private Location otherThing;

    @Configurable(name = "Woah woah", description = "its a woah", id = "woah")
    public Location woah1;

    @Logic("spawn-loc")
    public BasicLogic spawnLocLogic = new BasicLogic();

    private Map<UUID, Location> originalLocations = new HashMap<>();

    @Override
    public void onBegin() {
        spawnLocation.add(0.5, 1, 0.5);
        //spawn location atuomatically set
    }

    @Override
    public void onEnd(EventManager.ActionCause cause) {
        //ending logic
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

    private class BasicLogic implements Task.Logic<Location> {

        @Override
        public boolean check(Location location, Player player) {
            return true;
        }
    }
}
