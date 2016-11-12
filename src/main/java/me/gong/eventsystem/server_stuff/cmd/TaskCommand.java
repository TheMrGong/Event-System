package me.gong.eventsystem.server_stuff.cmd;

import me.gong.eventsystem.EventSystem;
import me.gong.eventsystem.events.task.TaskManager;
import me.gong.eventsystem.util.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TaskCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command cmd, String label, String[] args) {
        if(!commandSender.hasPermission("eventsystem.admin")) {
            commandSender.sendMessage(StringUtils.warn("You need permission to execute this command"));
            return true;
        }
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            TaskManager tm = EventSystem.get().getTaskManager();
            if (args.length == 0) p.sendMessage(StringUtils.warn("Usage: &a/task <id | list>"));
            else if (args[0].equalsIgnoreCase("list")) {
                String error = tm.listTasks(p);
                if(error != null) p.sendMessage(StringUtils.warn("Unable to list tasks: " + error));
            } else {

                String error = tm.changeTaskId(p, args[0]);
                if (error != null) p.sendMessage(StringUtils.warn("Unable to change tasks: " + error));
            }
        } else commandSender.sendMessage(StringUtils.info("Unable to setup events as console"));
        return true;
    }
}
