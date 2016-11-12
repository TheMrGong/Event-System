package me.gong.eventsystem.events.impl.waterdrop.block;

import com.google.gson.JsonObject;
import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.config.meta.ConfigHandler;
import me.gong.eventsystem.events.Event;
import org.bukkit.Location;
import org.bukkit.Material;

public class StoredBlockConfig implements ConfigHandler {
    @Override
    public void save(Event evInstance, Object data, JsonObject obj) {
        StoredBlock sb = (StoredBlock) data;

        obj.addProperty("type", sb.getType().toString());
        if(sb.getData() != 0) obj.addProperty("data", sb.getData());

        ConfigHandler locHandler = EventSystem.get().getDataManager().findConfigHandler(Location.class);
        locHandler.save(evInstance, sb.getAt(), obj);
    }

    @Override
    public Object load(Event evInstance, JsonObject obj) {
        Material type = Material.valueOf(obj.get("type").getAsString());
        short data = obj.has("data") ? obj.get("data").getAsShort() : 0;

        ConfigHandler locHandler = EventSystem.get().getDataManager().findConfigHandler(Location.class);
        Location l = (Location) locHandler.load(evInstance, obj);
        return new StoredBlock(l, type, data);
    }

    @Override
    public Class<?> getHandling() {
        return StoredBlock.class;
    }
}
