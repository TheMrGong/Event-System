package me.gong.eventsystem.events;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.events.impl.BasicEvent;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.*;
import java.util.stream.Collectors;

public class EventManager {

    private static final String NO_EVENT_RUNNING = "No event is currently running",
            ALREADY_RUNNING = "An event is already running.", ISNT_PARTICIPATING = "You aren't participating in any event",
            ALREADY_PARTICIPATING = "You are already within an event.";

    private Event currentEvent;
    private Set<UUID> participating;
    private Set<Event> events;

    public EventManager() {
        events = new HashSet<>();
        participating = new HashSet<>();
        registerEvent(new BasicEvent());
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), EventSystem.getInstance());
    }

    public boolean isEventRunning() {
        return currentEvent != null;
    }

    public void registerEvent(Event... toRegister) {
        Arrays.stream(toRegister).forEach(e -> events.add(e));
    }

    public String beginEvent(Event event, CommandSender hoster) {
        if (isEventRunning()) return ALREADY_RUNNING;
        currentEvent = event;
        
        Bukkit.getPluginManager().registerEvents(currentEvent, EventSystem.getInstance());
        currentEvent.onBegin();
        
        Bukkit.broadcastMessage(StringUtils.format("&e&l" + hoster.getName() + " has hosted an event! (&6" + event.getEventId() + "&e&l)"));
        if (hoster instanceof Player) joinEvent((Player) hoster, ActionCause.PLUGIN);
        
        return null;
    }

    public String endCurrentEvent(ActionCause cause) {
        if (!isEventRunning()) return NO_EVENT_RUNNING;

        getCurrentlyPlaying().forEach(p -> currentEvent.quitEvent(p, ActionCause.PLUGIN));
        currentEvent.onEnd(cause);
        HandlerList.unregisterAll(currentEvent);

        participating.clear();
        currentEvent = null;

        return null;
    }

    public String joinEvent(Player player, ActionCause cause) {
        if (!isEventRunning() || isParticipating(player) || !currentEvent.joinEvent(player, cause))
            return !isEventRunning() ? NO_EVENT_RUNNING : isParticipating(player) ? ALREADY_PARTICIPATING : "";
        participating.add(player.getUniqueId());
        return null;
    }

    public String quitEvent(Player p, ActionCause cause) {
        if (!isEventRunning() || !isParticipating(p))
            return !isEventRunning() ? NO_EVENT_RUNNING : ISNT_PARTICIPATING;
        currentEvent.quitEvent(p, cause);
        participating.remove(p.getUniqueId());
        return null;
    }

    public boolean isParticipating(Player player) {
        return participating.stream().anyMatch(p -> player.getUniqueId().equals(p));
    }

    public Event getEventForId(String id) {
        return events.stream().filter(e -> e.getEventId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public List<Player> getCurrentlyPlaying() {
        return participating.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Event getCurrentEvent() {
        return currentEvent;
    }

    public Collection<Event> getAvailableEvents() {
        return events;
    }
    
    public enum ActionCause {
        MANUAL, PLUGIN
    }

}
