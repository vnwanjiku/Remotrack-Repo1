package com.example.remoteapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.util.Log;

public class MainUserLogin extends AppCompatActivity {

    private EditText editEmail;
    private EditText editPassword;
    private Button loginButton;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private static final String TAG = "MainUserLogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user_login);

        // Initialize the views
        editEmail = findViewById(R.id.useremail1);
        editPassword = findViewById(R.id.userpassword1);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Database reference
        mDatabase = FirebaseDatabase.getInstance().getReference("organization");

        // Set an OnClickListener on the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Set an OnClickListener on the register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start RegisterActivity
                Intent intent = new Intent(MainUserLogin.this, RegisterActivity.class);
                // Start RegisterActivity
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty()) {
            editEmail.setError("Email is required");
            editEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Enter a valid email");
            editEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editPassword.setError("Password is required");
            editPassword.requestFocus();
            return;
        }

        // Authenticate with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, get the user's UID
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Log.d(TAG, "Login successful, fetching user organization");
                                fetchUserOrganization(user.getUid());
                            } else {
                                Log.e(TAG, "Failed to get user information");
                                Toast.makeText(MainUserLogin.this, "Failed to get user information", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "Authentication failed: " + task.getException().getMessage());
                            Toast.makeText(MainUserLogin.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void fetchUserOrganization(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("organization");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot orgSnapshot : snapshot.getChildren()) {
                    DataSnapshot usersSnapshot = orgSnapshot.child("users");
                    if (usersSnapshot.child(uid).exists()) {
                        String organization = orgSnapshot.getKey(); // Get the organization name
                        if (organization != null) {
                            Log.d(TAG, "Organization found: " + organization);
                            checkUserRole(uid, organization);
                            return; // Exit loop once organization is found
                        }
                    }
                }
                Log.e(TAG, "Organization not found for user");
                Toast.makeText(MainUserLogin.this, "Organization not found", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to get organization: " + error.getMessage());
                Toast.makeText(MainUserLogin.this, "Failed to get organization: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void checkUserRole(String uid, String organization) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("organization")
                .child("Remo").child("users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    if (role != null) {
                        Log.d(TAG, "Role found: " + role);
                        if (role.equals("admin")) {
                            // Navigate to Admin activity
                            Intent intent = new Intent(MainUserLogin.this, Admin.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Navigate to MainActivity
                            Intent intent = new Intent(MainUserLogin.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Log.e(TAG, "Role not found");
                        Toast.makeText(MainUserLogin.this, "Role not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "User data not found in organization");
                    Toast.makeText(MainUserLogin.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to get role: " + error.getMessage());
                Toast.makeText(MainUserLogin.this, "Failed to get role: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
