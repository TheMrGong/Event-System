package me.gong.eventsystem.config.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomHandler {

    String id() default "";
    Class<?> clazz() default Class.class;
    Type type();

    enum Type {
        FRAME, CONFIG
    }
}
