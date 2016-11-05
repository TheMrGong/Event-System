package me.gong.eventsystem.server_stuff.cmd;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.events.EventManager;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinQuitCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if(commandSender instanceof Player) {
            EventManager em = EventSystem.getInstance().getEventManager();

            if(command.getName().equalsIgnoreCase("join")) {

                String error = em.joinEvent((Player) commandSender, EventManager.ActionCause.MANUAL);
                if (error != null) commandSender.sendMessage(StringUtils.warn("Unable to join event: " + error));
                else commandSender.sendMessage(StringUtils.info("Joined event successfully"));

            } else if(command.getName().equalsIgnoreCase("quit")) {

                String error = em.quitEvent((Player) commandSender, EventManager.ActionCause.MANUAL);
                if (error != null) commandSender.sendMessage(StringUtils.warn("Unable to quit event: " + error));
                else commandSender.sendMessage(StringUtils.info("You have left the event."));
            }
        } else commandSender.sendMessage("Unable to join/quit events as console");
        return true;
    }
}
