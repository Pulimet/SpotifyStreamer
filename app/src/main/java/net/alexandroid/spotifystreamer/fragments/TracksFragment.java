package net.alexandroid.spotifystreamer.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.alexandroid.spotifystreamer.R;
import net.alexandroid.spotifystreamer.activities.MainActivity;
import net.alexandroid.spotifystreamer.activities.PlayerActivity;
import net.alexandroid.spotifystreamer.adapters.ShowTracksAdapter;
import net.alexandroid.spotifystreamer.helpers.MyLogger;
import net.alexandroid.spotifystreamer.objects.CustomTrack;
import net.alexandroid.spotifystreamer.services.PlayerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TracksFragment extends Fragment {

    private static final String TAG = "TracksFragment";

    private String artistId, artistName;
    private ArrayList<CustomTrack> customTrackList = new ArrayList<>();
    private SpotifyService mSpotifyService;
    private ShowTracksAdapter mShowTracksAdapter;

    @InjectView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @InjectView(R.id.progressBar)
    ProgressBar mPogressBar;
    @InjectView(R.id.tv_tacks_not_found)
    TextView tvNotFound;

    public TracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            SpotifyApi api = new SpotifyApi();
            mSpotifyService = api.getService();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("track_list", customTrackList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracks, container, false);
        ButterKnife.inject(this, rootView);
        getArtistIdFromIntent();
        setActionBarSubTitle();
        setRecyclerView();
        if (savedInstanceState == null) {
            MyLogger.log(TAG, "get the artist tracks from api");
            getArtistTracks();
        } else {
            MyLogger.log(TAG, "get the artist tracks from savedInstanceState");
            customTrackList = savedInstanceState.getParcelableArrayList("track_list");
            setProgressBarVisibility(false);
            mShowTracksAdapter.swap(customTrackList);
        }
        return rootView;
    }

    private void getArtistIdFromIntent() {
        // TODO Find another solution
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            artistId = intent.getStringExtra(Intent.EXTRA_TEXT);
            artistName = intent.getStringExtra(Intent.EXTRA_REFERRER_NAME);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                artistId = arguments.getString(Intent.EXTRA_TEXT);
                artistName = arguments.getString(Intent.EXTRA_REFERRER_NAME);
            }
        }
    }

    private void setActionBarSubTitle() {
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        ActionBar mActionBar = appCompatActivity.getSupportActionBar();
        if (mActionBar != null) mActionBar.setSubtitle(artistName);
    }

    private void setRecyclerView() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        ShowTracksAdapter.ClickListener mClickListener = new ShowTracksAdapter.ClickListener() {
            @Override
            public void onClick(int position) {
                startPlayerActivity(position);
            }
        };
        mShowTracksAdapter = new ShowTracksAdapter(customTrackList, mClickListener);
        mRecyclerView.setAdapter(mShowTracksAdapter);
    }

    private void startPlayerActivity(int position) {
        if (MainActivity.sWideScreen) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            Bundle args = new Bundle();
            args.putParcelableArrayList(PlayerService.CUSTOM_TRACK_LIST, customTrackList);
            args.putString(PlayerService.ARTIST_NAME, artistName);
            args.putInt(PlayerService.POSITION, position);
            PlayerFragment playerFragment = new PlayerFragment();
            playerFragment.setArguments(args);
            playerFragment.show(fragmentManager, "dialog");
        } else {
            Intent intent = new Intent(getActivity(), PlayerActivity.class);
            intent.putParcelableArrayListExtra(PlayerService.CUSTOM_TRACK_LIST, customTrackList);
            intent.putExtra(PlayerService.ARTIST_NAME, artistName);
            intent.putExtra(PlayerService.POSITION, position);
            startActivity(intent);
        }
    }

    private void getArtistTracks() {
        Map<String, Object> options = new HashMap<>();
        // TODO change back
        //options.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());
        options.put(SpotifyService.COUNTRY, Locale.US.getCountry());
        mSpotifyService.getArtistTopTrack(artistId, options, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                if (tracks.tracks != null && tracks.tracks.size() > 0) {
                    for (Track track : tracks.tracks) {
                        List<Image> imageList = track.album.images;
                        String bigImgUrl = "";
                        String smallImgUrl = "";
                        if (imageList != null && imageList.size() != 0) {
                            bigImgUrl = "";
                            for (Image image : imageList) {
                                if (image.width < 700 && bigImgUrl.length() == 0) {
                                    bigImgUrl = image.url;
                                }
                            }
                            smallImgUrl = imageList.get(imageList.size() - 1).url;
                        }
                        customTrackList.add(new CustomTrack(track.name, track.album.name, smallImgUrl, bigImgUrl,
                                track.preview_url, String.valueOf(track.duration_ms)));
                    }
                } else {
                    getActivity().runOnUiThread(showNoResultsMessage);
                }
                getActivity().runOnUiThread(updateRecyclerView);
            }

            @Override
            public void failure(RetrofitError error) {
                getActivity().runOnUiThread(showNoResultsMessage);
            }
        });
    }

    private Runnable updateRecyclerView = new Runnable() {
        @Override
        public void run() {
            setProgressBarVisibility(false);
            mShowTracksAdapter.swap(customTrackList);
        }
    };

    private Runnable showNoResultsMessage = new Runnable() {
        @Override
        public void run() {
            setProgressBarVisibility(false);
            tvNotFound.setVisibility(View.VISIBLE);
        }
    };

    private void setProgressBarVisibility(boolean visible) {
        if (visible) {
            mPogressBar.setVisibility(View.VISIBLE);
        } else {
            mPogressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }


}
