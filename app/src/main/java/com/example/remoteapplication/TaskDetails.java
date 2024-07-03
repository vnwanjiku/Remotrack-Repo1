package com.example.remoteapplication;

public class TaskDetails {
    public String taskDetails;
    public String assignedTo;

    public TaskDetails() {
        // Default constructor required for calls to DataSnapshot.getValue(TaskDetails.class)
    }

    public TaskDetails(String taskDetails, String assignedTo) {
        this.taskDetails = taskDetails;
        this.assignedTo = assignedTo;
    }
}
