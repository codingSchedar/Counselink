package com.example.counselinlv1.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.counselinlv1.Models.Notification;
import com.example.counselinlv1.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notificationList;
    private Context context;
    private FirebaseFirestore firestore;

    public NotificationAdapter(List<Notification> notificationList, Context context) {
        this.notificationList = notificationList;
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance(); // Initialize Firestore
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        // Set title, message, and timestamp
        holder.notificationTitle.setText(notification.getTitle() != null ? notification.getTitle() : "New Notification");
        holder.notificationMessage.setText(notification.getMessage() != null ? notification.getMessage() : "No message provided");
        holder.notificationTimestamp.setText(formatTimestamp(notification.getTimestamp()));

        // Fetch and display profile picture of the sender
        String postedBy = notification.getPostedBy();
        if (postedBy != null && !postedBy.isEmpty()) {
            firestore.collection("Users")
                    .document(postedBy)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String profilePictureUrl = documentSnapshot.getString("profilePicture");
                            Glide.with(context)
                                    .load(profilePictureUrl)
                                    .placeholder(R.drawable.ic_notif)
                                    .into(holder.notificationIcon);
                        } else {
                            holder.notificationIcon.setImageResource(R.drawable.ic_notif);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("NotificationAdapter", "Error fetching user profile: ", e);
                        holder.notificationIcon.setImageResource(R.drawable.ic_notif);
                    });
        } else {
            // Default icon if postedBy is null or empty
            holder.notificationIcon.setImageResource(R.drawable.ic_notif);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    private String formatTimestamp(long timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            Log.e("NotificationAdapter", "Error formatting timestamp: ", e);
            return "Unknown date";
        }
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView notificationIcon;
        TextView notificationTitle, notificationMessage, notificationTimestamp;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationIcon = itemView.findViewById(R.id.notification_icon);
            notificationTitle = itemView.findViewById(R.id.notification_title);
            notificationMessage = itemView.findViewById(R.id.notification_message);
            notificationTimestamp = itemView.findViewById(R.id.notification_timestamp);
        }
    }
}
