package me.gong.eventsystem.events.impl.redrover.side;

import com.google.gson.JsonObject;
import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.config.DataManager;
import me.gong.eventsystem.config.meta.ConfigHandler;
import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.util.data.Box;
import org.bukkit.Location;

public class SideConfig implements ConfigHandler {

    @Override
    public void save(Event event, Object data, JsonObject obj) {
        SideBox tb = (SideBox) data;
        DataManager dm = EventSystem.get().getDataManager();
        {
            ConfigHandler h = dm.findConfigHandler(Box.class);
            saveBox(event, h, "detectionArea", tb.getDetectionArea(), obj);
            saveBox(event, h, "barrierArea", tb.getBarrierArea(), obj);
        }
        ConfigHandler h = dm.findConfigHandler(Location.class);
        h.save(event, tb.getSpawnLocation(), obj);
    }

    @Override
    public Object load(Event event, JsonObject obj) {
        DataManager dm = EventSystem.get().getDataManager();
        ConfigHandler boxHandler = dm.findConfigHandler(Box.class);

        ConfigHandler locHandler = dm.findConfigHandler(Location.class);
        return new SideBox(loadBox(event, boxHandler, "detectionArea", obj), loadBox(event, boxHandler, "barrierArea", obj),
                (Location) locHandler.load(event, obj));
    }

    @Override
    public Class<?> getHandling() {
        return SideBox.class;
    }

    private void saveBox(Event event, ConfigHandler handler, String name, Box box, JsonObject object) {
        JsonObject ret = new JsonObject();
        handler.save(event, box, ret);
        object.add(name, ret); //ez
    }

    private Box loadBox(Event event, ConfigHandler handler, String name, JsonObject object) {
        return (Box) handler.load(event, object.getAsJsonObject(name));
    }
}