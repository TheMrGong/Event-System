package me.gong.eventsystem;

import me.gong.eventsystem.events.EventManager;
import me.gong.eventsystem.server_stuff.cmd.HostEventCommand;
import me.gong.eventsystem.server_stuff.ServerManager;
import me.gong.eventsystem.server_stuff.cmd.JoinQuitCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class EventSystem extends JavaPlugin {

    private static EventSystem instance;

    public EventSystem() {
        instance = this;
    }

    private EventManager eventManager;

    @Override
    public void onEnable() {
        //demo
        new ServerManager(this);

        eventManager = new EventManager();

        HostEventCommand he = new HostEventCommand();
        JoinQuitCommand jq = new JoinQuitCommand();

        getCommand("host").setExecutor(he);
        getCommand("endevent").setExecutor(he);
        getCommand("join").setExecutor(jq);
        getCommand("quit").setExecutor(jq);
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public static EventSystem getInstance() {
        return instance;
    }
}
