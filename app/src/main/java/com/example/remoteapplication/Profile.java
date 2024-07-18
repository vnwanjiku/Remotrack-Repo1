package com.example.remoteapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

public class Profile extends AppCompatActivity {

    private TextView textinfoname1, textinfoname2, textinfoname3, textinfoname4;
    private Button logoutbtn;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        textinfoname1 = findViewById(R.id.textinfoname1);
        textinfoname2 = findViewById(R.id.textinfoname2);
        textinfoname3 = findViewById(R.id.textinfoname3);
        textinfoname4 = findViewById(R.id.textinfoname4);
        logoutbtn = findViewById(R.id.logoutbtn);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("users");

        loadUserProfile();

        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(Profile.this, MainUserLogin.class);
                startActivity(intent);
                finish();
            }
        });

        ImageView leftArrow = findViewById(R.id.left_arrow);
        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ImageView task = findViewById(R.id.task);
        task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TaskManager activity
                Intent intent = new Intent(Profile.this, TaskManager.class);
                startActivity(intent);
            }
        });

        ImageView calendar = findViewById(R.id.calendar);
        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start ScheduleTaskActivity activity
                Intent intent = new Intent(Profile.this, ScheduleTaskActivity.class);
                startActivity(intent);
            }
        });

        ImageView bell = findViewById(R.id.bell);
        bell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Notification activity
                Intent intent = new Intent(Profile.this, Notification.class);
                startActivity(intent);
            }
        });

        ImageView user = findViewById(R.id.user);
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Profile activity
                Intent intent = new Intent(Profile.this, Profile.class);
                startActivity(intent);
            }
        });
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(Profile.this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("organization");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot orgSnapshot : snapshot.getChildren()) {
                    DataSnapshot usersNode = orgSnapshot.child("users");
                    if (usersNode.hasChild(userId)) {
                        DataSnapshot userSnapshot = usersNode.child(userId);

                        // Retrieve user detailsedwin
                        String firstName = userSnapshot.child("firstName").getValue(String.class);
                        String lastName = userSnapshot.child("lastName").getValue(String.class);
                        String email = currentUser.getEmail(); // Use FirebaseUser's email
                        String organization = orgSnapshot.getKey(); // Get organization name

                        // Display user details in UI
                        textinfoname1.setText(firstName + " " + lastName);
                        textinfoname2.setText(email);
                        textinfoname3.setText(organization);
                        textinfoname4.setText("User"); // Assuming it's fixed for admins

                        return; // Exit loop after finding the user
                    }
                }

                // Handle case where user data is not found
                Toast.makeText(Profile.this, "User data not found", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Failed to load user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
