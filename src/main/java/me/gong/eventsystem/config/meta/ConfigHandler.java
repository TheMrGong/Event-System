package me.gong.eventsystem.config.meta;

import com.google.gson.JsonObject;
import me.gong.eventsystem.events.Event;

public interface ConfigHandler {

    void save(Event evInstance, Object data, JsonObject obj);

    Object load(Event evInstance, JsonObject obj);

    Class<?> getHandling();


}
