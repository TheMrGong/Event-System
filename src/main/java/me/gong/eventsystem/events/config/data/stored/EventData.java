package me.gong.eventsystem.events.config.data.stored;

import java.util.List;

public class EventData {
    private String event;
    private List<EventWorldData> worldData;

    public EventData(String event, List<EventWorldData> worldData) {
        this.event = event;
        this.worldData = worldData;
    }

    public String getEvent() {
        return event;
    }

    public List<EventWorldData> getWorldData() {
        return worldData;
    }
}
