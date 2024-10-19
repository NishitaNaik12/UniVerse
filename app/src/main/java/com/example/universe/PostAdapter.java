package com.example.universe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<Post> postList;
    private DatabaseReference usersRef;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);

        // Check if the post contains an image
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
           // holder.contentTextView.setVisibility(View.GONE);
            holder.postImageView.setVisibility(View.VISIBLE);
            holder.captionTextView.setVisibility(View.VISIBLE);
            holder.captionTextView.setText(post.getCaption());
            // Load image using Glide
            Glide.with(holder.itemView.getContext()).load(post.getImageUrl()).into(holder.postImageView);
        } else {
            // For text-only posts, show the contentTextView and hide the image views
            holder.postImageView.setVisibility(View.GONE);
            holder.captionTextView.setVisibility(View.VISIBLE);
            //holder.contentTextView.setVisibility(View.VISIBLE);
            holder.captionTextView.setText(post.getCaption());
            //holder.contentTextView.setText(post.getContent());

        }

        // Set the timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
        holder.timestampTextView.setText(sdf.format(post.getTimestamp()));

        usersRef.child(post.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.child("fullname").getValue(String.class);
                    holder.userNameTextView.setText(userName);  // Set user's name to the TextView
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.userNameTextView.setText("Unknown User");
            }

        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    // ViewHolder class to hold the view for each post item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView captionTextView, timestampTextView;
        public ImageView postImageView;
        public TextView userNameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            //contentTextView = itemView.findViewById(R.id.contentTextView);

            captionTextView = itemView.findViewById(R.id.postCaptionTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            postImageView = itemView.findViewById(R.id.postImageView);
            userNameTextView = itemView.findViewById(R.id.usernameTextView);
        }
    }

    // Update the dataset
    public void setPosts(List<Post> posts) {
        this.postList = posts;
        notifyDataSetChanged();
    }
}


