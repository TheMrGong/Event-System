package me.gong.eventsystem.server_stuff;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * demo class
 */
public class ServerManager implements Listener {

    public ServerManager(EventSystem eventSystem) {
        Bukkit.getPluginManager().registerEvents(this, eventSystem);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        p.teleport(p.getWorld().getSpawnLocation());
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setSaturation(20);
        event.setJoinMessage(StringUtils.format("&b"+p.getName()+"&3 has joined."));
    }
}
