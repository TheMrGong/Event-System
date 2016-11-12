package me.gong.eventsystem.config.data.custom;

import me.gong.eventsystem.EventSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractCustomList<Wrapper extends ICustom<Data>, Data> implements Iterable<Wrapper> {

    private List<Wrapper> handlers;

    public AbstractCustomList(List<Wrapper> handlers) {
        this.handlers = handlers;
    }

    public AbstractCustomList() {
        this(new ArrayList<>());
    }

    public abstract String getTypeName();

    public boolean addWrapper(Wrapper handler) {
        return !(findHandler(handler.getId()) != null || findHandler(handler.getClazz()) != null) && handlers.add(handler);
    }

    public Data findHandler(String id) {
        return findHandler(e -> !e.getId().isEmpty() && id.equalsIgnoreCase(e.getId()));
    }

    public Data findHandler(Class clazz) {
        return findHandler(e -> clazz == e.getClazz());
    }

    public List<Wrapper> getHandlers() {
        return handlers;
    }

    private Data findHandler(Predicate<Wrapper> predicate) {
        return handlers.stream().filter(predicate).map(Wrapper::getType).findFirst().orElse(null);
    }

    @Override
    public Iterator<Wrapper> iterator() {
        return handlers.iterator();
    }
}
