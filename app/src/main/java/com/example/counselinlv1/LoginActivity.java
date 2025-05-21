package com.example.counselinlv1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.counselinlv1.Models.User;
import com.example.counselinlv1.Utilities.BaseActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText et_user, et_password;
    private Button btn_sign_in;
    private TextView tx_sign_up, tx_forgot;
    private ProgressDialog progressDialog;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_login);

        firestore = FirebaseFirestore.getInstance();

        et_user = findViewById(R.id.user_input);
        et_password = findViewById(R.id.password_input);
        btn_sign_in = findViewById(R.id.sign_in_button);
        tx_sign_up = findViewById(R.id.sign_up_link);
        tx_forgot = findViewById(R.id.forgot_password);
        progressDialog = new ProgressDialog(this);

        btn_sign_in.setOnClickListener(view -> checkRegistryInfo());
        tx_forgot.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
        tx_sign_up.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void checkRegistryInfo() {
        String userID = et_user.getText().toString().trim();
        String userPassword = et_password.getText().toString().trim();

        if (userID.isEmpty()) {
            et_user.setError("ID is required");
            et_user.requestFocus();
            return;
        }

        if (userID.length() != 8) {
            et_user.setError("Invalid ID");
            et_user.requestFocus();
            return;
        }

        if (userPassword.isEmpty()) {
            et_password.setError("Password is required");
            et_password.requestFocus();
            return;
        }

        if (userPassword.length() < 6) {
            et_password.setError("Password is too short");
            et_password.requestFocus();
            return;
        }

        progressDialog.setTitle("Logging in");
        progressDialog.setMessage("Checking credentials");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        loginUser(userID, userPassword);
    }

    private void loginUser(final String userID, final String userPassword) {
        DocumentReference docRef = firestore.collection("Users").document(userID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    User userData = document.toObject(User.class);
                    if (userData != null && userData.getPassword().equals(userPassword)) {
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, HomeScreenActivity.class);
                        intent.putExtra("userID", userID);
                        intent.putExtra("department", userData.getDepartmentID());
                        intent.putExtra("userType", userData.getType());
                        startActivity(intent);
                        finish();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Account doesn't exist", Toast.LENGTH_SHORT).show();
                }
            } else {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        // Dismiss the ProgressDialog in case it's still showing when the Activity stops
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}
