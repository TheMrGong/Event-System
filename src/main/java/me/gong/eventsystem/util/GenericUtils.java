package me.gong.eventsystem.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GenericUtils {

    public static Class<?> getGenericType(Field f, int index) {
        if(index < 0) return null;

        Type t = f.getGenericType();
        if(t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) t;
            if(pt.getActualTypeArguments().length - 1 >= index) {
                Type ty = pt.getActualTypeArguments()[0];
                if(ty instanceof Class) return (Class<?>) ty;
            }
        }
        return null;
    }

    public static Class<?>[] getAllGenerics(Field f) {
        List<Class<?>> clazzes = new ArrayList<>();
        Type t = f.getGenericType();
        if(t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) t;
                Type ty = pt.getActualTypeArguments()[0];
                if(ty instanceof Class) clazzes.add((Class<?>) ty);
        }
        return clazzes.toArray(new Class<?>[clazzes.size()]);
    }
}
