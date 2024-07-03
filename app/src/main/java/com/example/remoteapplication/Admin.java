package com.example.remoteapplication;

import static androidx.fragment.app.FragmentManager.TAG;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class Admin extends AppCompatActivity {
    private TextView userrealname, userrealtime;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private Button registeruser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mAuth = FirebaseAuth.getInstance();
        userrealname = findViewById(R.id.userrealname);
        userrealtime = findViewById(R.id.userrealtime);

        // Fetch user name
        fetchUserName();

        // Set greeting based on time of day
        setGreetingMessage();

        // Initialize UI components and set click listeners
        initializeUIComponents();
    }

    private void fetchUserName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("organization");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot orgSnapshot : snapshot.getChildren()) {
                        DataSnapshot usersSnapshot = orgSnapshot.child("users");
                        if (usersSnapshot.child(uid).exists()) {
                            String firstName = usersSnapshot.child(uid).child("firstName").getValue(String.class);

                            if (firstName != null) {
                                userrealname.setText(firstName);
                            } else {
                                userrealname.setText("Welcome");
                            }
                            return; // Exit loop once user is found
                        }
                    }
                    userrealname.setText("Welcome"); // User not found scenario
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
}
