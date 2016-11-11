package me.gong.eventsystem.config.data.custom.config;

import me.gong.eventsystem.config.data.custom.AbstractCustomList;
import me.gong.eventsystem.config.meta.ConfigHandler;

public class CustomConfigList extends AbstractCustomList<CustomConfigHandler, ConfigHandler> {
    @Override
    public String getTypeName() {
        return "config handler";
    }
}
