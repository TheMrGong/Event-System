package me.gong.eventsystem.config.data.custom.frame;

import me.gong.eventsystem.config.data.custom.ICustom;
import me.gong.eventsystem.events.task.data.TaskFrame;

import java.lang.reflect.Field;

public class CustomTaskFrame extends ICustom<TaskFrame> {

    public CustomTaskFrame(Field field, Object instance) {
        super(field, instance);
    }
}
