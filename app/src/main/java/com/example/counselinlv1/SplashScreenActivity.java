package com.example.counselinlv1;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.counselinlv1.Utilities.BaseActivity;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge content
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Set the content view
        setContentView(R.layout.activity_splash_screen);

        //change color of the system bottom navigation bar
        getWindow().setNavigationBarColor(getResources().getColor(R.color.maroon));

        // Apply window insets to handle padding for system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_splash_screen);

        // Delay for a few seconds and then move to LoginActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start the login activity after 3 seconds
                Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Close the splash screen activity
            }
        }, 2000); // 3 seconds delay
    }
}
