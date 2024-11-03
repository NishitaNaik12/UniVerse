package com.example.universe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<Post> postList;
    private DatabaseReference usersRef;
    private boolean isGridView;
    private static final int VIEW_TYPE_POST = 0;
    private static final int VIEW_TYPE_POST_GRID = 1;


    public PostAdapter(List<Post> postList, boolean b) {
        this.postList = postList;
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_POST_GRID) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_grid, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (isGridView) {
            return VIEW_TYPE_POST_GRID;
        } else {
            return VIEW_TYPE_POST;
        }

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
                    //
                    String userImageUrl = snapshot.child("profileImage").getValue(String.class);
                    //
                    holder.userNameTextView.setText(userName);  // Set user's name to the TextView
                    //
                    Glide.with(holder.itemView.getContext())
                            .load(userImageUrl) // Assuming you have a field "profileImage" in your user node
                            .into(holder.userImageView);
                    //
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.userNameTextView.setText("Unknown User");
            }

        });


        holder.likeCounts.setText(String.valueOf(post.getLikeCount()));
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (post.getLikes() != null && post.getLikes().containsKey(currentUserId)) {
            holder.like.setImageResource(R.drawable.like_blue); // Liked state
        } else {
            holder.like.setImageResource(R.drawable.like_white); // Not liked
        }

        holder.like.setOnClickListener(view -> {

            DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(post.getPostId());

            postRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Post p = mutableData.getValue(Post.class);
                    if (p == null) {
                        return Transaction.success(mutableData);
                    }

                    if (p.getLikes().containsKey(currentUserId)) {
                        // User already liked, so unlike
                        p.setLikeCount(p.getLikeCount() - 1);
                        p.getLikes().remove(currentUserId);
                    } else {
                        // User hasn't liked, so add like
                        p.setLikeCount(p.getLikeCount() + 1);
                        p.getLikes().put(currentUserId, true);
                    }

                    // Set value and report transaction success
                    mutableData.setValue(p);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                    if (committed) {
                        // Update UI based on new like state
                        if (dataSnapshot.child("likes").hasChild(currentUserId)) {
                            holder.like.setImageResource(R.drawable.like_blue);
                        } else {
                            holder.like.setImageResource(R.drawable.like_white);
                        }
                        holder.likeCounts.setText(String.valueOf(dataSnapshot.child("likeCount").getValue(Integer.class)));
                    }
                }
            });
        });
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(post.getPostId());
        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post updatedPost = dataSnapshot.getValue(Post.class);
                if (updatedPost != null) {
                    holder.likeCounts.setText(String.valueOf(updatedPost.getLikeCount()));
                    if (updatedPost.getLikes().containsKey(currentUserId)) {
                        holder.like.setImageResource(R.drawable.like_blue);
                    } else {
                        holder.like.setImageResource(R.drawable.like_white);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
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
        ImageView like;
        TextView likeCounts;
        public CircleImageView userImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            //contentTextView = itemView.findViewById(R.id.contentTextView);
            userImageView = itemView.findViewById(R.id.userImageView);
            captionTextView = itemView.findViewById(R.id.postCaptionTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            postImageView = itemView.findViewById(R.id.postImageView);
            userNameTextView = itemView.findViewById(R.id.usernameTextView);
            likeCounts = itemView.findViewById(R.id.likeCount);
            like = itemView.findViewById(R.id.like);
        }
    }

    // Update the dataset
    public void setPosts(List<Post> posts) {
        this.postList = posts;
        notifyDataSetChanged();
    }
}


