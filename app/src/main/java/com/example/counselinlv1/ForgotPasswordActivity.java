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
import com.example.counselinlv1.Utilities.EmailSender;
import com.example.counselinlv1.Utilities.OtpManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnContinue;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge content
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.user_input);
        btnContinue = findViewById(R.id.continue_button);
        firestore = FirebaseFirestore.getInstance();

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString().trim().toLowerCase();

                if (!email.contains("@lpubatangas.edu.ph")) {
                    etEmail.setError("Invalid email");
                    etEmail.requestFocus();
                    return;
                }

                checkCredentials(email); // Call to check if the email exists in Firestore
            }
        });

        getWindow().setNavigationBarColor(getResources().getColor(R.color.white));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void checkCredentials(String userEmail) {
        firestore.collection("Users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String userID = task.getResult().getDocuments().get(0).getId();  // Get the userID
                        sendOtp(userEmail, userID);
                    } else {
                        // Email not found in the database
                        etEmail.setError("Email not registered");
                        etEmail.requestFocus();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ForgotPasswordActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendOtp(String email, String userID) {
        // Call OtpManager to send OTP via email
        OtpManager.sendOtp(email, new EmailSender.EmailCallback() {
            @Override
            public void onSuccess(String otpCode) {
                // Redirect to OTP screen on success
                Intent intent = new Intent(ForgotPasswordActivity.this, OtpActivity.class);
                intent.putExtra("otp", otpCode); // Pass the OTP to the OTP screen
                intent.putExtra("email", email);
                intent.putExtra("userID", userID);
                intent.putExtra("source", "forgotPassword"); // Pass the source for OTP verification
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> Toast.makeText(ForgotPasswordActivity.this, "Failed to send OTP", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
