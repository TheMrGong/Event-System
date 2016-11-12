package me.gong.eventsystem.events.impl.waterdrop;

import me.gong.eventsystem.config.meta.Configurable;
import me.gong.eventsystem.config.meta.CustomHandler;
import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.events.EventManager;
import me.gong.eventsystem.events.impl.waterdrop.block.StoredBlock;
import me.gong.eventsystem.events.impl.waterdrop.block.StoredBlockConfig;
import me.gong.eventsystem.events.impl.waterdrop.block.StoredBlockTask;
import me.gong.eventsystem.events.impl.waterdrop.round.Round;
import me.gong.eventsystem.events.impl.waterdrop.round.RoundConfig;
import me.gong.eventsystem.events.impl.waterdrop.round.RoundTask;
import me.gong.eventsystem.events.task.Logic;
import me.gong.eventsystem.events.task.Task;
import me.gong.eventsystem.events.task.data.TaskFrame;
import me.gong.eventsystem.util.BukkitUtils;
import me.gong.eventsystem.util.StringUtils;
import me.gong.eventsystem.util.TimeUtils;
import me.gong.eventsystem.util.data.Box;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;

public class WaterDropEvent extends Event {

    @Configurable(name = "Winning Area", description = "Area players will fall into, and trigger to teleport them back", id = "winningArea")
    public Box winningArea;

    @Configurable(name = "Jumping Location", description = "Location players will jump from.", id = "jumpLoc")
    public Location jumpingLocation;

    @Configurable(name = "Spawning Location", description = "Location players spawn in.", id = "spawnLoc")
    public Location spawningLocation;

    @Configurable(name = "Jump Time", description = "How long players have to jump", id = "jumpTime")
    public Long jumpTime;

    @Configurable(name = "Waiting Time", description = "How long players have to rest after all players have jumped", id = "waitTime")
    public Long waitTime;

    @Configurable(name = "Round Progression", description = "How many rounds to progress", id = "roundProgression")
    public Long roundProgression;

    @Configurable(name = "Rounds", description = "Rounds that make the event more difficult", id = "rounds")
    public List<Round> rounds;

    @CustomHandler(clazz = StoredBlock.class, type = CustomHandler.Type.CONFIG)
    public StoredBlockConfig storedBlockConfig = new StoredBlockConfig();

    @CustomHandler(clazz = StoredBlock.class, type = CustomHandler.Type.FRAME)
    public TaskFrame storedBlockFrame = new TaskFrame(StoredBlockTask.class, StoredBlock.class);

    @CustomHandler(clazz = Round.class, type = CustomHandler.Type.CONFIG)
    public RoundConfig roundConfig = new RoundConfig();

    @CustomHandler(clazz = Round.class, type = CustomHandler.Type.FRAME)
    public TaskFrame roundFrame = new TaskFrame(RoundTask.class, Round.class);

    @Logic("jumpTime")
    @Logic("waitTime")
    @Logic("roundProgression")
    public Task.Logic<Long> positiveOnly = ((aLong, player) -> {
        if (aLong <= 0) player.sendMessage(StringUtils.info("Must be a positive number"));
        else return true;
        return false;
    });

    private EventStage stage = EventStage.BEGINNING;
    private long lastChange;

    private List<UUID> jumped = new ArrayList<>();
    private Map<UUID, Location> origin = new HashMap<>();

    private int roundNumber, progressedRounds;

    @Override
    public void onBegin(CommandSender sender) {
        jumpingLocation = jumpingLocation.clone().add(0.5, 1, 0.5);
        spawningLocation = spawningLocation.clone().add(0.5, 1, 0.5);

        lastChange = 0;
        jumped.clear();

        roundNumber = 0;
        progressedRounds = -1;
        rounds.forEach(Round::reset);
        sender.sendMessage(StringUtils.info("Type &a/begin&7 when enough players have joined."));
    }

    @Override
    public void onEnd(EventManager.ActionCause cause) {
        stage = EventStage.BEGINNING;
        rounds.forEach(Round::reset);
    }

    @Override
    public boolean joinEvent(Player player, EventManager.ActionCause cause) {
        if (stage != EventStage.BEGINNING) {
            player.sendMessage(StringUtils.warn("WaterDrop is unable to be joined, currently in-game"));
            return false;
        }
        if (cause == EventManager.ActionCause.MANUAL)
            broadcast(StringUtils.info("&e" + player.getDisplayName() + "&7 has &ajoined&7 the event!"));
        origin.put(player.getUniqueId(), player.getLocation());
        player.teleport(spawningLocation);
        player.setGameMode(GameMode.SURVIVAL);
        resetPlayer(player);
        return true;
    }

