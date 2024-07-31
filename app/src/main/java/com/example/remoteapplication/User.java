package com.example.remoteapplication;

import java.util.HashMap;
import java.util.Map;

public class User {
    public String firstName;

    private String organization;
    public String lastName;
    public String email;
    public String role;
    public Map<String, TaskDetails> tasks;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public User(String firstName, String lastName, String email, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.tasks = new HashMap<>();
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
