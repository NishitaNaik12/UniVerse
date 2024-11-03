package com.example.universe;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setup);

        mAuth=FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        email = findViewById(R.id.email);
        String em = mAuth.getCurrentUser().getEmail();
        email.setText(em);
        UserName = (EditText) findViewById(R.id.setupUsername);
        FullName = (EditText) findViewById(R.id.setupFullName);
        Department = (EditText) findViewById(R.id.setupDepartment);
        saveButton=(Button) findViewById(R.id.setupSaveButton);
        Status = (EditText)findViewById(R.id.setupStatus);
        profileImage=(CircleImageView) findViewById(R.id.setupProfileImage);

        loadingBar=new ProgressDialog(this);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInfo();



            }
        });



    }
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
        }else {
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
        Intent mainIntent =new Intent(SetupActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }



}