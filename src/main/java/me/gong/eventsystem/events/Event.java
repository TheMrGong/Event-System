package me.gong.eventsystem.events;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.events.config.data.ConfigData;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public abstract class Event implements Listener {

    private Map<String, ConfigData> data = new HashMap<>();

    public boolean joinEvent(Player player, EventManager.ActionCause cause) {
        return true;
    }

    public void quitEvent(Player player, EventManager.ActionCause cause) {

    }

    public abstract void onBegin();

    public abstract void onEnd(EventManager.ActionCause cause);
    
    public abstract String getEventId();

    public Event registerConfigurables() {
        EventSystem.getInstance().getDataManager().createValues(data, this);
        return this;
    }

    public ConfigData getDataFor(String id) {
        return data.get(id);
    }

    public void resetValues() {
        data.values().forEach(c -> c.set(this, null));
    }

    public void loadValues(World world) {

    }

    public void saveValues(World world) {

    }
    
    protected boolean isEnabled() {
        return getClass().isInstance(EventSystem.getInstance().getEventManager().getCurrentEvent());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;
        String eventId = getEventId(), oEventId = event.getEventId();

        return eventId.equals(oEventId);

    }

    @Override
    public int hashCode() {
        return getEventId().hashCode();
    }

    @Override
    public String toString() {
        return getEventId();
    }
}
