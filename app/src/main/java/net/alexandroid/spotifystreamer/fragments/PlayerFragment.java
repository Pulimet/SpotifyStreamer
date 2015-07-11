package net.alexandroid.spotifystreamer.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.alexandroid.spotifystreamer.R;
import net.alexandroid.spotifystreamer.activities.MainActivity;
import net.alexandroid.spotifystreamer.events.PlayerCtrlEvent;
import net.alexandroid.spotifystreamer.events.UiUpdateEvent;
import net.alexandroid.spotifystreamer.helpers.Helper;
import net.alexandroid.spotifystreamer.helpers.MyApplication;
import net.alexandroid.spotifystreamer.helpers.MyLogger;
import net.alexandroid.spotifystreamer.objects.CustomTrack;
import net.alexandroid.spotifystreamer.services.PlayerService;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends DialogFragment {

    private String artistName;
    private static int position;
    private int resButtonPlay, resButtonPause;
    private ArrayList<CustomTrack> customTrackList;

    @InjectView(R.id.tvArtist) TextView tvArtist;
    @InjectView(R.id.tvAlbum) TextView tvAlbum;
    @InjectView(R.id.imgAlbum) ImageView imgAlbum;
    @InjectView(R.id.tvSong) TextView tvSong;
    @InjectView(R.id.progressBar) ProgressBar progressBar;
    @InjectView(R.id.tvTime) TextView tvTime;
    @InjectView(R.id.tvDuration) TextView tvDuration;
    @InjectView(R.id.btnPrevious) ImageButton btnPrevious;
    @InjectView(R.id.btnPlay) ImageButton btnPlay;
    @InjectView(R.id.btnNext) ImageButton btnNext;

    public PlayerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.inject(this, rootView);
        resButtonPause = getResources().getIdentifier("@android:drawable/ic_media_pause", null, getActivity().getPackageName());
        resButtonPlay = getResources().getIdentifier("@android:drawable/ic_media_play", null, getActivity().getPackageName());

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(PlayerService.CUSTOM_TRACK_LIST)) {
            customTrackList = intent.getParcelableArrayListExtra(PlayerService.CUSTOM_TRACK_LIST);
            artistName = intent.getStringExtra(PlayerService.ARTIST_NAME);
            if (savedInstanceState == null)
                position = intent.getIntExtra(PlayerService.POSITION, 0);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                customTrackList = arguments.getParcelableArrayList(PlayerService.CUSTOM_TRACK_LIST);
                artistName = arguments.getString(PlayerService.ARTIST_NAME);
                if (savedInstanceState == null)
                    position = arguments.getInt(PlayerService.POSITION, 0);
            }
        }


        if (customTrackList != null) {
            setButtonsClickListener();
            if (savedInstanceState == null) {
                PlayerService.position = -1;
                loadAndPlayCurrentPosition();
            }
        } else {
            MyLogger.log("ZAQ-PlayerFragment", "customTrackList == null");
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (customTrackList != null) {
            if (PlayerService.position > -1) position = PlayerService.position;
            setTrackData();
        }
    }

    private void setButtonsClickListener() {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnPlay:
                        EventBus.getDefault().post(new PlayerCtrlEvent(PlayerCtrlEvent.PLAY_OR_PAUSE));
                        break;
                    case R.id.btnNext:
                        if (position < customTrackList.size() - 1) {
                            EventBus.getDefault().post(new PlayerCtrlEvent(PlayerCtrlEvent.NEXT));
                            position++;
                            setTrackData();
                        }
                        break;
                    case R.id.btnPrevious:
                        if (position > 0) {
                            EventBus.getDefault().post(new PlayerCtrlEvent(PlayerCtrlEvent.PREV));
                            position--;
                            setTrackData();
                        }
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
        int duration = Integer.valueOf(customTrackList.get(position).getDuration());
        tvDuration.setText(Helper.formatDuration(duration));
        progressBar.setMax(duration / 1000);
    }


    private void loadAndPlayCurrentPosition() {
        Intent intent = new Intent(getActivity(), PlayerService.class);
        intent.setAction(PlayerService.ACTION_PLAY);
        intent.putParcelableArrayListExtra(PlayerService.CUSTOM_TRACK_LIST, customTrackList);
        intent.putExtra(PlayerService.ARTIST_NAME, artistName);
        intent.putExtra(PlayerService.POSITION, position);
        getActivity().startService(intent);
    }


    public void onEventMainThread(UiUpdateEvent event) {
        switch (event.duration) {
            case UiUpdateEvent.PAUSE:
                btnPlay.setImageResource(resButtonPlay);
                break;
            case UiUpdateEvent.PLAY:
                btnPlay.setImageResource(resButtonPause);
                break;
            default:
                tvTime.setText(Helper.formatDuration(event.duration));
                progressBar.setProgress(event.duration / 1000);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        MyApplication.setPlayerVisibility(true);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        MyApplication.setPlayerVisibility(false);
        super.onStop();
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


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_tracks_f, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        if (!MainActivity.sWideScreen) {
            item.setVisible(true);
            ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            mShareActionProvider.setShareIntent(createShareIntent());
        }
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        String title = customTrackList.get(position).getTitle();
        String url = customTrackList.get(position).getPreviewUrl();
        shareIntent.putExtra(Intent.EXTRA_TEXT, artistName + " - " + title + ": " + url);
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
