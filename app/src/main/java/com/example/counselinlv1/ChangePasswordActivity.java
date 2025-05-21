package com.example.counselinlv1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.counselinlv1.Utilities.BaseActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etNewPassword, etConfirmPassword;
    private Button btnChangePassword;
    private String userID;  // Variable to retrieve userID
    private FirebaseFirestore firestore;  // Firestore instance
    private DocumentReference userRef;  // Firestore document reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge content
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_change_password);

        // Initialize views
        etNewPassword = findViewById(R.id.new_password_input);
        etConfirmPassword = findViewById(R.id.confirm_password_input);
        btnChangePassword = findViewById(R.id.change_password_button);

        // Retrieve the userID from the intent
        userID = getIntent().getStringExtra("userID");

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
        // Reference the user's document in the "Users" collection
        userRef = firestore.collection("Users").document(userID);

        // Change password logic
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPassword = etNewPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                // Password validation
                if (newPassword.length() < 8) {
                    etNewPassword.setError("Password must be at least 8 characters long");
                    etNewPassword.requestFocus();
                    return;
                } else if (!newPassword.equals(confirmPassword)) {
                    etNewPassword.setError("");
                    etConfirmPassword.setError("Passwords don't match");
                    etConfirmPassword.requestFocus();
                    return;
                } else {
                    // Update the password in Firestore
                    updatePassword(newPassword);
                }
            }
        });
    }

    // Method to update the password in Firestore
    private void updatePassword(String newPassword) {
        // Update the user's password in Firestore
        userRef.update("password", newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Password updated successfully
                    Toast.makeText(ChangePasswordActivity.this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
                    // Redirect to login screen
                    Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Failed to update password
                    Toast.makeText(ChangePasswordActivity.this, "Failed to change password. Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChangePasswordActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
        finish();
    }
}
