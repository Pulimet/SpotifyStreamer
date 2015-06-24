package net.alexandroid.spotifystreamer.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.alexandroid.spotifystreamer.R;
import net.alexandroid.spotifystreamer.objects.CustomTrack;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends DialogFragment {

    private String artistName;
    private int position, resButtonPlay, resButtonPause;
    private ArrayList<CustomTrack> customTrackList;
    private MediaPlayer mediaPlayer;

    @InjectView(R.id.tvArtist)
    TextView tvArtist;
    @InjectView(R.id.tvAlbum)
    TextView tvAlbum;
    @InjectView(R.id.imgAlbum)
    ImageView imgAlbum;
    @InjectView(R.id.tvSong)
    TextView tvSong;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    @InjectView(R.id.tvTime)
    TextView tvTime;
    @InjectView(R.id.tvDuration)
    TextView tvDuration;
    @InjectView(R.id.btnPrevious)
    ImageButton btnPrevious;
    @InjectView(R.id.btnPlay)
    ImageButton btnPlay;
    @InjectView(R.id.btnNext)
    ImageButton btnNext;

    public PlayerFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.inject(this, rootView);
        resButtonPause = getResources().getIdentifier("@android:drawable/ic_media_pause", null, getActivity().getPackageName());
        resButtonPlay = getResources().getIdentifier("@android:drawable/ic_media_play", null, getActivity().getPackageName());

        // TODO Find another solution
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_STREAM)) {
            customTrackList = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            artistName = intent.getStringExtra(Intent.EXTRA_REFERRER_NAME);
            position = intent.getIntExtra(Intent.EXTRA_SHORTCUT_NAME, 0);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                customTrackList = arguments.getParcelableArrayList(Intent.EXTRA_STREAM);
                artistName = arguments.getString(Intent.EXTRA_REFERRER_NAME);
                position = arguments.getInt(Intent.EXTRA_SHORTCUT_NAME, 0);
            }
        }

        setTrackData();
        setButtonsClickListener();
        loadAndPlayCurrentPosition();
        return rootView;
    }


    private void setButtonsClickListener() {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnPlay:
                        playOrPause();
                        break;
                    case R.id.btnNext:
                        if (position < customTrackList.size() - 1) {
                            position++;
                            setTrackData();
                        }
                        loadAndPlayCurrentPosition();
                        break;
                    case R.id.btnPrevious:
                        if (position > 0) {
                            position--;
                            setTrackData();
                        }
                        loadAndPlayCurrentPosition();
                        break;
                }
            }
        };
        btnNext.setOnClickListener(clickListener);
        btnPlay.setOnClickListener(clickListener);
        btnPrevious.setOnClickListener(clickListener);
    }

    private void setTrackData() {
        tvArtist.setText(artistName);
        tvAlbum.setText(customTrackList.get(position).getAlbum());
        String imgUrl = customTrackList.get(position).getBigImgUrl();
        if (imgUrl.length() > 0) {
            Picasso.with(getActivity()).load(imgUrl).into(imgAlbum);
        }
        tvSong.setText(customTrackList.get(position).getTitle());
        tvDuration.setText(formatDuration(customTrackList.get(position).getDuration()));
    }

    private String formatDuration(String duration) {
        int miliSeconds = Integer.valueOf(duration);
        int secs = miliSeconds / 1000;
        int minutes = secs / 60;
        int seconds = secs - 60 * minutes;
        String zero = "";
        if (seconds < 10) zero = "0";
        return minutes + ":" + zero + seconds;
    }


    public void loadAndPlayCurrentPosition() {
        try {
            btnPlay.setImageResource(resButtonPause);
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            } else {
                mediaPlayer.reset();
            }
            String url = customTrackList.get(position).getPreviewUrl();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    btnPlay.setImageResource(resButtonPlay);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playOrPause() {
        if (mediaPlayer == null) {
            loadAndPlayCurrentPosition();
        } else if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlay.setImageResource(resButtonPlay);
        } else {
            mediaPlayer.start();
            btnPlay.setImageResource(resButtonPause);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        btnPlay.setImageResource(resButtonPlay);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    // The system calls this only when creating the layout in a dialog.
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

}
