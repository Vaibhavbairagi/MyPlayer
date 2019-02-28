package com.vaibhav.myplayer.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.vaibhav.myplayer.R;
import com.vaibhav.myplayer.activities.MainActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {
    Activity myActivity = null;
    Switch shakeSwitch = null;
    public static String MY_PREFS_NAME = "ShakeFeature";

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        shakeSwitch = view.findViewById(R.id.switchShake);
        getActivity().setTitle("Settings");
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedPreferences prefs = myActivity.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        boolean isAllowed = prefs.getBoolean("feature", false);
        if (isAllowed) {
            shakeSwitch.setChecked(true);
        } else {
            shakeSwitch.setChecked(false);
        }
        shakeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SharedPreferences.Editor editor = myActivity.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
                    editor.putBoolean("feature", true);
                    editor.apply();
                } else {
                    SharedPreferences.Editor editor = myActivity.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
                    editor.putBoolean("feature", false);
                    editor.apply();
                }
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_sort);
        item.setVisible(false);
    }

}
