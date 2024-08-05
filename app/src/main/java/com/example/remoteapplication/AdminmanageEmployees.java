package com.example.remoteapplication;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminmanageEmployees extends AppCompatActivity {

    private EditText emailField, firstname, lastname, role;
    private Spinner usersSpinner;
    private Button buttonUpdate, buttonDelete;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private String currentUserOrganization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminmanage_employees);

        emailField = findViewById(R.id.email_edittext);
        firstname = findViewById(R.id.firstname_edittext);
        lastname = findViewById(R.id.lastname_edittext);
        role = findViewById(R.id.role_edittext);
        usersSpinner = findViewById(R.id.users_spinner);
        buttonUpdate = findViewById(R.id.button_update);
        buttonDelete = findViewById(R.id.button_delete);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("organization");

        setupNavigation();
        fetchUserOrganization();
        setupSpinnerListener();
        setupButtonListeners();
    }

    private void setupNavigation() {
        ImageView leftArrow = findViewById(R.id.left_arrow);
        leftArrow.setOnClickListener(v -> startActivity(new Intent(AdminmanageEmployees.this, Admin.class)));

        ImageView task = findViewById(R.id.task);
        task.setOnClickListener(v -> startActivity(new Intent(AdminmanageEmployees.this, AssignTaskActivity.class)));

        ImageView barchart = findViewById(R.id.barchart);
        barchart.setOnClickListener(v -> startActivity(new Intent(AdminmanageEmployees.this, Adminreports.class)));

        ImageView bell = findViewById(R.id.bell);
        bell.setOnClickListener(v -> startActivity(new Intent(AdminmanageEmployees.this, Adminnotifications.class)));

        ImageView user = findViewById(R.id.user);
        user.setOnClickListener(v -> startActivity(new Intent(AdminmanageEmployees.this, Adminprofile.class)));
    }

    private void fetchUserOrganization() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("organization");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean organizationFound = false;
                    for (DataSnapshot orgSnapshot : snapshot.getChildren()) {
                        DataSnapshot usersNode = orgSnapshot.child("users");
                        if (usersNode.hasChild(currentUserId)) {
                            currentUserOrganization = orgSnapshot.getKey();
                            Log.d(TAG, "Current user's organization: " + currentUserOrganization);
                            organizationFound = true;
                            populateUserSpinner();
                            break;
                        }
                    }

                    if (!organizationFound) {
                        Log.e(TAG, "Organization not found for current user");
                        Toast.makeText(AdminmanageEmployees.this, "Organization not found for current user", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to get organization: " + error.getMessage());
                    Toast.makeText(AdminmanageEmployees.this, "Failed to get organization: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "User not logged in");
        }
    }
    private void populateUserSpinner() {
        DatabaseReference usersRef = mDatabase.child(currentUserOrganization).child("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> userEmails = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String email = userSnapshot.child("email").getValue(String.class);
                    if (email != null) {
                        userEmails.add(email);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminmanageEmployees.this, android.R.layout.simple_spinner_item, userEmails);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                usersSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminmanageEmployees.this, "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinnerListener() {
        usersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedEmail = (String) parent.getItemAtPosition(position);
                loadUserData(selectedEmail);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void loadUserData(String email) {
        Query userQuery = mDatabase.child(currentUserOrganization).child("users").orderByChild("email").equalTo(email);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    firstname.setText(userSnapshot.child("firstName").getValue(String.class));
                    lastname.setText(userSnapshot.child("lastName").getValue(String.class));
                    emailField.setText(userSnapshot.child("email").getValue(String.class));
                    role.setText(userSnapshot.child("role").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminmanageEmployees.this, "Failed to load user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtonListeners() {
        buttonUpdate.setOnClickListener(v -> {
            String selectedEmail = (String) usersSpinner.getSelectedItem();
            updateUserData(selectedEmail);
        });

        buttonDelete.setOnClickListener(v -> {
            String selectedEmail = (String) usersSpinner.getSelectedItem();
            deleteUser(selectedEmail);
        });
    }

    private void updateUserData(String email) {
        Query userQuery = mDatabase.child(currentUserOrganization).child("users").orderByChild("email").equalTo(email);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    userSnapshot.getRef().child("firstName").setValue(firstname.getText().toString());
                    userSnapshot.getRef().child("lastName").setValue(lastname.getText().toString());
                    userSnapshot.getRef().child("email").setValue(emailField.getText().toString());
                    userSnapshot.getRef().child("role").setValue(role.getText().toString());
                    Toast.makeText(AdminmanageEmployees.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminmanageEmployees.this, "Failed to update user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUser(String email) {
        Query userQuery = mDatabase.child(currentUserOrganization).child("users").orderByChild("email").equalTo(email);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    userSnapshot.getRef().removeValue();
                    Toast.makeText(AdminmanageEmployees.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                    // Refresh the spinner after deleting the user
                    populateUserSpinner();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminmanageEmployees.this, "Failed to delete user: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