    @Override
    public void quitEvent(Player player, EventManager.ActionCause cause) {
        if (cause == EventManager.ActionCause.MANUAL)
            broadcast(StringUtils.info("&e" + player.getDisplayName() + "&7 has &cleft&7 the event."));
        Location l = origin.remove(player.getUniqueId());
        if (l != null) player.teleport(l);
        resetPlayer(player);
        jumped.remove(player.getUniqueId());
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent ev) {
        if (ev.getPlayer().isOp() && isParticipating(ev.getPlayer()) && ev.getMessage().toLowerCase().startsWith("/begin")) {
            ev.setCancelled(true);
            broadcast(StringUtils.info("&f&lEvent now beginning"));
            progressState();
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent ev) {
        if (isParticipating(ev.getPlayer()) && stage == EventStage.JUMPING) {
            if (winningArea.intersectsWith(winningArea.playerToBox(ev.getTo()))) {
                Location f = locationKeepingRot(ev.getPlayer(), spawningLocation);
                ev.setTo(f);
                ev.getPlayer().setFallDistance(0);
                jumped.add(ev.getPlayer().getUniqueId());
                ev.getPlayer().playSound(f, Sound.VILLAGER_YES, 2.0f, 1.45f);
                ev.getPlayer().sendMessage(StringUtils.info("You made it! &e&oNice!"));
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if (isParticipating(ev.getPlayer())) ev.setCancelled(true);
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent ev) {
        if (isParticipating((Player) ev.getEntity())) ev.setFoodLevel(20);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent ev) {
        if (ev.getDamager() instanceof Player) {
            Player p = (Player) ev.getDamager();
            if (isParticipating(p)) ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onFall(EntityDamageEvent ev) {
        if (ev.getEntity() instanceof Player) {
            Player p = (Player) ev.getEntity();
            if (isParticipating(p)) {
                ev.setCancelled(true);
                if (stage == EventStage.JUMPING && ev.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    broadcast(StringUtils.info("&e" + p.getDisplayName() + "&7 has &clost&7!"));
                    getManager().quitEvent(p, EventManager.ActionCause.PLUGIN);
                }
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent ev) {
        if (ev.getItemDrop().getItemStack().getType() != Material.BOWL) ev.setCancelled(true);
    }

    @EventHandler
    public void onDamage(BlockDamageEvent ev) {
        if (isParticipating(ev.getPlayer())) ev.setCancelled(true);
    }

    @Override
    public void gameTick() {
        if (stage != EventStage.BEGINNING) checkStage();
    }

    private void checkStage() {
        long max = stage == EventStage.JUMPING ? jumpTime : waitTime;
        if (System.currentTimeMillis() - lastChange > max) progressState();

        max = stage == EventStage.JUMPING ? jumpTime : waitTime;
        long timeLeft = (lastChange + max) - System.currentTimeMillis();
        if (stage == EventStage.JUMPING)
            broadcastAction("&eJumping! &e" + TimeUtils.convertToString(timeLeft).toLowerCase() + " &7left to jump!");
        else if (stage == EventStage.WAITING) {
            broadcastAction("&eWaiting&7 for " + TimeUtils.convertToString(timeLeft) + ".");
            long secondsLeft = timeLeft / 1000;
            double other = (timeLeft / 50) / 20d;
            if(secondsLeft > 5 ? secondsLeft % 10 == 0 : (other - secondsLeft) < 0.02) {
                System.out.println("precise; "+other + " seconds Left > 5? "+(secondsLeft > 5)+" precise ");
                broadcastSound(Sound.NOTE_PLING, 2.0f, 2.0f);
            }
        }
    }

    private void beginJumping() {
        jumped.clear();
        getPlaying().forEach(p -> p.teleport(locationKeepingRot(p, jumpingLocation)));
        broadcastSound(Sound.CHEST_OPEN, 2.0f, 1.0f);
        broadcast(new BukkitUtils.Title("&a&lJUMP!", false, 3, 38, 4));
    }

    private void beginWaiting() {
        getPlaying().stream()
                .filter(p -> !jumped.contains(p.getUniqueId()))
                .forEach(p -> {
                    broadcast(StringUtils.format("&e" + p.getDisplayName() + " &c&ldidn't jump in time!"));
                    getManager().quitEvent(p, EventManager.ActionCause.PLUGIN);
                    Location l = origin.get(p.getUniqueId());
                    if (l == null) l = p.getLocation();
                    p.playSound(l, Sound.VILLAGER_NO, 2.0f, 1.0f);
                });
        roundNumber++;
        if(roundNumber % roundProgression == 0) progressRound();
        broadcastSound(Sound.CHEST_CLOSE, 2.0f, 1.0f);
    }

    private void progressRound() {
        if(progressedRounds < rounds.size()) {
            rounds.get(++progressedRounds).begin();
            broadcast(StringUtils.info("Jumping difficulty increased."));
        }

    }

    private void progressState() {
        lastChange = System.currentTimeMillis();
        if (stage == EventStage.BEGINNING) stage = EventStage.WAITING;
        else if (stage == EventStage.WAITING) {
            stage = EventStage.JUMPING;
            beginJumping();
        } else if (stage == EventStage.JUMPING) {
            stage = EventStage.WAITING;
            beginWaiting();
        }
    }

    @Override
    public String getEventId() {
        return "waterdrop";
    }

    public enum EventStage {
        BEGINNING, WAITING, JUMPING
    }
}
