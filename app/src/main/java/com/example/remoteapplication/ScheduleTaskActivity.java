package com.example.remoteapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

public class ScheduleTaskActivity extends AppCompatActivity {

    private EditText editTaskName;
    private EditText editTaskDescription;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button btnSaveTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_task);

        // Initialize UI elements
        editTaskName = findViewById(R.id.edit_task_name);
        editTaskDescription = findViewById(R.id.edit_task_description);
        datePicker = findViewById(R.id.date_picker);
        timePicker = findViewById(R.id.time_picker);
        btnSaveTask = findViewById(R.id.btn_save_task);

        // Set TimePicker to 24-hour view
        timePicker.setIs24HourView(true);

        // Set onClick listener for the save task button
        btnSaveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();
            }
        });

        ImageView leftArrow = findViewById(R.id.left_arrow);
        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start MainUserLogin activity
                Intent intent = new Intent(ScheduleTaskActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ImageView task = findViewById(R.id.task);
        task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start TaskManager activity
                Intent intent = new Intent(ScheduleTaskActivity.this, TaskManager.class);
                startActivity(intent);
            }
        });

        ImageView calendar = findViewById(R.id.calendar);
        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start ScheduleTaskActivity activity
                Intent intent = new Intent(ScheduleTaskActivity.this, ScheduleTaskActivity.class);
                startActivity(intent);
            }
        });

        ImageView bell = findViewById(R.id.bell);
        bell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Notification activity
                Intent intent = new Intent(ScheduleTaskActivity.this, Notification.class);
                startActivity(intent);
            }
        });

        ImageView user = findViewById(R.id.user);
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Profile activity
                Intent intent = new Intent(ScheduleTaskActivity.this, Profile.class);
                startActivity(intent);
            }
        });
    }

    private void saveTask() {
        String taskName = editTaskName.getText().toString();
        String taskDescription = editTaskDescription.getText().toString();

        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();

        if (!taskName.isEmpty() && !taskDescription.isEmpty()) {
            // Implement your logic to save the task here

            // Display toast message
            Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show();
        } else {
            // Display toast message if fields are empty
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
        }
    }
}
