package me.gong.eventsystem.events.impl.redrover.stage;

import com.google.gson.JsonObject;
import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.config.meta.ConfigHandler;
import me.gong.eventsystem.events.Event;
import org.bukkit.Location;

import java.util.List;

public class StageConfig implements ConfigHandler {

    @Override
    public void save(Event event, Object data, JsonObject obj) {
        Stage s = (Stage) data;
        if(!s.getLocations().isEmpty()) {
            ConfigHandler listHandler = EventSystem.get().getDataManager().findConfigHandler(List.class);
            listHandler.save(event, s.getLocations(), obj);
        }
        obj.addProperty("stageType", s.getType().toString());
    }

    @Override
    public Object load(Event event, JsonObject obj) {

        List<Location> l;
        if(obj.has("list")) {
            //noinspection unchecked
            l = (List<Location>) EventSystem.get().getDataManager().findConfigHandler(List.class).load(event, obj);
        } else l = null;
        return new Stage(l, Stage.StageType.valueOf(obj.get("stageType").getAsString().toUpperCase()));
    }

    @Override
    public Class<?> getHandling() {
        return Stage.class;
    }
}