package com.example.counselinlv1;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.counselinlv1.Utilities.BaseActivity;
import com.example.counselinlv1.Utilities.EmailSender;
import com.example.counselinlv1.Utilities.OtpManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

public class OtpActivity extends AppCompatActivity {

    private EditText otpInput1, otpInput2, otpInput3, otpInput4;
    private Button btnContinue;
    private TextView tvResendCodeTimer;
    private String email, otpCode, userID, password, source;

    private long resendCodeTime = 60000; // 60 seconds timer
    private FirebaseFirestore firestore;  // Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge content
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_otp);

        // Initialize views
        otpInput1 = findViewById(R.id.otp_input_1);
        otpInput2 = findViewById(R.id.otp_input_2);
        otpInput3 = findViewById(R.id.otp_input_3);
        otpInput4 = findViewById(R.id.otp_input_4);
        btnContinue = findViewById(R.id.continue_button);
        tvResendCodeTimer = findViewById(R.id.tv_resend_code_timer);

        // Retrieve intent extras
        Intent intent = getIntent();
        source = intent.getStringExtra("source");  // Identify the source (ForgotPassword or Register)
        email = intent.getStringExtra("email");
        userID = intent.getStringExtra("userID");
        otpCode = intent.getStringExtra("otp");

        // If the source is RegisterActivity, retrieve additional extras
        if (source.equals("register")) {
            password = intent.getStringExtra("password");
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Start the resend code timer
        startResendCodeTimer();

        // Continue button action logic
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredOtp = otpInput1.getText().toString() + otpInput2.getText().toString() +
                        otpInput3.getText().toString() + otpInput4.getText().toString();

                if (isValidOtp(enteredOtp)) {
                    // Handle OTP verification based on the source
                    verifyOtp(enteredOtp);
                } else {
                    otpInput1.setError("Invalid OTP");
                    otpInput1.requestFocus();
                }
            }
        });

        // Set up the automatic cursor move functionality
        setupOtpInputFields();

        getWindow().setNavigationBarColor(getResources().getColor(R.color.white));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (source.equals("forgotPassword")) {
            Intent intent = new Intent(OtpActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
            finish();
        } else if (source.equals("register")) {
            // Navigate to PersonalInformationActivity for registration flow
            Intent intent = new Intent(OtpActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }

    // Function to start the resend code timer
    private void startResendCodeTimer() {
        tvResendCodeTimer.setTextColor(getResources().getColor(android.R.color.darker_gray));
        new CountDownTimer(resendCodeTime, 1000) {
            public void onTick(long millisUntilFinished) {
                tvResendCodeTimer.setText("Resend code in " + millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                tvResendCodeTimer.setText("Resend Code");
                tvResendCodeTimer.setTextColor(getResources().getColor(R.color.maroon));
                tvResendCodeTimer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        resendOtp();
                    }
                });
            }
        }.start();
    }

    // Function to resend OTP
    private void resendOtp() {
        OtpManager.sendOtp(email, new EmailSender.EmailCallback() {
            @Override
            public void onSuccess(String newOtpCode) {
                runOnUiThread(() -> {
                    // Notify user that OTP was resent
                    Toast.makeText(OtpActivity.this, "OTP Resent", Toast.LENGTH_SHORT).show();
                    otpCode = newOtpCode; // Update OTP code

                    // Disable clicking during countdown and restart the timer
                    tvResendCodeTimer.setOnClickListener(null);
                    tvResendCodeTimer.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    startResendCodeTimer(); // Restart the timer
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    // Notify user about the failure
                    Toast.makeText(OtpActivity.this, "Failed to resend OTP", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Function to check if the entered OTP is valid
    private boolean isValidOtp(String enteredOtp) {
        return enteredOtp.equals(otpCode);
    }

    // Handle OTP verification and navigation logic based on the source
    private void verifyOtp(String otp) {
        if (source.equals("forgotPassword")) {
            // Verify if the user exists in Firestore before navigating to ChangePasswordActivity
            DocumentReference userRef = firestore.collection("Users").document(userID);
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    // User exists, navigate to ChangePasswordActivity
                    Intent intent = new Intent(OtpActivity.this, ChangePasswordActivity.class);
                    intent.putExtra("userID", userID);  // Pass userID to ChangePasswordActivity
                    startActivity(intent);
                    finish();
                } else {
                    // Handle case where user doesn't exist
                    Toast.makeText(OtpActivity.this, "User not found. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (source.equals("register")) {
            // Navigate to PersonalInformationActivity for registration flow
            Intent intent = new Intent(OtpActivity.this, PersonalInformationActivity.class);
            intent.putExtra("email", email);  // Pass email
            intent.putExtra("userID", userID);  // Pass userID
            intent.putExtra("password", password);  // Pass password
            startActivity(intent);
            finish();
        }
    }

    // Function to set up automatic cursor movement between OTP fields
    private void setupOtpInputFields() {
        otpInput1.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() == 1) {
                    otpInput2.requestFocus();  // Move to the next input field
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {}
        });

        otpInput2.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() == 1) {
                    otpInput3.requestFocus();  // Move to the next input field
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {}
        });

        otpInput3.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() == 1) {
                    otpInput4.requestFocus();  // Move to the next input field
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {}
        });

        otpInput4.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable editable) {}
        });
    }
}
