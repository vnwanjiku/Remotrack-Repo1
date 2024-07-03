package com.example.remoteapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RegisterUser extends AppCompatActivity {

    private EditText editFirstName;
    private EditText editLastName;
    private EditText editEmail;
    private EditText editPassword;
    private Button btnRegister;
    private ImageView imageRegister;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserOrganization; // To store current user's organization dynamically

    private static final String TAG = "RegisterUser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        editFirstName = findViewById(R.id.edit_first_name);
        editLastName = findViewById(R.id.edit_last_name);
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        btnRegister = findViewById(R.id.btn_register);
        imageRegister = findViewById(R.id.image_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Fetch current user's organization dynamically
        fetchCurrentUserOrganization();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void fetchCurrentUserOrganization() {
        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("organization");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot orgSnapshot : snapshot.getChildren()) {
                    DataSnapshot usersNode = orgSnapshot.child("users");
                    if (usersNode.hasChild(uid)) {
                        // User found under this organization
                        currentUserOrganization = orgSnapshot.getKey(); // Get the organization name
                        Log.d(TAG, "Current user's organization: " + currentUserOrganization);
                        return; // Exit loop after finding the organization
                    }
                }

                // If no organization found for current user
                Log.e(TAG, "Organization not found for current user");
                Toast.makeText(RegisterUser.this, "Organization not found for current user", Toast.LENGTH_SHORT).show();
                // Handle the case where organization is not found
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to get organization: " + error.getMessage());
                Toast.makeText(RegisterUser.this, "Failed to get organization: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void registerUser() {
        String firstName = editFirstName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(RegisterUser.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Enter a valid email");
            editEmail.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editPassword.setError("Password should be at least 6 characters long");
            editPassword.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User registered successfully");

                            // User registration successful, save additional user details to Realtime Database
                            saveUserDetails(firstName, lastName, email);
                        } else {
                            // Registration failed
                            Log.e(TAG, "Registration failed: " + task.getException().getMessage());
                            Toast.makeText(RegisterUser.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveUserDetails(String firstName, String lastName, String email) {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = mDatabase.child("organization").child(currentUserOrganization).child("users").child(userId);

        Map<String, Object> user = new HashMap<>();
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("email", email);
        user.put("role", "user"); // Assign role as "user"

        userRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User details saved successfully");
                    Toast.makeText(RegisterUser.this, "User registered successfully", Toast.LENGTH_SHORT).show();

                    // Navigate to MainActivity after successful registration
                    Intent intent = new Intent(RegisterUser.this, Admin.class);
                    startActivity(intent);

                    // Optionally, finish this activity to remove it from the back stack
                    finish();
                } else {
                    Log.e(TAG, "Failed to save user details: " + task.getException().getMessage());
                    Toast.makeText(RegisterUser.this, "Failed to save user details: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
