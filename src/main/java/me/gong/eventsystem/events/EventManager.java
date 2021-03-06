package me.gong.eventsystem.events;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.events.impl.BasicEvent;
import me.gong.eventsystem.events.impl.redrover.RedRoverEvent;
import me.gong.eventsystem.events.impl.waterdrop.WaterDropEvent;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class EventManager implements Listener {

    private static final String NO_EVENT_RUNNING = "No event is currently running",
            ALREADY_RUNNING = "An event is already running.", ISNT_PARTICIPATING = "You aren't participating in any event",
            ALREADY_PARTICIPATING = "You are already within an event.";

    private Event currentEvent;
    private Set<UUID> participating = new HashSet<>();
    private Set<Event> events = new HashSet<>();

    public void initialize() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if(currentEvent != null) currentEvent.gameTick();
            }
        }.runTaskTimer(EventSystem.get(), 2, 1);
        registerEvent(new BasicEvent());
        registerEvent(new RedRoverEvent());
        registerEvent(new WaterDropEvent());
        Bukkit.getPluginManager().registerEvents(this, EventSystem.get());
    }

    public boolean isEventRunning() {
        return currentEvent != null;
    }

    public boolean isEventRunning(Event event) {
        return isEventRunning() && currentEvent.getClass().isAssignableFrom(event.getClass());
    }

    public void registerEvent(Event... toRegister) {
        Arrays.stream(toRegister).forEach(e -> events.add(e.registerConfigurables()));
    }

    public String beginEvent(Event event, World world, CommandSender hoster) {
        if (isEventRunning()) return ALREADY_RUNNING;

        String errorLoading = EventSystem.get().getDataManager().loadDataFor(event, world.getName());
        if(errorLoading != null) return errorLoading;
        currentEvent = event;

        
        Bukkit.getPluginManager().registerEvents(currentEvent, EventSystem.get());
        currentEvent.onBegin(hoster);
        
        Bukkit.broadcastMessage(StringUtils.format("&e&l" + hoster.getName() + " has hosted an event! (&6" + event.getEventId() + "&e&l)"));
        Bukkit.broadcastMessage(StringUtils.format("&e&lType &a/join&e&l to join in on the fun!"));
        if (hoster instanceof Player) joinEvent((Player) hoster, ActionCause.PLUGIN);
        
        return null;
    }

    public String endCurrentEvent(ActionCause cause) {
        if (!isEventRunning()) return NO_EVENT_RUNNING;

        getCurrentlyPlaying().forEach(p -> currentEvent.quitEvent(p, ActionCause.PLUGIN));

        currentEvent.onEnd(cause);
        currentEvent.resetValues();

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
        resetPlayer(p);
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

    public void resetPlayer(Player p) {
        p.setHealth(20);
        p.setSaturation(20);
        p.setFoodLevel(20);
        p.setFireTicks(0);
        p.getInventory().clear();
        p.getInventory().setBoots(null);
        p.getInventory().setLeggings(null);
        p.getInventory().setChestplate(null);
        p.getInventory().setHelmet(null);
        p.updateInventory();
        p.getActivePotionEffects().forEach(e -> p.removePotionEffect(e.getType()));
    }

    public Event getCurrentEvent() {
        return currentEvent;
    }

    public Collection<Event> getAvailableEvents() {
        return events;
    }

    @EventHandler
    public void onJoin(PlayerQuitEvent ev) {
        quitEvent(ev.getPlayer(), ActionCause.MANUAL);
    }
    
    public enum ActionCause {
        MANUAL, PLUGIN
    }

}
