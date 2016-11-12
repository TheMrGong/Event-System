package me.gong.eventsystem.events.task;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Logics.class)
public @interface Logic {

    String value();
}
