package me.gong.eventsystem.events.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.events.config.build.ConfigDataBuilder;
import me.gong.eventsystem.events.config.data.ConfigData;
import me.gong.eventsystem.events.config.data.ConfigHandler;
import me.gong.eventsystem.events.config.data.stored.EventData;
import me.gong.eventsystem.events.config.data.stored.EventWorldData;
import me.gong.eventsystem.events.config.data.Task;
import me.gong.eventsystem.events.config.data.impl.LocationConfigHandler;
import me.gong.eventsystem.events.config.data.meta.Configurable;
import me.gong.eventsystem.events.config.data.meta.Logic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataManager {

    private List<ConfigHandler> handlers = new ArrayList<>();
    private List<EventData> eventData = new ArrayList<>();

    public DataManager() {
        handlers.add(new LocationConfigHandler());
    }

    public ConfigHandler findConfigHandler(Class clazz) {
        //noinspection unchecked
        return handlers.stream().filter(h -> h.getHandling().isAssignableFrom(clazz)).findFirst().orElse(null);
    }

    //ayy it worked first time i ran it
    public void createValues(Map<String, ConfigData> data, Event instance) {
        Logger logger = EventSystem.getInstance().getLogger();
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
                if (!builder.hasLogic()) builder.setLogic(Task.Logic.DEFAULT);
                System.out.println("DEBUG: Registered configurable for id " + id + " : " + builder);
                data.put(id, builder.build());
            }
        });
    }
    

}
