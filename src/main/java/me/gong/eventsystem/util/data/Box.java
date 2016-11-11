package me.gong.eventsystem.util.data;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Box {
    private double minX, minY, minZ, maxX, maxY, maxZ;
    private Location pos1, pos2;

    public Box(World world, double x1, double y1, double z1, double x2, double y2, double z2) {
        minX = Math.min(x1, x2);
        minY = Math.min(y1, y2);
        minZ = Math.min(z1, z2);

        maxX = Math.max(x1, x2);
        maxY = Math.max(y1, y2);
        maxZ = Math.max(z1, z2);

        pos1 = new Location(world, minX, minY, minZ);
        pos2 = new Location(world, maxX, maxY, maxZ);
    }

    public Box(Location pos1, Location pos2) {
        this(pos1.getWorld(), pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
    }

    public Location getPos1() {
        return pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public List<Location> getAllBlocks() {
        List<Location> ret = new ArrayList<>();
        for (int x = pos1.getBlockX(); x <= pos2.getBlockX(); x++) {
            for (int y = pos1.getBlockY(); y <= pos2.getBlockY(); y++) {
                for (int z = pos1.getBlockZ(); z <= pos2.getBlockZ(); z++) {
                    ret.add(new Location(pos1.getWorld(), x, y, z));
                }
            }
        }
        return ret;
    }

    public boolean intersectsWith(Box other) {
        return this.intersectsWith(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    public boolean intersectsWith(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return this.minX < maxX && this.maxX > minX && this.minY < maxY && this.maxY > minY && this.minZ < maxZ && this.maxZ > minZ;
    }

    public boolean intersectsWith(Player player) {
        Location pos1 = player.getLocation().clone().subtract(0.3, 0, 0.3);
        Location pos2 = pos1.clone().add(0.6, 1.8, 0.6);
        return new Box(pos1, pos2).intersectsWith(this);
    }


}
