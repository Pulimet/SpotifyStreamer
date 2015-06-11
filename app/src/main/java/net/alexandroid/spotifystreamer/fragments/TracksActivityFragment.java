package net.alexandroid.spotifystreamer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import net.alexandroid.spotifystreamer.R;
import net.alexandroid.spotifystreamer.activities.TracksActivity;
import net.alexandroid.spotifystreamer.adapters.ShowTracksAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TracksActivityFragment extends Fragment {

    public static final String PREVIEW_URL = "preview";
    public static final String BIG_IMG_URL = "big";
    private String artistId, artistName;
    private SpotifyService mSpotifyService;
    private RecyclerView mRecyclerView;
    private ShowTracksAdapter mShowTracksAdapter;
    private List<Track> trackList;

    public TracksActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpotifyApi api = new SpotifyApi();
        mSpotifyService = api.getService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracks, container, false);
        getArtistIdFromIntent();
        ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (mActionBar != null) mActionBar.setSubtitle(artistName);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        setRecyclerView();
        getArtistTracks();
        return rootView;
    }

    private void getArtistIdFromIntent() {
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            artistId = intent.getStringExtra(Intent.EXTRA_TEXT);
            artistName = intent.getStringExtra(Intent.EXTRA_REFERRER_NAME);
        }
    }

    private void setRecyclerView() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        ShowTracksAdapter.ClickListener mClickListener = new ShowTracksAdapter.ClickListener() {
            @Override
            public void onClick(int position) {
                List<Image> imageList = trackList.get(position).album.images;
                String imgUrl = "";
                if (imageList != null && imageList.size() != 0) {
                    imgUrl = imageList.get(0).url;
                }
                String previewUrl = trackList.get(position).preview_url;

                startPlayerActivity(previewUrl, imgUrl);
            }
        };
        mShowTracksAdapter = new ShowTracksAdapter(trackList, mClickListener);
        mRecyclerView.setAdapter(mShowTracksAdapter);
    }

    private void startPlayerActivity(String previewUrl, String imgUrl) {
        // TODO TracksActivity = > PlayerActivity (Stage 2)
        Intent intent = new Intent(getActivity(), TracksActivity.class);
        intent.putExtra(PREVIEW_URL, previewUrl);
        intent.putExtra(BIG_IMG_URL, imgUrl);
        //startActivity(intent);
    }

    private void getArtistTracks() {
        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());
        mSpotifyService.getArtistTopTrack(artistId, options, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                trackList = tracks.tracks;
                getActivity().runOnUiThread(update_recycler_view);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    Runnable update_recycler_view = new Runnable() {
        @Override
        public void run() {
            mShowTracksAdapter.swap(trackList);
        }
    };
}
