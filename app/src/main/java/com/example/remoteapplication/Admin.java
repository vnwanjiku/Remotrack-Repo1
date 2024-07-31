package com.example.remoteapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mAuth = FirebaseAuth.getInstance();
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

        fetchLatestAnnouncement();
    }

    private void fetchUserName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("organization");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean userFound = false;
                    for (DataSnapshot orgSnapshot : snapshot.getChildren()) {
                        DataSnapshot usersSnapshot = orgSnapshot.child("users");
                        if (usersSnapshot.child(uid).exists()) {
                            Object firstNameObj = usersSnapshot.child(uid).child("firstName").getValue();
                            if (firstNameObj instanceof String) {
                                String firstName = (String) firstNameObj;
                                userrealname.setText(firstName != null ? firstName : "Welcome");
                            } else {
                                Log.e(TAG, "firstName is not a string: " + firstNameObj);
                                userrealname.setText("Welcome");
                            }
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
                Intent intent = new Intent(Admin.this, Adminnotifications.class);
                startActivity(intent);
            }
        });

        Button registeruser = findViewById(R.id.registeruser);
        registeruser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Admin.this, RegisterUser.class);
                startActivity(intent);
            }
        });

        ImageView user = findViewById(R.id.user);
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TaskManager activity
                Intent intent = new Intent(Admin.this, Adminprofile.class);
                startActivity(intent);
            }
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
                final boolean[] announcementFetched = {false};
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
                                        announcementFetched[0] = true;
                                        break;
                                    }
                                }
                                if (!announcementFetched[0]) {
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
                if (!announcementFetched[0]) {
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
        DatabaseReference orgRef = FirebaseDatabase.getInstance().getReference("organization");

        orgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int userCount = 0;
                int adminCount = 0;
                int taskCount = 0;
                int completedTaskCount = 0;

                for (DataSnapshot orgSnapshot : snapshot.getChildren()) {
                    DataSnapshot usersSnapshot = orgSnapshot.child("users");
                    for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                        String role = userSnapshot.child("role").getValue(String.class);
                        if ("user".equals(role)) {
                            userCount++;
                        } else if ("admin".equals(role)) {
                            adminCount++;
                        }
                    }

                    DataSnapshot tasksSnapshot = orgSnapshot.child("tasks");
                    for (DataSnapshot taskSnapshot : tasksSnapshot.getChildren()) {
                        taskCount++;
                        String status = taskSnapshot.child("status").getValue(String.class);
                        if ("Complete".equals(status)) {
                            completedTaskCount++;
                        }
                    }
                }

                usernumbers.setText(String.valueOf(userCount));
                adminnumber.setText(String.valueOf(adminCount));
                tasksassigned.setText(String.valueOf(taskCount));
                completedtasks.setText(String.valueOf(completedTaskCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Admin.this, "Failed to load counts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
