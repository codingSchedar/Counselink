package com.example.counselinlv1.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.counselinlv1.Models.Post;
import com.example.counselinlv1.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final List<Post> postList;
    private final Context context;

    // Constructor with postList and context
    public PostAdapter(List<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // Set the caption
        if (post.getCaption() != null && !post.getCaption().isEmpty()) {
            holder.captionTextView.setText(post.getCaption());
            holder.captionTextView.setVisibility(View.VISIBLE);
        } else {
            holder.captionTextView.setVisibility(View.GONE);
        }

        // Format and display the timestamp
        try {
            long timestamp = post.getTimestamp();
            if (timestamp > 0) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                String formattedDate = dateFormat.format(timestamp);
                holder.timestampTextView.setText(formattedDate);
            } else {
                holder.timestampTextView.setText("No timestamp");
            }
        } catch (NumberFormatException e) {
            holder.timestampTextView.setText("Invalid timestamp");
        }

        // Load the post image using Glide
        if (post.getFileURL() != null && !post.getFileURL().isEmpty()) {
            holder.posterImageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(post.getFileURL())
                    .apply(new RequestOptions().placeholder(R.drawable.ic_placeholder))
                    .into(holder.posterImageView);
        } else {
            holder.posterImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView captionTextView;
        TextView timestampTextView;
        ImageView posterImageView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            captionTextView = itemView.findViewById(R.id.post_caption);
            timestampTextView = itemView.findViewById(R.id.post_timestamp);
            posterImageView = itemView.findViewById(R.id.post_image);
        }
    }
}
