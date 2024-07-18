package com.example.remoteapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.AdapterView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Adminnotifications extends AppCompatActivity {
    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText editTextMessage;
    private Spinner spinnerRecipients;
    private DatabaseReference databaseReference;
    private String organizationName;
    private List<String> userList;
    private ValueEventListener messagesEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminnotifications);

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
                if ("All".equals(receiverId)) {
                    sendMessageToAll(messageText, senderId, senderName);
                } else {
                    sendMessage(messageText, senderId, senderName, receiverId);
                }
                editTextMessage.setText("");
            }
        });

        ImageView leftArrow = findViewById(R.id.left_arrow);
        leftArrow.setOnClickListener(v -> startActivity(new Intent(Adminnotifications.this, Admin.class)));

        ImageView task = findViewById(R.id.task);
        task.setOnClickListener(v -> startActivity(new Intent(Adminnotifications.this, AssignTaskActivity.class)));

        ImageView barchart = findViewById(R.id.barchart);
        barchart.setOnClickListener(v -> startActivity(new Intent(Adminnotifications.this, Adminreports.class)));

        ImageView bell = findViewById(R.id.bell);
        bell.setOnClickListener(v -> startActivity(new Intent(Adminnotifications.this, Adminnotifications.class)));

        ImageView user = findViewById(R.id.user);
        user.setOnClickListener(v -> startActivity(new Intent(Adminnotifications.this, Adminprofile.class)));
    }

    private void fetchOrganizationNameAndUsers() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("organization").orderByChild("users/" + userId)
                .startAt("").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot organizationSnapshot : dataSnapshot.getChildren()) {
                            organizationName = organizationSnapshot.getKey();
                            Log.d("Adminnotifications", "Organization found: " + organizationName);
                            fetchUsers(organizationName);
                            break;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("Adminnotifications", "Failed to fetch organization name", databaseError.toException());
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
                userList.add("All");

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String email = userSnapshot.child("email").getValue(String.class);
                    if (email != null) {
                        userList.add(email);
                    }
                }

                Log.d("Adminnotifications", "Users found for " + organizationName + ": " + userList);
                populateSpinner(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Adminnotifications", "Failed to fetch users for " + organizationName, databaseError.toException());
            }
        });
    }

    private void populateSpinner(List<String> userList) {
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

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
                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                })
                .addOnFailureListener(e -> Log.e("Notification", "Failed to send message", e));
    }

    private void sendMessageToAll(String messageText, String senderId, String senderName) {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference()
                .child("organization")
                .child(organizationName)
                .child("notifications");

        for (String receiverEmail : userList) {
            if (!receiverEmail.equals(senderName)) {
                String messageId = notificationsRef.push().getKey();
                Message message = new Message(senderId, senderName, receiverEmail, messageText, System.currentTimeMillis());

                notificationsRef.child(messageId).setValue(message)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Notification", "Message sent to all successfully");
                        })
                        .addOnFailureListener(e -> Log.e("Notification", "Failed to send message to all", e));
            }
        }
        loadChatMessages(senderId, "All");
    }

    private void loadChatMessages(String currentUserId, String receiverId) {
        if (messagesEventListener != null) {
            databaseReference.child("organization").child(organizationName).child("notifications").removeEventListener(messagesEventListener);
        }

        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference()
                .child("organization")
                .child(organizationName)
                .child("notifications");

        messagesEventListener = messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Message> tempMessageList = new ArrayList<>(messageList);
                messageList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) {
                        if ((message.getSenderName().equals(currentUserId) && message.getReceiverId().equals(receiverId)) ||
                                (message.getSenderName().equals(receiverId) && message.getReceiverId().equals(currentUserId)) ||
                                (receiverId.equals("All") && message.getReceiverId().equals(currentUserId))) {
                            messageList.add(message);
                        }
                    }
                }
                if (messageList.isEmpty()) {
                    messageList.addAll(tempMessageList);
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
