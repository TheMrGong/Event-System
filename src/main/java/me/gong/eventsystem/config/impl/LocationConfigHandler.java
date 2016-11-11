package me.gong.eventsystem.config.impl;

import com.google.gson.JsonObject;
import me.gong.eventsystem.config.meta.ConfigHandler;
import me.gong.eventsystem.util.JsonUtils;
import org.bukkit.Location;

public class LocationConfigHandler implements ConfigHandler {
    @Override
    public void save(Object data, JsonObject obj) {
        JsonUtils.locationToElement((Location) data, obj);
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
