package me.gong.eventsystem.events;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.config.data.ConfigData;
import me.gong.eventsystem.config.data.custom.AbstractCustomList;
import me.gong.eventsystem.config.data.custom.ICustom;
import me.gong.eventsystem.config.data.custom.config.CustomConfigHandler;
import me.gong.eventsystem.config.data.custom.config.CustomConfigList;
import me.gong.eventsystem.config.data.custom.frame.CustomFrameList;
import me.gong.eventsystem.config.data.custom.frame.CustomTaskFrame;
import me.gong.eventsystem.config.data.event.EventWorldData;
import me.gong.eventsystem.config.meta.ConfigHandler;
import me.gong.eventsystem.events.task.data.TaskFrame;
import me.gong.eventsystem.util.BukkitUtils;
import me.gong.eventsystem.util.GenericUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.*;

public abstract class Event implements Listener {

    private Map<String, ConfigData> data = new HashMap<>();
    private CustomConfigList customHandlers = new CustomConfigList();
    private CustomFrameList customFrames = new CustomFrameList();

    public boolean joinEvent(Player player, EventManager.ActionCause cause) {
        return true;
    }

    public void quitEvent(Player player, EventManager.ActionCause cause) {

    }

    public void gameTick() {

    }

    public abstract void onBegin(CommandSender hoster);

    public abstract void onEnd(EventManager.ActionCause cause);
    
    public abstract String getEventId();

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

    public CustomConfigList getCustomHandlers() {
        return customHandlers;
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

    protected List<Player> getPlaying() {
        return EventSystem.get().getEventManager().getCurrentlyPlaying();
    }

    public void broadcast(String msg) {
        getPlaying().forEach(p -> p.sendMessage(msg));
    }

    public void broadcast(BukkitUtils.Title msg) {
        getPlaying().forEach(msg::sendTo);
    }

    public void broadcastAction(String actionMsg) {
        getPlaying().forEach(p -> BukkitUtils.sendActionMessage(p, actionMsg));
    }

    protected void broadcastSound(Sound sound, float volume, float pitch) {
        getPlaying().forEach(p -> p.playSound(p.getLocation(), sound, volume, pitch));
    }

    protected void resetPlayer(Player p) {
        EventSystem.get().getEventManager().resetPlayer(p);
    }

    protected boolean isParticipating(Player player) {
        return getManager().isParticipating(player);
    }

    protected boolean isRunning() {
        return getManager().isEventRunning() && getManager().getCurrentEvent().equals(this);
    }

    protected Location locationKeepingRot(Player player, Location location) {
        Location ret = location.clone(), cur = player.getLocation();
        ret.setYaw(cur.getYaw());
        ret.setPitch(cur.getPitch());
        return ret;
    }

    protected EventManager getManager() {
        return EventSystem.get().getEventManager();
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
