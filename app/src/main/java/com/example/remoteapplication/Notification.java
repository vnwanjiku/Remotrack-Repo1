package com.example.remoteapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Notification extends AppCompatActivity {
    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText editTextMessage;
    private Spinner spinnerRecipients;
    private DatabaseReference databaseReference;
    private String organizationName;
    private List<String> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        spinnerRecipients = findViewById(R.id.spinnerRecipients);
        editTextMessage = findViewById(R.id.editTextMessage);
        Button buttonSend = findViewById(R.id.buttonSend);

        userList = new ArrayList<>();
        messageList = new ArrayList<>();

        messageAdapter = new MessageAdapter(this, messageList, FirebaseAuth.getInstance().getCurrentUser().getUid());
        recyclerViewMessages.setAdapter(messageAdapter);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));

        fetchOrganizationNameAndUsers();

        buttonSend.setOnClickListener(v -> {
            String messageText = editTextMessage.getText().toString().trim();
            String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String senderName = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String receiverId = spinnerRecipients.getSelectedItem().toString();

            if (!messageText.isEmpty()) {
                sendMessage(messageText, senderId, senderName, receiverId);
                editTextMessage.setText("");
            }
        });

        ImageView leftArrow = findViewById(R.id.left_arrow);
        leftArrow.setOnClickListener(v -> startActivity(new Intent(Notification.this, MainActivity.class)));

        ImageView task = findViewById(R.id.task);
        task.setOnClickListener(v -> startActivity(new Intent(Notification.this, AssignTaskActivity.class)));

        ImageView barchart = findViewById(R.id.calendar);
        barchart.setOnClickListener(v -> startActivity(new Intent(Notification.this, ScheduleTaskActivity.class)));

        ImageView bell = findViewById(R.id.bell);
        bell.setOnClickListener(v -> startActivity(new Intent(Notification.this, Notification.class)));

        ImageView user = findViewById(R.id.user);
        user.setOnClickListener(v -> startActivity(new Intent(Notification.this, Profile.class)));
    }

    private void fetchOrganizationNameAndUsers() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("organization").orderByChild("users/" + userId)
                .startAt("").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot organizationSnapshot : dataSnapshot.getChildren()) {
                            organizationName = organizationSnapshot.getKey();
                            if (organizationName != null) {
                                fetchUsers(organizationName);
                                break;
                            } else {
                                Log.e("Notification", "Organization name is null");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Notification", "Failed to fetch organization name", databaseError.toException());
                    }
                });
    }

    private void fetchUsers(String organizationName) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference()
                .child("organization")
                .child(organizationName)
                .child("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String email = userSnapshot.child("email").getValue(String.class);
                    if (email != null) {
                        userList.add(email);
                    }
                }
                populateSpinner(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Notification", "Failed to fetch users for " + organizationName, databaseError.toException());
            }
        });
    }

    private void populateSpinner(List<String> userList) {
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Filter out the current user's email from the list
        List<String> recipientsList = new ArrayList<>(userList);
        recipientsList.remove(currentUserEmail);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, recipientsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecipients.setAdapter(adapter);

        spinnerRecipients.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedUserEmail = parent.getItemAtPosition(position).toString();
                loadChatMessages(currentUserEmail, selectedUserEmail);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    private void sendMessage(String messageText, String senderId, String senderName, String receiverId) {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference()
                .child("organization")
                .child(organizationName)
                .child("notifications");

        String messageId = notificationsRef.push().getKey();

        Message message = new Message(senderId, senderName, receiverId, messageText, System.currentTimeMillis());

        notificationsRef.child(messageId).setValue(message)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Notification", "Message sent successfully");
                    editTextMessage.setText("");
                    loadChatMessages(senderId, receiverId); // Update with Firebase IDs
                })
                .addOnFailureListener(e -> Log.e("Notification", "Failed to send message", e));
    }


    private void loadChatMessages(String currentUserId, String receiverId) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference()
                .child("organization")
                .child(organizationName)
                .child("notifications");

        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) {
                        // Compare senderName and receiverId to filter messages
                        if ((message.getSenderName().equals(currentUserId) && message.getReceiverId().equals(receiverId)) ||
                                (message.getSenderName().equals(receiverId) && message.getReceiverId().equals(currentUserId))) {
                            messageList.add(message);
                        }
                    }
                }
                messageAdapter.notifyDataSetChanged();
                recyclerViewMessages.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Notification", "Failed to load messages", databaseError.toException());
            }
        });
    }




}
