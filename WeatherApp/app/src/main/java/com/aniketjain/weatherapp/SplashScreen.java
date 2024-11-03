package com.aniketjain.weatherapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.aniketjain.weatherapp.databinding.ActivitySplashScreenBinding;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {
    private ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashScreenBinding binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //Removing status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Applique le thème choisi par l'utilisateur (clair ou sombre) à partir des SharedPreferences.
        layout = findViewById(R.id.layout);
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE);
        boolean isThemeDark = sharedPreferences.getBoolean("ThemeChoice", false);
        if (isThemeDark) {
            layout.setBackgroundColor(getResources().getColor(R.color.grey));
        } else {
            layout.setBackgroundColor(getResources().getColor(R.color.mainBGColor));
        }

        //Setting Splash
        splashScreen();


    }

    private void splashScreen() {
        int SPLASH_TIME = 4000;
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_TIME);
    }
}