package com.example.bookgraph;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText userName,userFullName,userStatus,userCountry,userGender,userRelation,userDOB;
    private Button updateAccountButton;
    private CircleImageView userProfImage;
    private DatabaseReference settingRef;
    private FirebaseAuth mAuth;
    private String currentUserId;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        settingRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);


        userCountry = (EditText) findViewById(R.id.settings_country);
        userName = (EditText) findViewById(R.id.settings_username);
        userGender = (EditText) findViewById(R.id.settings_gender);
        userDOB = (EditText) findViewById(R.id.settings_dob);
        userFullName = (EditText) findViewById(R.id.settings_profile_fullname);
        userStatus = (EditText) findViewById(R.id.settings_status);
        userCountry = (EditText) findViewById(R.id.settings_country);
        userRelation = (EditText) findViewById(R.id.settings_relationship);
        updateAccountButton = (Button) findViewById(R.id.update_account_settings_button);
        userProfImage = (CircleImageView) findViewById(R.id.settings_profile_image);



        settingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String myProfileImage = snapshot.child("profileimage").getValue().toString();
                    String myUserName = snapshot.child("username").getValue().toString();
                    String myFullName = snapshot.child("fullname").getValue().toString();
                    String myProfileStatus = snapshot.child("status").getValue().toString();
                    String myDOB = snapshot.child("dob").getValue().toString();
                    String myCountry = snapshot.child("country").getValue().toString();
                    String myGender = snapshot.child("gender").getValue().toString();
                    String myRelationshipStatus = snapshot.child("relationstatus").getValue().toString();

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);
                    userName.setText(myUserName);
                    userFullName.setText(myFullName);
                    userCountry.setText(myCountry);
                    userDOB.setText(myDOB);
                    userStatus.setText(myProfileStatus);
                    userRelation.setText(myRelationshipStatus);
                    userGender.setText(myGender);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}