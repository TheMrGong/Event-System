package me.gong.eventsystem.events.impl.redrover;

import me.gong.eventsystem.EventSystem;
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
import me.gong.eventsystem.events.task.Logic;
import me.gong.eventsystem.events.task.Task;
import me.gong.eventsystem.events.task.data.TaskFrame;
import me.gong.eventsystem.util.*;
import me.gong.eventsystem.util.data.Box;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class RedRoverEvent extends Event {

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

    @Configurable(name = "Resting Time", description = "Amount of time in milliseconds players rest between rounds", id = "resting_time")
    public Long restingTime;

    @Configurable(name = "Running Initial Time", description = "Amount of time in milliseconds players have to run between sides",
            id = "running_time")
    public Long initialRunningTime;

    @Configurable(name = "Running Decrease", description = "Decrease the amount of time to cross by this amount every round", id = "decrease")
    public Long decrease;

    @Configurable(name = "Running Minimum", description = "Minimum amount of time required to run. " +
            "Used to prevent the decrease for being too insane",
            id = "minimum")
    public Long minimum;

    @Configurable(name = "Stage Separation", description = "How many rounds before progressing a stage", id = "separation")
    public Long stageSeperation;

    @Configurable(name = "Restock Time", description = "Time between restocks", id = "restockTime")
    public Long restockTime;

    @Logic("separation")
    public Task.Logic<Long> separationLogic = (aLong, player) -> {
        if(aLong <= 0) player.sendMessage(StringUtils.info("Separation must be greater than 1"));
        else if(aLong > 6) player.sendMessage(StringUtils.info("Maximum separation is 6"));
        else return true;
        return false;
    };

    @Logic("resting_time")
    @Logic("running_time")
    @Logic("decrease")
    @Logic("minimum")
    @Logic("restockTime")
    public Task.Logic<Long> positiveOnly = ((aLong, player) -> {
        if(aLong <= 0) player.sendMessage(StringUtils.info("Must be a positive number"));
        else return true;
        return false;
    });

    private Map<UUID, Location> origin = new HashMap<>();
    private UUID slayer;

    private int stageIndex, rounds;
    private boolean runToRed;

    private EventState eventState;
    private long timeSinceChange, curRunningTime, lastRestock, slappyNoise, slappyDelay;

    @Override
    public void onBegin(CommandSender sender) {

        stageIndex = -1;
        rounds = 0;
        timeSinceChange = 0;
        eventState = EventState.BEGINNING;
        spawnLoc = spawnLoc.clone().add(0.5, 1, 0.5);
        curRunningTime = initialRunningTime;
        slayer = null;

        updateBoxes();

        sender.sendMessage(StringUtils.info("Type &a/begin&7 when enough players have joined."));

    }

    @Override
    public void onEnd(EventManager.ActionCause cause) {
        eventState = EventState.BEGINNING;
        origin.clear();
        stages.forEach(Stage::reset);
        lastRestock = 0;
        slappyNoise = 0;
    }

    @Override
    public boolean joinEvent(Player player, EventManager.ActionCause cause) {
        if (eventState != EventState.BEGINNING) {
            player.sendMessage(StringUtils.warn("RedRover is unable to be joined, currently in-game"));
            return false;
        }
        if (cause == EventManager.ActionCause.MANUAL)
            broadcast(StringUtils.info("&e" + player.getDisplayName() + "&7 has &ajoined&7 the event!"));
        origin.put(player.getUniqueId(), player.getLocation());
        player.teleport(spawnLoc);
        player.setGameMode(GameMode.SURVIVAL);
        resetPlayer(player);
        return true;
    }

    @Override
    public void quitEvent(Player player, EventManager.ActionCause cause) {
        if(getPlaying().size() - 1 <= 1 && isRunning()  && cause == EventManager.ActionCause.MANUAL) {
            Bukkit.broadcastMessage(StringUtils.format("&c&lRedRover event &e&lwas cancelled due to lack of players"));
            getManager().endCurrentEvent(EventManager.ActionCause.PLUGIN);
        }
        if (cause == EventManager.ActionCause.MANUAL) {
            broadcast(StringUtils.info("&e" + player.getDisplayName() + "&7 has &cleft&7 the event."));
            if (isSlayer(player)) {
                slayer = null;
                broadcast(StringUtils.info("&e" + player.getDisplayName() + "&7 was the slayer, choosing new one."));
                chooseSlayer();
            }
        }
        Location l = origin.remove(player.getUniqueId());
        resetPlayer(player);
        if (l != null) player.teleport(l);
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent ev) {
        if (ev.getPlayer().isOp() && isParticipating(ev.getPlayer()) && ev.getMessage().toLowerCase().startsWith("/begin")) {
            ev.setCancelled(true);
            broadcast(StringUtils.info("&f&lEvent now beginning"));
            lastRestock = System.currentTimeMillis();
            chooseSlayer();
            SideBox boxStarting;
            runToRed = NumberUtils.r.nextBoolean();

            if (runToRed) boxStarting = redSide;
            else boxStarting = redSide;

            getRunners().forEach(r -> {
                giveKit(r);
                boxStarting.teleport(r);
            });
            progressState();
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent ev) {
        if(isSlayer(ev.getPlayer()) && !slayerArea.intersectsWith(slayerArea.playerToBox(ev.getTo()))) {
            Location f = ev.getFrom().clone(), t = ev.getTo();
            f.setYaw(t.getYaw());
            f.setPitch(f.getPitch());
            ev.setTo(f);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if(isParticipating(ev.getPlayer()) && (ev.getItem() == null || ev.getItem().getType() != Material.COOKED_BEEF)) {
            if(ev.getPlayer().getGameMode() != GameMode.CREATIVE) ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent ev) {
        if(ev.getDamager() instanceof Player) {
            Player p = (Player) ev.getDamager();
            if(isParticipating(p) && !isSlayer(p)) ev.setCancelled(true);
            else if(isSlayer(p) && ev.getEntity() instanceof Player) {
                Player damaged = (Player) ev.getEntity();
                if(isParticipating(damaged)) {
                    if(damaged.getHealth() - ev.getFinalDamage() <= 0) {
                        ev.setCancelled(true);
                        broadcast(StringUtils.info("&b&l" + damaged.getDisplayName()+"&e&l has died to &c&lThe Slayer&e&l!"));
                        getManager().quitEvent(damaged, EventManager.ActionCause.PLUGIN);
                    }
                } else ev.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent ev) {
        if(ev.getItemDrop().getItemStack().getType() != Material.BOWL) {
            if(ev.getPlayer().getGameMode() != GameMode.CREATIVE) ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(BlockDamageEvent ev) {
        if(isParticipating(ev.getPlayer())) {
            if(ev.getPlayer().getGameMode() != GameMode.CREATIVE) ev.setCancelled(true);
        }
    }

    @Override
    public void gameTick() {
        if (eventState != EventState.BEGINNING) {
            checkRunning();
            checkRestock();
            checkSlappyNoise();
        }
    }

    private void checkRunning() {
        long timeDiff = System.currentTimeMillis() - timeSinceChange, maxTime = calculateMaxTime();
        if (timeDiff > maxTime) progressState();
        if (eventState == EventState.RESTING && rounds % stageSeperation == 0 && timeDiff >= maxTime) progressStage();

        maxTime = calculateMaxTime();
        long timeLeft = (timeSinceChange + maxTime) - System.currentTimeMillis();
        if (eventState == EventState.RESTING)
            broadcastAction("&3Resting for &e" + TimeUtils.convertToString(timeLeft));
        else
            broadcastAction("&a&lRunning to " + (runToRed ? "&c&lred" : "&9&lblue") + "&a&l side. (" + TimeUtils.convertToString(timeLeft) + ")");
    }

    private void checkSlappyNoise() {

        if(System.currentTimeMillis() - slappyNoise > slappyDelay) {
            Player slayer = Bukkit.getPlayer(this.slayer);
            slappyNoise = System.currentTimeMillis();
            slappyDelay = NumberUtils.getRandom(150, 2000);
            getRunners().stream().filter(r -> r.getLocation().distanceSquared(slayer.getLocation()) < 7 * 7).forEach(r -> slayer.playSound(r.getLocation(), Sound.BAT_TAKEOFF, 0.1f, 1.0f));
        }
    }

    private void checkRestock() {
        if(System.currentTimeMillis() - lastRestock > restockTime) {
            lastRestock = System.currentTimeMillis();
            getPlaying().forEach(this::giveKit);
            broadcast(StringUtils.format("&b&lAll players have been restocked!"));
        }
    }

    private void killOutsidePlayers() {
        getRunners().stream().filter(p -> {
            SideBox s = runToRed ? redSide : blueSide;
            return !s.containsPlayer(p);
        }).forEach(p -> {
            broadcast(StringUtils.info("&b&l"+p.getDisplayName()+"&e&l has lost!"));
            getManager().quitEvent(p, EventManager.ActionCause.PLUGIN);
        });
    }

    private void giveKit(Player p) {
        PlayerInventory i = p.getInventory();
        if(isSlayer(p)) {
            i.setHelmet(new ItemBuilder(Material.DIAMOND_HELMET).unbreakable(true).build());
            i.setChestplate(new ItemBuilder(Material.DIAMOND_CHESTPLATE).unbreakable(true).build());
            i.setLeggings(new ItemBuilder(Material.DIAMOND_LEGGINGS).unbreakable(true).build());
            i.setBoots(new ItemBuilder(Material.DIAMOND_BOOTS).unbreakable(true).build());
            i.setItem(0, new ItemBuilder(Material.DIAMOND_SWORD).unbreakable(true).enchantment(Enchantment.DAMAGE_ALL, 1).build());
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 99999999, 2));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 99999999, 3));
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 99999999, 1));
        } else {
            p.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 9999999, 10));
            for (int in = 0; in < 36; in++) {
                if(in == 8) i.setItem(in, new ItemBuilder(Material.COOKED_BEEF).amount(64).name("&eMunchable Meat").build());
                else i.setItem(in, new ItemBuilder(Material.MUSHROOM_SOUP).name("&6Stew").build());
            }
        }
    }

    private long calculateMaxTime() {
        return eventState == EventState.RESTING ? restingTime : curRunningTime;
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
        giveKit(f);
        broadcast(new BukkitUtils.Title("&e" + f.getDisplayName() + "&7 was chosen as &c&lThe Slayer&7!", true, 2, 42, 4));
    }

    private void beginRunning() {
        runToRed = !runToRed;
        updateBoxes();
        broadcast(new BukkitUtils.Title("&e&lRUN TO " + (runToRed ? "&cRED" : "&9BLUE") + "!", false, 2, 28, 1));
    }

    private void beginResting() {
        rounds++;
        if(curRunningTime > minimum) curRunningTime -= decrease;
        updateBoxes();
        killOutsidePlayers();
        broadcast(new BukkitUtils.Title("&7&l" + TimeUtils.convertToString(restingTime).toLowerCase() + " to rest.", true, 2, 36, 4));
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
