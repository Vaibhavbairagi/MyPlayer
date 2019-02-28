package com.vaibhav.myplayer.utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.vaibhav.myplayer.R;
import com.vaibhav.myplayer.activities.MainActivity;
import com.vaibhav.myplayer.fragments.SongPlayingFragment;

import java.util.Objects;

public class CaptureBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context p0, Intent p1) {
        if (p1.getAction() == Intent.ACTION_NEW_OUTGOING_CALL) {
            try {
                MainActivity.notificationManager.cancel(1978);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (SongPlayingFragment.mediaplayer.isPlaying()) {
                    SongPlayingFragment.mediaplayer.pause();
                    SongPlayingFragment.playpauseImageButton.setBackgroundResource(R.drawable.play_icon);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            TelephonyManager tm = (TelephonyManager) p0.getSystemService(Service.TELEPHONY_SERVICE);
            if (Objects.requireNonNull(tm).getCallState() == TelephonyManager.CALL_STATE_RINGING) {
                try {
                    MainActivity.notificationManager.cancel(1978);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (SongPlayingFragment.mediaplayer.isPlaying()) {
                        SongPlayingFragment.mediaplayer.pause();
                        SongPlayingFragment.playpauseImageButton.setBackgroundResource(R.drawable.play_icon);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
