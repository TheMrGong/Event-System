package me.gong.eventsystem.events.config.data.impl;

import com.google.gson.JsonObject;
import me.gong.eventsystem.events.config.data.ConfigHandler;
import me.gong.eventsystem.util.JsonUtils;
import org.bukkit.Location;

public class LocationConfigHandler implements ConfigHandler {
    @Override
    public void save(Object location, JsonObject obj) {
        JsonUtils.locationToElement((Location) location, obj);
    }

    @Override
    public Object load(JsonObject data) {
        return JsonUtils.elementToLocation(data);
    }

    @Override
    public Class<?> getHandling() {
        return Location.class;
    }
}
