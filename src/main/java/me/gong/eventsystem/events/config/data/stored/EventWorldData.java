package me.gong.eventsystem.events.config.data.stored;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.util.Pair;
import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.events.config.DataManager;
import me.gong.eventsystem.events.config.data.ConfigData;
import me.gong.eventsystem.events.config.data.ConfigHandler;
import me.gong.eventsystem.util.SimpleEntry;

import java.util.HashMap;
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

    public JsonObject save() {
        JsonObject ret = new JsonObject();
        ret.addProperty("world", world);
        JsonArray jData = new JsonArray();
        data.entrySet().forEach(d -> jData.add(saveEntry(d)));
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
        return new EventWorldData(world, data);
    }

    private JsonObject saveEntry(Map.Entry<String, Object> data) {

        ConfigHandler handler = getHandlerFor(data.getValue());
        if (handler != null) {
            JsonObject ret = new JsonObject();
            ret.addProperty("id", data.getKey());
            handler.save(data.getValue(), ret);
            return ret;
        }

        return null;
    }

    private static Map.Entry<String, Object> loadEntry(Event event, JsonObject obj) {
        String id = obj.get("id").getAsString();
        ConfigData data = event.getDataFor(id);
        if (data == null) return null; //no longer have field in event
        ConfigHandler handler = data.getHandler();
        if (handler == null) return null; //?? somehow handler was removed
        return new SimpleEntry<>(id, handler.load(obj));
    }

    private ConfigHandler getHandlerFor(Object object) {
        DataManager dm = EventSystem.get().getDataManager();
        return dm.findConfigHandler(object.getClass());
    }
}
