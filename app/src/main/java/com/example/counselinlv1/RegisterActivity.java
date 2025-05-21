package com.example.counselinlv1;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.counselinlv1.Utilities.BaseActivity;
import com.example.counselinlv1.Utilities.EmailSender;
import com.example.counselinlv1.Utilities.OtpManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText etIdNumber;
    private EditText etEmail;
    private EditText etPassword;
    private TextView txSignIn;
    private EditText etConfirmPassword;
    private Button btnSignUp;
    private ProgressDialog progressDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge content
        setContentView(R.layout.activity_register);

        etIdNumber = findViewById(R.id.id_input);
        etEmail = findViewById(R.id.email_register_input);
        etPassword = findViewById(R.id.password_input);
        etConfirmPassword = findViewById(R.id.confirm_password_input);
        txSignIn = findViewById(R.id.sign_in_link);
        btnSignUp = findViewById(R.id.sign_up_button);

        txSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userID = etIdNumber.getText().toString().trim();
                String email = etEmail.getText().toString().trim().toLowerCase();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                if (userID.length() != 8) {
                    etIdNumber.setError("Invalid ID Number");
                    etIdNumber.requestFocus();
                    return;
                }

                if (!email.endsWith("@lpubatangas.edu.ph")) {
                    etEmail.setError("Invalid email");
                    etEmail.requestFocus();
                    return;
                }

                if (password.length() < 8) {
                    etPassword.setError("Password must be at least 8 characters");
                    etPassword.requestFocus();
                    return;
                } else {
                    if (!password.equals(confirmPassword)) {
                        etPassword.setError("");
                        etConfirmPassword.setError("Password do not match");
                        etConfirmPassword.requestFocus();
                        return;
                    }
                }

                // Check if user ID or email already exists in the database
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection("Users")
                        .whereEqualTo("userID", userID)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                                etIdNumber.setError("User ID already registered");
                                etIdNumber.requestFocus();
                            } else {
                                firestore.collection("Users")
                                        .whereEqualTo("email", email)
                                        .get()
                                        .addOnCompleteListener(emailTask -> {
                                            if (emailTask.isSuccessful() && emailTask.getResult() != null && !emailTask.getResult().isEmpty()) {
                                                etEmail.setError("Email already registered");
                                                etEmail.requestFocus();
                                            } else {
                                                // Proceed to OTP verification
                                                OtpManager.sendOtp(email, new EmailSender.EmailCallback() {
                                                    @Override
                                                    public void onSuccess(String otpCode) {
                                                        // Redirect to OTP screen on success
                                                        Intent intent = new Intent(RegisterActivity.this, OtpActivity.class);
                                                        intent.putExtra("otp", otpCode); // Pass OTP to the OTP screen
                                                        intent.putExtra("email", email);
                                                        intent.putExtra("userID", userID);
                                                        intent.putExtra("password", password);
                                                        intent.putExtra("source", "register");
                                                        startActivity(intent);
                                                    }

                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Failed to send OTP", Toast.LENGTH_SHORT).show());
                                                    }
                                                });
                                            }
                                        });
                            }
                        });
            }
        });

    }


    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }
}