package com.vaibhav.myplayer.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.vaibhav.myplayer.R;

public class SplashActivity extends AppCompatActivity {
    String[] permissionString = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (!hasPermission(SplashActivity.this, permissionString)) {
            ActivityCompat.requestPermissions(SplashActivity.this, permissionString, 131);
        } else {
            splashEndStartAct();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 131) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED
                    && grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                splashEndStartAct();
            } else {
                Toast.makeText(SplashActivity.this, "Plaese grant all the permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(SplashActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void splashEndStartAct() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 1000);
    }

    boolean hasPermission(Context context, String[] permissions) {
        boolean hasAllPermissions = true;
        for (String permission : permissions) {
            int res = context.checkCallingOrSelfPermission(permission);
            if (res != PackageManager.PERMISSION_GRANTED)
                hasAllPermissions = false;
        }
        return hasAllPermissions;
    }
}
