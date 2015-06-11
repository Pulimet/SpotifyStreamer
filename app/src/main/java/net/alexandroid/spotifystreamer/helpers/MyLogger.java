package net.alexandroid.spotifystreamer.helpers;

import android.util.Log;


public class MyLogger {
    public static void log(String tag, String msg) {
        if (MyApplication.SHOW_LOGS) {
            Log.d(tag, msg);
        }
    }
}


