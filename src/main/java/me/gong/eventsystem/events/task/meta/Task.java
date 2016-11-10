package me.gong.eventsystem.events.task.meta;

import me.gong.eventsystem.util.CancellableCallback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.UUID;

public abstract class Task<Type> implements Listener {

    protected CancellableCallback<Type> callback;
    protected Logic<Type> logic;
    protected UUID creating;
    private String id;
    protected String help;

    public Task(String id, UUID creating, String help, CancellableCallback<Type> callback, Logic<Type> logic) {
        this.id = id;
        this.creating = creating;
        this.help = help;
        this.callback = callback;
        this.logic = logic;
    }

    public void beginTask() {

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

    public String toString(Class<?> creating) {
        return creating.getSimpleName() + " " + id + " - " + help;
    }

    public interface Logic<Type> {

        Logic<?> DEFAULT = (o, player) -> true;

        boolean check(Type type, Player player);
    }
}
