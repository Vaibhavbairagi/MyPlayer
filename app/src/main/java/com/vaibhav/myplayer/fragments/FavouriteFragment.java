package com.vaibhav.myplayer.fragments;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vaibhav.myplayer.R;
import com.vaibhav.myplayer.Songs;
import com.vaibhav.myplayer.activities.MainActivity;
import com.vaibhav.myplayer.adapters.FavouriteAdapter;
import com.vaibhav.myplayer.databases.EchoDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class FavouriteFragment extends Fragment {
    Activity myActivity;

    RelativeLayout nowPlayingBottomBar = null;
    RecyclerView recyclerView = null;
    ImageButton playPauseButton = null;
    TextView noFavourites = null, songTitle = null;
    int trackPosition = 0;
    public static MediaPlayer mediaPlayer = null;
    EchoDatabase favouriteContent;

    ArrayList<Songs> refreshList = null;
    ArrayList<Songs> getListFromDatabase = null;

    public FavouriteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite, container, false);
        getActivity().setTitle("Favourites");
        noFavourites = view.findViewById(R.id.noFavourites);
        nowPlayingBottomBar = view.findViewById(R.id.hiddenBarFavScreen);
        songTitle = view.findViewById(R.id.songTitleFavScreen);
        playPauseButton = view.findViewById(R.id.playpauseButton);
        recyclerView = view.findViewById(R.id.favouriteRecycler);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myActivity = (Activity) context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myActivity = activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        favouriteContent = new EchoDatabase(myActivity);
        display_favourites_by_searching();
        bottomBarSetup();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_sort);
        item.setVisible(false);
    }

    public ArrayList<Songs> getSongsFromPhone() {
        ArrayList<Songs> arrayList = new ArrayList<>();
        ContentResolver contentResolver = myActivity.getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, null);
        if (songCursor != null && songCursor.moveToFirst()) {
            int songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
            while (songCursor.moveToNext()) {
                Long currentId = songCursor.getLong(songId);
                String currentTitle = songCursor.getString(songTitle);
                String currentArtist = songCursor.getString(songArtist);
                String currentData = songCursor.getString(songData);
                Long currentDate = songCursor.getLong(dateIndex);
                arrayList.add(new Songs(currentId, currentTitle, currentArtist, currentData, currentDate));
            }
        }
        return arrayList;
    }

    public void bottomBarSetup() {
        try {
            bottomBarClickHandler();
            songTitle.setText(SongPlayingFragment.currentSongHelper.songTitle);
            SongPlayingFragment.mediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    songTitle.setText(SongPlayingFragment.currentSongHelper.songTitle);
                    SongPlayingFragment.onSongComplete();
                }
            });
            if (SongPlayingFragment.mediaplayer.isPlaying()) {
                nowPlayingBottomBar.setVisibility(View.VISIBLE);
            } else {
                nowPlayingBottomBar.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bottomBarClickHandler() {
        nowPlayingBottomBar.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                mediaPlayer = SongPlayingFragment.mediaplayer;
                SongPlayingFragment songPlayingFragment = new SongPlayingFragment();
                Bundle args = new Bundle();
                args.putString("songArtist", SongPlayingFragment.currentSongHelper.songArtist);
                args.putString("path", SongPlayingFragment.currentSongHelper.songPath);
                args.putString("songTitle", SongPlayingFragment.currentSongHelper.songTitle);
                args.putInt("songId", Math.toIntExact(SongPlayingFragment.currentSongHelper.songId));
                args.putInt("songPosition", SongPlayingFragment.currentSongHelper.currentPosition);
                args.putParcelableArrayList("songData", SongPlayingFragment.fetchSongs);
                args.putString("FavBottomBar", "success");
                songPlayingFragment.setArguments(args);
                Objects.requireNonNull(getFragmentManager()).beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right,R.anim.slide_in_right,R.anim.slide_out_left)
                        .replace(R.id.details_fragment, songPlayingFragment).addToBackStack("SongPlayingFragment")
                        .commit();
            }
        });
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SongPlayingFragment.mediaplayer.isPlaying()) {
                    SongPlayingFragment.mediaplayer.pause();
                    trackPosition = SongPlayingFragment.mediaplayer.getCurrentPosition();
                    playPauseButton.setBackgroundResource(R.drawable.play_icon);
                    SongPlayingFragment.playpauseImageButton.setBackgroundResource(R.drawable.play_icon);
                    SongPlayingFragment.currentSongHelper.isPlaying = false;
                } else {
                    SongPlayingFragment.mediaplayer.seekTo(trackPosition);
                    SongPlayingFragment.mediaplayer.start();
                    playPauseButton.setBackgroundResource(R.drawable.pause_icon);

                    SongPlayingFragment.playpauseImageButton.setBackgroundResource(R.drawable.pause_icon);
                    SongPlayingFragment.currentSongHelper.isPlaying = true;
                }
            }
        });
    }

    public void display_favourites_by_searching() {
        if (favouriteContent.checkSize() > 0) {
            refreshList = new ArrayList<>();
            getListFromDatabase = favouriteContent.queryDBList();
            ArrayList<Songs> fetchListfromDevice = getSongsFromPhone();
            if (fetchListfromDevice != null) {
                for (int i = 0; i < fetchListfromDevice.size(); i++) {
                    for (int j = 0; j < getListFromDatabase.size(); j++) {
                        if (getListFromDatabase.get(j).songID.equals(fetchListfromDevice.get(i).songID)) {
                            refreshList.add(getListFromDatabase.get(j));
                        }
                    }
                }
            } else {

            }
            if (refreshList == null) {
                recyclerView.setVisibility(View.INVISIBLE);
                noFavourites.setVisibility(View.VISIBLE);
            } else {
                FavouriteAdapter favouriteAdapter = new FavouriteAdapter(refreshList, myActivity);
                LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(favouriteAdapter);
                recyclerView.setHasFixedSize(true);
            }
        } else {
            recyclerView.setVisibility(View.INVISIBLE);
            noFavourites.setVisibility(View.VISIBLE);
        }
    }
}
