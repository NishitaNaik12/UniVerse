package com.example.universe;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;


    private FirebaseAuth mAuth;
    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private DatabaseReference postsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        recyclerViewPosts.setLayoutManager(new GridLayoutManager(this, 2)); // Set grid layout with 3 columns

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, true);
        recyclerViewPosts.setAdapter(postAdapter);

        mAuth = FirebaseAuth.getInstance();
        postsRef = FirebaseDatabase.getInstance().getReference().child("posts");

        loadUserPosts();

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
}
