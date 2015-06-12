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
import net.alexandroid.spotifystreamer.objects.CustomArtist;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

public class ShowArtistsAdapter extends RecyclerView.Adapter<ShowArtistsAdapter.ViewHolder> {
    private static final String TAG = "ShowArtistsAdapter";

    private List<CustomArtist> customArtistList;
    private static ClickListener mClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
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
            textView = (TextView) v.findViewById(R.id.textView);
            imageView = (ImageView) v.findViewById(R.id.imageView);
        }

        public TextView getTextView() {
            return textView;
        }

        public ImageView getImageView() {
            return imageView;
        }
    }

    public ShowArtistsAdapter(List<CustomArtist> artists, ClickListener clickListener) {
        customArtistList = artists;
        mClickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_artist_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // image
        ImageView imageView = viewHolder.getImageView();
        String url = customArtistList.get(position).getImage();
        if (url.length() > 0) {
            Picasso.with(imageView.getContext()).load(url).into(imageView);
        }

        // text
        viewHolder.getTextView().setText(customArtistList.get(position).getName());
    }

    public void swap(List<CustomArtist> artists) {
        customArtistList = artists;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (customArtistList != null) {
            return customArtistList.size();
        } else {
            return 0;
        }
    }

    public static abstract class ClickListener {
        public abstract void onClick(int position);
    }

}