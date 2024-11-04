package com.example.universe;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, Department, Status, email;
    private Button saveButton;
    private CircleImageView profileImage;
    private ImageView addapostIcon;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private ImageView backButton;

    String currentUserID;

    private ProgressDialog loadingBar;
    //
    private ImageView editProfileIcon;
    private StorageReference UserProfileImageRef; // Firebase Storage reference
    private static final int GALLERY_PICK = 1;
//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setup);

        //
        Intent intent = getIntent();
        handleIncomingIntent(intent);
        //
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        //
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        //
        email = findViewById(R.id.email);
        String em = mAuth.getCurrentUser().getEmail();
        email.setText(em);
        UserName = (EditText) findViewById(R.id.setupUsername);
        FullName = (EditText) findViewById(R.id.setupFullName);
        Department = (EditText) findViewById(R.id.setupDepartment);
        saveButton = (Button) findViewById(R.id.setupSaveButton);
        Status = (EditText) findViewById(R.id.setupStatus);
        profileImage = (CircleImageView) findViewById(R.id.setupProfileImage);
        //
        editProfileIcon = findViewById(R.id.editProfileIcon);

        editProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(); // Open gallery for image selection
            }
        });
        //
        loadingBar = new ProgressDialog(this);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInfo();


            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //
    private void handleIncomingIntent(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                // Handle the verification logic here
                // For example, you might want to check if the user is already logged in
                // or take some action specific to the verification
                Toast.makeText(this, "Welcome back! You clicked the verification link.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //
    private void SaveAccountSetupInfo() {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String department = Department.getText().toString();
        String status = Status.getText().toString();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "enter username", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "Enter full name", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(department)) {
            Toast.makeText(this, "Enter country", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(status)) {
            Toast.makeText(this, "Enter Status ", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Saving Info");
            loadingBar.setMessage("wait");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);


            HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", fullname);
            userMap.put("department", department);
            userMap.put("status", status);
            userMap.put("gender", "none");
            userMap.put("dob", "none");
            userMap.put("relationshipStatus", "none");
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "acc saved succesfully", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Errot " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });

        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }

    //
    private void selectImage() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_PICK);
    }
//

    //
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            profileImage.setImageURI(imageUri); // Set image in the CircleImageView
            uploadImageToFirebase(imageUri);
        }
    }
    //
    private void uploadProfileImage(Uri imageUri) {
        if (imageUri != null) {
            // Create a reference to Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("profileImages/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");

            // Upload the image
            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString(); // Get the URL of the uploaded image

                        // Store the image URL in Firebase Database
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        userRef.child("userImage").setValue(imageUrl)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("ProfileImageUpload", "Image URL saved successfully!");
                                    } else {
                                        Log.d("ProfileImageUpload", "Failed to save image URL");
                                    }
                                });
                    }))
                    .addOnFailureListener(e -> Log.e("ProfileImageUpload", "Image upload failed", e));
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            loadingBar.setTitle("Uploading Image");
            loadingBar.setMessage("Please wait while we upload your profile image...");
            loadingBar.show();

            StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                // Save the download URL to Firebase Database
                                UsersRef.child("profileImage").setValue(downloadUri.toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SetupActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(SetupActivity.this, "Failed to save image URL", Toast.LENGTH_SHORT).show();
                                                }
                                                loadingBar.dismiss();
                                            }
                                        });
                            }
                        });
                    } else {
                        Toast.makeText(SetupActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    //
}