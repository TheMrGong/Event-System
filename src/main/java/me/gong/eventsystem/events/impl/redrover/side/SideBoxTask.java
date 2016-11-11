package me.gong.eventsystem.events.impl.redrover.side;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.events.task.MultiTask;
import me.gong.eventsystem.events.task.Task;
import me.gong.eventsystem.events.task.TaskManager;
import me.gong.eventsystem.events.task.data.TaskData;
import me.gong.eventsystem.util.CancellableCallback;
import me.gong.eventsystem.util.StringUtils;
import me.gong.eventsystem.util.data.Box;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SideBoxTask extends MultiTask<SideBox> {

    public SideBoxTask(String id, String event, UUID creating, String name, String help, CancellableCallback<SideBox> callback, Logic<SideBox> logic) {
        super(id, event, creating, name, help, callback, logic);
    }

    @Override
    protected List<Task> generateTasks() {
        TaskManager tm = EventSystem.get().getTaskManager();
        return Arrays.asList(generateType(tm, Box.class, "detectionArea", "Area where players will be detected for &b" + name),
                generateType(tm, Box.class, "barrierArea", "Area set to obsidian/air when players are running for &b" + name),
                generateType(tm, Location.class, "spawnLocation", "Area players will spawn at for &b" + name));
    }

    private Task generateType(TaskManager tm, Class<?> type, String id, String help) {
        return tm.getTaskFrameFor(type).createTask(new TaskData(id, event, creating, id, help, new MultiCallback<Box>(), null));
    }

    @Override
    protected SideBox createType(Map<String, Object> completed) {
        return new SideBox((Box) completed.get("detectionArea"), (Box) completed.get("barrierArea"), (Location) completed.get("spawnLocation"));
    }

    @Override
    public void beginTask() {
        Player p = getPlayer();
        p.sendMessage(StringUtils.info("Creating team box. Help: &e" + help));
        progress(null);
    }
}