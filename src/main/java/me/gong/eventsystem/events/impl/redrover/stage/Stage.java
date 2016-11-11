package me.gong.eventsystem.events.impl.redrover.stage;

import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.util.NumberUtils;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.stream.Collectors;

public class Stage {

    private List<Location> locations;
    private StageType type;

    public Stage(List<Location> locations, StageType type) {
        this(type);
        this.locations = locations.stream().map(l -> l.clone().add(0, 1, 0)).collect(Collectors.toList());
    }

    public Stage(StageType type) {
        this.type = type;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public StageType getType() {
        return type;
    }

    public void apply(Event event, List<Player> players) {
        if (type == StageType.ADD_COBWEBS) setBlocks(Material.WEB);
        else if (type == StageType.ADD_LAVA) setBlocks(Material.LAVA);
        else if (type == StageType.GIVE_SLOW)
            giveEffect(players, new PotionEffect(PotionEffectType.SLOW, 999999, 0, true, false));
        else giveEffect(players, new PotionEffect(PotionEffectType.CONFUSION, 999999, 0, true, false));
        event.broadcast(StringUtils.info("This stage was: " + type.getName() + " (" + type.pickTaunt() + ")"));
    }

    public void reset() {
        if(locations != null) setBlocks(Material.AIR);
    }

    private void setBlocks(Material material) {
        locations.forEach(l -> l.getBlock().setType(material));
    }

    private void giveEffect(List<Player> players, PotionEffect effect) {
        players.forEach(p -> p.addPotionEffect(effect));
    }

    public enum StageType {
        ADD_COBWEBS("Cobwebs", "Get some cobwebs!", "More cobwebs!", "Get ready to be annoyed!"),
        ADD_LAVA("Lava", "Burn, burn!", "I spilled something", "Might want to watch out for the lava"),
        GIVE_SLOW("Slowness", "\"Brakes turned on\"", "No sanic for you!", ""),
        GIVE_NAUSEA("Nausea", "Which ways left?", "Good luck seeing where you're going", "That's going to be difficult");

        private String name;
        private String[] taunts;

        StageType(String s, String... taunts) {
            this.name = name();
            this.taunts = taunts;
        }

        public String pickTaunt() {
            return taunts[NumberUtils.r.nextInt(taunts.length)];
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}