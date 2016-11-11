package me.gong.eventsystem.config.data.custom.frame;

import me.gong.eventsystem.config.data.custom.AbstractCustomList;
import me.gong.eventsystem.events.task.data.TaskFrame;

public class CustomFrameList extends AbstractCustomList<CustomTaskFrame, TaskFrame> {
    @Override
    public String getTypeName() {
        return "task frame";
    }
}
