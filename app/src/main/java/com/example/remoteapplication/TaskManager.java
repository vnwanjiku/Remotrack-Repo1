package com.example.remoteapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
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

public class TaskManager extends AppCompatActivity {

    private TextView textDurationPicker;
    private Spinner taskNameSpinner, taskDescriptionSpinner;
    private Button btnstarted, btninprogress, btnpending, btncomplete;
    private int hours = 0;
    private int minutes = 0;
    private String currentUserEmail;

    private DatabaseReference tasksRef;
    private List<String> taskNamesList;
    private ArrayAdapter<String> taskNameAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskmanager);

        textDurationPicker = findViewById(R.id.text_duration_picker);
        taskNameSpinner = findViewById(R.id.spinner_task_name);
        taskDescriptionSpinner = findViewById(R.id.spinner_task_description);
        btnstarted = findViewById(R.id.btn_view_started);
        btninprogress = findViewById(R.id.btn_view_inprogress);
        btnpending = findViewById(R.id.btn_view_pending);
        btncomplete = findViewById(R.id.btn_view_complete);

        // Simulate getting current user's email
        currentUserEmail = getCurrentUserEmail();

        initializeSpinners();

        textDurationPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDurationPickerDialog();
            }
        });

        btnstarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTaskStatus("Started");
            }
        });

        btninprogress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTaskStatus("In Progress");
            }
        });

        btnpending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTaskStatus("Pending");
            }
        });

        btncomplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTaskStatus("Complete");
            }
        });

        initializeNavigation();
    }

    private void initializeSpinners() {
        taskNamesList = new ArrayList<>();
        taskNameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, taskNamesList);
        taskNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskNameSpinner.setAdapter(taskNameAdapter);

        tasksRef = FirebaseDatabase.getInstance().getReference().child("organization").child("Remo").child("tasks");
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                taskNamesList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String assignedUser = snapshot.child("assignedUser").getValue(String.class);
                    if (assignedUser != null && assignedUser.equals(currentUserEmail)) {
                        String taskName = snapshot.child("taskName").getValue(String.class);
                        taskNamesList.add(taskName);
                    }
                }
                taskNameAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TaskManager.this, "Failed to load tasks: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        taskNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTaskName = taskNamesList.get(position);
                populateTaskDescriptionSpinner(selectedTaskName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void populateTaskDescriptionSpinner(String selectedTaskName) {
        tasksRef.orderByChild("taskName").equalTo(selectedTaskName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String assignedUser = snapshot.child("assignedUser").getValue(String.class);
                    if (assignedUser != null && assignedUser.equals(currentUserEmail)) {
                        String taskDescription = snapshot.child("taskDescription").getValue(String.class);
                        List<String> taskDescriptions = new ArrayList<>();
                        taskDescriptions.add(taskDescription);
                        ArrayAdapter<String> descriptionAdapter = new ArrayAdapter<>(TaskManager.this,
                                android.R.layout.simple_spinner_item, taskDescriptions);
                        descriptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        taskDescriptionSpinner.setAdapter(descriptionAdapter);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TaskManager.this, "Failed to load task descriptions: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDurationPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_duration_picker, null);
        builder.setView(dialogView);

        final NumberPicker hoursPicker = dialogView.findViewById(R.id.hours_picker);
        final NumberPicker minutesPicker = dialogView.findViewById(R.id.minutes_picker);

        // Set min and max values programmatically
        hoursPicker.setMinValue(0);
        hoursPicker.setMaxValue(23);
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(59);

        hoursPicker.setValue(hours);
        minutesPicker.setValue(minutes);

        builder.setPositiveButton("OK", (dialog, which) -> {
            hours = hoursPicker.getValue();
            minutes = minutesPicker.getValue();
            String duration = String.format("%02d:%02d", hours, minutes);
            textDurationPicker.setText(duration);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateTaskStatus(String status) {
        String selectedTaskName = taskNameSpinner.getSelectedItem().toString();
        String selectedTaskDescription = taskDescriptionSpinner.getSelectedItem().toString();
        String duration = textDurationPicker.getText().toString();
        tasksRef.orderByChild("taskName").equalTo(selectedTaskName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String taskDescription = snapshot.child("taskDescription").getValue(String.class);
                    if (taskDescription != null && taskDescription.equals(selectedTaskDescription)) {
                        snapshot.getRef().child("status").setValue(status);
                        snapshot.getRef().child("duration").setValue(duration);
                    }
                }
                Toast.makeText(TaskManager.this, "Task updated with status: " + status + " and duration: " + duration, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TaskManager.this, "Failed to update task status: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeNavigation() {
        ImageView leftArrow = findViewById(R.id.left_arrow);
        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskManager.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ImageView task = findViewById(R.id.task);
        task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskManager.this, TaskManager.class);
                startActivity(intent);
            }
        });

        ImageView calendar = findViewById(R.id.calendar);
        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskManager.this, ScheduleTaskActivity.class);
                startActivity(intent);
            }
        });

        ImageView bell = findViewById(R.id.bell);
        bell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskManager.this, Notification.class);
                startActivity(intent);
            }
        });

        ImageView user = findViewById(R.id.user);
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskManager.this, Profile.class);
                startActivity(intent);
            }
        });
    }

    private String getCurrentUserEmail() {
        // Get current user email from Firebase Auth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getEmail();
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if user is not authenticated
            return null; // Return null or handle as needed based on your app's flow
        }
    }
}
