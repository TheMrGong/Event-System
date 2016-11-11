package me.gong.eventsystem.config;

import me.gong.eventsystem.config.data.ConfigData;
import me.gong.eventsystem.config.meta.Configurable;
import me.gong.eventsystem.events.task.Task;
import me.gong.eventsystem.events.task.data.TaskFrame;

import java.lang.reflect.Field;

public class ConfigDataBuilder {

    private Field field;
    private String name, description;
    private Task.Logic<?> logic;

    public ConfigDataBuilder setField(Field field) {
        this.field = field;
        return this;
    }

    public ConfigDataBuilder setLogic(Task.Logic<?> logic) {
        this.logic = logic;
        return this;
    }

    public ConfigDataBuilder setMeta(Configurable data) {
        return setMeta(data.name(), data.description());
    }

    public ConfigDataBuilder setMeta(String name, String description) {
        this.name = name;
        this.description = description;
        return this;
    }

    public String isComplete() {
        if (field == null || name == null) return "Didn't find original field for logic.";
        return null;
    }

    public boolean hasLogic() {
        return logic != null;
    }

    public ConfigData build() {
        return new ConfigData(field, logic, name, description);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConfigDataBuilder{");
        sb.append("field=").append(field);
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", logic=").append(logic);
        sb.append('}');
        return sb.toString();
    }
}
