package me.gong.eventsystem.config.meta;

import com.google.gson.JsonObject;

public interface ConfigHandler {

    void save(Object data, JsonObject obj);

    Object load(JsonObject data);

    Class<?> getHandling();


}
