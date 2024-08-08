package com.example.remoteapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScheduleTaskActivity extends AppCompatActivity {

    private Spinner spinnerTaskName;
    private Spinner spinnerTaskDescription;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button btnSaveTask;
    private DatabaseReference tasksRef;
    private List<String> taskNames = new ArrayList<>();
    private List<String> taskDescriptions = new ArrayList<>();
    private List<String> taskIds = new ArrayList<>();
    private String currentUserEmail;
    private String currentUserOrganization;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private ArrayAdapter<String> taskDescriptionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_task);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        spinnerTaskName = findViewById(R.id.spinner_task_name);
        spinnerTaskDescription = findViewById(R.id.spinner_task_description);
        datePicker = findViewById(R.id.date_picker);
        timePicker = findViewById(R.id.time_picker);
        btnSaveTask = findViewById(R.id.btn_save_task);

        // Set TimePicker to 24-hour view
        timePicker.setIs24HourView(true);

        // Get current user email from Firebase Auth
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserEmail = currentUser.getEmail();
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize DatabaseReference for users
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("organization");

        // Fetch current user organization
        fetchCurrentUserOrganization();

        // Set onClick listener for the save task button
        btnSaveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();
            }
        });

        // Initialize navigation
        initializeNavigation();
    }

    private void fetchCurrentUserOrganization() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(ScheduleTaskActivity.this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        mDatabaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot organizationSnapshot : snapshot.getChildren()) {
                    if (organizationSnapshot.child("users").hasChild(uid)) {
                        currentUserOrganization = organizationSnapshot.getKey();
                        tasksRef = FirebaseDatabase.getInstance().getReference()
                                .child("organization")
                                .child(currentUserOrganization)
                                .child("tasks");
                        loadTasks();
                        break;
                    }
                }
                if (currentUserOrganization == null) {
                    Toast.makeText(ScheduleTaskActivity.this, "Organization not found for current user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ScheduleTaskActivity.this, "Failed to get organization: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTasks() {
        if (tasksRef != null) {
            tasksRef.orderByChild("assignedUser").equalTo(currentUserEmail).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    taskNames.clear();
                    taskDescriptions.clear();
                    taskIds.clear();
                    for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                        String taskName = taskSnapshot.child("taskName").getValue(String.class);
                        String taskDescription = taskSnapshot.child("taskDescription").getValue(String.class);
                        taskNames.add(taskName);
                        taskDescriptions.add(taskDescription);
                        taskIds.add(taskSnapshot.getKey());
                    }
                    // Update the spinners with task names and descriptions
                    updateTaskNameSpinner();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ScheduleTaskActivity.this, "Failed to load tasks", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateTaskNameSpinner() {
        ArrayAdapter<String> taskNameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, taskNames);
        taskNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTaskName.setAdapter(taskNameAdapter);

        taskDescriptionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        taskDescriptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTaskDescription.setAdapter(taskDescriptionAdapter);

        spinnerTaskName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTaskDescriptionSpinner(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                taskDescriptionAdapter.clear();
            }
        });
    }

    private void updateTaskDescriptionSpinner(int position) {
        if (position >= 0 && position < taskDescriptions.size()) {
            List<String> descriptions = new ArrayList<>();
            descriptions.add(taskDescriptions.get(position));
            taskDescriptionAdapter.clear();
            taskDescriptionAdapter.addAll(descriptions);
        }
    }

    private void saveTask() {
        String selectedTaskName = spinnerTaskName.getSelectedItem().toString();
        String selectedTaskDescription = spinnerTaskDescription.getSelectedItem().toString();

        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();

        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, day, hour, minute);

        // Check if the selected date is past the due date
        String selectedTaskId = getTaskIdByName(selectedTaskName);
        if (selectedTaskId != null) {
            DatabaseReference selectedTaskRef = tasksRef.child(selectedTaskId);
            selectedTaskRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String dueDateStr = dataSnapshot.child("dueDate").getValue(String.class);
                    Calendar dueDate = Calendar.getInstance();
                    try {
                        dueDate.setTime(new SimpleDateFormat("d/M/yyyy").parse(dueDateStr));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (selectedDate.after(dueDate)) {
                        Toast.makeText(ScheduleTaskActivity.this, "Cannot schedule task past the due date", Toast.LENGTH_SHORT).show();
                    } else {
                        // Update task with new date and time
                        selectedTaskRef.child("taskDate").setValue(day + "/" + (month + 1) + "/" + year);
                        selectedTaskRef.child("taskTime").setValue(hour + ":" + minute);
                        Toast.makeText(ScheduleTaskActivity.this, "Task updated successfully!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ScheduleTaskActivity.this, "Failed to update task", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getTaskIdByName(String taskName) {
        for (int i = 0; i < taskNames.size(); i++) {
            if (taskNames.get(i).equals(taskName)) {
                return taskIds.get(i);
            }
        }
        return null;
    }

    private void initializeNavigation() {
        ImageView leftArrow = findViewById(R.id.left_arrow);
        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScheduleTaskActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ImageView task = findViewById(R.id.task);
        task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScheduleTaskActivity.this, TaskManager.class);
                startActivity(intent);
            }
        });

        ImageView calendar = findViewById(R.id.calendar);
        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScheduleTaskActivity.this, ScheduleTaskActivity.class);
                startActivity(intent);
            }
        });

        ImageView bell = findViewById(R.id.bell);
        bell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScheduleTaskActivity.this, Notification.class);
                startActivity(intent);
            }
        });

        ImageView user = findViewById(R.id.user);
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScheduleTaskActivity.this, Profile.class);
                startActivity(intent);
            }
        });
    }
}
