package me.gong.eventsystem.server_stuff.cmd;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.events.Event;
import me.gong.eventsystem.events.EventManager;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetupEventCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command cmd, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(StringUtils.info("Unable to setup events as console"));
            return true;
        }

        Player p = (Player) commandSender;

        EventSystem es = EventSystem.get();
        EventManager em = es.getEventManager();

        if (args.length == 0) {
            p.sendMessage(StringUtils.warn("Usage: &a/setup <event | finish | cancel>"));
            informEvents(commandSender, em);
        } else {
            if(args[0].equalsIgnoreCase("finish")) {
                String error = EventSystem.get().getTaskManager().finishTask(p);
                if(error != null) p.sendMessage(StringUtils.format("Unable to finish task: "+error));
            } else if(args[0].equalsIgnoreCase("cancel")) {
                String error = EventSystem.get().getTaskManager().cancelTask(p);
                if(error != null) p.sendMessage(StringUtils.format("Unable to cancel task: "+error));
            } else {
                Event e = em.getEventForId(args[0]);
                if (e == null) informEvents(commandSender, em);
                else {

                    String error = es.getTaskManager().beginSetup(p, e);
                    if (error != null) p.sendMessage(StringUtils.warn("Unable to begin event setup: " + error));
                }
            }
        }
        return true;
    }

    private void informEvents(CommandSender sender, EventManager em) {
        sender.sendMessage(StringUtils.info("The currently available events are: &a" + StringUtils.concat(em.getAvailableEvents(), ", ")));
    }
}
