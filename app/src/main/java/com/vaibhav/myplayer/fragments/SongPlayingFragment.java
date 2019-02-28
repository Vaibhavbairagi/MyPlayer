package com.vaibhav.myplayer.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.sip.SipSession;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cleveroad.audiovisualization.AudioVisualization;
import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;
import com.cleveroad.audiovisualization.VisualizerDbmHandler;
import com.vaibhav.myplayer.CurrentSongHelper;
import com.vaibhav.myplayer.R;
import com.vaibhav.myplayer.Songs;
import com.vaibhav.myplayer.activities.MainActivity;
import com.vaibhav.myplayer.databases.EchoDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 */
public class SongPlayingFragment extends Fragment {
    public static Activity myActivity;
    public static MediaPlayer mediaplayer;
    public static TextView startTimeText = null, endTimeText = null, songArtistView = null, songTitleView = null;
    public static ImageButton playpauseImageButton = null, previousImageButton = null, nextImageButton = null, loopImageButton = null, shuffleImageButton = null, fab = null;
    public static SeekBar seekbar = null;
    public static AudioVisualization audioVisualization = null;
    public static GLAudioVisualizationView glView = null;

    public static int currentPosition = 0;
    public static ArrayList<Songs> fetchSongs = null;
    public static CurrentSongHelper currentSongHelper = null;
    public static String MY_PREFS_SHUFFLE = "Shuffle feature";
    public static String MY_PREFS_LOOP = "Loop feature";

    public static SensorManager mSensorManager = null;
    public static SensorEventListener mSensorListener = null;
    public static String MY_PREFS_NAME = "ShakeFeature";

