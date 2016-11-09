package me.gong.eventsystem.events.config.data.stored;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.events.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public EventWorldData getWorldDataFor(String world) {
        return worldData.stream().filter(wd -> wd.getWorld().equalsIgnoreCase(world)).findFirst().orElse(null);
    }

    public boolean isEmpty() {
        return worldData.isEmpty();
    }

    public JsonObject save() {
        JsonObject ret = new JsonObject();
        ret.addProperty("event", event);

        JsonArray data = new JsonArray();
        worldData.stream().map(EventWorldData::save).filter(Objects::nonNull).forEach(data::add);

        ret.add("data", data);
        return ret;
    }

    public static EventData load(JsonObject obj) {
        String event = obj.get("event").getAsString();
        Event e = EventSystem.get().getEventManager().getEventForId(event);

        if(e == null) return null;

        JsonArray data = obj.getAsJsonArray("data");
        List<EventWorldData> dat = new ArrayList<>();

        data.forEach(b -> dat.add(EventWorldData.load(e, b.getAsJsonObject())));
        return new EventData(event, dat);
    }
}
