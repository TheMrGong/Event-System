package me.gong.eventsystem.events.task;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.util.CancellableCallback;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class MultiTask<Type> extends Task<Type> {

    protected int taskIndex;
    protected List<Task> toComplete;

    protected Map<String, Object> completed;

    public MultiTask(String id, String event, UUID creating, String name, String help, CancellableCallback<Type> callback, Logic<Type> logic) {
        super(id, event, creating, name, help, callback, logic);
        this.toComplete = generateTasks();
        this.completed = new HashMap<>();
        this.taskIndex = -1;
    }

    protected abstract List<Task> generateTasks();

    protected abstract Type createType(Map<String, Object> completed);

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

    protected List<String> toStringTasks(List<Task> tasks, boolean appendComplete) {
        return tasks.stream().map(t -> StringUtils.info("- " + t.toString(null) +
                (appendComplete ? " " + (isTaskComplete(t) ? "&a[complete]" : "&c[incomplete]") : ""))).collect(Collectors.toList());
    }

    protected boolean isTaskComplete(Task task) {
        return completed.containsKey(task.getId()) && completed.get(task.getId()) != null;
    }

    protected boolean isComplete() {
        return toComplete.stream().allMatch(this::isTaskComplete);
    }

    protected int findNextIncomplete() {
        for (int i = taskIndex + 1; i < toComplete.size(); i++) {
            Task at = toComplete.get(i);
            if (!isTaskComplete(at)) return i;
        }
        return -1;
    }

    protected void unsetTask() {

        Task t = getCurrentTask();
        if (t != null) {
            t.endTask();
            HandlerList.unregisterAll(t);
        }
    }

    protected void setTask() {
        Task t = getCurrentTask();
        if (t != null) {
            Bukkit.getPluginManager().registerEvents(t, EventSystem.get());
            t.beginTask();
        }
    }

    protected Task getCurrentTask() {
        return taskIndex < 0 || taskIndex >= toComplete.size() ? null : toComplete.get(taskIndex);
    }

    private void beginTask(String id) {
        Map<Task, Integer> taskMap = new HashMap<>();
        int index = 0;
        for (Task t : toComplete) taskMap.put(t, index++);

        Map.Entry<Task, Integer> task = taskMap.entrySet().stream().filter(b -> b.getKey().getId().equalsIgnoreCase(id)).findFirst().orElse(null);
        Player p = getPlayer();

        if (task == null)
            p.sendMessage(StringUtils.info("Invalid task id &b'" + id + "'. Run &a/mtask list&7 to &eview all tasks&7."));
        else {
            Task prev = getCurrentTask();
            unsetTask();
            if (prev != null) p.sendMessage(StringUtils.info("Discarded changes to task &b'" + prev.getId() + "'"));
            taskIndex = task.getValue();
            setTask();
        }
    }

    private void showUsage(Player player) {
        player.sendMessage(StringUtils.info("Usage: &a/mtask <list | id>"));
    }

    protected void listTasks(List<Task> tasks, boolean append, Player player) {
        toStringTasks(tasks, append).forEach(player::sendMessage);
        player.sendMessage(StringUtils.info("Run &a/mtask <id>&7 to choose a task to complete"));
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent ev) {
        Player p = ev.getPlayer();

        if (isCreating(p)) {

            String msg = ev.getMessage().toLowerCase();

            if (msg.startsWith("/tfinish")) {
                ev.setCancelled(true);

                if (!isComplete()) {
                    p.sendMessage(StringUtils.warn("All tasks aren't complete: "));
                    toStringTasks(toComplete, true).forEach(s -> p.sendMessage(StringUtils.info(s)));
                    p.sendMessage(StringUtils.info("Run &a/mtask <id>&7 to choose a task to complete"));
                } else {
                    Type created = createType(completed);
                    if (checkWithLogic(created, p)) callback.onComplete(created);
                }

            } else if (msg.startsWith("/mtask")) {
                ev.setCancelled(true);

                String[] dat = msg.split(" ");
                msg = msg.replace(dat[0], "").trim(); //lazy way of getting rid of '/mtask'

                String[] args = msg.split(" ");

                if (args.length == 0) showUsage(p);
                else if (args[0].equalsIgnoreCase("list")) {
                    p.sendMessage(StringUtils.info("The following tasks can be completed"));
                    listTasks(toComplete, true, p);
                } else beginTask(args[0]);
            }
        }
    }

    @Override
    public void endTask() {
        unsetTask();
    }

    public class MultiCallback<Creating> implements CancellableCallback<Creating> {
        @Override
        public void onComplete(Creating type) {
            progress(type);
        }

        @Override
        public void onCancel() {
            Player p = getPlayer();
            if (p != null) p.sendMessage(StringUtils.warn("Unable to finish task."));
            callback.onCancel();
        }
    }
}
