package me.gong.eventsystem.events.config.data;

import com.google.gson.JsonElement;

public interface ConfigHandler {

    JsonElement save(Object Object);

    Object load(JsonElement data);

    Class<?> getHandling();


}
