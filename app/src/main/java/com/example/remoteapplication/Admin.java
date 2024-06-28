package com.example.remoteapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class Admin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        ImageView leftArrow = findViewById(R.id.left_arrow);
        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start MainUserLogin activity
                Intent intent = new Intent(Admin.this, MainUserLogin.class);
                startActivity(intent);
            }
        });

        ImageView task = findViewById(R.id.task);
        task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TaskManager activity
                Intent intent = new Intent(Admin.this, AssignTaskActivity.class);
                startActivity(intent);
            }
        });

        ImageView barchart = findViewById(R.id.barchart);
        barchart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TaskManager activity
                Intent intent = new Intent(Admin.this, Adminreports.class);
                startActivity(intent);
            }
        });

        ImageView bell = findViewById(R.id.bell);
        bell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TaskManager activity
                Intent intent = new Intent(Admin.this, Notification.class);
                startActivity(intent);
            }
        });

        ImageView user = findViewById(R.id.user);
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TaskManager activity
                Intent intent = new Intent(Admin.this, Profile.class);
                startActivity(intent);
            }
        });
    }
}