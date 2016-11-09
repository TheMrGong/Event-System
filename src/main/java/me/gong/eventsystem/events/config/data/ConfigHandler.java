package me.gong.eventsystem.events.config.data;

import com.google.gson.JsonObject;

public interface ConfigHandler {

    void save(Object Object, JsonObject element);

    Object load(JsonObject data);

    Class<?> getHandling();


}
