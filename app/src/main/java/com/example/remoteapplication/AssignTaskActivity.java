package com.example.remoteapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignTaskActivity extends AppCompatActivity {

    private EditText editTaskName, editTaskDescription, editTaskDescriptionConfig;
    private DatePicker datePicker;
    private Spinner spinnerSelectIndividual, spinnerSelectTaskToConfigure;
    private Button btnAddTask, btnUpdateTask, btnDeleteTask;
    private DatabaseReference mDatabaseUsers, mDatabaseTasks;
    private FirebaseAuth mAuth;
    private String currentUserOrganization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_task);

        // Initialize views
        editTaskName = findViewById(R.id.edit_task_name);
        editTaskDescription = findViewById(R.id.edit_task_description);
        datePicker = findViewById(R.id.date_picker);
        spinnerSelectIndividual = findViewById(R.id.spinner_select_individual);
        spinnerSelectTaskToConfigure = findViewById(R.id.spinner_select_task_to_configure);
        editTaskDescriptionConfig = findViewById(R.id.edit_task_description_config);
        btnAddTask = findViewById(R.id.btn_add_task);
        btnUpdateTask = findViewById(R.id.btn_update_task);
        btnDeleteTask = findViewById(R.id.btn_delete_task);

        // Initialize Firebase Database references
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("organization");

        fetchCurrentUserOrganization();

        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask();
            }
        });

        btnUpdateTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTask();
            }
        });

        btnDeleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTask();
            }
        });

        ImageView leftArrow = findViewById(R.id.left_arrow);
        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start MainUserLogin activity
                Intent intent = new Intent(AssignTaskActivity.this, Admin.class);
                startActivity(intent);
            }
        });

        ImageView task = findViewById(R.id.task);
        task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TaskManager activity
                Intent intent = new Intent(AssignTaskActivity.this, AssignTaskActivity.class);
                startActivity(intent);
            }
        });

        ImageView barchart = findViewById(R.id.barchart);
        barchart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TaskManager activity
                Intent intent = new Intent(AssignTaskActivity.this, Adminreports.class);
                startActivity(intent);
            }
        });

        ImageView bell = findViewById(R.id.bell);
        bell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TaskManager activity
                Intent intent = new Intent(AssignTaskActivity.this, Adminnotifications.class);
                startActivity(intent);
            }
        });

        ImageView user = findViewById(R.id.user);
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TaskManager activity
                Intent intent = new Intent(AssignTaskActivity.this, Adminprofile.class);
                startActivity(intent);
            }
        });

        spinnerSelectTaskToConfigure.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTask = (String) parent.getItemAtPosition(position);
                populateTaskDetails(selectedTask);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void fetchCurrentUserOrganization() {
        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = mDatabaseUsers;

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot organizationSnapshot : snapshot.getChildren()) {
                    if (organizationSnapshot.child("users").hasChild(uid)) {
                        currentUserOrganization = organizationSnapshot.getKey();
                        mDatabaseTasks = mDatabaseUsers.child(currentUserOrganization).child("tasks");
                        populateUserSpinner();
                        populateTaskSpinner();
                        break;
                    }
                }
                if (currentUserOrganization == null) {
                    Toast.makeText(AssignTaskActivity.this, "Organization not found for current user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AssignTaskActivity.this, "Failed to get organization: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUserSpinner() {
        DatabaseReference usersRef = mDatabaseUsers.child(currentUserOrganization).child("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> users = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String role = userSnapshot.child("role").getValue(String.class);
                    if ("user".equals(role)) {
                        String email = userSnapshot.child("email").getValue(String.class);
                        users.add(email);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AssignTaskActivity.this, android.R.layout.simple_spinner_item, users);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSelectIndividual.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AssignTaskActivity.this, "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateTaskSpinner() {
        mDatabaseTasks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> tasks = new ArrayList<>();
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    String taskName = taskSnapshot.child("taskName").getValue(String.class);
                    tasks.add(taskName);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AssignTaskActivity.this, android.R.layout.simple_spinner_item, tasks);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSelectTaskToConfigure.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AssignTaskActivity.this, "Failed to load tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateTaskDetails(String taskName) {
        mDatabaseTasks.orderByChild("taskName").equalTo(taskName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                        String taskDescription = taskSnapshot.child("taskDescription").getValue(String.class);
                        String assignedUser = taskSnapshot.child("assignedUser").getValue(String.class);

                        editTaskDescriptionConfig.setText(taskDescription);
                        spinnerSelectIndividual.setSelection(((ArrayAdapter) spinnerSelectIndividual.getAdapter()).getPosition(assignedUser));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AssignTaskActivity.this, "Failed to load task details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTask() {
        String taskName = editTaskName.getText().toString().trim();
        String taskDescription = editTaskDescription.getText().toString().trim();
        String assignedUser = spinnerSelectIndividual.getSelectedItem().toString();

        if (taskName.isEmpty() || taskDescription.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();
        String dueDate = day + "/" + (month + 1) + "/" + year;

        String taskId = mDatabaseTasks.push().getKey();
        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("taskName", taskName);
        taskMap.put("taskDescription", taskDescription);
        taskMap.put("assignedUser", assignedUser);
        taskMap.put("dueDate", dueDate);

        mDatabaseTasks.child(taskId).setValue(taskMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AssignTaskActivity.this, "Task added successfully", Toast.LENGTH_SHORT).show();
                clearFields();
            } else {
                Toast.makeText(AssignTaskActivity.this, "Failed to add task: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTask() {
        String selectedTask = spinnerSelectTaskToConfigure.getSelectedItem().toString();
        String taskDescription = editTaskDescriptionConfig.getText().toString().trim();
        String assignedUser = spinnerSelectIndividual.getSelectedItem().toString();

        if (taskDescription.isEmpty()) {
            Toast.makeText(this, "Please enter a task description", Toast.LENGTH_SHORT).show();
            return;
        }

        mDatabaseTasks.orderByChild("taskName").equalTo(selectedTask).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                        String taskId = taskSnapshot.getKey();

                        Map<String, Object> taskMap = new HashMap<>();
                        taskMap.put("taskDescription", taskDescription);
                        taskMap.put("assignedUser", assignedUser);

                        mDatabaseTasks.child(taskId).updateChildren(taskMap).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(AssignTaskActivity.this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AssignTaskActivity.this, "Failed to update task: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AssignTaskActivity.this, "Failed to update task: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteTask() {
        String selectedTask = spinnerSelectTaskToConfigure.getSelectedItem().toString();

        mDatabaseTasks.orderByChild("taskName").equalTo(selectedTask).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                        String taskId = taskSnapshot.getKey();

                        mDatabaseTasks.child(taskId).removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(AssignTaskActivity.this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AssignTaskActivity.this, "Failed to delete task: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AssignTaskActivity.this, "Failed to delete task: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFields() {
        editTaskName.setText("");
        editTaskDescription.setText("");
        spinnerSelectIndividual.setSelection(0);
        datePicker.updateDate(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
    }
}
