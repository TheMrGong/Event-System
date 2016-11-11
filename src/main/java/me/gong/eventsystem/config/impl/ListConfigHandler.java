package me.gong.eventsystem.config.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.config.meta.ConfigHandler;
import me.gong.eventsystem.events.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListConfigHandler implements ConfigHandler {
    @Override
    public void save(Event event, Object data, JsonObject obj) {
        List<Object> l = (List<Object>) data;
        JsonArray dat = new JsonArray();
        if (l.isEmpty()) obj.add("list", dat);
        else {
            Class<?> containing = l.get(0).getClass();
            ConfigHandler ch = EventSystem.get().getDataManager().findConfigHandler(containing, event);

            if (ch != null) {

                l.forEach(o -> {
                    JsonObject d = new JsonObject();
                    ch.save(event, o, d);
                    dat.add(d);
                });
                obj.add("list", dat);
                obj.addProperty("type", containing.getName());
            }
        }
    }

    @Override
    public Object load(Event event, JsonObject obj) {
        if (!obj.has("list")) return null;

        JsonArray dat = obj.getAsJsonArray("list");
        List<Object> l = new ArrayList<>();

        if (dat.size() == 0) return l;

        Logger log = EventSystem.get().getLogger();
        String type = obj.get("type").getAsString();

        try {
            Class<?> containing = Class.forName(type);
            ConfigHandler ch = EventSystem.get().getDataManager().findConfigHandler(containing, event);
            if (ch == null)
                log.warning("Unable to find config handler for type " + containing.getSimpleName() + " in list");
            else {
                dat.forEach(e -> l.add(ch.load(event, e.getAsJsonObject())));
                return l;
            }
        } catch (ClassNotFoundException ex) {
            log.log(Level.WARNING, "Unable to find type '" + type + "'", ex);
        }
        return null;

    }

    @Override
    public Class<?> getHandling() {
        return List.class;
    }
}
