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
import net.alexandroid.spotifystreamer.objects.CustomTrack;

import java.util.ArrayList;
import java.util.List;


public class ShowTracksAdapter extends RecyclerView.Adapter<ShowTracksAdapter.ViewHolder> {
    private static final String TAG = "ShowTracksAdapter";

    private List<CustomTrack> customTrackList;
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

    public ShowTracksAdapter(ArrayList<CustomTrack> tracks, ClickListener clickListener) {
        customTrackList = tracks;
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
        String imgUrl = customTrackList.get(position).getSmallImgUrl();
        if (imgUrl.length() > 0) {
            Picasso.with(imageView.getContext()).load(imgUrl).into(imageView);
        }

        // Track name
        viewHolder.getTrackTextView().setText(customTrackList.get(position).getTitle());

        // Album name
        viewHolder.getALbumTextView().setText(customTrackList.get(position).getAlbum());
    }

    public void swap(List<CustomTrack> tracks) {
        customTrackList = tracks;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (customTrackList != null) {
            return customTrackList.size();
        } else {
            return 0;
        }
    }

    public static abstract class ClickListener {
        public abstract void onClick(int position);
    }

}