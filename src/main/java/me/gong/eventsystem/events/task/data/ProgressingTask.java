package me.gong.eventsystem.events.task.data;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.events.task.Task;
import me.gong.eventsystem.util.CancellableCallback;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProgressingTask {

    private List<Task> toComplete;
    private Map<String, Object> completed;
    private int taskIndex;
    private UUID player;
    private CancellableCallback<Map<String, Object>> callback;
    private String eventId;

    public ProgressingTask(Player player, Map<String, Object> completed, Event event, CancellableCallback<Map<String, Object>> callback) {
        this.toComplete = EventSystem.get().getTaskManager().generateTaskFor(player, new CancellableCallback() {
            @Override
            public void onComplete(Object o) {
                progress(o);
            }

            @Override
            public void onCancel() {
                unsetTask();
            }
        }, event);
        this.completed = completed;
        this.callback = callback;
        this.player = player.getUniqueId();
        this.eventId = event.getEventId();
        this.taskIndex = -1;
        player.sendMessage(StringUtils.info("Beginning setup for event '" + eventId + "'"));
        player.sendMessage(StringUtils.info("The following tasks can be completed: "));
        listAllTasks();
        player.sendMessage("");
        player.sendMessage(StringUtils.info("Run &a/task <id>&7 to do &ea different task&7 or redo a task."));
        player.sendMessage(StringUtils.info("To &esave all progress&7, run &a/setup finish&7."));
        player.sendMessage(StringUtils.info("To &ediscard all changes&7, run &a/setup cancel&7."));
        player.sendMessage("");
        player.sendMessage(StringUtils.info("You can &esee your progress&7 by running &a/task list&7."));
        progress(null);
    }

    private void progress(Object obj) {
        Task t = getCurrentTask();
        if(t != null) {
            unsetTask();
            if(obj != null) completed.put(t.getId(), obj);
        }

        int index = findNextIncomplete();
        Player pl = getPlayer();
        if (index == -1) {
            if (!isComplete()) {
                List<Task> incompleteTasks = toComplete.stream().filter(ta -> !isTaskComplete(ta)).collect(Collectors.toList());
                pl.sendMessage(StringUtils.info("Reached last incomplete task. " +
                        "The following task" + (incompleteTasks.size() != 1 ? "s" : "") + " aren't setup: "));
                toStringTasks(incompleteTasks, false).forEach(pl::sendMessage);
                pl.sendMessage(StringUtils.info("You may run &a/setup finish&7 to save values but the event wont be able to be hosted."));
            } else pl.sendMessage(StringUtils.info("All tasks for event setup. Run &a/setup finish&7 to save all values."));
        } else {
            taskIndex = index;
            setTask();
        }
    }

    private List<String> toStringTasks(List<Task> tasks, boolean appendComplete) {
        return tasks.stream().map(t -> StringUtils.info("- " + t.toString(findCreating(t)) +
                (appendComplete ? " " + (isTaskComplete(t) ? "&a[complete]" : "&c[incomplete]") : ""))).collect(Collectors.toList());
    }

    private boolean isComplete() {
        return toComplete.stream().allMatch(this::isTaskComplete);
    }

    private int findNextIncomplete() {
        for (int i = taskIndex + 1; i < toComplete.size(); i++) {
            Task at = toComplete.get(i);
            if (!isTaskComplete(at)) return i;
        }
        return -1;
    }

    private boolean isTaskComplete(Task task) {
        return completed.containsKey(task.getId()) && completed.get(task.getId()) != null;
    }

    private Class<?> findCreating(Task task) {
        return EventSystem.get().getEventManager().getEventForId(eventId).getDataFor(task.getId()).getConfigType();
    }

    public void unsetTask() {

        Task t = getCurrentTask();
        if (t != null) {
            t.endTask();
            HandlerList.unregisterAll(t);
        }
    }

    private void setTask() {
        Task t = getCurrentTask();
        if (t != null) {
            Bukkit.getPluginManager().registerEvents(t, EventSystem.get());
            t.beginTask();
        }
    }

    private Task getCurrentTask() {
        return taskIndex < 0 || taskIndex >= toComplete.size() ? null : toComplete.get(taskIndex);
    }

    public void listAllTasks() {
        Player p = getPlayer();
        toStringTasks(toComplete, true).forEach(p::sendMessage);
    }

    public void beginTask(String id) {
        Map<Task, Integer> taskMap = new HashMap<>();
        int index = 0;
        for (Task t : toComplete) taskMap.put(t, index++);

        Map.Entry<Task, Integer> task = taskMap.entrySet().stream().filter(b -> b.getKey().getId().equalsIgnoreCase(id)).findFirst().orElse(null);
        Player p = getPlayer();

        if(task == null) p.sendMessage(StringUtils.info("Invalid task id &b'" + id + "'. Run &a/task list&7 to &eview all tasks&7."));
        else {
            Task prev = getCurrentTask();
            unsetTask();
            if(prev != null) p.sendMessage(StringUtils.info("Discarded changes to task &b'" + prev.getId() + "'"));
            taskIndex = task.getValue();
            setTask();
        }
    }

    public void finish() {
        callback.onComplete(completed);
    }

    public void cancel() {
        callback.onCancel();
    }

    public UUID getUUID() {
        return player;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(player);
    }

    public String getEventId() {
        return eventId;
    }
}
