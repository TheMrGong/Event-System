package me.gong.eventsystem.events.task.data.inprog;

import java.util.Map;

public interface ProgressCallback {

    void onComplete(Map<String, Object> data);

    void onFail();
}
