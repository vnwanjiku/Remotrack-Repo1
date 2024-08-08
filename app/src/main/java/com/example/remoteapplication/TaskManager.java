package com.example.remoteapplication;

import androidx.annotation.NonNull;
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

    private static final String TASKS_NODE = "tasks";
    private static final String ORGANIZATION_NODE = "organization";
    private static final String USERS_NODE = "users";

    private TextView textDurationPicker;
    private Spinner taskNameSpinner, taskDescriptionSpinner;
    private Button btnStarted, btnInProgress, btnPending, btnComplete;
    private int hours = 0;
    private int minutes = 0;
    private String currentUserEmail;
    private FirebaseAuth mAuth;
    private DatabaseReference tasksRef, mDatabaseUsers;
    private List<String> taskNamesList;
    private String currentUserOrganization;
    private ArrayAdapter<String> taskNameAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskmanager);

        initializeViews();
        initializeFirebase();
        setupListeners();
        fetchCurrentUserOrganization();
    }

    private void initializeViews() {
        textDurationPicker = findViewById(R.id.text_duration_picker);
        taskNameSpinner = findViewById(R.id.spinner_task_name);
        taskDescriptionSpinner = findViewById(R.id.spinner_task_description);
        btnStarted = findViewById(R.id.btn_view_started);
        btnInProgress = findViewById(R.id.btn_view_inprogress);
        btnPending = findViewById(R.id.btn_view_pending);
        btnComplete = findViewById(R.id.btn_view_complete);
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUserEmail = getCurrentUserEmail();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child(ORGANIZATION_NODE);
    }

    private void setupListeners() {
        textDurationPicker.setOnClickListener(v -> showDurationPickerDialog());

        btnStarted.setOnClickListener(v -> updateTaskStatus("Started"));
        btnInProgress.setOnClickListener(v -> updateTaskStatus("In Progress"));
        btnPending.setOnClickListener(v -> updateTaskStatus("Pending"));
        btnComplete.setOnClickListener(v -> updateTaskStatus("Complete"));

        initializeNavigation();
    }

    private void fetchCurrentUserOrganization() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(TaskManager.this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        mDatabaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot organizationSnapshot : snapshot.getChildren()) {
                    if (organizationSnapshot.child(USERS_NODE).hasChild(uid)) {
                        currentUserOrganization = organizationSnapshot.getKey();
                        tasksRef = FirebaseDatabase.getInstance().getReference()
                                .child(ORGANIZATION_NODE)
                                .child(currentUserOrganization)
                                .child(TASKS_NODE);
                        initializeSpinners();
                        break;
                    }
                }
                if (currentUserOrganization == null) {
                    Toast.makeText(TaskManager.this, "Organization not found for current user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TaskManager.this, "Failed to get organization: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeSpinners() {
        taskNamesList = new ArrayList<>();
        taskNameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, taskNamesList);
        taskNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskNameSpinner.setAdapter(taskNameAdapter);

        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                taskNamesList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String assignedUser = snapshot.child("assignedUser").getValue(String.class);
                    if (assignedUser != null && assignedUser.equals(currentUserEmail)) {
                        String taskName = snapshot.child("taskName").getValue(String.class);
                        if (taskName != null) {
                            taskNamesList.add(taskName);
                        }
                    }
                }
                taskNameAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
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
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> taskDescriptions = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String assignedUser = snapshot.child("assignedUser").getValue(String.class);
                    if (assignedUser != null && assignedUser.equals(currentUserEmail)) {
                        String taskDescription = snapshot.child("taskDescription").getValue(String.class);
                        if (taskDescription != null) {
                            taskDescriptions.add(taskDescription);
                        }
                    }
                }
                ArrayAdapter<String> descriptionAdapter = new ArrayAdapter<>(TaskManager.this,
                        android.R.layout.simple_spinner_item, taskDescriptions);
                descriptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                taskDescriptionSpinner.setAdapter(descriptionAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
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
        String selectedTaskName = (String) taskNameSpinner.getSelectedItem();
        String selectedTaskDescription = (String) taskDescriptionSpinner.getSelectedItem();
        String duration = textDurationPicker.getText().toString();
        if (selectedTaskName == null || selectedTaskDescription == null) {
            Toast.makeText(this, "Please select a task name and description", Toast.LENGTH_SHORT).show();
            return;
        }

        tasksRef.orderByChild("taskName").equalTo(selectedTaskName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String taskDescription = snapshot.child("taskDescription").getValue(String.class);
                    if (taskDescription != null && taskDescription.equals(selectedTaskDescription)) {
                        snapshot.getRef().child("status").setValue(status);
                        snapshot.getRef().child("duration").setValue(duration);
                        Toast.makeText(TaskManager.this, "Task status updated to: " + status, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
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
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null ? currentUser.getEmail() : null;
    }
}
