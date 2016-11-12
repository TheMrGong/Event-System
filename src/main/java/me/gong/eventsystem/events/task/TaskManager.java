package me.gong.eventsystem.events.task;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.config.data.ConfigData;
import me.gong.eventsystem.events.task.data.TaskFrame;
import me.gong.eventsystem.events.task.impl.BoxTask;
import me.gong.eventsystem.events.task.impl.ListTask;
import me.gong.eventsystem.events.task.impl.LocationTask;
import me.gong.eventsystem.events.task.data.ProgressingTask;
import me.gong.eventsystem.events.task.impl.LongTask;
import me.gong.eventsystem.util.CancellableCallback;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.stream.Collectors;

public class TaskManager implements Listener {

    private static final String NOT_SETTING_UP = "Event isn't currently being setup",
            EVENT_BEING_SETUP = "Event %s is already being setup",
            IN_SETUP = "Player is already setting up an event",
            EVENT_IN_USE = "Event is currently in use";

    private List<TaskFrame> tasks = new ArrayList<>();
    private List<ProgressingTask> inprogress = new ArrayList<>();

    public TaskManager() {
        registerTasks(LocationTask.class, ListTask.class, BoxTask.class, LongTask.class);
        Bukkit.getPluginManager().registerEvents(this, EventSystem.get());
    }

    public TaskFrame getTaskFrameFor(Class<?> creating) {
        return tasks.stream().filter(f -> f.getCreating().isAssignableFrom(creating)).findFirst().orElse(null);
    }

    public TaskFrame getTaskFrameFor(Class<?> creating, Event event) {
        TaskFrame got = event.findCustomFrame(creating);
        return got == null ? getTaskFrameFor(creating) : got;
    }

    public List<Task> generateTaskFor(Player player, CancellableCallback callback, Event event) {
        return event.getData().entrySet().stream().map(dat -> {
            ConfigData d = dat.getValue();
            TaskFrame frame = d.getFrame(dat.getKey(), event);

            if (frame != null) return frame.createTask(d.generateData(dat.getKey(), event, player.getUniqueId(), callback));
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public String beginSetup(Player player, Event event) {
        if (isBuilding(event)) return String.format(EVENT_BEING_SETUP, event);
        if(getSettingUp(player) != null) return IN_SETUP;
        if(EventSystem.get().getEventManager().isEventRunning(event)) return EVENT_IN_USE;

        final String world = player.getWorld().getName();
        inprogress.add(new ProgressingTask(player, EventSystem.get().getDataManager().getCurrentWorldData(event, world),
                event, new CancellableCallback<Map<String, Object>>() {
            @Override
            public void onComplete(Map<String, Object> data) {
                player.sendMessage(StringUtils.info("Finished event setup for '" + event.getEventId() + "'"));
                resetTask(getSettingUp(player));
                EventSystem.get().getDataManager().createWorldDataFor(event, world, data);
            }

            @Override
            public void onCancel() {
                resetTask(getSettingUp(player));
                if(player.isOnline()) player.sendMessage(StringUtils.warn("Unable to finish task setup."));
            }
        }));
        return null;
    }

    public String finishTask(Player player) {
        ProgressingTask t = getSettingUp(player);
        if(t == null) return NOT_SETTING_UP;
        t.finish();
        return null;
    }

    public String cancelTask(Player player) {
        ProgressingTask t = getSettingUp(player);
        if(t == null) return NOT_SETTING_UP;
        t.cancel();
        return null;
    }

    public String changeTaskId(Player player, String id) {
        ProgressingTask t = getSettingUp(player);
        if(t == null) return NOT_SETTING_UP;
        t.beginTask(id);
        return null;
    }

    public String listTasks(Player player) {
        ProgressingTask t = getSettingUp(player);
        if(t == null) return NOT_SETTING_UP;
        t.listAllTasks();
        return null;
    }

    private void resetTask(ProgressingTask task) {
        if(task != null) {
            task.unsetTask();
            inprogress.remove(task);
        }
    }

    private ProgressingTask getSettingUp(Player player) {
        return inprogress.stream().filter(p -> p.getUUID().equals(player.getUniqueId())).findFirst().orElse(null);
    }

    private boolean isBuilding(Event event) {
        return inprogress.stream().anyMatch(p -> p.getEventId().equalsIgnoreCase(event.getEventId()));
    }

    @SafeVarargs
    private final void registerTasks(Class<? extends Task>... tasks) {
        this.tasks.addAll(Arrays.stream(tasks).map(TaskFrame::new).collect(Collectors.toList()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        resetTask(getSettingUp(ev.getPlayer()));
    }
}
