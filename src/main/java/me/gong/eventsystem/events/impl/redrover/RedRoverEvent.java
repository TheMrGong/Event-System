package me.gong.eventsystem.events.impl.redrover;

import me.gong.eventsystem.config.meta.Configurable;
import me.gong.eventsystem.config.meta.CustomHandler;
import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.events.EventManager;
import me.gong.eventsystem.events.impl.redrover.side.SideBox;
import me.gong.eventsystem.events.impl.redrover.side.SideBoxTask;
import me.gong.eventsystem.events.impl.redrover.side.SideConfig;
import me.gong.eventsystem.events.impl.redrover.stage.Stage;
import me.gong.eventsystem.events.impl.redrover.stage.StageConfig;
import me.gong.eventsystem.events.impl.redrover.stage.StageTask;
import me.gong.eventsystem.events.task.data.TaskFrame;
import me.gong.eventsystem.util.BukkitUtils;
import me.gong.eventsystem.util.NumberUtils;
import me.gong.eventsystem.util.StringUtils;
import me.gong.eventsystem.util.TimeUtils;
import me.gong.eventsystem.util.data.Box;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class RedRoverEvent extends Event {

    private static final long RESTING_TIME = 7000, RUN_TIME = 12000;

    @Configurable(name = "Blue Side", description = "Area on blue side", id = "blue")
    public SideBox blueSide;

    @Configurable(name = "Red Side", description = "Area on red side", id = "red")
    public SideBox redSide;

    @CustomHandler(clazz = SideBox.class, type = CustomHandler.Type.FRAME)
    public TaskFrame sideTask = new TaskFrame(SideBoxTask.class, SideBox.class);

    @CustomHandler(clazz = SideBox.class, type = CustomHandler.Type.CONFIG)
    public SideConfig sideConfig = new SideConfig();

    @CustomHandler(clazz = Stage.class, type = CustomHandler.Type.FRAME)
    public TaskFrame stageTask = new TaskFrame(StageTask.class, Stage.class);

    @CustomHandler(clazz = Stage.class, type = CustomHandler.Type.CONFIG)
    public StageConfig stageConfig = new StageConfig();

    @Configurable(name = "Stages", description = "Stages for the event", id = "stages")
    public List<Stage> stages;

    @Configurable(name = "Slayer Area", description = "Area where slayer may walk around in.", id = "slayer-area")
    public Box slayerArea;

    @Configurable(name = "Event Spawn Location", description = "Where players will spawn when joining the event. Also slayer spawn location.", id = "spawnLoc")
    public Location spawnLoc;

    private boolean isJoinable;

    private Map<UUID, Location> origin = new HashMap<>();
    private UUID slayer;

    private int stageIndex, rounds;
    private boolean runToRed;

    private EventState eventState;
    private long timeSinceChange;

    @Override
    public void onBegin(CommandSender sender) {
        isJoinable = true;

        stageIndex = -1;
        rounds = 0;
        timeSinceChange = 0;
        eventState = EventState.BEGINNING;
        spawnLoc = spawnLoc.clone().add(0.5, 1, 0.5);

        updateBoxes();

        sender.sendMessage(StringUtils.info("Type &a/begin&7 when enough players have joined."));

    }

    @Override
    public void onEnd(EventManager.ActionCause cause) {
        eventState = EventState.BEGINNING;
        origin.clear();
        stages.forEach(Stage::reset);
    }

    @Override
    public boolean joinEvent(Player player, EventManager.ActionCause cause) {
        if (!isJoinable) {
            player.sendMessage(StringUtils.warn("RedRover is unable to be joined, currently in-game"));
            return false;
        }
        if (cause == EventManager.ActionCause.MANUAL)
            broadcast(StringUtils.info("&e" + player.getDisplayName() + "&7 has &ajoined&7 the event!"));
        origin.put(player.getUniqueId(), player.getLocation());
        player.teleport(spawnLoc);
        return true;
    }

    @Override
    public void quitEvent(Player player, EventManager.ActionCause cause) {
        if (cause == EventManager.ActionCause.MANUAL) {
            broadcast(StringUtils.info("&e" + player.getDisplayName() + "&7 has &cleft&7 the event."));
            if (isSlayer(player)) {
                slayer = null;
                broadcast(StringUtils.info("&e" + player.getDisplayName() + "&7 was the slayer, choosing new one."));
                chooseSlayer();
            }
        }
        Location l = origin.remove(player.getUniqueId());
        if (l != null) player.teleport(l);
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent ev) {
        if (ev.getPlayer().isOp() && isParticipating(ev.getPlayer()) && ev.getMessage().toLowerCase().startsWith("/begin")) {
            broadcast(StringUtils.info("&f&lEvent now beginning"));
            chooseSlayer();
            SideBox boxStarting;
            runToRed = NumberUtils.r.nextBoolean();

            if (runToRed) boxStarting = redSide;
            else boxStarting = redSide;

            getRunners().forEach(boxStarting::teleport);
            progressState();
        }
    }

    @Override
    public void gameTick() {
        if (eventState != EventState.BEGINNING) {
            long timeDiff = System.currentTimeMillis() - timeSinceChange,
                    maxTime = eventState == EventState.RESTING ? RESTING_TIME : RUN_TIME;
            if (timeDiff > maxTime) progressState();
            if(eventState == EventState.RESTING && rounds % 5 == 0 && timeDiff >= maxTime) progressStage();

            maxTime = eventState == EventState.RESTING ? RESTING_TIME : RUN_TIME;
            long timeLeft = (timeSinceChange + maxTime) - System.currentTimeMillis();
            if (eventState == EventState.RESTING)
                broadcastAction("&3Resting for &e" + TimeUtils.convertToString(timeLeft));
            else broadcastAction("&a&lRunning to " + (runToRed ? "&c&lred" : "&9&lblue") + "&a&l side. (" + TimeUtils.convertToString(timeLeft) + ")");
        }
    }

    private boolean isSlayer(Player p) {
        return slayer != null && slayer.equals(p.getUniqueId());
    }

    private List<Player> getRunners() {
        return getPlaying().stream().filter(p -> !isSlayer(p)).collect(Collectors.toList());
    }

    private void chooseSlayer() {
        List<Player> l = getPlaying();
        Player f = l.get(NumberUtils.r.nextInt(l.size()));
        slayer = f.getUniqueId();
        f.teleport(spawnLoc);
        broadcast(new BukkitUtils.Title("&e" + f.getDisplayName() + "&7 was chosen as &c&lThe Slayer&7!", true, 2, 42, 4));
    }

    private void beginRunning() {
        runToRed = !runToRed;
        updateBoxes();
        broadcast(new BukkitUtils.Title("&e&lRUN TO " + (runToRed ? "&cRED" : "&9BLUE") + "!", false, 2, 28, 1));
    }

    private void beginResting() {
        rounds++;
        updateBoxes();
        broadcast(new BukkitUtils.Title("&7&l5 seconds to rest.", true, 2, 36, 4));
    }

    private void updateBoxes() {
        if (eventState == EventState.BEGINNING) {
            blueSide.setBarrier(true);
            redSide.setBarrier(true);
        } else {
            if (eventState == EventState.RESTING) {
                redSide.setBarrier(runToRed);
                blueSide.setBarrier(!runToRed);
            } else {
                blueSide.setBarrier(false);
                redSide.setBarrier(false);
            }
        }
    }

    private void progressState() {
        timeSinceChange = System.currentTimeMillis();
        if (eventState == EventState.BEGINNING) eventState = EventState.RESTING;
        else if (eventState == EventState.RESTING) {
            eventState = EventState.RUNNING;
            beginRunning();
        } else if (eventState == EventState.RUNNING) {
            eventState = EventState.RESTING;
            beginResting();
        }
    }

    private void progressStage() {
        if (stageIndex < stages.size() - 1) {
            stageIndex++;
            Stage s = stages.get(stageIndex);
            s.apply(this, getRunners());
        }
    }

    @Override
    public String getEventId() {
        return "redrover";
    }

    public enum EventState {
        BEGINNING, RESTING, RUNNING
    }
}
