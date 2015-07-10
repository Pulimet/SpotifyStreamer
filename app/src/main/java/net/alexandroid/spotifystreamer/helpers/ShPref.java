package net.alexandroid.spotifystreamer.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public class ShPref {

    private static final String SAVE = "save001";

    public static void setCountryCode(Context ctx, String countryCode) {
        SharedPreferences settings = ctx.getSharedPreferences(SAVE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("country_code", countryCode);
        editor.apply();
    }

    public static String getCountryCode(Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences(SAVE, Context.MODE_PRIVATE);
        return settings.getString("country_code", Locale.US.getCountry());
    }

    public static void setLockScreenShow(Context ctx, boolean isShowed) {
        SharedPreferences settings = ctx.getSharedPreferences(SAVE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("lock_screen", isShowed);
        editor.apply();
    }

    public static boolean isShowLockScreen(Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences(SAVE, Context.MODE_PRIVATE);
        return settings.getBoolean("lock_screen", true);
    }
}
