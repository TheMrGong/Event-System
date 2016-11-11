package me.gong.eventsystem.events.task.data;

import me.gong.eventsystem.events.task.Task;
import me.gong.eventsystem.util.CancellableCallback;

import java.util.UUID;

public class TaskData {

    private UUID creating;
    private CancellableCallback callback;
    private Task.Logic logic;
    private String id, event, name, help;

    public TaskData(String id, String event, UUID creating, String name, String help, CancellableCallback callback, Task.Logic logic) {
        this.id = id;
        this.name = name;
        this.event = event;
        this.creating = creating;
        this.help = help;
        this.callback = callback;
        this.logic = logic;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEvent() {
        return event;
    }

    public UUID getCreating() {
        return creating;
    }

    public CancellableCallback getCallback() {
        return callback;
    }

    public Task.Logic getLogic() {
        return logic;
    }

    public String getHelp() {
        return help;
    }
}
