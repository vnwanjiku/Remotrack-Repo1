package com.example.remoteapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class TaskManager extends AppCompatActivity {

    private EditText taskNameEditText;
    private EditText taskDurationEditText;
    private Spinner taskCategorySpinner;
    private Button addTaskButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskmanager);

        // Initialize UI elements
        taskNameEditText = findViewById(R.id.edit_task_name);
        taskDurationEditText = findViewById(R.id.edit_duration);
        taskCategorySpinner = findViewById(R.id.spinner_category);
        addTaskButton = findViewById(R.id.btn_add_task);

        // Populate the spinner with categories
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.task_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskCategorySpinner.setAdapter(adapter);

        // Set onClick listener for the add task button
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskName = taskNameEditText.getText().toString();
                String taskDuration = taskDurationEditText.getText().toString();
                String taskCategory = taskCategorySpinner.getSelectedItem().toString();

                if (!taskName.isEmpty() && !taskDuration.isEmpty() && !taskCategory.isEmpty()) {
                    // Show toast message
                    Toast.makeText(TaskManager.this, "Task added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TaskManager.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
