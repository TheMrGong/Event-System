package me.gong.eventsystem.events.impl.redrover.stage;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.config.ConfigDataBuilder;
import me.gong.eventsystem.config.data.ConfigData;
import me.gong.eventsystem.events.task.MultiTask;
import me.gong.eventsystem.events.task.Task;
import me.gong.eventsystem.events.task.TaskManager;
import me.gong.eventsystem.events.task.data.TaskData;
import me.gong.eventsystem.events.task.data.TaskFrame;
import me.gong.eventsystem.util.CancellableCallback;
import me.gong.eventsystem.util.StringUtils;
import me.gong.eventsystem.util.data.Box;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class StageTask extends MultiTask<Stage> {

    public StageTask(String id, String event, UUID creating, String name, String help, CancellableCallback<Stage> callback, Logic<Stage> logic) {
        super(id, event, creating, name, help, callback, logic);
    }

    @Override
    protected List<Task> generateTasks() {
        TaskManager tm = EventSystem.get().getTaskManager();
        return Arrays.asList(generateType(new TaskFrame(StageTypeTask.class, Stage.StageType.class), "stageType", "StageType", "What kind of stage it is"),
                generateType(tm.getTaskFrameFor(List.class), "locs", "Affected Locations", "Locations affected by setting to lava/cobweb", generateConfigData()));
    }

    private ConfigData generateConfigData() {
        try {
            Field f = Stage.class.getDeclaredField("locations");
            return new ConfigDataBuilder().setMeta("Affected Locations", "Locations that will have either cobwebs or lava").setField(f).build();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Unable to find locations field", e);
        }
    }

    private Task generateType(TaskFrame frame, String id, String name, String help, ConfigData... data) {
        Task task = frame.createTask(new TaskData(id, event, creating, name, help, new TaskCallback<>(), null));
        if(data.length > 0) task.setConfigData(data[0]);
        return task;
    }

    @Override
    protected Stage createType(Map<String, Object> completed) {
        return new Stage((List<Location>) completed.get("locs"), (Stage.StageType) completed.get("stageType"));
    }

    @Override
    public void beginTask() {
        Player p = getPlayer();
        p.sendMessage(StringUtils.info("Creating new stage. "));
        progress(null);
    }

    @Override
    protected void progress(Object obj) {
        Task t = getCurrentTask();
        if (t != null) {
            unsetTask();
            if (obj != null) completed.put(t.getId(), obj);
        }

        int index = findNextIncomplete();
        Player pl = getPlayer();
        if (index == -1) {
            if (!isComplete()) {
                List<Task> incompleteTasks = toComplete.stream().filter(ta -> !isTaskComplete(ta)).collect(Collectors.toList());
                pl.sendMessage(StringUtils.info("Reached last incomplete task. " +
                        "The following task" + (incompleteTasks.size() != 1 ? "s" : "") + " aren't setup: "));
                listTasks(incompleteTasks, false, pl);
            } else pl.sendMessage(StringUtils.info("All tasks setup. Run &a/tfinish&7 to save all values."));
        } else {
            taskIndex = index;
            setTask();
        }
    }

    private class TaskCallback<Creating> extends MultiCallback<Creating> {
        @Override
        public void onComplete(Creating type) {
            if(type instanceof Stage.StageType) {
                Stage.StageType t = (Stage.StageType) type;
                if(t != Stage.StageType.ADD_LAVA && t != Stage.StageType.ADD_COBWEBS) {
                    completed.put(getCurrentTask().getId(), type);
                    Stage s = createType(completed);
                    if(checkWithLogic(s, getPlayer())) callback.onComplete(s);
                    return;
                }
            }
            progress(type);
        }
    }
}
