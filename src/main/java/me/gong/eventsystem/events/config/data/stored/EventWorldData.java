package me.gong.eventsystem.events.config.data.stored;

import java.util.Map;

public class EventWorldData {
    private String world;
    private Map<String, Object> data;

    public EventWorldData(String world, Map<String, Object> data) {
        this.world = world;
        this.data = data;
    }

    public String getWorld() {
        return world;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
