package com.example.counselinlv1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.counselinlv1.Utilities.BaseActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class CreatePostActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;

    private EditText postContent;
    private ImageView postImageView, uploadPhotoButton, capturePhotoButton, backIcon;
    private Button postButton;
    private Uri selectedImageUri;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;
    private ProgressDialog progressDialog;
    private String currentUserId, departmentID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Initialize Firestore and Firebase Storage
        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("postImages");

        Intent intent = getIntent();
        currentUserId = intent.getStringExtra("userID");
        departmentID = intent.getStringExtra("departmentID");

        // Initialize UI elements
        postContent = findViewById(R.id.post_content);
        postImageView = findViewById(R.id.post_image_view);
        uploadPhotoButton = findViewById(R.id.upload_photo_button);
        capturePhotoButton = findViewById(R.id.capture_photo_button);
        backIcon = findViewById(R.id.back_icon);
        postButton = findViewById(R.id.post_button);
        progressDialog = new ProgressDialog(this);

        // Set up click listeners
        uploadPhotoButton.setOnClickListener(v -> selectImageFromGallery());
        capturePhotoButton.setOnClickListener(v -> openCamera());
        backIcon.setOnClickListener(v -> finish());
        postButton.setOnClickListener(v -> createPost());
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                selectedImageUri = data.getData();
                postImageView.setVisibility(View.VISIBLE);
                postImageView.setImageURI(selectedImageUri);
            } else if (requestCode == CAPTURE_IMAGE_REQUEST && data != null && data.getExtras() != null) {
                selectedImageUri = data.getData();
                postImageView.setVisibility(View.VISIBLE);
                postImageView.setImageURI(selectedImageUri);
            }
        }
    }

    private void createPost() {
        String caption = postContent.getText().toString().trim();

        if (caption.isEmpty()) {
            Toast.makeText(this, "Please enter a caption", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Creating post...");
        progressDialog.show();

        if (selectedImageUri != null) {
            StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpg");
            fileRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                savePostToFirestore(caption, uri.toString());
            })).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
            });
        } else {
            savePostToFirestore(caption, null);
        }
    }

    private void savePostToFirestore(String caption, String imageUrl) {
        String postId = firestore.collection("Posts").document().getId();

        HashMap<String, Object> postMap = new HashMap<>();
        postMap.put("postID", postId);
        postMap.put("caption", caption);
        postMap.put("fileURL", imageUrl != null ? imageUrl : "");
        postMap.put("postedBy", currentUserId);
        postMap.put("departmentID", departmentID != null ? departmentID : "General");
        postMap.put("timestamp", System.currentTimeMillis());

        firestore.collection("Posts").document(postId).set(postMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                createNotification(caption, departmentID); // Create a notification
            } else {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed to create post", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to save post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void createNotification(String caption, String departmentId) {
        String notificationId = firestore.collection("Notifications").document().getId();

        HashMap<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("notificationID", notificationId);
        notificationMap.put("targetType", "department");
        notificationMap.put("targetID", departmentId);
        notificationMap.put("title", "New Post");
        notificationMap.put("message", caption);
        notificationMap.put("type", "post");
        notificationMap.put("timestamp", System.currentTimeMillis());
        notificationMap.put("seen", false);

        firestore.collection("Notifications").document(notificationId).set(notificationMap).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(this, "Post created successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to create notification", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to create notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }
}
