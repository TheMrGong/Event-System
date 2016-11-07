package me.gong.eventsystem.events.config.data;

import me.gong.eventsystem.events.config.data.meta.Configurable;

import java.lang.reflect.Field;

public class ConfigData {

    private Field field;
    private Class<?> configType;
    private Task.Logic<?> logic;
    private String name, description;

    public ConfigData(Field field, Task.Logic<?> logic, Configurable data, Object instance) {
        this.field = field;

        field.setAccessible(true);

        configType = get(instance).getClass();

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

    public Object get(Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Getting config field " + name, e);
        }
    }

    public Class<?> getConfigType() {
        return configType;
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
