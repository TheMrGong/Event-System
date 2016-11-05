package me.gong.eventsystem.events;

import me.gong.eventsystem.EventSystem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerQuitEvent ev) {
        getManager().quitEvent(ev.getPlayer(), EventManager.ActionCause.PLUGIN);
    }

    private EventManager getManager() {
        return EventSystem.getInstance().getEventManager();
    }
}
