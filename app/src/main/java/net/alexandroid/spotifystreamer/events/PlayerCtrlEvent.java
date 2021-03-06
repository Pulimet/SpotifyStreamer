package net.alexandroid.spotifystreamer.events;

/**
 * Created by Alexey on 04.07.2015.
 */
public class PlayerCtrlEvent {
    public static final int PLAY = 0;
    public static final int PAUSE = 1;
    public static final int NEXT = 2;
    public static final int PREV = 3;
    public static final int PLAY_OR_PAUSE = 4;
    public static final int KILL_SERVER = 5;
    public static final int REMOVE_NOTIFICATION = 6;
    public static final int SHOW_NOTIFICATION = 7;
    public final int action;

    public PlayerCtrlEvent(int action) {
        this.action = action;
    }
}
