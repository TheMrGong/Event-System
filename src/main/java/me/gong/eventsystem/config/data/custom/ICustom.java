package me.gong.eventsystem.config.data.custom;

import me.gong.eventsystem.config.meta.CustomHandler;

import java.lang.reflect.Field;

public abstract class ICustom<Type> {

    private Type handler;
    private String id;
    private Class clazz, dataType;

    public ICustom(Field field, Object instance) {
        CustomHandler handler = field.getAnnotation(CustomHandler.class);
        this.id = handler.id();
        this.clazz = handler.clazz();
        this.dataType = field.getType();
        try {
            //noinspection unchecked
            this.handler = (Type) field.get(instance);
        } catch (IllegalAccessException | ClassCastException ex) {

        }
    }

    public Type getType() {
        return handler;
    }

    public String getId() {
        return id;
    }

    public Class getClazz() {
        return clazz;
    }

    public String isComplete() {
        if (handler == null) return "Either field was null or didn't extend " + dataType.getSimpleName();
        if (id.isEmpty() && clazz == null) return "Both id and clazz weren't set";
        return null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + (clazz == null ? "[id: " + id + "]" : "[class: " + clazz.getSimpleName() + "]");
    }
}
