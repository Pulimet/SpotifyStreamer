package net.alexandroid.spotifystreamer.objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Alexey on 12.06.2015.
 */
public class CustomArtist implements Parcelable {

    String name, image, id;

    public CustomArtist(String name, String image, String id) {
        this.name = name;
        this.image = image;
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(image);
        dest.writeString(id);
    }

    public static final Creator<CustomArtist> CREATOR = new Creator<CustomArtist>() {
        public CustomArtist createFromParcel(Parcel in) {
            return new CustomArtist(in);
        }

        public CustomArtist[] newArray(int size) {
            return new CustomArtist[size];
        }
    };

    private CustomArtist(Parcel parcel) {
        name = parcel.readString();
        image = parcel.readString();
        id = parcel.readString();
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getId() {
        return id;
    }
}
