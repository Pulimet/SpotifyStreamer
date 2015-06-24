package net.alexandroid.spotifystreamer.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomTrack implements Parcelable {

    String title, album, smallImgUrl, bigImgUrl, previewUrl, duration;

    public CustomTrack(String title, String album, String smallImgUrl, String bigImgUrl, String previewUrl, String duration) {
        this.title = title;
        this.album = album;
        this.smallImgUrl = smallImgUrl;
        this.bigImgUrl = bigImgUrl;
        this.previewUrl = previewUrl;
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(album);
        dest.writeString(smallImgUrl);
        dest.writeString(bigImgUrl);
        dest.writeString(previewUrl);
        dest.writeString(duration);
    }

    public static final Parcelable.Creator<CustomTrack> CREATOR = new Parcelable.Creator<CustomTrack>() {
        public CustomTrack createFromParcel(Parcel in) {
            return new CustomTrack(in);
        }

        public CustomTrack[] newArray(int size) {
            return new CustomTrack[size];
        }
    };

    private CustomTrack(Parcel parcel) {
        title = parcel.readString();
        album = parcel.readString();
        smallImgUrl = parcel.readString();
        bigImgUrl = parcel.readString();
        previewUrl = parcel.readString();
        duration = parcel.readString();
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getSmallImgUrl() {
        return smallImgUrl;
    }

    public String getBigImgUrl() {
        return bigImgUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public String getDuration() {
        return duration;
    }
}
