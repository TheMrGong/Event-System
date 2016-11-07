package me.gong.eventsystem.events.config.data;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class Task<Type> implements Listener {

    private Callback<Type> callback;
    private Logic<Type> logic;

    public Task(Callback<Type> callback, Logic<Type> logic) {
        this.callback = callback;
        this.logic = logic;
    }

    public interface Callback<Type> {
        void onComplete(Type type);
        void onCancel();
    }

    public interface Logic<Type> {

        Logic<?> DEFAULT = (o, player) -> true;

        boolean check(Type type, Player player);
    }
}
