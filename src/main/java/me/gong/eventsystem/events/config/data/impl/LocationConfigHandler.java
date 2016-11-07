package me.gong.eventsystem.events.config.data.impl;

import com.google.gson.JsonElement;
import me.gong.eventsystem.events.config.data.ConfigHandler;
import me.gong.eventsystem.util.JsonUtils;
import org.bukkit.Location;

public class LocationConfigHandler implements ConfigHandler {
    @Override
    public JsonElement save(Object location) {
        return JsonUtils.locationToElement((Location) location);
    }

    @Override
    public Object load(JsonElement data) {
        return JsonUtils.elementToLocation(data);
    }

    @Override
    public Class<?> getHandling() {
        return Location.class;
    }
}
