package me.gong.eventsystem;

import me.gong.eventsystem.events.EventManager;
import me.gong.eventsystem.config.DataManager;
import me.gong.eventsystem.events.task.TaskManager;
import me.gong.eventsystem.server_stuff.ServerManager;
import me.gong.eventsystem.server_stuff.cmd.*;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EventSystem extends JavaPlugin {

    private static EventSystem instance;

    public EventSystem() {
        instance = this;
    }

    private EventManager eventManager;
    private DataManager dataManager;
    private TaskManager taskManager;

    @Override
    public void onEnable() {
        //demo
        new ServerManager(this);

        taskManager = new TaskManager();

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
        getCommand("setup").setExecutor(new SetupEventCommand());
        getCommand("task").setExecutor(new TaskCommand());

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onCMd(PlayerCommandPreprocessEvent cmd) {
                if(cmd.getMessage().toLowerCase().startsWith("/saveit")) {
                    dataManager.saveData();
                    cmd.getPlayer().sendMessage("yes.");
                }
            }
        }, this);
    }

    @Override
    public void onDisable() {

        dataManager.saveData();

        //note to self, set the instance to null _at the end_
        instance = null;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public static EventSystem get() {
        return instance;
    }
}