    static final Runnable updateSongTime = new Runnable() {
        @Override
        public void run() {
            int getCurrent = mediaplayer.getCurrentPosition();
            int ctimeinminutes = (int) TimeUnit.MILLISECONDS.toMinutes(getCurrent);
            int ctimeinsecs = (int) TimeUnit.MILLISECONDS.toSeconds(getCurrent) - (int) TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent));
            String min, secs;
            if (ctimeinminutes < 10) {
                min = "0" + ctimeinminutes;
            } else {
                min = String.valueOf(ctimeinminutes);
            }
            if (ctimeinsecs < 10) {
                secs = "0" + ctimeinsecs;
            } else {
                secs = String.valueOf(ctimeinsecs);
            }
            String time = min + ":" + secs;
            startTimeText.setText(time);
            seekbar.setProgress(getCurrent);
            Handler timeUpdateHandler = new Handler();
            timeUpdateHandler.postDelayed(this, 1000);
        }
    };

    public static EchoDatabase favouriteContent = null;

    float mAcceleration = 0f;
    float mAccelerationCurrent = 0f;
    float mAccelerationLast = 0f;

    public SongPlayingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_song_playing, container, false);
        setHasOptionsMenu(true);
        getActivity().setTitle("Now Playing");
        seekbar = v.findViewById(R.id.seekBar);
        startTimeText = v.findViewById(R.id.startTime);
        endTimeText = v.findViewById(R.id.endTime);
        playpauseImageButton = v.findViewById(R.id.playPauseButton);
        nextImageButton = v.findViewById(R.id.nextButton);
        previousImageButton = v.findViewById(R.id.previousButton);
        loopImageButton = v.findViewById(R.id.loopButton);
        shuffleImageButton = v.findViewById(R.id.shuffleButton);
        songArtistView = v.findViewById(R.id.songArtist);
        songTitleView = v.findViewById(R.id.songTitle);
        glView = v.findViewById(R.id.visualizer_view);
        fab = v.findViewById(R.id.favouriteIcon);
        fab.setAlpha(0.8f);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        audioVisualization = glView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myActivity = activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        audioVisualization.onResume();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onPause() {
        audioVisualization.onPause();
        super.onPause();

        mSensorManager.unregisterListener(mSensorListener);
    }

    @Override
    public void onDestroyView() {
        audioVisualization.release();
        super.onDestroyView();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mSensorManager = (SensorManager) myActivity.getSystemService(Context.SENSOR_SERVICE);
        mAcceleration = 0.0f;
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH;
        mAccelerationLast = SensorManager.GRAVITY_EARTH;
        bindShakeListener();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.song_playing_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_redirect);
        item.setVisible(true);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_redirect:
                myActivity.onBackPressed();
                return false;
        }
        return false;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        favouriteContent = new EchoDatabase(myActivity);
        currentSongHelper = new CurrentSongHelper();
        currentSongHelper.isPlaying = true;
        currentSongHelper.isLoop = false;
        currentSongHelper.isShuffle = false;

        String path = null, _songTitle, _songArtist;
        Long songId;
        try {
            path = Objects.requireNonNull(getArguments()).getString("path");
            _songTitle = getArguments().getString("songTitle");
            _songArtist = getArguments().getString("songArtist");
            songId = (long) getArguments().getInt("songId");
            currentPosition = getArguments().getInt("songPosition");
            fetchSongs = getArguments().getParcelableArrayList("songData");

            currentSongHelper.songPath = path;
            currentSongHelper.songTitle = _songTitle;
            currentSongHelper.songArtist = _songArtist;
            currentSongHelper.songId = songId;
            currentSongHelper.currentPosition = currentPosition;

            updateTextViews(currentSongHelper.songTitle, currentSongHelper.songArtist);

        } catch (Exception e) {
            e.printStackTrace();
        }

        String fromFavBottomBar = getArguments().getString("FavBottomBar");
        String fromMainScreenBottomBar = getArguments().getString("MainScreenBottomBar");
        if (fromFavBottomBar != null) {
            mediaplayer = FavouriteFragment.mediaPlayer;
            currentSongHelper.isPlaying = FavouriteFragment.mediaPlayer.isPlaying();
        } else if (fromMainScreenBottomBar != null) {
            mediaplayer = MainScreenFragment.mediaPlayer;
            currentSongHelper.isPlaying = MainScreenFragment.mediaPlayer.isPlaying();
        } else {
            mediaplayer = new MediaPlayer();
            mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaplayer.setDataSource(myActivity, Uri.parse(path));
                mediaplayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaplayer.start();
        }
        processInformation(mediaplayer);

        if (currentSongHelper.isPlaying) {
            playpauseImageButton.setBackgroundResource(R.drawable.pause_icon);
        } else {
            playpauseImageButton.setBackgroundResource(R.drawable.play_icon);
        }
        mediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                onSongComplete();
            }
        });
        clickHandler();
        VisualizerDbmHandler visualizationHandler = DbmHandler.Factory.newVisualizerHandler(myActivity, 0);
        audioVisualization.linkTo(visualizationHandler);

        SharedPreferences prefsForShuffle = myActivity.getSharedPreferences(MY_PREFS_SHUFFLE, Context.MODE_PRIVATE);
        boolean isShuffleAllowed = prefsForShuffle.getBoolean("feature", false);
        if (isShuffleAllowed) {
            currentSongHelper.isShuffle = true;
            currentSongHelper.isLoop = false;
            shuffleImageButton.setBackgroundResource(R.drawable.shuffle_icon);
            loopImageButton.setBackgroundResource(R.drawable.loop_white_icon);
        } else {
            currentSongHelper.isShuffle = false;
            shuffleImageButton.setBackgroundResource(R.drawable.shuffle_white_icon);
        }
        SharedPreferences prefsForLoop = myActivity.getSharedPreferences(MY_PREFS_LOOP, Context.MODE_PRIVATE);
        boolean isLoopAllowed = prefsForLoop.getBoolean("feature", false);
        if (isLoopAllowed) {
            currentSongHelper.isShuffle = false;
            currentSongHelper.isLoop = true;
            shuffleImageButton.setBackgroundResource(R.drawable.shuffle_white_icon);
            loopImageButton.setBackgroundResource(R.drawable.loop_icon);
        } else {
            currentSongHelper.isLoop = false;
            shuffleImageButton.setBackgroundResource(R.drawable.shuffle_white_icon);
        }
        if (favouriteContent.checkIfIdExists(currentSongHelper.songId)) {
            fab.setImageDrawable(ContextCompat.getDrawable(myActivity, R.drawable.favorite_on));
        } else {
            fab.setImageDrawable(ContextCompat.getDrawable(myActivity, R.drawable.favorite_off));
        }

        seekBarProgresschangeHandler();
    }

    public void seekBarProgresschangeHandler() {
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean ontouch) {
                if (mediaplayer != null && ontouch) {
                    int ctimeinminutes = (int) TimeUnit.MILLISECONDS.toMinutes(progress);
                    int ctimeinsecs = (int) TimeUnit.MILLISECONDS.toSeconds(progress) - (int) TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress));
                    String min, secs;
                    if (ctimeinminutes < 10) {
                        min = "0" + ctimeinminutes;
                    } else {
                        min = String.valueOf(ctimeinminutes);
                    }
                    if (ctimeinsecs < 10) {
                        secs = "0" + ctimeinsecs;
                    } else {
                        secs = String.valueOf(ctimeinsecs);
                    }
                    String time = min + ":" + secs;
                    startTimeText.setText(time);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaplayer != null && mediaplayer.isPlaying()) {
                    mediaplayer.seekTo(seekBar.getProgress());
                }
            }
        });
    }

    public void clickHandler() {

        fab.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                if (favouriteContent.checkIfIdExists(Math.toIntExact(currentSongHelper.songId))) {
                    fab.setImageDrawable(ContextCompat.getDrawable(myActivity, R.drawable.favorite_off));
                    favouriteContent.deleteFavourite(Math.toIntExact(currentSongHelper.songId));
                    Toast.makeText(myActivity, "Removed From Favourites", Toast.LENGTH_SHORT).show();
                } else {
                    fab.setImageDrawable(ContextCompat.getDrawable(myActivity, R.drawable.favorite_on));
                    favouriteContent.storeAsFavourite(Math.toIntExact(currentSongHelper.songId), currentSongHelper.songArtist, currentSongHelper.songTitle
                            , currentSongHelper.songPath);
                    Toast.makeText(myActivity, "Added to Favourites", Toast.LENGTH_SHORT).show();
                }
            }
        });

        shuffleImageButton.setOnClickListener(new View.OnClickListener() {
            SharedPreferences.Editor editorShuffle = myActivity.getSharedPreferences(MY_PREFS_SHUFFLE, Context.MODE_PRIVATE).edit();
            SharedPreferences.Editor editorLoop = myActivity.getSharedPreferences(MY_PREFS_LOOP, Context.MODE_PRIVATE).edit();

            @Override
            public void onClick(View view) {
                if (currentSongHelper.isShuffle) {
                    shuffleImageButton.setBackgroundResource(R.drawable.shuffle_white_icon);
                    currentSongHelper.isShuffle = false;
                    editorShuffle.putBoolean("feature", false);
                    editorShuffle.apply();
                } else {
                    currentSongHelper.isShuffle = true;
                    currentSongHelper.isLoop = false;
                    shuffleImageButton.setBackgroundResource(R.drawable.shuffle_icon);
                    loopImageButton.setBackgroundResource(R.drawable.loop_white_icon);
                    editorShuffle.putBoolean("feature", true);
                    editorShuffle.apply();
                    editorLoop.putBoolean("feature", false);
                    editorLoop.apply();
                }
            }
        });

        nextImageButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                currentSongHelper.isPlaying = true;
                playpauseImageButton.setBackgroundResource(R.drawable.pause_icon);
                if (currentSongHelper.isShuffle) {
                    playNext("PlayNextLikeNormalShuffle");
                } else {
                    playNext("PlayNextNormal");
                }
            }
        });

        previousImageButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                currentSongHelper.isPlaying = true;
                if (currentSongHelper.isLoop) {
                    loopImageButton.setBackgroundResource(R.drawable.loop_white_icon);
                }
                playPrevious();
            }
        });

        loopImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editorShuffle = myActivity.getSharedPreferences(MY_PREFS_SHUFFLE, Context.MODE_PRIVATE).edit();
                SharedPreferences.Editor editorLoop = myActivity.getSharedPreferences(MY_PREFS_LOOP, Context.MODE_PRIVATE).edit();
                if (currentSongHelper.isLoop) {
                    currentSongHelper.isLoop = false;
                    loopImageButton.setBackgroundResource(R.drawable.loop_white_icon);
                    editorLoop.putBoolean("feature", false);
                    editorLoop.apply();
                } else {
                    currentSongHelper.isLoop = true;
                    currentSongHelper.isShuffle = false;
                    loopImageButton.setBackgroundResource(R.drawable.loop_icon);
                    shuffleImageButton.setBackgroundResource(R.drawable.shuffle_white_icon);
                    editorShuffle.putBoolean("feature", false);
                    editorShuffle.apply();
                    editorLoop.putBoolean("feature", true);
                    editorLoop.apply();
                }
            }
        });

        playpauseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaplayer.isPlaying()) {
                    mediaplayer.pause();
                    currentSongHelper.isPlaying = false;
                    playpauseImageButton.setBackgroundResource(R.drawable.play_icon);
                } else {
                    mediaplayer.start();
                    currentSongHelper.isPlaying = true;
                    playpauseImageButton.setBackgroundResource(R.drawable.pause_icon);
                }
            }
        });
    }

    public static void playNext(String check) {
        if (check.equals("PlayNextNormal")) {
            currentPosition = currentPosition + 1;
        } else if (check.equals("PlayNextLikeNormalShuffle")) {
            Random randomObject = new Random();
            int randomPosition = randomObject.nextInt(fetchSongs.size() + 1);
            currentPosition = randomPosition;
        }
        if (currentPosition == fetchSongs.size()) {
            currentPosition = 0;
        }
        currentSongHelper.isLoop = false;
        Songs nextSong = fetchSongs.get(currentPosition);
        currentSongHelper.songTitle = nextSong.songTitle;
        currentSongHelper.songPath = nextSong.songData;
        currentSongHelper.currentPosition = currentPosition;
        currentSongHelper.songId = nextSong.songID;
        currentSongHelper.songArtist=nextSong.artist;

        updateTextViews(currentSongHelper.songTitle, currentSongHelper.songArtist);

        mediaplayer.reset();
        try {
            mediaplayer.setDataSource(myActivity, Uri.parse(currentSongHelper.songPath));
            mediaplayer.prepare();
            mediaplayer.start();
            processInformation(mediaplayer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (favouriteContent.checkIfIdExists(currentSongHelper.songId)) {
            fab.setImageDrawable(ContextCompat.getDrawable(myActivity, R.drawable.favorite_on));
        } else {
            fab.setImageDrawable(ContextCompat.getDrawable(myActivity, R.drawable.favorite_off));
        }
    }

    public void playPrevious() {
        currentPosition = currentPosition - 1;
        if (currentPosition == -1) {
            currentPosition = 0;
        }
        if (currentSongHelper.isPlaying) {
            playpauseImageButton.setBackgroundResource(R.drawable.pause_icon);
        } else {
            playpauseImageButton.setBackgroundResource(R.drawable.play_icon);
        }
        currentSongHelper.isLoop = false;
        Songs nextSong = fetchSongs.get(currentPosition);
        currentSongHelper.songTitle = nextSong.songTitle;
        currentSongHelper.songPath = nextSong.songData;
        currentSongHelper.currentPosition = currentPosition;
        currentSongHelper.songId = nextSong.songID;
        currentSongHelper.songArtist=nextSong.artist;

        updateTextViews(currentSongHelper.songTitle, currentSongHelper.songArtist);

        mediaplayer.reset();
        try {
            mediaplayer.setDataSource(myActivity, Uri.parse(currentSongHelper.songPath));
            mediaplayer.prepare();
            mediaplayer.start();
            processInformation(mediaplayer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (favouriteContent.checkIfIdExists(currentSongHelper.songId)) {
            fab.setImageDrawable(ContextCompat.getDrawable(myActivity, R.drawable.favorite_on));
        } else {
            fab.setImageDrawable(ContextCompat.getDrawable(myActivity, R.drawable.favorite_off));
        }
    }

    public static void onSongComplete() {
        if (currentSongHelper.isShuffle) {
            playNext("PlayNextLikeNormalShuffle");
            currentSongHelper.isPlaying = true;
        } else {
            if (currentSongHelper.isLoop) {
                currentSongHelper.isPlaying = true;
                Songs nextSong = fetchSongs.get(currentPosition);
                currentSongHelper.songTitle = nextSong.songTitle;
                currentSongHelper.songPath = nextSong.songData;
                currentSongHelper.currentPosition = currentPosition;
                currentSongHelper.songId = nextSong.songID;

                updateTextViews(currentSongHelper.songTitle, currentSongHelper.songArtist);

                mediaplayer.reset();
                try {
                    mediaplayer.setDataSource(myActivity, Uri.parse(currentSongHelper.songPath));
                    mediaplayer.prepare();
                    mediaplayer.start();
                    processInformation(mediaplayer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                playNext("PlayNextNormal");
                currentSongHelper.isPlaying = true;
            }
        }
        if (favouriteContent.checkIfIdExists(currentSongHelper.songId)) {
            fab.setImageDrawable(ContextCompat.getDrawable(myActivity, R.drawable.favorite_on));
        } else {
            fab.setImageDrawable(ContextCompat.getDrawable(myActivity, R.drawable.favorite_off));
        }
    }

    public static void updateTextViews(String songTitle, String songArtist) {
        if (songTitle == "<unknown>") {
            songTitle = "unknown";
        }
        songTitleView.setText(songTitle);
        songArtistView.setText(songArtist);
    }

    public static void processInformation(MediaPlayer mediaPlayer) {
        int finalTime = mediaPlayer.getDuration();
        int startTime = mediaPlayer.getCurrentPosition();
        seekbar.setMax(finalTime);
        int ctimeinminutes = (int) TimeUnit.MILLISECONDS.toMinutes(startTime);
        int ctimeinsecs = (int) TimeUnit.MILLISECONDS.toSeconds(startTime) - (int) TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime));
        String min, secs;
        if (ctimeinminutes < 10) {
            min = "0" + ctimeinminutes;
        } else {
            min = String.valueOf(ctimeinminutes);
        }
        if (ctimeinsecs < 10) {
            secs = "0" + ctimeinsecs;
        } else {
            secs = String.valueOf(ctimeinsecs);
        }
        String time = min + ":" + secs;
        startTimeText.setText(time);
        ctimeinminutes = (int) TimeUnit.MILLISECONDS.toMinutes(finalTime);
        ctimeinsecs = (int) TimeUnit.MILLISECONDS.toSeconds(finalTime) - (int) TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime));
        if (ctimeinminutes < 10) {
            min = "0" + ctimeinminutes;
        } else {
            min = String.valueOf(ctimeinminutes);
        }
        if (ctimeinsecs < 10) {
            secs = "0" + ctimeinsecs;
        } else {
            secs = String.valueOf(ctimeinsecs);
        }
        time = min + ":" + secs;
        endTimeText.setText(time);
        seekbar.setProgress(startTime);
        Handler mHandler = new Handler();
        mHandler.postDelayed(updateSongTime, 1000);
    }

    public void bindShakeListener() {
        mSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                mAccelerationLast = mAccelerationCurrent;
                mAccelerationCurrent = (float) Math.sqrt(((x * x) + (y * y) + (z * z)));
                float delta = mAccelerationCurrent - mAccelerationLast;
                mAcceleration = mAcceleration * 0.9f + delta;

                if (mAcceleration > 12) {
                    SharedPreferences prefs = myActivity.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
                    boolean isAllowed = prefs.getBoolean("feature", false);
                    if (isAllowed) {
                        playNext("PlayNextNormal");
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }
}
