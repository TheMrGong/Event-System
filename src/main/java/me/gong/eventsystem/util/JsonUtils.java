package me.gong.eventsystem.util;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class JsonUtils {

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final JsonParser parser = new JsonParser();

    public static void locationToElement(Location location, JsonObject ret) {

        ret.addProperty("world", location.getWorld().getName());
        ret.addProperty("x", location.getX());
        ret.addProperty("y", location.getY());
        ret.addProperty("z", location.getZ());

        ret.addProperty("yaw", location.getYaw());
        ret.addProperty("pitch", location.getPitch());
    }

    public static Location elementToLocation(JsonObject obj) {

        String wrld = obj.get("world").getAsString();
        World world = Bukkit.getWorld(wrld);

        if(world == null) throw new RuntimeException("World didn't exist: "+wrld);

        return new Location(world, obj.get("x").getAsDouble(), obj.get("y").getAsDouble(), obj.get("z").getAsDouble(),
                obj.get("yaw").getAsFloat(), obj.get("pitch").getAsFloat());
    }
}
