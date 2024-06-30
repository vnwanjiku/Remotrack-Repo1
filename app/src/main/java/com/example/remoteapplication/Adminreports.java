package com.example.remoteapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class Adminreports extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        ImageView leftArrow = findViewById(R.id.left_arrow);
        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start MainUserLogin activity
                Intent intent = new Intent(Adminreports.this, MainUserLogin.class);
                startActivity(intent);
            }
        });

        ImageView task = findViewById(R.id.task);
        task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TaskManager activity
                Intent intent = new Intent(Adminreports.this, AssignTaskActivity.class);
                startActivity(intent);
            }
        });

        ImageView barchart = findViewById(R.id.barchart);
        barchart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TaskManager activity
                Intent intent = new Intent(Adminreports.this, Adminreports.class);
                startActivity(intent);
            }
        });

        ImageView bell = findViewById(R.id.bell);
        bell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TaskManager activity
                Intent intent = new Intent(Adminreports.this, Adminnotifications.class);
                startActivity(intent);
            }
        });

        ImageView user = findViewById(R.id.user);
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TaskManager activity
                Intent intent = new Intent(Adminreports.this, Adminprofile.class);
                startActivity(intent);
            }
        });
    }
}