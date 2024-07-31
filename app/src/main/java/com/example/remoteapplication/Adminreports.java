package com.example.remoteapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Adminreports extends AppCompatActivity {

    private static final String TAG = "Adminreports";

    private ListView listViewTasks;
    private ListView listViewAdmins;
    private ListView listViewEmployees;

    private List<Task> taskList = new ArrayList<>();
    private List<User> adminList = new ArrayList<>();
    private List<User> employeeList = new ArrayList<>();

    private TaskAdapter taskAdapter;
    private AdminAdapter adminAdapter;
    private EmployeeAdapter employeeAdapter;

    private String currentUserOrganization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        listViewTasks = findViewById(R.id.listViewTasks);
        listViewAdmins = findViewById(R.id.listViewAdmins);
        listViewEmployees = findViewById(R.id.listViewEmployees);

        taskAdapter = new TaskAdapter(this, taskList);
        adminAdapter = new AdminAdapter(this, adminList);
        employeeAdapter = new EmployeeAdapter(this, employeeList);

        listViewTasks.setAdapter(taskAdapter);
        listViewAdmins.setAdapter(adminAdapter);
        listViewEmployees.setAdapter(employeeAdapter);

        ImageView leftArrow = findViewById(R.id.left_arrow);
        leftArrow.setOnClickListener(v -> startActivity(new Intent(Adminreports.this, Admin.class)));

        ImageView task = findViewById(R.id.task);
        task.setOnClickListener(v -> startActivity(new Intent(Adminreports.this, AssignTaskActivity.class)));

        ImageView barchart = findViewById(R.id.barchart);
        barchart.setOnClickListener(v -> startActivity(new Intent(Adminreports.this, Adminreports.class)));

        ImageView bell = findViewById(R.id.bell);
        bell.setOnClickListener(v -> startActivity(new Intent(Adminreports.this, Adminnotifications.class)));

        ImageView user = findViewById(R.id.user);
        user.setOnClickListener(v -> startActivity(new Intent(Adminreports.this, Adminprofile.class)));

        fetchUserOrganization();
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
                            loadDataFromDatabase();
                            break;
                        }
                    }

                    if (!organizationFound) {
                        Log.e(TAG, "Organization not found for current user");
                        Toast.makeText(Adminreports.this, "Organization not found for current user", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to get organization: " + error.getMessage());
                    Toast.makeText(Adminreports.this, "Failed to get organization: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "User not logged in");
        }
    }

    private void loadDataFromDatabase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Load tasks
        databaseReference.child(currentUserOrganization).child("tasks").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                taskList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String taskName = snapshot.child("taskName").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    String assignedUser = snapshot.child("assignedUser").getValue(String.class);

                    if (taskName != null && status != null && assignedUser != null) {
                        Task task = new Task();
                        task.setTaskName(taskName);
                        task.setStatus(status);
                        task.setAssignedUser(assignedUser);
                        taskList.add(task);
                    }
                }
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading tasks", databaseError.toException());
            }
        });

        // Load admins
        databaseReference.child(currentUserOrganization).child("users").orderByChild("role").equalTo("admin").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adminList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    if (firstName != null && lastName != null && email != null) {
                        User admin = new User();
                        admin.setFirstName(firstName);
                        admin.setLastName(lastName);
                        admin.setEmail(email);
                        adminList.add(admin);
                    }
                }
                adminAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading admins", databaseError.toException());
            }
        });

        // Load employees
        databaseReference.child(currentUserOrganization).child("users").orderByChild("role").equalTo("user").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                employeeList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    if (firstName != null && lastName != null && email != null) {
                        User employee = new User();
                        employee.setFirstName(firstName);
                        employee.setLastName(lastName);
                        employee.setEmail(email);
                        employeeList.add(employee);
                    }
                }
                employeeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading employees", databaseError.toException());
            }
        });
    }
}
