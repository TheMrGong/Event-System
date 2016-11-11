package me.gong.eventsystem.config.data.event;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.config.DataManager;
import me.gong.eventsystem.config.data.ConfigData;
import me.gong.eventsystem.config.meta.ConfigHandler;
import me.gong.eventsystem.util.SimpleEntry;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    public boolean isComplete() {
        return !data.isEmpty() && data.values().stream().allMatch(Objects::nonNull);
    }

    public JsonObject save(Event event) {
        JsonObject ret = new JsonObject();
        ret.addProperty("world", world);
        JsonArray jData = new JsonArray();
        data.entrySet().forEach(d -> {
            JsonObject entry = saveEntry(d, event);
            if(entry != null) jData.add(entry);
        });
        ret.add("values", jData);
        return ret;
    }

    public static EventWorldData load(Event event, JsonObject obj) {
        String world = obj.get("world").getAsString();
        JsonArray jData = obj.getAsJsonArray("values");
        Map<String, Object> data = new HashMap<>();
        jData.forEach(e -> {
            Map.Entry<String, Object> entry = loadEntry(event, e.getAsJsonObject());
            if(entry != null) data.put(entry.getKey(), entry.getValue());
        });
        event.getData().keySet().stream().filter(k -> !data.containsKey(k)).forEach(k -> data.put(k, null));
        return new EventWorldData(world, data);
    }

    private JsonObject saveEntry(Map.Entry<String, Object> data, Event event) {

        ConfigHandler handler = getHandlerFor(data, event);
        if (handler != null) {
            JsonObject ret = new JsonObject();
            ret.addProperty("id", data.getKey());
            handler.save(event, data.getValue(), ret);
            return ret;
        }

        return null;
    }

    private static Map.Entry<String, Object> loadEntry(Event event, JsonObject obj) {
        String id = obj.get("id").getAsString();
        ConfigData data = event.getDataFor(id);
        if (data == null) return null; //no longer have field in event
        ConfigHandler handler = data.getHandler(id, event);
        if (handler == null) return null; //?? somehow handler was removed
        return new SimpleEntry<>(id, handler.load(event, obj));
    }

    private ConfigHandler getHandlerFor(Map.Entry<String, Object> entry, Event event) {
        if(entry == null || entry.getValue() == null) return null;
        ConfigHandler handler = event.findCustomHandler(entry.getKey());
        if(handler == null && (handler = event.findCustomHandler(entry.getValue().getClass())) == null) {
            DataManager dm = EventSystem.get().getDataManager();
            return dm.findConfigHandler(entry.getValue().getClass());
        } else return handler;
    }

    public void updateData(Map<String, Object> data) {
        this.data.putAll(data);
    }
}
