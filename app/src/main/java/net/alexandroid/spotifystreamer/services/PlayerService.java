package net.alexandroid.spotifystreamer.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.alexandroid.spotifystreamer.R;
import net.alexandroid.spotifystreamer.activities.PlayerActivity;
import net.alexandroid.spotifystreamer.events.PlayerCtrlEvent;
import net.alexandroid.spotifystreamer.events.UiUpdateEvent;
import net.alexandroid.spotifystreamer.helpers.MyLogger;
import net.alexandroid.spotifystreamer.objects.CustomTrack;

import java.io.IOException;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;


public class PlayerService extends Service {
    public static final String TAG = "ZAQ-Service";
    public static final int NOTIFICATION_ID = 1;
    public static final int SHOW_PLAY = 2;
    public static final int SHOW_PAUSE = 3;
    public static final String ACTION_PLAY = "net.alexandroid.spotifystreamer.action.PLAY";
    public static final String ACTION_PLAY_OR_PAUSE = "net.alexandroid.spotifystreamer.action.PLAYORPAUSE";
    public static final String ACTION_NEXT = "net.alexandroid.spotifystreamer.action.NEXT";
    public static final String ACTION_PREV = "net.alexandroid.spotifystreamer.action.PREV";
    public static final String CUSTOM_TRACK_LIST = "LIST";
    public static final String POSITION = "POSITION";
    public static final String ARTIST_NAME = "ARTIST_NAME";
    private MediaPlayer mMediaPlayer;
    private ArrayList<CustomTrack> customTrackList;
    private int position;
    private String artistName;
    private int resButtonPlay, resButtonPause, resButtonPrev, resButtonNext;
    private Bitmap tempBitmap;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        EventBus.getDefault().register(this);

