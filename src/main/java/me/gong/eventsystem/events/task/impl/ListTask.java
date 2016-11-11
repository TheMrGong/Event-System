package me.gong.eventsystem.events.task.impl;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.events.task.Task;
import me.gong.eventsystem.events.task.TaskManager;
import me.gong.eventsystem.events.task.data.ProgressingTask;
import me.gong.eventsystem.events.task.data.TaskFrame;
import me.gong.eventsystem.util.CancellableCallback;
import me.gong.eventsystem.util.GenericUtils;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ListTask extends Task<List> {

    private Class<?> listType;
    private List<Object> returning;

    private TaskFrame frame;
    private TaskFinishHandler finishHandler;

    private Task currentTask;

    public ListTask(String id, String event, UUID creating, String help, CancellableCallback<List> callback, Logic<List> logic) {
        super(id, event, creating, help, callback, logic);
        listType = GenericUtils.getGenericType(getConfigData().getField(), 0);
        returning = new ArrayList<>();
        frame = EventSystem.get().getTaskManager().getTaskFrameFor(listType);
        finishHandler = new TaskFinishHandler();
    }

    @Override
    public void beginTask() {
        Player p = getPlayer();
        p.sendMessage(StringUtils.info("Run &a/finish&7 when you are complete with all the tasks."));
        setTask();
    }

    @Override
    public void endTask() {
        Player p = getPlayer();
        if(p != null) p.sendMessage(StringUtils.info("List task ended."));
        resetCurrentTask();
    }

    private Task createNewTask() {
        return frame.createTask(getConfigData().generateData(id, EventSystem.get().getEventManager().getEventForId(event), creating, finishHandler));
    }

    private void setTask() {
        resetCurrentTask();
        currentTask = createNewTask();
        Bukkit.getPluginManager().registerEvents(currentTask, EventSystem.get());
        currentTask.beginTask();
    }

    private void resetCurrentTask() {
        if(currentTask != null) {
            currentTask.endTask();
            HandlerList.unregisterAll(currentTask);
            currentTask = null;
        }
    }

    public static Class<?> getCreating() {
        return List.class;
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent ev) {
        if(ev.getMessage().toLowerCase().startsWith("/finish") && isCreating(ev.getPlayer())) {
            callback.onComplete(returning);
            ev.setCancelled(true);
        }
    }

    private class TaskFinishHandler implements CancellableCallback<Object> {

        @Override
        public void onComplete(Object o) {
            getPlayer().sendMessage(StringUtils.info(listType.getSimpleName()+" created."));
            returning.add(o);
            setTask();
        }

        @Override
        public void onCancel() {
            getPlayer().sendMessage(StringUtils.warn("Unable to create "+listType.getSimpleName()));
            setTask();
        }
    }
}
