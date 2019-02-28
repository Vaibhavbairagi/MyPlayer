package com.vaibhav.myplayer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

public class Songs implements Parcelable {
    public Long songID;
    public String songTitle, artist, songData;
    public Long dateAdded;

    public static Comparator<Songs> nameComparator = new Comparator<Songs>() {
        @Override
        public int compare(Songs song1, Songs song2) {
            String songOne = song1.songTitle.toUpperCase();
            String songTwo = song2.songTitle.toUpperCase();
            return songOne.compareTo(songTwo);
        }
    };
    public static Comparator<Songs> dateComparator = new Comparator<Songs>() {
        @Override
        public int compare(Songs song1, Songs song2) {
            String songOne = String.valueOf(song1.dateAdded);
            String songTwo = String.valueOf(song2.dateAdded);
            return songTwo.compareTo(songOne);
        }
    };

    public Songs(Long currentId, String currentTitle, String currentArtist, String currentData, Long currentDate) {
        this.songID = currentId;
        this.songTitle = currentTitle;
        this.artist = currentArtist;
        this.songData = currentData;
        this.dateAdded = currentDate;
    }

    protected Songs(Parcel in) {
        if (in.readByte() == 0) {
            songID = null;
        } else {
            songID = in.readLong();
        }
        songTitle = in.readString();
        artist = in.readString();
        songData = in.readString();
        if (in.readByte() == 0) {
            dateAdded = null;
        } else {
            dateAdded = in.readLong();
        }
    }

    public static final Creator<Songs> CREATOR = new Creator<Songs>() {
        @Override
        public Songs createFromParcel(Parcel in) {
            return new Songs(in);
        }

        @Override
        public Songs[] newArray(int size) {
            return new Songs[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (songID == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(songID);
        }
        parcel.writeString(songTitle);
        parcel.writeString(artist);
        parcel.writeString(songData);
        if (dateAdded == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(dateAdded);
        }
    }
}
