package me.gong.eventsystem.config.data.custom.config;

import me.gong.eventsystem.config.data.custom.ICustom;
import me.gong.eventsystem.config.meta.ConfigHandler;

import java.lang.reflect.Field;

public class CustomConfigHandler extends ICustom<ConfigHandler> {

    public CustomConfigHandler(Field field, Object instance) {
        super(field, instance);
    }
}
