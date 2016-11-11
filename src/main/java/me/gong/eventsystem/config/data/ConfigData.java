package me.gong.eventsystem.config.data;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.config.meta.ConfigHandler;
import me.gong.eventsystem.config.meta.Configurable;
import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.events.task.Task;
import me.gong.eventsystem.events.task.data.TaskData;
import me.gong.eventsystem.events.task.data.TaskFrame;
import me.gong.eventsystem.util.CancellableCallback;

import java.lang.reflect.Field;
import java.util.UUID;

public class ConfigData {

    private Field field;
    private Class<?> configType;

    private Task.Logic<?> logic;

    private String name, description;

    public ConfigData(Field field, Task.Logic<?> logic, Configurable data) {
        this.field = field;

        field.setAccessible(true);

        configType = field.getType();

        this.logic = logic;
        this.name = data.name();
        this.description = data.description();
    }

    public void set(Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Setting config field " + name, e);
        }
    }

    public Field getField() {
        return field;
    }

    public Class<?> getConfigType() {
        return configType;
    }

    public TaskData generateData(String id, Event event, UUID player, CancellableCallback callback) {
        return new TaskData(id, event.getEventId(), player, description, callback, logic);
    }

    public ConfigHandler getHandler(String id, Event event) {
        ConfigHandler handler = event.findCustomHandler(id);
        return handler == null && (handler = event.findCustomHandler(configType)) == null ?
                EventSystem.get().getDataManager().findConfigHandler(configType) : handler;
    }

    public TaskFrame getFrame(String id, Event event) {
        TaskFrame handler = event.findCustomFrame(id);
        return handler == null && (handler = event.findCustomFrame(configType)) == null ?
                EventSystem.get().getTaskManager().getTaskFrameFor(configType) : handler;
    }

    public Task.Logic<?> getLogic() {
        return logic;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }
}
