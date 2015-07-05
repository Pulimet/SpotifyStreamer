package net.alexandroid.spotifystreamer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
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
    private Bitmap tempBitmap;
    private PendingIntent piPlayOrPause, piNext, piPrev;
    private MediaSessionCompat mMediaSession;
    private boolean showControlsAtLockScreen = true;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        EventBus.getDefault().register(this);


        piPlayOrPause = getPendingIntent(PlayerService.ACTION_PLAY_OR_PAUSE);
        piNext = getPendingIntent(PlayerService.ACTION_NEXT);
        piPrev = getPendingIntent(PlayerService.ACTION_PREV);

        setMediaSession();
    }

    private void setMediaSession() {
        ComponentName c = new ComponentName("net.alexandroid.spotifystreamer", "BackgroundService");
        mMediaSession = new MediaSessionCompat(this, "Spotify", c, getPendingIntentForPlayer());
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
/*        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
        });*/

/*        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
        metadataBuilder
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 10)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "1")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "2")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "3")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, "4")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 10000);
        mMediaSession.setMetadata(metadataBuilder.build());*/

/*        PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID | PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .setState(PlaybackStateCompat.STATE_PLAYING, position, 10, SystemClock.elapsedRealtime())
                .build();
        mMediaSession.setPlaybackState(playbackState);
        mMediaSession.setSessionActivity(getPendingIntentForPlayer());
        mMediaSession.setActive(true);*/
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_PLAY)) {
            customTrackList = intent.getParcelableArrayListExtra(CUSTOM_TRACK_LIST);
            position = intent.getIntExtra(POSITION, 0);
            artistName = intent.getStringExtra(ARTIST_NAME);
            loadAndPlay();
            startForeground();
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

    private void startForeground() {
        getBitmap();

/*        int playOrPauseBtn;
        if (show == SHOW_PLAY) {
            playOrPauseBtn = resButtonPlay;
        } else {
            playOrPauseBtn = resButtonPause;
        }


        Notification notif = new NotificationCompat.Builder(this)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setTicker(songName)
                .setLargeIcon(tempBitmap)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(artistName + " - " + albumName)
                .setContentText(songName)
                .setContentIntent(getPendingIntentForPlayer())
                .setStyle(new NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .addAction(resButtonPrev, "", piPrev)
                .addAction(playOrPauseBtn, "", piPlayOrPause)
                .addAction(resButtonNext, "", piNext)
                .build();*/

        startForeground(NOTIFICATION_ID, getNotifiaction());
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

    private Notification getNotifiaction() {
        String songName = customTrackList.get(position).getTitle();
        String albumName = customTrackList.get(position).getAlbum();

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this);
        notifBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notifBuilder.setContentTitle(artistName + " - " + albumName);
        notifBuilder.setContentText(songName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && showControlsAtLockScreen) {
            notifBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        int playOrPauseBtn;
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            playOrPauseBtn = android.R.drawable.ic_media_play;
        } else {
            playOrPauseBtn = android.R.drawable.ic_media_pause;
        }

        notifBuilder.addAction(android.R.drawable.ic_media_previous, "", piPrev);
        notifBuilder.addAction(playOrPauseBtn, "", piPlayOrPause);
        notifBuilder.addAction(android.R.drawable.ic_media_next, "", piNext);

        notifBuilder.setContentIntent(getPendingIntentForPlayer());



        return notifBuilder.build();
    }

    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(PlayerService.this, PlayerService.class);
        intent.setAction(action);
        return PendingIntent.getService(PlayerService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getPendingIntentForPlayer() {
        Intent intentStartPlayer = new Intent(PlayerService.this, PlayerActivity.class);
        intentStartPlayer.putParcelableArrayListExtra(PlayerService.CUSTOM_TRACK_LIST, customTrackList);
        intentStartPlayer.putExtra(PlayerService.ARTIST_NAME, artistName);
        intentStartPlayer.putExtra(PlayerService.POSITION, position);
        intentStartPlayer.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(PlayerService.this, 0, intentStartPlayer, PendingIntent.FLAG_UPDATE_CURRENT);
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
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
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
        startForeground();
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
                    startForeground();
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
                startForeground();
                pause();
            } else {
                startForeground();
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
