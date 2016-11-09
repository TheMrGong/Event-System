package me.gong.eventsystem;

import me.gong.eventsystem.events.EventManager;
import me.gong.eventsystem.events.config.DataManager;
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
    private DataManager dataManager;

    @Override
    public void onEnable() {
        //demo
        new ServerManager(this);

        //note to self: dont try to be compact by including initializing code in constructor;
        //just caused event manager and datamanager depending on eachother when
        //they weren't ready

        dataManager = new DataManager();
        dataManager.initialize();

        eventManager = new EventManager();
        eventManager.initialize();

        dataManager.loadData(); //^ has to be done in this order


        HostEventCommand he = new HostEventCommand();
        JoinQuitCommand jq = new JoinQuitCommand();

        getCommand("host").setExecutor(he);
        getCommand("endevent").setExecutor(he);
        getCommand("join").setExecutor(jq);
        getCommand("quit").setExecutor(jq);
    }

    @Override
    public void onDisable() {
        instance = null;

        dataManager.saveData();
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public static EventSystem get() {
        return instance;
    }
}
