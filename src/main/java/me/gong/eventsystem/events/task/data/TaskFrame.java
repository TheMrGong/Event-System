package me.gong.eventsystem.events.task.data;

import me.gong.eventsystem.events.task.Task;
import me.gong.eventsystem.util.CancellableCallback;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class TaskFrame {

    private Class<?> creating;
    private Class<? extends Task> taskClass;

    private Constructor<? extends Task> taskConstructor;

    public TaskFrame(Class<? extends Task> taskClass, Class<?> creating) {
        setupValues(taskClass, creating);
    }

    public TaskFrame(Class<? extends Task> taskClass) {
        try {
            creating = (Class<?>) taskClass.getMethod("getCreating").invoke(null);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            throw new RuntimeException("Unable to find 'getCreating' method for task " + taskClass.getSimpleName(), ex);
        }
        setupValues(taskClass, creating);
    }

    private void setupValues(Class<? extends Task> taskClass, Class<?> creating) {
        this.taskClass = taskClass;
        this.creating = creating;

        try {
            taskConstructor = taskClass.getConstructor(String.class, String.class, UUID.class, String.class,
                    CancellableCallback.class, Task.Logic.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Invalid constructor for task " + taskClass.getSimpleName(), e);
        }
    }

    public Class<? extends Task> getTaskClass() {
        return taskClass;
    }

    public Class<?> getCreating() {
        return creating;
    }

    public Task createTask(TaskData data) {
        try {
            return taskConstructor.newInstance(data.getId(), data.getEvent(), data.getCreating(), data.getHelp(), data.getCallback(), data.getLogic());
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Creating task " + taskClass.getSimpleName());
        }
    }
}
