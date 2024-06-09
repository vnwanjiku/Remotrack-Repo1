package com.example.remoteapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class AssignTaskActivity extends AppCompatActivity {

    private EditText editTaskName, editTaskDescription;
    private DatePicker datePicker;
    private Spinner spinnerSelectIndividual;
    private Button btnAddTask, btnUpdateTask, btnDeleteTask, btnSaveTask;

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

        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add task logic here
                Toast.makeText(AssignTaskActivity.this, "Task Added", Toast.LENGTH_SHORT).show();
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
    }
}

