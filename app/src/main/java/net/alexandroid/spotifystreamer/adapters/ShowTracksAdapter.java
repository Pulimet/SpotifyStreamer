package net.alexandroid.spotifystreamer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.alexandroid.spotifystreamer.R;
import net.alexandroid.spotifystreamer.helpers.MyLogger;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class ShowTracksAdapter extends RecyclerView.Adapter<ShowTracksAdapter.ViewHolder> {
    private static final String TAG = "ShowTracksAdapter";
    private List<Track> listOfTracks;
    private static ClickListener mClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTrackName, tvAlbumName;
        private final ImageView imageView;


        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyLogger.log(TAG, "Element " + getPosition());
                    mClickListener.onClick(getPosition());
                }
            });
            tvTrackName = (TextView) v.findViewById(R.id.tvTrackName);
            tvAlbumName = (TextView) v.findViewById(R.id.tvAlbumName);
            imageView = (ImageView) v.findViewById(R.id.imageView);
        }

        public TextView getTrackTextView() {
            return tvTrackName;
        }

        public TextView getALbumTextView() {
            return tvAlbumName;
        }

        public ImageView getImageView() {
            return imageView;
        }
    }

    public ShowTracksAdapter(List<Track> tracks, ClickListener clickListener) {
        listOfTracks = tracks;
        mClickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_track_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Album image
        ImageView imageView = viewHolder.getImageView();
        List<Image> imageList = listOfTracks.get(position).album.images;
        if (imageList != null && imageList.size() != 0) {
            String url = imageList.get(imageList.size() - 1).url;
            Picasso.with(imageView.getContext()).load(url).into(imageView);
        }

        // Track name
        viewHolder.getTrackTextView().setText(listOfTracks.get(position).name);

        // Album name
        viewHolder.getALbumTextView().setText(listOfTracks.get(position).album.name);
    }

    public void swap(List<Track> tracks) {
        if (listOfTracks == null) {
            listOfTracks = tracks;
        } else {
            listOfTracks.clear();
            listOfTracks.addAll(tracks);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (listOfTracks != null) {
            return listOfTracks.size();
        } else {
            return 0;
        }
    }

    public static abstract class ClickListener {
        public abstract void onClick(int position);
    }

}