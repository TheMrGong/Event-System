package me.gong.eventsystem.events.impl.waterdrop.round;

import me.gong.eventsystem.events.impl.waterdrop.block.StoredBlock;

import java.util.List;

public class Round {
    private List<StoredBlock> toSet;

    public Round(List<StoredBlock> toSet) {
        this.toSet = toSet;
    }

    public void begin() {
        toSet.forEach(StoredBlock::set);
    }

    public void reset() {
        toSet.forEach(StoredBlock::unset);
    }

    public List<StoredBlock> getToSet() {
        return toSet;
    }
}
