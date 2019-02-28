package com.vaibhav.myplayer.activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.vaibhav.myplayer.R;
import com.vaibhav.myplayer.adapters.NavigationDrawerAdapter;
import com.vaibhav.myplayer.fragments.MainScreenFragment;
import com.vaibhav.myplayer.fragments.SongPlayingFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> navigationDrawerIconsList = new ArrayList<>();
    public static DrawerLayout drawerLayout = null;
    int[] images_for_navdrawer = {R.drawable.navigation_allsongs, R.drawable.navigation_favorites, R.drawable.navigation_settings, R.drawable.navigation_aboutus};
    public static NotificationManager notificationManager = null;
    Notification trackNotificationBuilder = null;
    ActionBarDrawerToggle toggle;
    int notify_ID = 1978;
    String CHANNEL_ID = "echo_channel";
    CharSequence channelname = "Echo Player";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);

        navigationDrawerIconsList.add("All Songs");
        navigationDrawerIconsList.add("Favourites");
        navigationDrawerIconsList.add("Settings");
        navigationDrawerIconsList.add("About Us");

        toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        MainScreenFragment mainScreenFragment = new MainScreenFragment();
        this.getSupportFragmentManager().beginTransaction()
                .add(R.id.details_fragment, mainScreenFragment, "MainScreenFragment")
                .commit();

        NavigationDrawerAdapter _navigationAdapter = new NavigationDrawerAdapter(navigationDrawerIconsList, images_for_navdrawer, this);
        _navigationAdapter.notifyDataSetChanged();

        RecyclerView navigation_recycler_view = findViewById(R.id.navigation_recycler_view);
        navigation_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        navigation_recycler_view.setItemAnimator(new DefaultItemAnimator());
        navigation_recycler_view.setAdapter(_navigationAdapter);
        navigation_recycler_view.setHasFixedSize(true);

        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, (int) System.currentTimeMillis(), intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            trackNotificationBuilder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                    .setContentTitle("A track is playing in the background")
                    .setSmallIcon(R.drawable.echo_icon)
                    .setChannelId(CHANNEL_ID)
                    .setContentIntent(pIntent)
                    .setOngoing(true)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build();
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, channelname, importance);
            notificationManager.createNotificationChannel(mChannel);
        } else {
            trackNotificationBuilder = new Notification.Builder(MainActivity.this)
                    .setContentTitle("A track is playing in the background")
                    .setSmallIcon(R.drawable.echo_logo)
                    .setContentIntent(pIntent)
                    .setOngoing(true)
                    .setAutoCancel(true)
                    .build();
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            notificationManager.cancel(notify_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (SongPlayingFragment.mediaplayer.isPlaying()) {
                notificationManager.notify(notify_ID, trackNotificationBuilder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (SongPlayingFragment.mediaplayer.isPlaying()) {
                notificationManager.notify(notify_ID, trackNotificationBuilder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.isFinishing()) {
            try {
                notificationManager.cancel(notify_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.onDestroy();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (SongPlayingFragment.mediaplayer.isPlaying()) {
                notificationManager.notify(notify_ID, trackNotificationBuilder);
                onStop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            notificationManager.cancel(notify_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
