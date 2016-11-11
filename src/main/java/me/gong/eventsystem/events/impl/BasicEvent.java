package me.gong.eventsystem.events.impl;

import com.google.gson.JsonObject;
import me.gong.eventsystem.config.meta.ConfigHandler;
import me.gong.eventsystem.config.meta.Configurable;
import me.gong.eventsystem.config.meta.CustomHandler;
import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.events.EventManager;
import me.gong.eventsystem.events.task.Logic;
import me.gong.eventsystem.events.task.Task;
import me.gong.eventsystem.events.task.data.TaskFrame;
import me.gong.eventsystem.util.CancellableCallback;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BasicEvent extends Event {

    @Configurable(name = "Spawn Location", description = "Location where players will spawn", id = "spawn-loc")
    private Location spawnLocation;

    @Configurable(name = "if this works", description = "i'll be suprised", id = "not-gonna-work") //oh, it worked
    public List<Location> not_gonna_work;

    @Logic("spawn-loc")
    public BasicLogic spawnLocLogic = new BasicLogic();

    @Configurable(name = "MAGIC", description = "woah dynamic", id = "magical")
    public SomeCustomData someCustomData;


    @Configurable(name = "MAGIC", description = "woah dynamic", id = "magical2")
    public SomeCustomData otherCustomData;

    @CustomHandler(clazz = SomeCustomData.class, type = CustomHandler.Type.FRAME)
    public TaskFrame magicalTask = new TaskFrame(MyCustomTask.class, SomeCustomData.class);

    @CustomHandler(clazz = SomeCustomData.class, type = CustomHandler.Type.CONFIG)
    public CustomMagicHandler magicalHandler = new CustomMagicHandler();

    private Map<UUID, Location> originalLocations = new HashMap<>();

    @Override
    public void onBegin() {
        spawnLocation.add(0.5, 1, 0.5);
        //spawn location automatically set
    }

    @Override
    public void onEnd(EventManager.ActionCause cause) {
        //ending logic
        originalLocations.clear();
    }

    @Override
    public String getEventId() {
        return "basic-event";
    }

    @Override
    public boolean joinEvent(Player player, EventManager.ActionCause cause) {
        originalLocations.put(player.getUniqueId(), player.getLocation());
        player.teleport(spawnLocation);
        return true;
    }

    @Override
    public void quitEvent(Player player, EventManager.ActionCause cause) {
        Location l = originalLocations.get(player.getUniqueId());
        if(l != null) {
            player.teleport(l);
            originalLocations.remove(player.getUniqueId());
        }

    }

    private class BasicLogic implements Task.Logic<Location> {

        @Override
        public boolean check(Location location, Player player) {
            return true;
        }
    }

    private static class SomeCustomData {
        private String customMagic;

        public SomeCustomData(String customMagic) {
            this.customMagic = customMagic;
        }

        public String getCustomMagic() {
            return customMagic;
        }
    }

    public static class MyCustomTask extends Task<SomeCustomData> {


        public MyCustomTask(String id, String event, UUID creating, String help, CancellableCallback<SomeCustomData> callback, Logic<SomeCustomData> logic) {
            super(id, event, creating, help, callback, logic);
        }

        @Override
        public void beginTask() {
            getPlayer().sendMessage("say something to finish the task");
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onChat(AsyncPlayerChatEvent ev) {
            callback.onComplete(new SomeCustomData(ev.getMessage()));
            ev.setCancelled(true);
        }
    }

    public static class CustomMagicHandler implements ConfigHandler {

        @Override
        public void save(Object data, JsonObject obj) {
            obj.addProperty("my_magic", ((SomeCustomData) data).getCustomMagic());
        }

        @Override
        public Object load(JsonObject data) {
            return new SomeCustomData(data.get("my_magic").getAsString());
        }

        @Override
        public Class<?> getHandling() {
            return SomeCustomData.class;
        }
    }
}
