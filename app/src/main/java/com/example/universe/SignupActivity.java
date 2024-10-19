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
        FirebaseUser currentUser= mAuth.getCurrentUser();
        if(currentUser!=null){
            SendUserToMainActivity();
        }
    }
    private void SendUserToMainActivity() {
        Intent mainIntent =new Intent(SignupActivity.this,HomeActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }

    private void CreateNewAccount() {

        String email =UserEmail.getText().toString();
        String password =UserPassword.getText().toString();
        String confirmPassword =UserConfirnPassword.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "enter Email", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "enter password", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(this, "re-enter password", Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(confirmPassword)){
            Toast.makeText(this, "password should match", Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Creating....");
            loadingBar.setMessage("wait");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(SignupActivity.this, "acc created", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                SendUserToSetupActivity();
                            }
                            else{
                                String message= task.getException().getMessage();
                                Toast.makeText(SignupActivity.this, "Error "+ message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }

    }


    private void SendUserToSetupActivity() {

        Intent setupIntent = new Intent(SignupActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}