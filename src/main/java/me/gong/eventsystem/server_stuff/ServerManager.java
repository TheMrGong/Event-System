package me.gong.eventsystem.server_stuff;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

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

    @EventHandler
    public void onBlock(BlockDamageEvent ev) {
        if(!EventSystem.get().getEventManager().isParticipating(ev.getPlayer())) {
            if(ev.getPlayer().getGameMode() != GameMode.CREATIVE) ev.setCancelled(true);
        }
        else doSoup(ev.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if(!EventSystem.get().getEventManager().isParticipating(ev.getPlayer())) {
            if(ev.getPlayer().getGameMode() != GameMode.CREATIVE) ev.setCancelled(true);
        }
        else doSoup(ev.getPlayer());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent ev) {
        if(ev.getDamager() instanceof Player) {
            Player p = (Player) ev.getDamager();

            if(!EventSystem.get().getEventManager().isParticipating(p)) {
                if(p.getGameMode() != GameMode.CREATIVE) ev.setCancelled(true);
            }
            else doSoup(p);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent ev) {

        if(!EventSystem.get().getEventManager().isParticipating(ev.getPlayer())) {
            if(ev.getPlayer().getGameMode() != GameMode.CREATIVE) ev.setCancelled(true);
        }
        else if(ev.getItemDrop().getItemStack().getType() == Material.BOWL) ev.getItemDrop().remove();
    }

    private void doSoup(Player p) {
        if(p.getHealth() < p.getMaxHealth() && p.getItemInHand() != null && p.getItemInHand().getType() == Material.MUSHROOM_SOUP) {
            p.setItemInHand(new ItemStack(Material.BOWL));
            p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + 7));
            p.setFoodLevel(Math.min(20, p.getFoodLevel() + 5));
            p.setSaturation(Math.min(20, p.getSaturation() + 3));
            p.updateInventory();
        }
    }
}
