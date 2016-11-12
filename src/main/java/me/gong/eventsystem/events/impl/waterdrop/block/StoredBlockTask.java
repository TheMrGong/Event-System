package me.gong.eventsystem.events.impl.waterdrop.block;

import me.gong.eventsystem.events.task.Task;
import me.gong.eventsystem.util.CancellableCallback;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.UUID;

public class StoredBlockTask extends Task<StoredBlock> {

    public StoredBlockTask(String id, String event, UUID creating, String name, String help, CancellableCallback<StoredBlock> callback, Logic<StoredBlock> logic) {
        super(id, event, creating, name, help, callback, logic);
    }

    @Override
    public void beginTask() {
        Player p = getPlayer();
        p.sendMessage(StringUtils.info("Place a block while &esneaking&7 for &b'" + name + "'&7. Help: &e" + help));
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent ev) {
        if(isCreating(ev.getPlayer()) && ev.getPlayer().isSneaking()) {
            StoredBlock sb = new StoredBlock(ev.getBlock());
            if(checkWithLogic(sb, ev.getPlayer())) callback.onComplete(sb);
        }
    }
}
