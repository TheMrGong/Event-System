package me.gong.eventsystem.util;

import org.bukkit.ChatColor;

import java.util.Collection;

public final class StringUtils {

    private static final String basicPrefix = format("&8&l> ");

    public static String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String info(String message) {
        return format(basicPrefix+"&7"+message);
    }

    public static String warn(String message) {
        return format(basicPrefix+"&e"+message);
    }

    public static <T> String concat(Collection<T> objects) {
        return concat(objects, 0);
    }

    public static <T> String concat(Collection<T> objects, int beginIndex) {
        return concat(objects, " ", beginIndex);
    }

    public static <T> String concat(Collection<T> objects, String separator) {
        return concat(objects, separator, 0);
    }

    public static <T> String concat(Collection<T> objects, String separator, int beginIndex) {
        return concat(objects.toArray(new Object[objects.size()]), separator, beginIndex);
    }

    public static <T> String concat(T[] objects) {
        return concat(objects, 0);
    }

    public static <T> String concat(T[] objects, int beginIndex) {
        return concat(objects, " ", beginIndex);
    }

    public static <T> String concat(T[] objects, String separator) {
        return concat(objects, separator, 0);
    }

    public static <T> String concat(T[] objects, String separator, int beginIndex) {
        if (objects.length == 0) {
            return "";
        }

        int index = -1;
        StringBuilder ret = new StringBuilder();

        for (Object s : objects) {
            index++;

            if (index >= beginIndex) if (s.toString().length() > 0) {
                ret.append(separator).append(s);
            }
        }

        return ret.length() >= separator.length() ? ret.substring(separator.length()) : ret.toString();
    }

}
