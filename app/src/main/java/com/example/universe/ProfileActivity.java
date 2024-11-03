package com.example.universe;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;


    private FirebaseAuth mAuth;
    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private DatabaseReference postsRef;
    private TextView userNameTextView;
    private TextView userDepartmentTextView;
    private TextView StatusText;
    private DatabaseReference usersRef;
    private TextView editProfileText;
    //
    private String currentUserID;
    private CircleImageView profileImage;
//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        userNameTextView = findViewById(R.id.userName);
        userDepartmentTextView = findViewById(R.id.userDepartment);
        StatusText = findViewById(R.id.StatusText);
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        recyclerViewPosts.setLayoutManager(new GridLayoutManager(this, 2)); // Set grid layout with 3 columns
        editProfileText= findViewById(R.id.editProfileText);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, true);
        recyclerViewPosts.setAdapter(postAdapter);
        profileImage = findViewById(R.id.profileImage);

        mAuth = FirebaseAuth.getInstance();
        postsRef = FirebaseDatabase.getInstance().getReference().child("posts");
        String currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        //
        loadUserProfile();
        //
        bottomNavigationView = findViewById(R.id.bn);
        View profileIcon = bottomNavigationView.findViewById(R.id.b_profile);
        if (profileIcon != null) {
            profileIcon.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showLogoutMenu(v);
                    return true; // Indicate the long click was handled
                }
            });
        }
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(mAuth.getCurrentUser().getUid());

        // Fetch user profile info
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullname = snapshot.child("fullname").getValue(String.class);
                    String department = snapshot.child("department").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);

                    // Set these values to TextViews
                    if (fullname != null) userNameTextView.setText(fullname);
                    if (department != null) userDepartmentTextView.setText(department);
                    if(status != null) StatusText.setText(status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle potential errors
            }
        });

        loadUserPosts();

        editProfileText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setupIntent =new Intent(ProfileActivity.this,SetupActivity.class);
                setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(setupIntent);
            }
        });

        bottomNavigationView = findViewById(R.id.bn);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id=item.getItemId();
                if(id==R.id.b_home)
                {
                    startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                    finish();
                    return  true;

                }
                else if(id == R.id.b_calendar){
                    startActivity(new Intent(getApplicationContext(),CalendarActivity.class));
                    finish();
                    return  true;
                }
                else {
                    return true;
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        bottomNavigationView.setSelectedItemId(R.id.b_profile);
    }

    private void SendUserToLoginActivity() {

        Intent loginIntent =new Intent(ProfileActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    private void loadUserPosts() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the current user ID

        postsRef.orderByChild("userId").equalTo(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear(); // Clear the list to avoid duplicates
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class); // Convert snapshot to Post object
                    if (post != null) {
                        postList.add(post); // Add the new post to the list
                    }
                }
                postAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle potential errors here
            }
        });
    }
    private void showLogoutMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.logout_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_logout) {
                    logout();
                    return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }

    private void logout() {
        mAuth.signOut();
        SendUserToLoginActivity();
    }
    //
    private void loadUserProfile() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Load username and department
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String department = dataSnapshot.child("department").getValue(String.class);
                    String profileImageUrl = dataSnapshot.child("profileImage").getValue(String.class); // Retrieve image URL

                    userNameTextView.setText(username);
                    userDepartmentTextView.setText(department);
                    // Load status text if it exists (optional)
                    String status = dataSnapshot.child("status").getValue(String.class);
                    if (status != null) {
                        StatusText.setText(status);
                    }

                    // Load profile image using Glide
                    if (profileImageUrl != null) {
                        Glide.with(ProfileActivity.this)
                                .load(profileImageUrl)
                                .into(profileImage);
                    } else {
                        // Optionally set a default image if no image URL is found
                        profileImage.setImageResource(R.drawable.profile_icon); // Your default image
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }
    //
}
