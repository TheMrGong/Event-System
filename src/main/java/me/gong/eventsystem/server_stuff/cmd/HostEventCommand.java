package me.gong.eventsystem.server_stuff.cmd;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.events.EventManager;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HostEventCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String cmd, String[] args) {
        EventManager em = EventSystem.get().getEventManager();
        if (command.getName().equalsIgnoreCase("host")) {
            if (args.length == 0) {
                commandSender.sendMessage(StringUtils.warn("An event name is required."));
                informEvents(commandSender, em);
            } else {
                Event ev = em.getEventForId(args[0]);
                if (ev == null) {
                    commandSender.sendMessage(StringUtils.warn("No event exists for name '" + args[0] + "'"));
                    informEvents(commandSender, em);
                } else {
                    World world;
                    if (commandSender instanceof Player) world = ((Player) commandSender).getWorld();
                    else if (args.length == 1) {
                        commandSender.sendMessage(StringUtils.warn("World wasn't provided for event."));
                        return true;
                    } else {
                        world = Bukkit.getWorld(args[1]);
                        if (world == null) {
                            commandSender.sendMessage(StringUtils.warn("World '"+args[1]+"' doesn't exist"));
                            return true;
                        }
                    }
                    String error = em.beginEvent(ev, world, commandSender);
                    if (error != null) commandSender.sendMessage(StringUtils.warn("Unable to host event: " + error));
                    else
                        commandSender.sendMessage(StringUtils.info("Event &e" + ev.getEventId() + "&7 successfully hosted."));
                }
            }
        } else if (command.getName().equalsIgnoreCase("endevent")) {

            String error = em.endCurrentEvent(EventManager.ActionCause.MANUAL);
            if (error != null) commandSender.sendMessage(StringUtils.warn("Unable to end event: " + error));
            else {
                Bukkit.broadcastMessage(StringUtils.info("&6&l" + commandSender.getName() + " &c&lhas ended the current event."));
                commandSender.sendMessage(StringUtils.info("Event has been ended."));
            }

        }
        return true;
    }

    private void informEvents(CommandSender sender, EventManager em) {
        sender.sendMessage(StringUtils.info("The currently available events are: &a" + StringUtils.concat(em.getAvailableEvents(), ", ")));
    }
}
