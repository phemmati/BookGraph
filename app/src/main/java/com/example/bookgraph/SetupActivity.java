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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText userName, fullName, countryName;
    private Button saveInformationButton;
    private CircleImageView profileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String current_user_id;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        userName = (EditText) findViewById(R.id.setup_user_name);
        fullName = (EditText) findViewById(R.id.setup_fullname);
        countryName = (EditText) findViewById(R.id.setup_country);
        saveInformationButton = (Button) findViewById(R.id.setup_information_button);
        profileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);
        loadingBar = new ProgressDialog(this);

        saveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountSetupInformation();
            }
        });

    }

    private void saveAccountSetupInformation() {
        String username = userName.getText().toString();
        String fullname = fullName.getText().toString();
        String country = countryName.getText().toString();
        if(TextUtils.isEmpty(username)){
            Toast.makeText(this,"Please provide your username.",Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(fullname)){
            Toast.makeText(this,"Please provide your full name.",Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(country)){
            Toast.makeText(this,"Please provide your country.",Toast.LENGTH_LONG).show();
        }
        else{

            loadingBar.setTitle("Saving Information...");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("username",username);
            userMap.put("fullname",fullname);
            userMap.put("status","");
            userMap.put("gender","");
            userMap.put("dob","");
            userMap.put("relationstatus","");

            usersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        sendUserToMainActivity();
                        Toast.makeText(SetupActivity.this,"Yor account is created successfully.",Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                    else{
                        String  message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this,"Error accrued: " + message,Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                }
            });


        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}