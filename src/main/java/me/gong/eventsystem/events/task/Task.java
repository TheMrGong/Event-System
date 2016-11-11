package me.gong.eventsystem.events.task;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.config.data.ConfigData;
import me.gong.eventsystem.util.CancellableCallback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.UUID;

public abstract class Task<Type> implements Listener {

    protected CancellableCallback<Type> callback;
    private Logic<Type> logic;
    protected UUID creating;
    protected String id, event;
    protected String name, help;
    protected ConfigData configData;

    public Task(String id, String event, UUID creating, String name, String help, CancellableCallback<Type> callback, Logic<Type> logic) {
        this.id = id;
        this.name = name;
        this.event = event;
        this.creating = creating;
        this.help = help;
        this.callback = callback;
        this.logic = logic;
        this.configData = EventSystem.get().getEventManager().getEventForId(event).getDataFor(id);
    }

    public void beginTask() {

    }

    public void endTask() {

    }

    public final String getId() {
        return id;
    }

    public final Player getPlayer() {
        return Bukkit.getPlayer(creating);
    }

    protected final boolean isCreating(Player player) {
        return player.getUniqueId().equals(creating);
    }

    protected boolean checkWithLogic(Type type, Player player) {
        return logic == null || logic.check(type, player);
    }

    protected ConfigData getConfigData() {
        return configData;
    }

    public void setConfigData(ConfigData data) {
        this.configData = data;
    }

    public final String toString(Class<?> creating) {
        return (creating != null ? creating.getSimpleName() + " " : "") + id + " - &e" + help + "&7";
    }

    public interface Logic<Type> {

        Logic<?> DEFAULT = (o, player) -> true;

        boolean check(Type type, Player player);
    }
}
