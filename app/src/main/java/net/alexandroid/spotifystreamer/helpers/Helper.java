package net.alexandroid.spotifystreamer.helpers;

/**
 * Created by Alexey on 04.07.2015.
 */
public class Helper {

    public static String formatDuration(int duration) {
        int secs = duration / 1000;
        int minutes = secs / 60;
        int seconds = secs - 60 * minutes;
        String zero = "";
        if (seconds < 10) zero = "0";
        return minutes + ":" + zero + seconds;
    }

    public static String formatDuration(String duration) {
        int miliSeconds = Integer.valueOf(duration);
        return formatDuration(miliSeconds);
    }
}
