package com.example.remoteapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class LandpageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landpage);

        ProgressBar loadingCircle = findViewById(R.id.loadingCircle);
        loadingCircle.setVisibility(View.GONE); // Ensure the progress bar is hidden when the activity is created

        Button btn_get_started = findViewById(R.id.getstarted_button);
        btn_get_started.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingCircle.setVisibility(View.VISIBLE); // Show the progress bar
                Intent intent = new Intent(LandpageActivity.this, MainUserLogin.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ProgressBar loadingCircle = findViewById(R.id.loadingCircle);
        loadingCircle.setVisibility(View.GONE); // Hide the progress bar when the activity is resumed
    }
}