        resButtonPause = getResources().getIdentifier("@android:drawable/ic_media_pause", null, getPackageName());
        resButtonPlay = getResources().getIdentifier("@android:drawable/ic_media_play", null, getPackageName());
        resButtonPrev = getResources().getIdentifier("@android:drawable/ic_media_previous", null, getPackageName());
        resButtonNext = getResources().getIdentifier("@android:drawable/ic_media_next", null, getPackageName());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_PLAY)) {
            customTrackList = intent.getParcelableArrayListExtra(CUSTOM_TRACK_LIST);
            position = intent.getIntExtra(POSITION, 0);
            artistName = intent.getStringExtra(ARTIST_NAME);
            loadAndPlay();
            startForeground(SHOW_PAUSE);
            MyLogger.log(TAG, "ACTION_PLAY");
        }
        if (intent.getAction().equals(ACTION_PLAY_OR_PAUSE)) {
            playOrPause();
            MyLogger.log(TAG, "ACTION_PLAY_OR_PAUSE");
        }
        if (intent.getAction().equals(ACTION_NEXT)) {
            next();
            MyLogger.log(TAG, "ACTION_NEXT");
        }
        if (intent.getAction().equals(ACTION_PREV)) {
            prev();
            MyLogger.log(TAG, "ACTION_PREV");
        }
        return (START_NOT_STICKY);
    }

    private void startForeground(int show) {
        getBitmap();
        String songName = customTrackList.get(position).getTitle();
        String albumName = customTrackList.get(position).getAlbum();

        Intent intentStartPlayer = new Intent(PlayerService.this, PlayerActivity.class);
        intentStartPlayer.putParcelableArrayListExtra(PlayerService.CUSTOM_TRACK_LIST, customTrackList);
        intentStartPlayer.putExtra(PlayerService.ARTIST_NAME, artistName);
        intentStartPlayer.putExtra(PlayerService.POSITION, position);
        intentStartPlayer.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent piStartPlayer = PendingIntent.getActivity(PlayerService.this, 0, intentStartPlayer, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent piPlayOrPause = getPendingIntent(PlayerService.ACTION_PLAY_OR_PAUSE);
        PendingIntent piNext = getPendingIntent(PlayerService.ACTION_NEXT);
        PendingIntent piPrev = getPendingIntent(PlayerService.ACTION_PREV);

        int playOrPauseBtn;
        if (show == SHOW_PLAY) {
            playOrPauseBtn = resButtonPlay;
        } else {
            playOrPauseBtn = resButtonPause;
        }

        ComponentName c = new ComponentName("net.alexandroid.spotifystreamer", "BackgroundService");
        MediaSessionCompat ms = new MediaSessionCompat(this, "Spotify", c, piStartPlayer);
        /*ms.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        ms.setCallback(new MediaSessionCompat.Callback() {
        });
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
        metadataBuilder
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 10)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "1")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "2")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "3")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, "4")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 10000);
        ms.setMetadata(metadataBuilder.build());

        PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID | PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .setState(PlaybackStateCompat.STATE_PLAYING, position, 10, SystemClock.elapsedRealtime())
                .build();
        ms.setPlaybackState(playbackState);
        ms.setSessionActivity(piStartPlayer);*/
        ms.setActive(true);


        Notification notif = new NotificationCompat.Builder(this)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setTicker(songName)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(tempBitmap)
                .setContentTitle(artistName + " - " + albumName)
                .setContentText(songName)
                .setContentIntent(piStartPlayer)
                .setStyle(new NotificationCompat.MediaStyle()
                        .setMediaSession(ms.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .addAction(resButtonPrev, "", piPrev)
                .addAction(playOrPauseBtn, "", piPlayOrPause)
                .addAction(resButtonNext, "", piNext)
                .build();

        startForeground(NOTIFICATION_ID, notif);
    }

    private void getBitmap() {
        Picasso.with(this).load(customTrackList.get(position).getSmallImgUrl())
                .resize((int) getResources().getDimension(R.dimen.notification_large_icon_width),
                        (int) getResources().getDimension(R.dimen.notification_large_icon_height))
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        tempBitmap = bitmap;
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }

    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(PlayerService.this, PlayerService.class);
        intent.setAction(action);
        return PendingIntent.getService(PlayerService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    // Event handling
    public void onEvent(PlayerCtrlEvent event) {
        switch (event.action) {
            case PlayerCtrlEvent.PLAY_OR_PAUSE:
                playOrPause();
                break;
            case PlayerCtrlEvent.NEXT:
                next();
                break;
            case PlayerCtrlEvent.PREV:
                prev();
                break;
        }
    }

    // Player control
    private void loadAndPlay() {
        startForeground(SHOW_PAUSE);
        String url = customTrackList.get(position).getPreviewUrl();
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
                    EventBus.getDefault().post(new UiUpdateEvent(UiUpdateEvent.PLAY));
                    new Thread(mUpdaterCounter).start();
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    EventBus.getDefault().post(new UiUpdateEvent(UiUpdateEvent.PAUSE));
                    startForeground(SHOW_PLAY);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Runnable mUpdaterCounter = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer.isPlaying()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                EventBus.getDefault().post(new UiUpdateEvent(mMediaPlayer.getCurrentPosition()));
                mUpdaterCounter.run();
            }
        }
    };


    private void play() {
        if (mMediaPlayer != null) {
            MyLogger.log(TAG, "Play");
            mMediaPlayer.start();
            EventBus.getDefault().post(new UiUpdateEvent(UiUpdateEvent.PLAY));
            new Thread(mUpdaterCounter).start();
        }
    }

    private void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            MyLogger.log(TAG, "Pause");
            mMediaPlayer.pause();
            EventBus.getDefault().post(new UiUpdateEvent(UiUpdateEvent.PAUSE));
        }
    }

    private void playOrPause() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                startForeground(SHOW_PLAY);
                pause();
            } else {
                startForeground(SHOW_PAUSE);
                play();
            }
        }
    }

    private void next() {
        if (position < customTrackList.size() - 1) {
            position++;
            loadAndPlay();
        }
    }

    private void prev() {
        if (position > 0) {
            position--;
            loadAndPlay();
        }
    }
}
