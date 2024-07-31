package com.example.remoteapplication;

import static androidx.fragment.app.FragmentManager.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.androidplot.xy.XYPlot;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private MaterialCardView card3, card4, card5;
    private TextView taskTextView3, taskTextView4, taskTextView5, userrealname, userrealtime, announcements, tasknumber, taskstarted, taskcompleted;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers, mDatabaseTasks;
    private String currentUserOrganization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("organization");

        card3 = findViewById(R.id.card3);
        card4 = findViewById(R.id.card4);
        card5 = findViewById(R.id.card5);

        taskTextView3 = findViewById(R.id.task_text_view_3);
        taskTextView4 = findViewById(R.id.task_text_view_4);
        taskTextView5 = findViewById(R.id.task_text_view_5);

        tasknumber = findViewById(R.id.tasknumber);
        taskstarted = findViewById(R.id.taskstarted);
        taskcompleted = findViewById(R.id.taskcompleted);

        userrealname = findViewById(R.id.userrealname);
        userrealtime = findViewById(R.id.userrealtime);
        announcements = findViewById(R.id.announcements);

        fetchCurrentUserOrganization();
        setGreetingMessage();

        ImageView leftArrow = findViewById(R.id.left_arrow);
        leftArrow.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainUserLogin.class);
            startActivity(intent);
        });

        ImageView task = findViewById(R.id.task);
        task.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TaskManager.class);
            startActivity(intent);
        });

        ImageView calendar = findViewById(R.id.calendar);
        calendar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScheduleTaskActivity.class);
            startActivity(intent);
        });

        ImageView bell = findViewById(R.id.bell);
        bell.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Notification.class);
            startActivity(intent);
        });

        ImageView user = findViewById(R.id.user);
        user.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Profile.class);
            startActivity(intent);
        });
    }

    private void fetchCurrentUserOrganization() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(MainActivity.this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        DatabaseReference userRef = mDatabaseUsers;

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot organizationSnapshot : snapshot.getChildren()) {
                    if (organizationSnapshot.child("users").hasChild(uid)) {
                        currentUserOrganization = organizationSnapshot.getKey();
                        mDatabaseTasks = mDatabaseUsers.child(currentUserOrganization).child("tasks");
                        fetchUserName();
                        loadUserTasks();
                        fetchLatestAnnouncement();  // Fetch the latest announcement after loading tasks
                        break;
                    }
                }
                if (currentUserOrganization == null) {
                    Toast.makeText(MainActivity.this, "Organization not found for current user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to get organization: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("organization").child(currentUserOrganization).child("users").child(uid);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String firstName = snapshot.child("firstName").getValue(String.class);

                    if (firstName != null) {
                        userrealname.setText(firstName);
                    } else {
                        userrealname.setText("Welcome");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    userrealname.setText("Welcome");
                    Log.e(TAG, "Failed to get user data: " + error.getMessage());
                }
            });
        } else {
            // No user is signed in, handle this case if needed
            // For example, redirect to login screen
            Intent intent = new Intent(MainActivity.this, MainUserLogin.class);
            startActivity(intent);
            finish();
        }
    }

    private void setGreetingMessage() {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        String timeOfDay;
        if (hourOfDay >= 0 && hourOfDay < 12) {
            timeOfDay = "Morning,";
        } else if (hourOfDay >= 12 && hourOfDay < 17) {
            timeOfDay = "Afternoon,";
        } else {
            timeOfDay = "Evening,";
        }
        userrealtime.setText(timeOfDay);
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
                int startedCount = 0;
                int completedCount = 0;

                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    String taskName = taskSnapshot.child("taskName").getValue(String.class);
                    String dueDate = taskSnapshot.child("dueDate").getValue(String.class);
                    String description = taskSnapshot.child("taskDescription").getValue(String.class);
                    String status = taskSnapshot.child("status").getValue(String.class);

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

                    if ("Started".equals(status)) {
                        startedCount++;
                    } else if ("Completed".equals(status)) {
                        completedCount++;
                    }

                    if (taskCount >= 3) break;
                }

                tasknumber.setText(String.valueOf(taskCount));
                taskstarted.setText(String.valueOf(startedCount));
                taskcompleted.setText(String.valueOf(completedCount));

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

    private void fetchLatestAnnouncement() {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference()
                .child("organization")
                .child(currentUserOrganization)
                .child("notifications");

        notificationsRef.orderByChild("receiverId").equalTo("All").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message latestMessage = messageSnapshot.getValue(Message.class);
                    if (latestMessage != null) {
                        announcements.setText(latestMessage.getText());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load announcement: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
