package me.gong.eventsystem.events.impl.waterdrop.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class StoredBlock {
    private Location at;
    private Material type;
    private short data;

    public StoredBlock(Location at, Material type, short data) {
        this.at = at;
        this.type = type;
        this.data = data;
    }

    public StoredBlock(Block block) {
        this(block.getLocation(), block.getType(), block.getData());
    }

    public void set() {
        at.getBlock().setTypeIdAndData(type.getId(), (byte) data, false);
    }

    public void unset() {
        at.getBlock().setTypeIdAndData(0, (byte) 0, false);
    }

    public Location getAt() {
        return at;
    }

    public Material getType() {
        return type;
    }

    public short getData() {
        return data;
    }
}
