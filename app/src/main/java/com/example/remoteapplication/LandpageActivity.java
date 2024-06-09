package com.example.remoteapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class LandpageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landpage);

        ImageButton btn_get_started = findViewById(R.id.btn_get_started);
        btn_get_started.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LandpageActivity.this, MainUserLogin.class);
                startActivity(intent);
            }
        });
    }
}
