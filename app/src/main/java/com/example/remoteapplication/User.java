package com.example.remoteapplication;

import java.util.HashMap;
import java.util.Map;

public class User {
    public String firstName;
    public String lastName;
    public String role;
    public Map<String, TaskDetails> tasks;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String firstName, String lastName, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.tasks = new HashMap<>();
    }
}