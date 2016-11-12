package me.gong.eventsystem.config.impl;

import com.google.gson.JsonObject;
import me.gong.eventsystem.config.meta.ConfigHandler;
import me.gong.eventsystem.events.Event;

public class LongConfigHandler implements ConfigHandler {
    @Override
    public void save(Event evInstance, Object data, JsonObject obj) {
        obj.addProperty("long", (Long) data);
    }

    @Override
    public Object load(Event evInstance, JsonObject obj) {
        return obj.has("long") ? obj.get("long").getAsLong() : null;
    }

    @Override
    public Class<?> getHandling() {
        return Number.class;
    }
}
