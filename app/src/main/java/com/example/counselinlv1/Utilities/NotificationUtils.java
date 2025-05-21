package com.example.counselinlv1.Utilities;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class NotificationUtils {
    public static void createNotification(FirebaseFirestore firestore, String userID, String title, String message) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("userID", userID); // Who this notification is for
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("timestamp", FieldValue.serverTimestamp());
        notificationData.put("seen", false);

        firestore.collection("Notifications")
                .add(notificationData)
                .addOnSuccessListener(documentReference -> Log.d("NotificationUtils", "Notification created successfully"))
                .addOnFailureListener(e -> Log.e("NotificationUtils", "Error creating notification", e));
    }
}
