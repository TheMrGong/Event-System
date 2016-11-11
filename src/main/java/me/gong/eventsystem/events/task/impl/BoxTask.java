package me.gong.eventsystem.events.task.impl;

import me.gong.eventsystem.events.task.Task;
import me.gong.eventsystem.util.CancellableCallback;
import me.gong.eventsystem.util.StringUtils;
import me.gong.eventsystem.util.data.Box;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class BoxTask extends Task<Box> {

    private Location pos1, pos2;

    public BoxTask(String id, String event, UUID creating, String name, String help, CancellableCallback<Box> callback, Logic<Box> logic) {
        super(id, event, creating, name, help, callback, logic);
    }

    @Override
    public void beginTask() {
        Player p = getPlayer();
        p.sendMessage(StringUtils.info((!hasAxe(p) ? "Grab a &bdiamond axe&7 then either l" : "L") + "eft click or right click for the first and second positions."));
        p.sendMessage(StringUtils.info("Run &a/finish&7 when you are complete. Box help for &b'" + name + "'&7: &e" + help));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if (isCreating(ev.getPlayer())) {
            if (ev.getItem().getType() == Material.DIAMOND_AXE) {
                if (ev.getAction() == Action.LEFT_CLICK_BLOCK || ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    checkAndPrint(ev.getAction() == Action.LEFT_CLICK_BLOCK, ev.getClickedBlock().getLocation(), ev.getPlayer());
                    ev.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent ev) {
        if (ev.getMessage().toLowerCase().startsWith("/finish") && isCreating(ev.getPlayer())) {
            ev.setCancelled(true);
            if (check(ev.getPlayer())) {
                Box created = new Box(pos1.clone().add(0.5, (pos1.getY() > pos2.getY() ? 0.5 : 0), 0.5),
                        pos2.clone().add(0.5, pos2.getY() > pos1.getY() ? 0.5 : 0, 0.5));
                if (checkWithLogic(created, ev.getPlayer())) callback.onComplete(created);
            }
        }
    }

    private boolean hasAxe(Player player) {
        return Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).anyMatch(i -> i.getType() == Material.DIAMOND_AXE);
    }

    private boolean check(Player player) {
        if (pos1 != null && pos2 != null) return true;
        player.sendMessage(StringUtils.warn("Position " + (pos1 == null ? "one" : "two") + " isn't set."));
        return false;
    }

    private void checkAndPrint(boolean isPos1, Location l, Player player) {
        if (((isPos1 && pos2 != null) || (!isPos1 && pos1 != null)) && (isPos1 ? pos1 == null : pos2 == null)) //make sure its not overwriting, otherwise its spammy
            player.sendMessage(StringUtils.info("Both positions set. Run &a/finish&7 when ready."));
        else player.sendMessage(StringUtils.info("Position " + (isPos1 ? "one" : "two") + "set."));
        if (isPos1) pos1 = l;
        else pos2 = l;
    }

    public static Class<?> getCreating() {
        return Box.class;
    }
}
