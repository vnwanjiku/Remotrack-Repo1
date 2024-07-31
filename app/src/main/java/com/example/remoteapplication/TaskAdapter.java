package com.example.remoteapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class TaskAdapter extends ArrayAdapter<Task> {

    private Context context;
    private List<Task> taskList;

    public TaskAdapter(Context context, List<Task> taskList) {
        super(context, 0, taskList);
        this.context = context;
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        }

        Task task = taskList.get(position);

        TextView taskNameTextView = convertView.findViewById(R.id.taskName);
        TextView statusTextView = convertView.findViewById(R.id.status);
        TextView assignedUserTextView = convertView.findViewById(R.id.assignedUser);

        taskNameTextView.setText(task.getTaskName());
        statusTextView.setText(task.getStatus());
        assignedUserTextView.setText(task.getAssignedUser());

        return convertView;
    }
}
