package me.gong.eventsystem.events;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.config.data.ConfigData;
import me.gong.eventsystem.config.data.event.EventWorldData;
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
        EventSystem.get().getDataManager().createValues(data, this);
        return this;
    }

    public ConfigData getDataFor(String id) {
        return data.get(id);
    }

    public Map<String, ConfigData> getData() {
        return data;
    }

    public void resetValues() {
        data.values().forEach(c -> c.set(this, null));
    }

    public void loadValuesFrom(EventWorldData data) {
        data.getData().forEach((string, o) -> {
            ConfigData d = Event.this.data.get(string);
            if(d != null) d.set(this, o);
        });
    }
    
    protected boolean isEnabled() {
        return getClass().isInstance(EventSystem.get().getEventManager().getCurrentEvent());
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
