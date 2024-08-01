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
    private List<Task> tasks;

    public TaskAdapter(Context context, List<Task> tasks) {
        super(context, 0, tasks);
        this.context = context;
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        }

        Task task = tasks.get(position);

        TextView taskName = convertView.findViewById(R.id.taskName);
        TextView taskStatus = convertView.findViewById(R.id.status);
        TextView assignedUser = convertView.findViewById(R.id.assignedUser);

        taskName.setText(task.getTaskName());
        taskStatus.setText(task.getStatus());
        assignedUser.setText(task.getAssignedUser());

        return convertView;
    }
}
