package net.alexandroid.spotifystreamer.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;

import net.alexandroid.spotifystreamer.R;
import net.alexandroid.spotifystreamer.activities.PlayerActivity;
import net.alexandroid.spotifystreamer.helpers.MyLogger;
import net.alexandroid.spotifystreamer.objects.CustomTrack;

import java.io.IOException;
import java.util.ArrayList;


public class PlayerService extends Service {
    public static final String TAG = "ZAQ-Service";
    public static final int NOTIFICATION_ID = 1;
    public static final String ACTION_PLAY = "net.alexandroid.spotifystreamer.action.PLAY";
    public static final String ACTION_PAUSE = "net.alexandroid.spotifystreamer.action.PAUSE";
    public static final String URL = "URL";
    public static final String CUSTOM_TRACK_LIST = "LIST";
    public static final String POSITION = "POSITION";
    public static final String ARTIST_NAME = "ARTIST_NAME";
    private MediaPlayer mMediaPlayer;
    private String currentUrl;
    private ArrayList<CustomTrack> customTrackList;
    private int position;
    private String artistName;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_PLAY)) {

            customTrackList = intent.getParcelableArrayListExtra(CUSTOM_TRACK_LIST);
            position = intent.getIntExtra(POSITION, 0);
            artistName = intent.getStringExtra(ARTIST_NAME);
            String url = customTrackList.get(position).getPreviewUrl();
            if (url.equals(currentUrl)) {
                if (mMediaPlayer != null) {
                    MyLogger.log(TAG, "Play");
                    mMediaPlayer.start();
                }
            } else {
                MyLogger.log(TAG, "Load and Play");
                currentUrl = url;
                play(url);
            }
            startForeground();
        }
        if (intent.getAction().equals(ACTION_PAUSE)) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                MyLogger.log(TAG, "Pause");
                mMediaPlayer.pause();
            }
        }
        return (START_NOT_STICKY);
    }

    private void startForeground() {
        String songName = customTrackList.get(position).getTitle();
        Intent intent = new Intent(PlayerService.this, PlayerActivity.class);
        intent.putParcelableArrayListExtra(PlayerService.CUSTOM_TRACK_LIST, customTrackList);
        intent.putExtra(PlayerService.ARTIST_NAME, artistName);
        intent.putExtra(PlayerService.POSITION, position);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(PlayerService.this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification();
        notification.tickerText = songName;
        notification.icon = R.mipmap.ic_launcher;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.setLatestEventInfo(getApplicationContext(), artistName + " - " + customTrackList.get(position).getAlbum(),
                "Playing: " + songName, pi);
        startForeground(NOTIFICATION_ID, notification);
    }

    private void play(String url) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //btnPlay.setImageResource(resButtonPlay);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
