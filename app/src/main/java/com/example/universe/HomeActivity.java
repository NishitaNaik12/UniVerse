package com.example.universe;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    RecyclerView postRecyclerView;
    private PostAdapter postsAdapter;
    private List<Post> postList;
    private DatabaseReference postsRef;

    private ImageView addPost;

    @Override
    protected void onStart() {
        super.onStart();
        bottomNavigationView.setSelectedItemId(R.id.b_home);
        FirebaseUser currentUser= mAuth.getCurrentUser();
        if (currentUser==null){
            SendUserToLoginActivity();
        }else{
            CheckUserExistence();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        mAuth= FirebaseAuth.getInstance();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        postRecyclerView= findViewById(R.id.postsRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setReverseLayout(true);  // Reverse the order of items
        layoutManager.setStackFromEnd(true);   // Stack items from the bottom of the view
        postRecyclerView.setLayoutManager(layoutManager);

        postList = new ArrayList<>();
        postsAdapter = new PostAdapter(postList, true);
        postRecyclerView.setAdapter(postsAdapter);

        postsRef = FirebaseDatabase.getInstance().getReference().child("posts");

        loadPosts();

        addPost = findViewById(R.id.add_post_icon);
        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToAddPostActivity();
            }
        });


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
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id=item.getItemId();
                if(id==R.id.b_profile)
                {
                    startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
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

    private void loadPosts() {
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    postList.add(post);
                }
                postsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void SendUserToLoginActivity() {

        Intent loginIntent =new Intent(HomeActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void CheckUserExistence() {
        final String current_user_data_id = mAuth.getCurrentUser().getUid();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(!(snapshot.hasChild(current_user_data_id))){
                    SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendUserToSetupActivity() {
        Intent setupIntent =new Intent(HomeActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToAddPostActivity() {
        Intent addpostIntent =new Intent(HomeActivity.this,AddPostActivity.class);
        startActivity(addpostIntent);

    }
}