package com.example.universe;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {
    TextView logInLink;

    private EditText UserEmail, UserPassword, UserConfirnPassword;
    private Button CreateAccountButton;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        mAuth= FirebaseAuth.getInstance();

        UserEmail = (EditText) findViewById(R.id.loginemail);
        UserPassword= (EditText) findViewById(R.id.liginpasswd);
        UserConfirnPassword = (EditText) findViewById(R.id.reenterPasswordEditText);
        CreateAccountButton=(Button) findViewById(R.id.signupButton);

        loadingBar=new ProgressDialog(this);

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });

        logInLink= findViewById(R.id.logInLink);
        logInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                SendUserToSetupActivity();
            } else {
                Toast.makeText(this, "Please verify your email address before logging in.", Toast.LENGTH_SHORT).show();
                mAuth.signOut(); // Sign out if the email is not verified
            }
        }
    }
    private boolean isValidEmail(String email) {
        // Regular expression for validating email format
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }
    private boolean isDummyEmail(String email) {
        // List of common dummy email patterns
        String[] dummyEmails = {
                "example@example.com",
                "test@test.com",
                "user@dummy.com",
                "test@gmail.com",
                "test@hotmail.com"
        };

        for (String dummyEmail : dummyEmails) {
            if (email.equalsIgnoreCase(dummyEmail)) {
                return true;
            }
        }
        return false;
    }

    private void SendUserToMainActivity() {
        Intent mainIntent =new Intent(SignupActivity.this,HomeActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }

    private void CreateNewAccount() {
        String email = UserEmail.getText().toString().trim();
        String password = UserPassword.getText().toString().trim();
        String confirmPassword = UserConfirnPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isDummyEmail(email)) {
            Toast.makeText(this, "Please use a valid email address, not a dummy email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Re-enter Password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords should match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading bar
        loadingBar.setTitle("Creating Account");
        loadingBar.setMessage("Please wait...");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);

        // Create the user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Send verification email
                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Use your dynamic link URL here
                                            String dynamicLinkUrl = "http://yourapp.example.com/verify"; // Example URL
                                            Toast.makeText(SignupActivity.this, "Verification email sent. Please verify your email.", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                            mAuth.signOut(); // Sign out the user after sending verification email
                                        } else {
                                            Toast.makeText(SignupActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });

                            }
                        } else {
                            String message = task.getException().getMessage();
                            Toast.makeText(SignupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }
    private void SendUserToHomeActivity() {
        Intent homeIntent = new Intent(SignupActivity.this, HomeActivity.class); // Replace HomeActivity with your actual home activity class
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish(); // Optional: This will close the SignupActivity so the user can't navigate back to it
    }



    private void SendUserToSetupActivity() {

        Intent setupIntent = new Intent(SignupActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

}