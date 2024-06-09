package com.example.remoteapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainUserLogin extends AppCompatActivity {

    private EditText editEmail;
    private EditText editPassword;
    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user_login);

        // Initialize the views
        editEmail = findViewById(R.id.useremail1);
        editPassword = findViewById(R.id.userpassword1);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);

        // Set an OnClickListener on the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Set an OnClickListener on the register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start RegisterActivity
                Intent intent = new Intent(MainUserLogin.this, RegisterActivity.class);
                // Start RegisterActivity
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty()) {
            editEmail.setError("Email is required");
            editEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Enter a valid email");
            editEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editPassword.setError("Password is required");
            editPassword.requestFocus();
            return;
        }

        // Here you can add code to verify the email and password with your server or database
        // For simplicity, we'll assume the login is always successful

        Toast.makeText(MainUserLogin.this, "Login successful", Toast.LENGTH_SHORT).show();

        // Navigate to MainActivity after successful login
        Intent intent = new Intent(MainUserLogin.this, MainActivity.class);
        startActivity(intent);

        // Optionally, finish this activity to remove it from the back stack
        finish();
    }
}
