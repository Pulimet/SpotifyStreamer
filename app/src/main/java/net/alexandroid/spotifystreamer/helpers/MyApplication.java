package net.alexandroid.spotifystreamer.helpers;

import android.app.Application;

public class MyApplication extends Application {
    // ----- DEBUG ------------------------------------
    public static final boolean SHOW_LOGS = true;
    // -----------------------------------------------

    public static boolean isPlayerVisible;

    public static void setPlayerVisibility(boolean isPlayerVisible) {
        MyApplication.isPlayerVisible = isPlayerVisible;
    }

    public static boolean isMainActivityVisible;
    public static void setMainActivityVisibility(boolean isPlayerVisible) {
        MyApplication.isMainActivityVisible = isPlayerVisible;
    }


}
