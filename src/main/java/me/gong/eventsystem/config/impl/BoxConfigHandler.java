package me.gong.eventsystem.config.impl;

import com.google.gson.JsonObject;
import me.gong.eventsystem.config.meta.ConfigHandler;
import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.util.JsonUtils;
import me.gong.eventsystem.util.data.Box;
import org.bukkit.Location;

public class BoxConfigHandler implements ConfigHandler {
    @Override
    public void save(Event event, Object data, JsonObject obj) {
        Box b = (Box) data;
        savePos("pos1", b.getPos1(), obj);
        savePos("pos2", b.getPos2(), obj);
    }

    @Override
    public Object load(Event event, JsonObject obj) {
        return new Box(loadPos("pos1", obj), loadPos("pos2", obj));
    }

    private void savePos(String pos, Location loc, JsonObject obj) {
        obj.add(pos, saveLocation(loc));
    }

    private Location loadPos(String pos, JsonObject obj) {
        return JsonUtils.elementToLocation(obj.getAsJsonObject(pos));
    }

    private JsonObject saveLocation(Location loc) {
        JsonObject ret = new JsonObject();
        JsonUtils.locationToElement(loc, ret);
        return ret;
    }

    @Override
    public Class<?> getHandling() {
        return Box.class;
    }
}
