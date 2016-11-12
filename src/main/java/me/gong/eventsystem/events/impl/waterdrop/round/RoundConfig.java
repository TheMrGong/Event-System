package me.gong.eventsystem.events.impl.waterdrop.round;

import com.google.gson.JsonObject;
import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.config.meta.ConfigHandler;
import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.events.impl.waterdrop.block.StoredBlock;

import java.util.List;

public class RoundConfig implements ConfigHandler {
    @Override
    public void save(Event evInstance, Object data, JsonObject obj) {
        Round r = (Round) data;
        ConfigHandler ch = EventSystem.get().getDataManager().findConfigHandler(List.class);
        ch.save(evInstance, r.getToSet(), obj);
    }

    @Override
    public Object load(Event evInstance, JsonObject obj) {

        ConfigHandler ch = EventSystem.get().getDataManager().findConfigHandler(List.class);
        List<StoredBlock> lcs = (List<StoredBlock>) ch.load(evInstance, obj);
        return new Round(lcs);
    }

    @Override
    public Class<?> getHandling() {
        return null;
    }
}
