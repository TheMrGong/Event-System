package me.gong.eventsystem.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.config.meta.ConfigHandler;
import me.gong.eventsystem.config.meta.Configurable;
import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.config.data.ConfigData;
import me.gong.eventsystem.events.task.meta.Task;
import me.gong.eventsystem.config.impl.LocationConfigHandler;
import me.gong.eventsystem.events.task.meta.Logic;
import me.gong.eventsystem.config.data.event.EventData;
import me.gong.eventsystem.config.data.event.EventWorldData;
import me.gong.eventsystem.util.JsonUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataManager {

    private static final String NO_WORLD_DATA = "No event world data for world '%s' event '%s'",
            DATA_INCOMPLETE = "Data was incomplete for event";

    private List<ConfigHandler> handlers = new ArrayList<>();
    private List<EventData> eventData = new ArrayList<>();
    private File eventDataFile;

    public void initialize() {
        handlers.add(new LocationConfigHandler());
        File dir = EventSystem.get().getDataFolder();
        if (!dir.exists() && !dir.mkdir()) throw new RuntimeException("Unable to create directory");
        eventDataFile = new File(dir, "eventData.json");
        try {
            if (!eventDataFile.exists() && !eventDataFile.createNewFile())
                throw new RuntimeException("Unable to create event data file");
        } catch (IOException e) {
            throw new RuntimeException("Creating event data file", e);
        }
    }

    public ConfigHandler findConfigHandler(Class clazz) {
        //noinspection unchecked
        return handlers.stream().filter(h -> h.getHandling().isAssignableFrom(clazz)).findFirst().orElse(null);
    }

    //ayy it worked first time i ran it
    public void createValues(Map<String, ConfigData> data, Event instance) {
        Logger logger = EventSystem.get().getLogger();
        Map<String, ConfigDataBuilder> builderMap = new HashMap<>();
        for (Field field : instance.getClass().getDeclaredFields()) {

            if (field.isAnnotationPresent(Configurable.class)) {
                Configurable dat = field.getAnnotation(Configurable.class);
                if (builderMap.containsKey(dat.id())) {
                    logger.warning("Found duplicate id '" + dat.id() + "' for event " + instance.getClass().getSimpleName());
                    continue;
                }
                builderMap.put(dat.id(), new ConfigDataBuilder().setData(field, dat));
            } else if (field.isAnnotationPresent(Logic.class)) {
                Logic dat = field.getAnnotation(Logic.class);
                boolean contains = builderMap.containsKey(dat.value());

                if (contains && builderMap.get(dat.value()).hasLogic()) {
                    logger.warning("More than one config logic for id " + dat.value());
                    continue;
                }
                try {
                    field.setAccessible(true);
                    Object logic = field.get(instance);
                    if (!(logic instanceof Task.Logic)) {
                        logger.warning("Logic for id " + dat.value() + " didn't implement Task.Logic, instead was " + logic.getClass().getSimpleName());
                        continue;
                    }
                    ConfigDataBuilder builder = (contains ? builderMap.get(dat.value()) : new ConfigDataBuilder()).setLogic((Task.Logic) logic);
                    if (!contains) builderMap.put(dat.value(), builder);
                } catch (IllegalAccessException e) {
                    logger.log(Level.WARNING, "Error getting logic for id " + dat.value());
                }
            }
        }

        builderMap.entrySet().forEach(e -> {
            String id = e.getKey(), error;
            ConfigDataBuilder builder = e.getValue();
            if ((error = builder.isComplete()) != null) {
                logger.warning("Unable to create config data for id " + id + ": " + error);
            } else {
                if (!builder.hasLogic()) builder.setLogic(Task.Logic.DEFAULT); //take some default logic
                data.put(id, builder.build());
            }
        });
    }

    public EventData getEventDataFor(Event event, boolean createNew) {
        return eventData.stream().filter(e -> e.getEvent().equalsIgnoreCase(event.getEventId()))
                .findFirst().orElseGet(() -> {
                    if (createNew) { //create that new event data
                        EventData d = new EventData(event.getEventId(), new ArrayList<>());
                        eventData.add(d);
                        return d;
                    } else return null;
                });
    }

    public EventWorldData getWorldDataFor(Event event, String world) {
        EventData data = getEventDataFor(event, false);
        if (data == null) return null;
        return data.getWorldDataFor(world);
    }

    public void createWorldDataFor(Event event, String world, Map<String, Object> data) {
        EventData d = getEventDataFor(event, true);
        d.createWorldDataFor(world, data);
    }

    public Map<String, Object> getCurrentWorldData(Event event, String world) {
        EventData d = getEventDataFor(event, true);
        EventWorldData wd = d.getWorldDataFor(world);

        Map<String, Object> map;
        if(wd == null) {
            map = new HashMap<>();
            event.getData().keySet().forEach(k -> map.put(k, null));
            d.createWorldDataFor(world, map);
        } else map = wd.getData();

        return new HashMap<>(map);
    }

    public String loadDataFor(Event event, String world) {
        EventWorldData data = getWorldDataFor(event, world);
        if (data == null) return String.format(NO_WORLD_DATA, world, event);
        if(!data.isComplete()) return DATA_INCOMPLETE;
        event.loadValuesFrom(data);
        return null;
    }

    public void loadData() {
        eventData.clear(); //just in case

        try (BufferedReader r = new BufferedReader(new FileReader(eventDataFile))) {
            String jsonRaw = "", line;

            while ((line = r.readLine()) != null) jsonRaw += line;
            JsonElement e = JsonUtils.parser.parse(jsonRaw);

            if(e instanceof JsonNull) return; //signifies this is a new file/empty one.. or something weird.. meh

            if (!(e instanceof JsonArray))
                throw new RuntimeException("Expected JsonArray from event data, got " + e.getClass().getSimpleName());

            JsonArray json = (JsonArray) e;
            json.forEach(dat -> eventData.add(EventData.load(dat.getAsJsonObject())));
        } catch (IOException e) {
            throw new RuntimeException("Reading event data", e);
        }

    }

    public void saveData() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(eventDataFile))) {
            JsonArray ret = new JsonArray();
            eventData.stream().filter(e -> !e.isEmpty()).forEach(e -> ret.add(e.save()));
            w.write(JsonUtils.gson.toJson(ret));
        } catch (IOException e) {
            throw new RuntimeException("Saving event data", e);
        }
    }


}
