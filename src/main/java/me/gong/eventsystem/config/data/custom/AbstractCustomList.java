package me.gong.eventsystem.config.data.custom;

import me.gong.eventsystem.EventSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

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

    public void pruneIds(Collection<String> ids) {
        pruneUnused(e -> e.getId().isEmpty() || ids.contains(e.getId()));
    }

    public void pruneClasses(Collection<Class> validClasses) {
        pruneUnused(e -> e.getClazz() == null || validClasses.contains(e.getClazz()));
    }

    private void pruneUnused(Predicate<Wrapper> predicate) {
        Iterator<Wrapper> it = handlers.iterator();
        while (it.hasNext()) {
            Wrapper next = it.next();
            if (!predicate.test(next)) {
                it.remove();
                EventSystem.get().getLogger().warning("Unused " + getTypeName() + ": " + next);
            }
        }
    }

    public Data findHandler(String id) {
        return findHandler(e -> id.equalsIgnoreCase(e.getId()));
    }

    public Data findHandler(Class clazz) {
        return findHandler(e -> clazz == e.getClazz());
    }

    private Data findHandler(Predicate<Wrapper> predicate) {
        return handlers.stream().filter(predicate).map(Wrapper::getType).findFirst().orElse(null);
    }

    @Override
    public Iterator<Wrapper> iterator() {
        return handlers.iterator();
    }
}
