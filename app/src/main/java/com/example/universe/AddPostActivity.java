package com.example.universe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText postCaptionEditText;
    private Button selectImageButton, postButton;
    private ImageView selectedImageView;

    private Uri imageUri;
    private DatabaseReference postsRef;
    private StorageReference storageRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        postCaptionEditText = findViewById(R.id.postCaptionEditText);
        selectImageButton = findViewById(R.id.selectImageButton);
        postButton = findViewById(R.id.postButton);
        selectedImageView = findViewById(R.id.selectedImageView);

        mAuth = FirebaseAuth.getInstance();
        postsRef = FirebaseDatabase.getInstance().getReference().child("posts");
        storageRef = FirebaseStorage.getInstance().getReference().child("post_images");

        selectImageButton.setOnClickListener(v -> openFileChooser());

        postButton.setOnClickListener(v -> postContent());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            selectedImageView.setImageURI(imageUri);
            selectedImageView.setVisibility(View.VISIBLE);
        }
    }

    private void postContent() {
        String caption = postCaptionEditText.getText().toString().trim();
        if (imageUri != null) {
            // Upload image to Firebase Storage
            uploadImage(caption);
        } else if (!TextUtils.isEmpty(caption)) {
            // Post text only
            savePostToDatabase("", caption);
        } else {
            Toast.makeText(this, "Please select an image or write something.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage(String caption) {
        String userId = mAuth.getCurrentUser().getUid();
        StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpg");

        fileRef.putFile(imageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    savePostToDatabase(imageUrl, caption);
                });
            } else {
                Toast.makeText(AddPostActivity.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePostToDatabase(String imageUrl, String caption) {
        String userId = mAuth.getCurrentUser().getUid();
        String postId = postsRef.push().getKey();
        long timestamp = System.currentTimeMillis();

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("userId", userId);
        postMap.put("imageUrl", imageUrl);
        postMap.put("caption", caption);
        postMap.put("timestamp", timestamp);

        postsRef.child(postId).setValue(postMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AddPostActivity.this, "Post uploaded successfully!", Toast.LENGTH_SHORT).show();
                postCaptionEditText.setText(""); // Clear input
                selectedImageView.setVisibility(View.GONE);
            } else {
                Toast.makeText(AddPostActivity.this, "Failed to upload post.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
