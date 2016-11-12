package me.gong.eventsystem.events.impl.waterdrop.round;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.config.ConfigDataBuilder;
import me.gong.eventsystem.config.data.ConfigData;
import me.gong.eventsystem.events.impl.waterdrop.block.StoredBlock;
import me.gong.eventsystem.events.task.Task;
import me.gong.eventsystem.events.task.data.TaskData;
import me.gong.eventsystem.util.CancellableCallback;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public class RoundTask extends Task<Round> {

    private Task<List> listTask;

    public RoundTask(String id, String event, UUID creating, String name, String help, CancellableCallback<Round> callback, Logic<Round> logic) {
        super(id, event, creating, name, help, callback, logic);
    }

    @Override
    public void beginTask() {
        listTask = generateListTask();
        Player p = getPlayer();
        p.sendMessage(StringUtils.info("Creating round for &b'" + name + "'&7. Help: &e" + help));
        Bukkit.getPluginManager().registerEvents(listTask, EventSystem.get());
        listTask.beginTask();
    }

    @Override
    public void endTask() {
        HandlerList.unregisterAll(listTask);
        listTask.endTask();
    }

    private Task<List> generateListTask() {
        @SuppressWarnings("unchecked") Task<List> task = EventSystem.get().getTaskManager().getTaskFrameFor(List.class).
                createTask(new TaskData("roundBlocks", event, creating,
                        "Round Blocks", "Blocks that make landing in water more difficult",
                        new CancellableCallback<List<StoredBlock>>() {

                            @Override
                            public void onComplete(List<StoredBlock> list) {
                                Round r = new Round(list);
                                if (checkWithLogic(r, getPlayer())) callback.onComplete(r);
                            }

                            @Override
                            public void onCancel() {
                                Player p = getPlayer();
                                if (p != null) p.sendMessage(StringUtils.warn("Unable to complete list task"));
                                callback.onCancel();
                            }
                        }, null));
        task.setConfigData(generateConfigData());
        return task;
    }

    private ConfigData generateConfigData() {
        try {
            Field f = Round.class.getDeclaredField("toSet");
            return new ConfigDataBuilder().setMeta("Blocks Placed", "Blocks that make landing in water more difficult").setField(f).build();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Unable to find toSet field", e);
        }
    }
}
