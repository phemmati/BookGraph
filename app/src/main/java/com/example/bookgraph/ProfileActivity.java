package com.example.bookgraph;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName,userFullName,userStatus,userCountry,userGender,userRelation,userDOB;
    private CircleImageView userProfImage;
    private DatabaseReference profileUserRef,friendsRef,postRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private Button myPosts,myFriends;
    private int countFriends = 0,countPost=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userCountry = (TextView) findViewById(R.id.my_country);
        userName = (TextView) findViewById(R.id.my_username);
        userGender = (TextView) findViewById(R.id.my_gender);
        userDOB = (TextView) findViewById(R.id.my_dob);
        userFullName = (TextView) findViewById(R.id.my_profile_full_name);
        userStatus = (TextView) findViewById(R.id.my_profile_status);
        userRelation = (TextView) findViewById(R.id.my_relationship);
        userProfImage = (CircleImageView) findViewById(R.id.my_profile_pic);

        myFriends = (Button) findViewById(R.id.my_friends_button);
        myPosts = (Button) findViewById(R.id.my_post_button);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        myFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToFriendsActivity();
            }
        });

        myPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToMyPostsActivity();
            }
        });


        postRef.child(currentUserId).orderByChild("uid").startAt(currentUserId).endAt(currentUserId + "\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    countPost= (int) snapshot.getChildrenCount();
                    myPosts.setText(Integer.toString(countPost)+ " Posts");

                }
                else{

                    myPosts.setText("0 Post");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        friendsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    countFriends = (int) snapshot.getChildrenCount();

                    myFriends.setText(Integer.toString(countFriends) + " Friends");
                }
                else{
                    myFriends.setText("No Friends");

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        profileUserRef.addValueEventListener(new ValueEventListener() {
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

    private void sendUserToFriendsActivity() {
        Intent friendsIntent = new Intent(ProfileActivity.this,FriendsActivity.class);
        startActivity(friendsIntent);
    }

    private void sendUserToMyPostsActivity() {
        Intent friendsIntent = new Intent(ProfileActivity.this,MyPostsActivity.class);
        startActivity(friendsIntent);
    }

}