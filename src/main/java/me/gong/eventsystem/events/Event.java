package me.gong.eventsystem.events;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.config.data.ConfigData;
import me.gong.eventsystem.config.data.custom.ICustom;
import me.gong.eventsystem.config.data.custom.config.CustomConfigHandler;
import me.gong.eventsystem.config.data.custom.AbstractCustomList;
import me.gong.eventsystem.config.data.custom.config.CustomConfigList;
import me.gong.eventsystem.config.data.custom.frame.CustomFrameList;
import me.gong.eventsystem.config.data.custom.frame.CustomTaskFrame;
import me.gong.eventsystem.config.data.event.EventWorldData;
import me.gong.eventsystem.config.meta.ConfigHandler;
import me.gong.eventsystem.events.task.data.TaskFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Event implements Listener {

    private Map<String, ConfigData> data = new HashMap<>();
    private CustomConfigList customHandlers = new CustomConfigList();
    private CustomFrameList customFrames = new CustomFrameList();

    public boolean joinEvent(Player player, EventManager.ActionCause cause) {
        return true;
    }

    public void quitEvent(Player player, EventManager.ActionCause cause) {

    }

    public abstract void onBegin();

    public abstract void onEnd(EventManager.ActionCause cause);
    
    public abstract String getEventId();

    private Collection<Class> getAllClasses() {
        return data.values().stream().map(ConfigData::getConfigType).collect(Collectors.toList());
    }

    public ConfigHandler findCustomHandler(Object object) {
        return findCustom(object, customHandlers);
    }

    public TaskFrame findCustomFrame(Object object) {
        return findCustom(object, customFrames);
    }

    private <Wrapper extends ICustom<Data>, Data> Data findCustom(Object object, AbstractCustomList<Wrapper, Data> list) {
        if(object instanceof Class) return list.findHandler((Class) object);
        else if(object instanceof String) return list.findHandler((String) object);
        return null;
    }

    public void pruneAll() {
        customHandlers.pruneClasses(getAllClasses());
        customHandlers.pruneIds(data.keySet());
    }

    public Event registerConfigurables() {
        EventSystem.get().getDataManager().createValues(data, this);
        return this;
    }

    public ConfigData getDataFor(String id) {
        return data.get(id);
    }

    public Map<String, ConfigData> getData() {
        return data;
    }

    public boolean addCustomHandler(CustomConfigHandler handler) {
        return customHandlers.addWrapper(handler);
    }

    public boolean addCustomFrame(CustomTaskFrame frame) {
        return customFrames.addWrapper(frame);
    }

    public void resetValues() {
        data.values().forEach(c -> c.set(this, null));
    }

    public void loadValuesFrom(EventWorldData data) {
        data.getData().forEach((string, o) -> {
            ConfigData d = Event.this.data.get(string);
            if(d != null) d.set(this, o);
        });
    }
    
    protected boolean isEnabled() {
        return getClass().isInstance(EventSystem.get().getEventManager().getCurrentEvent());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;
        String eventId = getEventId(), oEventId = event.getEventId();

        return eventId.equals(oEventId);

    }

    @Override
    public int hashCode() {
        return getEventId().hashCode();
    }

    @Override
    public String toString() {
        return getEventId();
    }
}
