package me.gong.eventsystem.util;

public interface CancellableCallback<Type> {
    void onComplete(Type type);
    void onCancel();
}
