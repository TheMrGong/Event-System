package me.gong.eventsystem.events;

import me.gong.eventsystem.EventSystem;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class Event implements Listener {

    public boolean joinEvent(Player player, EventManager.ActionCause cause) {
        return true;
    }

    public void quitEvent(Player player, EventManager.ActionCause cause) {

    }

    public abstract void onBegin();

    public abstract void onEnd(EventManager.ActionCause cause);
    
    public abstract String getEventId();
    
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
