package com.example.remoteapplication;

import static androidx.fragment.app.FragmentManager.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.ValueAnimator;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private MaterialCardView card3, card4, card5;
    private TextView taskTextView3, taskTextView4, taskTextView5;
    private TextView userrealname, userrealtime, announcements;
    private TextView tasknumber, taskstarted, taskcompleted;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers, mDatabaseTasks;
    private String currentUserOrganization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUI();
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("organization");

        fetchCurrentUserOrganization();
        setGreetingMessage();
        setupNavigation();
    }

    private void initializeUI() {
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
    }

    private void setupNavigation() {
        ImageView leftArrow = findViewById(R.id.left_arrow);
        leftArrow.setOnClickListener(v -> navigateTo(MainUserLogin.class));

        ImageView task = findViewById(R.id.task);
        task.setOnClickListener(v -> navigateTo(TaskManager.class));

        ImageView calendar = findViewById(R.id.calendar);
        calendar.setOnClickListener(v -> navigateTo(ScheduleTaskActivity.class));

        ImageView bell = findViewById(R.id.bell);
        bell.setOnClickListener(v -> navigateTo(Notification.class));

        ImageView user = findViewById(R.id.user);
        user.setOnClickListener(v -> navigateTo(Profile.class));
    }

    private void navigateTo(Class<?> destination) {
        Intent intent = new Intent(MainActivity.this, destination);
        startActivity(intent);
    }

    private void fetchCurrentUserOrganization() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(MainActivity.this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        mDatabaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot organizationSnapshot : snapshot.getChildren()) {
                    if (organizationSnapshot.child("users").hasChild(uid)) {
                        currentUserOrganization = organizationSnapshot.getKey();
                        mDatabaseTasks = mDatabaseUsers.child(currentUserOrganization).child("tasks");
                        fetchUserName();
                        loadUserTasks();
                        fetchLatestAnnouncement();
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
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("organization")
                    .child(currentUserOrganization).child("users").child(uid);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    userrealname.setText(firstName != null ? firstName : "Welcome");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    userrealname.setText("Welcome");
                    Log.e(TAG, "Failed to get user data: " + error.getMessage());
                }
            });
        } else {
            navigateTo(MainUserLogin.class);
            finish();
        }
    }

    private void setGreetingMessage() {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        String timeOfDay;
        if (hourOfDay < 12) {
            timeOfDay = "Morning,";
        } else if (hourOfDay < 17) {
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

                animateTextView(0, taskCount, tasknumber);
                animateTextView(0, startedCount, taskstarted);
                animateTextView(0, completedCount, taskcompleted);

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

    private void animateTextView(int initialValue, int finalValue, TextView textView) {
        ValueAnimator animator = ValueAnimator.ofInt(initialValue, finalValue);
        animator.setDuration(1000);
        animator.addUpdateListener(animation -> textView.setText(String.valueOf(animation.getAnimatedValue())));
        animator.start();
    }
}
