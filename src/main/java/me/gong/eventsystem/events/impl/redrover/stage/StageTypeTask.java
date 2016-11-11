package me.gong.eventsystem.events.impl.redrover.stage;

import me.gong.eventsystem.events.task.Task;
import me.gong.eventsystem.util.CancellableCallback;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class StageTypeTask extends Task<Stage.StageType> {
    public StageTypeTask(String id, String event, UUID creating, String name, String help, CancellableCallback<Stage.StageType> callback, Logic<Stage.StageType> logic) {
        super(id, event, creating, name, help, callback, logic);
    }

    @Override
    public void beginTask() {
        Player p = getPlayer();
        p.sendMessage(StringUtils.info("Setting stage type"));
        p.sendMessage(StringUtils.info("Choose a stage from the following selection: &e" + StringUtils.concat(Stage.StageType.values(), ", ")));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent ev) {
        Player p = ev.getPlayer();
        if (isCreating(p)) {
            ev.setCancelled(true);
            String msg = ev.getMessage().toUpperCase();
            try {
                Stage.StageType type = Stage.StageType.valueOf(msg);
                if(checkWithLogic(type, p)) callback.onComplete(type);
            } catch (IllegalArgumentException ex) {
                p.sendMessage(StringUtils.info("Invalid stage type &b'" + ev.getMessage() + "'. " +
                        "Valids: &e" + StringUtils.concat(Stage.StageType.values(), ", ")));
            }
        }
    }
}
