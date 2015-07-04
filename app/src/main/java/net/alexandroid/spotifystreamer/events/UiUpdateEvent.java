package net.alexandroid.spotifystreamer.events;

/**
 * Created by Alexey on 04.07.2015.
 */
public class UiUpdateEvent {
    public static final int PAUSE = -1;
    public static final int PLAY = -2;
    public final int duration;

    public UiUpdateEvent(int duration) {
        this.duration = duration;
    }
}
