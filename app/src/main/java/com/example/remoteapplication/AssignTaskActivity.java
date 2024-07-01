package com.example.remoteapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

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

    private EditText editTaskName, editTaskDescription;
    private DatePicker datePicker;
    private Spinner spinnerSelectIndividual;
    private Button btnAddTask, btnUpdateTask, btnDeleteTask, btnSaveTask;
    private DatabaseReference mDatabaseUsers, mDatabaseTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_task);

        editTaskName = findViewById(R.id.edit_task_name);
        editTaskDescription = findViewById(R.id.edit_task_description);
        datePicker = findViewById(R.id.date_picker);
        spinnerSelectIndividual = findViewById(R.id.spinner_select_individual);
        btnAddTask = findViewById(R.id.btn_add_task);
        btnUpdateTask = findViewById(R.id.btn_update_task);
        btnDeleteTask = findViewById(R.id.btn_delete_task);
        btnSaveTask = findViewById(R.id.btn_save_task);

        // Initialize Firebase Database references
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("users");
        mDatabaseTasks = FirebaseDatabase.getInstance().getReference("tasks");

        // Populate spinner with users
        populateUserSpinner();

        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask();
            }
        });

        btnUpdateTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update task logic here
                Toast.makeText(AssignTaskActivity.this, "Task Updated", Toast.LENGTH_SHORT).show();
            }
        });

        btnDeleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Delete task logic here
                Toast.makeText(AssignTaskActivity.this, "Task Deleted", Toast.LENGTH_SHORT).show();
            }
        });

        btnSaveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save assigned task logic here
                Toast.makeText(AssignTaskActivity.this, "Task Saved", Toast.LENGTH_SHORT).show();
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
                Intent intent = new Intent(AssignTaskActivity.this, Notification.class);
                startActivity(intent);
            }
        });

        ImageView user = findViewById(R.id.user);
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TaskManager activity
                Intent intent = new Intent(AssignTaskActivity.this, Profile.class);
                startActivity(intent);
            }
        });
    }

    private void populateUserSpinner() {
        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
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

    private void addTask() {
        String taskName = editTaskName.getText().toString().trim();
        String taskDescription = editTaskDescription.getText().toString().trim();
        String assignedUser = spinnerSelectIndividual.getSelectedItem().toString();
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        if (taskName.isEmpty() || taskDescription.isEmpty() || assignedUser.isEmpty()) {
            Toast.makeText(AssignTaskActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String dueDate = day + "/" + (month + 1) + "/" + year;

        Map<String, String> taskData = new HashMap<>();
        taskData.put("taskName", taskName);
        taskData.put("taskDescription", taskDescription);
        taskData.put("assignedUser", assignedUser);
        taskData.put("dueDate", dueDate);

        mDatabaseTasks.push().setValue(taskData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AssignTaskActivity.this, "Task added successfully", Toast.LENGTH_SHORT).show();
                        // Clear input fields
                        editTaskName.setText("");
                        editTaskDescription.setText("");
                    } else {
                        Toast.makeText(AssignTaskActivity.this, "Failed to add task", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
