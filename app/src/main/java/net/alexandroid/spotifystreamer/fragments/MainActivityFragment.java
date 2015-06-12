package net.alexandroid.spotifystreamer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import net.alexandroid.spotifystreamer.R;
import net.alexandroid.spotifystreamer.activities.TracksActivity;
import net.alexandroid.spotifystreamer.adapters.ShowArtistsAdapter;
import net.alexandroid.spotifystreamer.helpers.MyLogger;
import net.alexandroid.spotifystreamer.objects.CustomArtist;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivityFragment extends Fragment {

    private static final String TAG = "MainActivityFragment";

    private boolean boolSuspend;

    private ArrayList<CustomArtist> customArtistList = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private ShowArtistsAdapter mShowArtistsAdapter;
    private EditText mEditText;
    private SpotifyService mSpotifyService;
    private Toast mToast;
    private ProgressBar mPogressBar;


    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpotifyApi api = new SpotifyApi();
        mSpotifyService = api.getService();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("artist_list", customArtistList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        setViews(rootView);
        setRecyclerView();
        setEditTextListener();

        if (savedInstanceState != null) {
            customArtistList = savedInstanceState.getParcelableArrayList("artist_list");
            mShowArtistsAdapter.swap(customArtistList);
            boolSuspend = true;
        }

        return rootView;
    }

    private void setViews(View rootView) {
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mEditText = (EditText) rootView.findViewById(R.id.editText);
        mPogressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
    }

    private void setRecyclerView() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        ShowArtistsAdapter.ClickListener mClickListener = new ShowArtistsAdapter.ClickListener() {
            @Override
            public void onClick(int position) {
                startTracksActivity(customArtistList.get(position).getId(), customArtistList.get(position).getName());
            }
        };
        mShowArtistsAdapter = new ShowArtistsAdapter(customArtistList, mClickListener);
        mRecyclerView.setAdapter(mShowArtistsAdapter);
    }

    private void startTracksActivity(String artistId, String artistName) {
        Intent intent = new Intent(getActivity(), TracksActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, artistId);
        intent.putExtra(Intent.EXTRA_REFERRER_NAME, artistName);
        startActivity(intent);
    }

    private void setEditTextListener() {
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                MyLogger.log(TAG, "Text: " + s.toString());
                if (boolSuspend) {
                    boolSuspend = false;
                } else {
                    setProgressBarVisibility(true);
                    customArtistList.clear();
                    mShowArtistsAdapter.swap(customArtistList);
                    getArtists(s.toString());
                }
            }
        });
    }

    private void getArtists(String artistStr) {
        if (mToast != null) mToast.cancel();

        if (artistStr.length() > 0) {
            mSpotifyService.searchArtists(artistStr, new Callback<ArtistsPager>() {
                @Override
                public void success(ArtistsPager artistsPager, Response response) {
                    List<Artist> artistsList = artistsPager.artists.items;
                    if (artistsList == null || artistsList.size() == 0) {
                        getActivity().runOnUiThread(showToastNotFound);
                    } else {
                        customArtistList.clear();
                        for (Artist artist : artistsList) {
                            List<Image> imageList = artist.images;
                            String smallImgUrl = "";
                            if (imageList != null && imageList.size() != 0) {
                                smallImgUrl = imageList.get(imageList.size() - 1).url;
                            }
                            customArtistList.add(new CustomArtist(artist.name, smallImgUrl, artist.id));
                        }
                    }

                    getActivity().runOnUiThread(updateRecyclerView);
                }

                @Override
                public void failure(RetrofitError error) {
                    getActivity().runOnUiThread(showToastNotFound);
                }
            });
        } else {
            customArtistList.clear();
            setProgressBarVisibility(false);
            mShowArtistsAdapter.swap(customArtistList);
        }
    }

    private Runnable updateRecyclerView = new Runnable() {
        @Override
        public void run() {
            setProgressBarVisibility(false);
            mShowArtistsAdapter.swap(customArtistList);
        }
    };

    private Runnable showToastNotFound = new Runnable() {
        @Override
        public void run() {
            if (mToast != null) mToast.cancel();
            setProgressBarVisibility(false);
            mToast = Toast.makeText(getActivity(), getResources().getString(R.string.toast_not_found), Toast.LENGTH_SHORT);
            mToast.show();
        }
    };

    private void setProgressBarVisibility(boolean visible) {
        if (visible) {
            mPogressBar.setVisibility(View.VISIBLE);
        } else {
            mPogressBar.setVisibility(View.GONE);
        }
    }
}
