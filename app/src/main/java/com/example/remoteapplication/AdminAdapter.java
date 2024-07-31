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

public class AdminAdapter extends ArrayAdapter<User> {

    private Context context;
    private List<User> adminList;

    public AdminAdapter(Context context, List<User> adminList) {
        super(context, 0, adminList);
        this.context = context;
        this.adminList = adminList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_admin, parent, false);
        }

        User admin = adminList.get(position);

        TextView firstNameTextView = convertView.findViewById(R.id.firstName);
        TextView lastNameTextView = convertView.findViewById(R.id.lastName);
        TextView emailTextView = convertView.findViewById(R.id.email);

        firstNameTextView.setText(admin.getFirstName());
        lastNameTextView.setText(admin.getLastName());
        emailTextView.setText(admin.getEmail());

        return convertView;
    }
}

