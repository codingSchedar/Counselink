package com.example.counselinlv1;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.counselinlv1.Adapters.NotificationAdapter;
import com.example.counselinlv1.Models.Notification;
import com.example.counselinlv1.Utilities.BaseActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private FirebaseFirestore firestore;

    private String currentUserID, currentUserDepartmentID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Get userID passed from HomeScreenActivity
        currentUserID = getIntent().getStringExtra("userID");
        currentUserDepartmentID = getIntent().getStringExtra("departmentID");

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Get current user details from Intent
        currentUserID = getIntent().getStringExtra("userID");
        currentUserDepartmentID = getIntent().getStringExtra("departmentID");

        if (currentUserID == null || currentUserDepartmentID == null) {
            Toast.makeText(this, "Unable to load notifications. Missing user details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.notifications_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize data and adapter
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList, this);
        recyclerView.setAdapter(notificationAdapter);

        // Fetch notifications
        fetchNotifications();
        fetchDepartmentNotifications();
    }

    private void fetchNotifications() {
        // Clear the list to avoid duplicates
        notificationList.clear();

        // Fetch individual notifications
        firestore.collection("Notifications")
                .whereEqualTo("targetID", currentUserID)
                .get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful() && task1.getResult() != null) {
                        for (DocumentSnapshot doc : task1.getResult()) {
                            try {
                                Notification notification = doc.toObject(Notification.class);
                                notificationList.add(notification);
                            } catch (Exception ex) {
                                Log.e("NotificationsActivity", "Error parsing individual notification", ex);
                            }
                        }
                        // Fetch department-wide notifications after fetching individual notifications
                        fetchDepartmentNotifications();
                    } else {
                        Log.e("NotificationsActivity", "Error fetching individual notifications", task1.getException());
                    }
                });
    }

    private void fetchDepartmentNotifications() {
        // Fetch department-wide notifications
        firestore.collection("Notifications")
                .whereEqualTo("targetID", currentUserDepartmentID)
                .get()
                .addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful() && task2.getResult() != null) {
                        for (DocumentSnapshot doc : task2.getResult()) {
                            try {
                                Notification notification = doc.toObject(Notification.class);
                                notificationList.add(notification);
                            } catch (Exception ex) {
                                Log.e("NotificationsActivity", "Error parsing department notification", ex);
                            }
                        }
                        // Sort notifications by timestamp in descending order
                        sortNotificationsByTimestamp();
                    } else {
                        Log.e("NotificationsActivity", "Error fetching department notifications", task2.getException());
                    }
                });
    }

    private void sortNotificationsByTimestamp() {
        Collections.sort(notificationList, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));
        notificationAdapter.notifyDataSetChanged();
    }

}
