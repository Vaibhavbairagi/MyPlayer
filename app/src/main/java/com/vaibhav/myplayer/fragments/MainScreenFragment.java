package com.vaibhav.myplayer.fragments;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vaibhav.myplayer.R;
import com.vaibhav.myplayer.Songs;
import com.vaibhav.myplayer.adapters.MainScreenAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class MainScreenFragment extends Fragment {
    ArrayList<Songs> getSongsList;
    RelativeLayout nowPlayingBottomBar, visibleLayout, noSongs;
    RecyclerView recyclerView;
    ImageButton playPauseButton;
    TextView songTitle;
    Activity myActivity;
    MainScreenAdapter _mainScreenAdapter;
    int trackPosition = 0;
    public static MediaPlayer mediaPlayer = null;

    public MainScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_screen, container, false);
        setHasOptionsMenu(true);
        Objects.requireNonNull(getActivity()).setTitle("All Songs");
        visibleLayout = view.findViewById(R.id.visibleLayout);
        noSongs = view.findViewById(R.id.noSongs);
        nowPlayingBottomBar = view.findViewById(R.id.hiddenBarMainScreen);
        recyclerView = view.findViewById(R.id.contentMain);
        playPauseButton = view.findViewById(R.id.mainscreen_playPauseButton);
        songTitle = view.findViewById(R.id.songTitleMainScreen);
        return view;
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int switcher = item.getItemId();
        if (switcher == R.id.action_sort_ascending) {
            SharedPreferences.Editor editor = myActivity.getSharedPreferences("action_sort", Context.MODE_PRIVATE).edit();
            editor.putString("action_sort_ascending", "true");
            editor.putString("action_sort_recent", "false");
            editor.apply();
            if (getSongsList != null) {
                Collections.sort(getSongsList, Songs.nameComparator);
            }
            _mainScreenAdapter.notifyDataSetChanged();
            return false;
        } else if (switcher == R.id.action_sort_recent) {
            SharedPreferences.Editor editor = myActivity.getSharedPreferences("action_sort", Context.MODE_PRIVATE).edit();
            editor.putString("action_sort_recent", "true");
            editor.putString("action_sort_ascending", "false");
            editor.apply();
            if (getSongsList != null) {
                Collections.sort(getSongsList, Songs.dateComparator);
            }
            _mainScreenAdapter.notifyDataSetChanged();
            return false;
        }
        return super.onOptionsItemSelected(item);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getSongsList = getSongsFromPhone();
        SharedPreferences prefs = myActivity.getSharedPreferences("action_sort", Context.MODE_PRIVATE);
        String action_sort_ascending = prefs.getString("action_sort_ascending", "true");
        String action_sort_recent = prefs.getString("action_sort_recent", "false");
        if (getSongsList == null) {
            visibleLayout.setVisibility(View.INVISIBLE);
            noSongs.setVisibility(View.VISIBLE);
        } else {
            _mainScreenAdapter = new MainScreenAdapter(getSongsList, myActivity);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(myActivity);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(_mainScreenAdapter);
        }

        if (getSongsList != null) {
            if (action_sort_ascending.equals("true")) {
                Collections.sort(getSongsList, Songs.nameComparator);
                _mainScreenAdapter.notifyDataSetChanged();
            } else if (action_sort_recent.equals("true")) {
                Collections.sort(getSongsList, Songs.dateComparator);
                _mainScreenAdapter.notifyDataSetChanged();
            }
        }
        bottomBarSetup();
    }

    @Override
    public void onResume() {
        super.onResume();
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
                args.putString("MainScreenBottomBar", "success");
                songPlayingFragment.setArguments(args);
                Objects.requireNonNull(getFragmentManager())
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right,R.anim.slide_in_right,R.anim.slide_out_left)
                        .replace(R.id.details_fragment, songPlayingFragment)
                        .addToBackStack("SongPlayingFragment")
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
}
