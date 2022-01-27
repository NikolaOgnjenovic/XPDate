package com.mrmi.roktrajanja;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Settings.loadLocale(this);

        //Go to the Main Activity after x milliseconds
        new Handler().postDelayed(() -> {
            Intent i = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(i);

            finish();
        }, 700);
    }
}