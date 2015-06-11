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

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

public class ShowArtistsAdapter extends RecyclerView.Adapter<ShowArtistsAdapter.ViewHolder> {
    private static final String TAG = "ShowArtistsAdapter";

    private List<Artist> listOfArtists;
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

    public ShowArtistsAdapter(List<Artist> artists, ClickListener clickListener) {
        listOfArtists = artists;
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
        List<Image> imageList = listOfArtists.get(position).images;
        if (imageList != null && imageList.size() != 0) {
            String url = imageList.get(imageList.size() - 1).url;
            Picasso.with(imageView.getContext()).load(url).into(imageView);
        }

        // text
        viewHolder.getTextView().setText(listOfArtists.get(position).name);
    }

    public void swap(List<Artist> artists) {
        if (listOfArtists == null) {
            listOfArtists = artists;
        } else {
            listOfArtists.clear();
            listOfArtists.addAll(artists);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (listOfArtists != null) {
            return listOfArtists.size();
        } else {
            return 0;
        }
    }

    public static abstract class ClickListener {
        public abstract void onClick(int position);
    }

}