package me.gong.eventsystem.util;

import java.text.DecimalFormat;
import java.util.Random;

public class NumberUtils {

    public static final Random r = new Random();

    public static double trim(int level, double value) {
        StringBuilder sb = new StringBuilder("#.#");
        for(int i=0; i < level; i++) sb.append("#");
        return Double.valueOf(new DecimalFormat(sb.toString()).format(value));
    }

    public static int getRandom(int min, int max) {
        return r.nextInt((max - min) + 1) + min;
    }
}
