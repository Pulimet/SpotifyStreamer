package net.alexandroid.spotifystreamer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import net.alexandroid.spotifystreamer.R;
import net.alexandroid.spotifystreamer.activities.PlayerActivity;
import net.alexandroid.spotifystreamer.events.PlayerCtrlEvent;
import net.alexandroid.spotifystreamer.events.UiUpdateEvent;
import net.alexandroid.spotifystreamer.helpers.MyApplication;
import net.alexandroid.spotifystreamer.helpers.MyLogger;
import net.alexandroid.spotifystreamer.helpers.ShPref;
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
    public static int position = -1;
    private String artistName;
    private Bitmap tempBitmap;
    private PendingIntent piPlayOrPause, piNext, piPrev;
    private MediaSessionCompat mMediaSession;

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
/*        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, getNotification());*/
        startForeground(NOTIFICATION_ID, getNotification());
    }

    private Notification getNotification() {
        RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.notif_control);
        remoteView.setTextViewText(R.id.track_name, customTrackList.get(position).getTitle());
        remoteView.setTextViewText(R.id.artist_name, artistName);

        remoteView.setOnClickPendingIntent(R.id.play_previous, piPrev);

        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            remoteView.setViewVisibility(R.id.pause, View.GONE);
            remoteView.setViewVisibility(R.id.resume, View.VISIBLE);
            remoteView.setOnClickPendingIntent(R.id.resume, piPlayOrPause);
        } else {
            remoteView.setViewVisibility(R.id.resume, View.GONE);
            remoteView.setViewVisibility(R.id.pause, View.VISIBLE);
            remoteView.setOnClickPendingIntent(R.id.pause, piPlayOrPause);
        }

        remoteView.setOnClickPendingIntent(R.id.play_next, piNext);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContent(remoteView)
                .setContentIntent(getPendingIntentForPlayer());


        MyLogger.log(TAG, "ShPref.isShowLockScreen: " + ShPref.isShowLockScreen(getApplicationContext()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ShPref.isShowLockScreen(getApplicationContext())) {
                notifBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
            } else {
                notifBuilder.setVisibility(Notification.VISIBILITY_SECRET);
            }
        }

        Notification notification = notifBuilder.build();

        String imgUrl = customTrackList.get(position).getSmallImgUrl();
        if (imgUrl != null && imgUrl.length() > 1) {
            Picasso.with(this).load(imgUrl).into(remoteView, R.id.album_img, NOTIFICATION_ID, notification);
        }

        return notification;
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
            case PlayerCtrlEvent.KILL_SERVER:
                stopForeground(true);
                stopSelf();
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
                    if (MyApplication.isPlayerVisible) {
                        EventBus.getDefault().post(new UiUpdateEvent(UiUpdateEvent.PLAY));
                    }
                    new Thread(mUpdaterCounter).start();
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (MyApplication.isPlayerVisible) {
                        EventBus.getDefault().post(new UiUpdateEvent(UiUpdateEvent.PAUSE));
                    }
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
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                if (MyApplication.isPlayerVisible) {
                    EventBus.getDefault().post(new UiUpdateEvent(mMediaPlayer.getCurrentPosition()));
                }
                mUpdaterCounter.run();
            }

        }
    };


    private void play() {
        if (mMediaPlayer != null) {
            MyLogger.log(TAG, "Play");
            mMediaPlayer.start();
            if (MyApplication.isPlayerVisible) {
                EventBus.getDefault().post(new UiUpdateEvent(UiUpdateEvent.PLAY));
            }
            new Thread(mUpdaterCounter).start();
        }
    }

    private void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            MyLogger.log(TAG, "Pause");
            mMediaPlayer.pause();
            if (MyApplication.isPlayerVisible) {
                EventBus.getDefault().post(new UiUpdateEvent(UiUpdateEvent.PAUSE));
            }
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
