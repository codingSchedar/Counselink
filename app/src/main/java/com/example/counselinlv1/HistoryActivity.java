package com.example.counselinlv1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> onBackPressed());

        // Handle LinkedIn links
        setupLinkedInClickListeners();
    }

    private void setupLinkedInClickListeners() {
        // Add LinkedIn click listeners for each developer
        setupLinkedInClickListener(findViewById(R.id.wayne_linkedin), "https://www.linkedin.com/in/wczsantiago/");
        setupLinkedInClickListener(findViewById(R.id.jamir_linkedin), "https://www.linkedin.com/in/jamir-asilo-498b67294/");
        setupLinkedInClickListener(findViewById(R.id.jane_linkedin), "ttps://www.linkedin.com/in/jane-cayla-mangubat-3b1415326/");
        setupLinkedInClickListener(findViewById(R.id.avery_linkedin), "https://www.linkedin.com/in/avery-jewel-apritado-48648832a?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app");
    }

    private void setupLinkedInClickListener(View view, String url) {
        view.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // Optional: Handle cases where no browser or LinkedIn app is available
                // You can show a Toast message or log the issue
            }
        });
    }
}
