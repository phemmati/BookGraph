package com.example.bookgraph;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText userEmail, userPassword,userConfirmPassword;
    private Button createAccountButton;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        userEmail = (EditText)findViewById(R.id.register_email);
        userPassword = (EditText)findViewById(R.id.register_password);
        userConfirmPassword = (EditText)findViewById(R.id.register_confirm_password);
        createAccountButton = (Button) findViewById(R.id.register_create_account);
        loadingBar = new ProgressDialog(this);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            sendUsertoMainActivity();
        }
    }

    private void sendUsertoMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void createNewAccount() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        String confirmPassword = userConfirmPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please provide your email address.",Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please provide a password",Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(this,"Please confirm your password",Toast.LENGTH_LONG).show();
        }
        if (!password .equals(confirmPassword) ){
            Toast.makeText(this,"Your passwords do not match.",Toast.LENGTH_LONG).show();

        }
        else{

            loadingBar.setTitle("Creating new account...");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                sendUserToSetupActivity();

                                Toast.makeText(RegisterActivity.this,"You are authenticated successfully.",Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                            else{
                                String  message = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this,"Error accrued: " + message,Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }

                        }
                    });

        }
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(RegisterActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}