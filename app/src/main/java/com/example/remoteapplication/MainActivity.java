package com.example.remoteapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private MaterialCardView card3, card4, card5;
    private TextView taskTextView3, taskTextView4, taskTextView5;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseTasks = FirebaseDatabase.getInstance().getReference("tasks");

        card3 = findViewById(R.id.card3);
        card4 = findViewById(R.id.card4);
        card5 = findViewById(R.id.card5);

        taskTextView3 = findViewById(R.id.task_text_view_3);
        taskTextView4 = findViewById(R.id.task_text_view_4);
        taskTextView5 = findViewById(R.id.task_text_view_5);

        ImageView leftArrow = findViewById(R.id.left_arrow);
        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start MainUserLogin activity
                Intent intent = new Intent(MainActivity.this, MainUserLogin.class);
                startActivity(intent);
            }
        });

        ImageView task = findViewById(R.id.task);
        task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TaskManager activity
                Intent intent = new Intent(MainActivity.this, TaskManager.class);
                startActivity(intent);
            }
        });

        ImageView calendar = findViewById(R.id.calendar);
        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start ScheduleTaskActivity activity
                Intent intent = new Intent(MainActivity.this, ScheduleTaskActivity.class);
                startActivity(intent);
            }
        });

        ImageView bell = findViewById(R.id.bell);
        bell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Notification activity
                Intent intent = new Intent(MainActivity.this, Notification.class);
                startActivity(intent);
            }
        });

        ImageView user = findViewById(R.id.user);
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Profile activity
                Intent intent = new Intent(MainActivity.this, Profile.class);
                startActivity(intent);
            }
        });

        loadUserTasks();
    }

    private void loadUserTasks() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(MainActivity.this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = currentUser.getEmail();
        if (userEmail == null) {
            Toast.makeText(MainActivity.this, "Failed to get user email", Toast.LENGTH_SHORT).show();
            return;
        }

        mDatabaseTasks.orderByChild("assignedUser").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int taskCount = 0;
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    String taskName = taskSnapshot.child("taskName").getValue(String.class);
                    String dueDate = taskSnapshot.child("dueDate").getValue(String.class);
                    String description = taskSnapshot.child("taskDescription").getValue(String.class);
                    if (taskName != null) {
                        taskCount++;
                        switch (taskCount) {
                            case 1:
                                taskTextView3.setText(taskName + "\n" + description + "\n" + dueDate);
                                break;
                            case 2:
                                taskTextView4.setText(taskName + "\n" + description + "\n" + dueDate);
                                break;
                            case 3:
                                taskTextView5.setText(taskName + "\n" + description + "\n" + dueDate);
                                break;
                            default:
                                break;
                        }
                    }
                    if (taskCount >= 3) break;
                }
                if (taskCount == 0) {
                    Toast.makeText(MainActivity.this, "No tasks assigned", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
