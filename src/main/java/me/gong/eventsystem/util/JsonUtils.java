package me.gong.eventsystem.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class JsonUtils {

    public static JsonElement locationToElement(Location location) {
        JsonObject ret = new JsonObject();

        ret.addProperty("world", location.getWorld().getName());
        ret.addProperty("x", location.getX());
        ret.addProperty("y", location.getY());
        ret.addProperty("z", location.getZ());

        ret.addProperty("yaw", location.getYaw());
        ret.addProperty("pitch", location.getPitch());
        return ret;
    }

    public static Location elementToLocation(JsonElement element) {
        if (!(element instanceof JsonObject)) throw new RuntimeException("Expected JsonObject, got " + element.getClass().getSimpleName());
        JsonObject obj = (JsonObject) element;

        String wrld = obj.get("world").getAsString();
        World world = Bukkit.getWorld(wrld);

        if(world == null) throw new RuntimeException("World didn't exist: "+wrld);

        return new Location(world, obj.get("x").getAsDouble(), obj.get("y").getAsDouble(), obj.get("z").getAsDouble(),
                obj.get("yaw").getAsFloat(), obj.get("pitch").getAsFloat());
    }
}
