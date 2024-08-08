package com.example.remoteapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class Admin extends AppCompatActivity {
    private static final String TAG = "Admin";
    private TextView userrealname, userrealtime, usernumbers, tasksassigned, adminnumber, completedtasks, announcements;
    private FirebaseAuth mAuth;
    private DatabaseReference orgRef;
    private boolean announcementFetched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mAuth = FirebaseAuth.getInstance();
        orgRef = FirebaseDatabase.getInstance().getReference("organization");

        userrealname = findViewById(R.id.userrealname);
        userrealtime = findViewById(R.id.userrealtime);
        completedtasks = findViewById(R.id.completedtasks);
        adminnumber = findViewById(R.id.adminnumber);
        usernumbers = findViewById(R.id.usernumbers);
        tasksassigned = findViewById(R.id.taskassigned);
        announcements = findViewById(R.id.announcements);

        // Fetch user name
        fetchUserName();

        // Set greeting based on time of day
        setGreetingMessage();

        // Fetch user and task counts
        fetchUserAndTaskCounts();

        // Initialize UI components and set click listeners
        initializeUIComponents();

        // Fetch latest announcement
        fetchLatestAnnouncement();
    }

    private void fetchUserName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            orgRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean userFound = false;
                    for (DataSnapshot orgSnapshot : snapshot.getChildren()) {
                        DataSnapshot usersSnapshot = orgSnapshot.child("users");
                        if (usersSnapshot.child(uid).exists()) {
                            String firstName = usersSnapshot.child(uid).child("firstName").getValue(String.class);
                            userrealname.setText(firstName != null ? firstName : "Welcome");
                            userFound = true;
                            break; // Exit loop once user is found
                        }
                    }
                    if (!userFound) {
                        userrealname.setText("Welcome"); // User not found scenario
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
            Intent intent = new Intent(Admin.this, MainUserLogin.class);
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

    private void initializeUIComponents() {
        ImageView leftArrow = findViewById(R.id.left_arrow);
        leftArrow.setOnClickListener(v -> {
            // Start MainUserLogin activity
            Intent intent = new Intent(Admin.this, MainUserLogin.class);
            startActivity(intent);
        });

        Button manage = findViewById(R.id.manageuser);
        manage.setOnClickListener(v -> {
            // Start AdminmanageEmployees activity
            Intent intent = new Intent(Admin.this, AdminmanageEmployees.class);
            startActivity(intent);
        });

        ImageView task = findViewById(R.id.task);
        task.setOnClickListener(v -> {
            // Start AssignTaskActivity activity
            Intent intent = new Intent(Admin.this, AssignTaskActivity.class);
            startActivity(intent);
        });

        ImageView barchart = findViewById(R.id.barchart);
        barchart.setOnClickListener(v -> {
            // Start Adminreports activity
            Intent intent = new Intent(Admin.this, Adminreports.class);
            startActivity(intent);
        });

        ImageView bell = findViewById(R.id.bell);
        bell.setOnClickListener(v -> {
            // Start Adminnotifications activity
            Intent intent = new Intent(Admin.this, Adminnotifications.class);
            startActivity(intent);
        });

        Button registeruser = findViewById(R.id.registeruser);
        registeruser.setOnClickListener(v -> {
            Intent intent = new Intent(Admin.this, RegisterUser.class);
            startActivity(intent);
        });

        ImageView user = findViewById(R.id.user);
        user.setOnClickListener(v -> {
            // Start Adminprofile activity
            Intent intent = new Intent(Admin.this, Adminprofile.class);
            startActivity(intent);
        });
    }

    private void fetchLatestAnnouncement() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(Admin.this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference orgRef = FirebaseDatabase.getInstance().getReference("organization");

        orgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                announcementFetched = false;
                for (DataSnapshot orgSnapshot : snapshot.getChildren()) {
                    if (orgSnapshot.child("users").hasChild(currentUser.getUid())) {
                        DatabaseReference notificationsRef = orgSnapshot.child("notifications").getRef();

                        notificationsRef.orderByChild("receiverId").equalTo("All").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                                    Message latestMessage = messageSnapshot.getValue(Message.class);
                                    if (latestMessage != null) {
                                        announcements.setText(latestMessage.getText());
                                        announcementFetched = true;
                                        break;
                                    }
                                }
                                if (!announcementFetched) {
                                    announcements.setText("No announcements available.");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(Admin.this, "Failed to load announcement: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        break; // Exit loop once organization is found
                    }
                }
                if (!announcementFetched) {
                    announcements.setText("No announcements available.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Admin.this, "Failed to get organization: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void fetchUserAndTaskCounts() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(Admin.this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        orgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int userCount = 0;
                int adminCount = 0;
                int taskCount = 0;
                int completedTaskCount = 0;

                for (DataSnapshot orgSnapshot : snapshot.getChildren()) {
                    if (orgSnapshot.child("users").hasChild(currentUser.getUid())){
                        DataSnapshot usersSnapshot = orgSnapshot.child("users");
                        for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                            String role = userSnapshot.child("role").getValue(String.class);
                            if ("user".equalsIgnoreCase(role)) {
                                userCount++;
                            } else if ("admin".equalsIgnoreCase(role)) {
                                adminCount++;
                            }
                        }

                        DataSnapshot tasksSnapshot = orgSnapshot.child("tasks");
                        for (DataSnapshot taskSnapshot : tasksSnapshot.getChildren()) {
                            taskCount++;
                            String status = taskSnapshot.child("status").getValue(String.class);
                            if ("Complete".equalsIgnoreCase(status)) {
                                completedTaskCount++;
                            }
                        }
                    }
                }

                animateTextView(0, userCount, usernumbers);
                animateTextView(0, adminCount, adminnumber);
                animateTextView(0, taskCount, tasksassigned);
                animateTextView(0, completedTaskCount, completedtasks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Admin.this, "Failed to load counts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to animate the TextView
    private void animateTextView(int start, int end, final TextView textView) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(1000); // Duration of the animation
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                textView.setText(valueAnimator.getAnimatedValue().toString());
            }
        });
        animator.start();
    }
}
