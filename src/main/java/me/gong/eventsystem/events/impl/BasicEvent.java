package me.gong.eventsystem.events.impl;

import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.events.EventManager;
import me.gong.eventsystem.events.config.data.Task;
import me.gong.eventsystem.events.config.data.meta.Configurable;
import me.gong.eventsystem.events.config.data.meta.Logic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BasicEvent extends Event {

    @Configurable(name = "Spawn Location", description = "Location where players will spawn", id = "spawn-loc")
    private Location spawnLocation;

    @Logic("spawn-loc")
    public BasicLogic spawnLocLogic = new BasicLogic();

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

    private class BasicLogic implements Task.Logic<Location> {

        @Override
        public boolean check(Location location, Player player) {
            System.out.println("Basic logic worked AYYYYY");
            return true;
        }
    }
}
