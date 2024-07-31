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

public class EmployeeAdapter extends ArrayAdapter<User> {

    private Context context;
    private List<User> employeeList;

    public EmployeeAdapter(Context context, List<User> employeeList) {
        super(context, 0, employeeList);
        this.context = context;
        this.employeeList = employeeList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_employee, parent, false);
        }

        User employee = employeeList.get(position);

        TextView firstNameTextView = convertView.findViewById(R.id.firstName);
        TextView lastNameTextView = convertView.findViewById(R.id.lastName);
        TextView emailTextView = convertView.findViewById(R.id.email);

        firstNameTextView.setText(employee.getFirstName());
        lastNameTextView.setText(employee.getLastName());
        emailTextView.setText(employee.getEmail());

        return convertView;
    }
}
