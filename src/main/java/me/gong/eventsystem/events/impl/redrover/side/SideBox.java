package me.gong.eventsystem.events.impl.redrover.side;

import me.gong.eventsystem.util.data.Box;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SideBox {
    private Box detectionArea, barrierArea;
    private Location spawnLocation;

    public SideBox(Box detectionArea, Box barrierArea, Location spawnLocation) {
        this.detectionArea = detectionArea;
        this.barrierArea = barrierArea;
        this.spawnLocation = spawnLocation.clone().add(0.5, 1, 0.5);
    }

    public void setBarrier(boolean isSet) {
        barrierArea.getAllBlocks().forEach(b -> b.getBlock().setType(isSet ? Material.OBSIDIAN : Material.AIR));
    }

    public boolean containsPlayer(Player player) {
        return detectionArea.intersectsWith(player);
    }

    public void teleport(Player player) {
        player.teleport(spawnLocation);
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public Box getDetectionArea() {
        return detectionArea;
    }

    public Box getBarrierArea() {
        return barrierArea;
    }
}