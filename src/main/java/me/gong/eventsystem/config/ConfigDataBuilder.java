package me.gong.eventsystem.config;

import me.gong.eventsystem.config.data.ConfigData;
import me.gong.eventsystem.config.meta.Configurable;
import me.gong.eventsystem.events.task.meta.Task;

import java.lang.reflect.Field;

public class ConfigDataBuilder {

    private Field field;
    private Configurable data;
    private Task.Logic<?> logic;

    public ConfigDataBuilder setData(Field field, Configurable data) {
        this.field = field;
        this.data = data;
        return this;
    }

    public ConfigDataBuilder setLogic(Task.Logic<?> logic) {
        this.logic = logic;
        return this;
    }

    public String isComplete() {
        if(field == null || data == null) return "Didn't find original field for logic.";
        return null;
    }

    public boolean hasLogic() {
        return logic != null;
    }

    public ConfigData build() {
        return new ConfigData(field, logic, data);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConfigDataBuilder{");
        sb.append("field=").append(field);
        sb.append(", data=").append(data);
        sb.append(", logic=").append(logic);
        sb.append('}');
        return sb.toString();
    }
}
