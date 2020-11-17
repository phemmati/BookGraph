package com.example.bookgraph;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userName,userFullName,userStatus,userCountry,userGender,userRelation,userDOB;
    private CircleImageView userProfImage;
    private FirebaseAuth mAuth;
    private String senderUserId,receiverUserId;
    private DatabaseReference profileUserRef,usersRef;
    private Button sendFriendRequestBtn,declineFriendRequestBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        initializeFields();

        usersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
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
                    userName.setText("@"+myUserName);
                    userFullName.setText(myFullName);
                    userCountry.setText("Country: "+myCountry);
                    userDOB.setText("DOB: "+myDOB);
                    userStatus.setText("Status: "+myProfileStatus);
                    userRelation.setText("Relationship Status: "+myRelationshipStatus);
                    userGender.setText("Gender: "+myGender);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void initializeFields(){

        userCountry = (TextView) findViewById(R.id.person_country);
        userName = (TextView) findViewById(R.id.person_username);
        userGender = (TextView) findViewById(R.id.person_gender);
        userDOB = (TextView) findViewById(R.id.person_dob);
        userFullName = (TextView) findViewById(R.id.person_full_name);
        userStatus = (TextView) findViewById(R.id.person_profile_status);
        userRelation = (TextView) findViewById(R.id.person_relationship);
        userProfImage = (CircleImageView) findViewById(R.id.person_profile_pic);
        sendFriendRequestBtn = (Button) findViewById(R.id.person_send_friend_request_btn);
        declineFriendRequestBtn = (Button) findViewById(R.id.person_decline_friend_request_btn);
        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        //profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


    }
}