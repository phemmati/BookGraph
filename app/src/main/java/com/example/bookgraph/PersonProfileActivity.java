package com.example.bookgraph;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userName,userFullName,userStatus,userCountry,userGender,userRelation,userDOB;
    private CircleImageView userProfImage;
    private FirebaseAuth mAuth;
    private String saveCurrentDate,saveCurrentTime;
    private String senderUserId,receiverUserId,currentState;
    private DatabaseReference friendRequestRef,usersRef,friendsRef;
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

                    maintenanceOfButtons();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        declineFriendRequestBtn.setVisibility(View.INVISIBLE);
        declineFriendRequestBtn.setEnabled(false);

        if(!senderUserId.equals(receiverUserId)){
            sendFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendFriendRequestBtn.setEnabled(false);
                    if(currentState.equals("not_friends")){
                        sendFriendRequest();
                    }
                    if(currentState.equals("request_sent")){
                        cancelFriendRequest();
                    }
                    if(currentState.equals("request_received")){
                        acceptFriendRequest();
                    }
                    if(currentState.equals("friends")){
                        unfriend();
                    }
                }
            });

        }
        else{
            declineFriendRequestBtn.setVisibility(View.INVISIBLE);
            sendFriendRequestBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void unfriend() {

        friendsRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    friendsRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                sendFriendRequestBtn.setEnabled(true);
                                currentState="not_friends";
                                sendFriendRequestBtn.setText("Send Friend Request");

                                declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                declineFriendRequestBtn.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });

    }

    private void acceptFriendRequest() {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        friendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    friendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                friendRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){
                                            friendRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if(task.isSuccessful()){
                                                        sendFriendRequestBtn.setEnabled(true);
                                                        currentState="friends";
                                                        sendFriendRequestBtn.setText("Unfriend This Person");

                                                        declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                        declineFriendRequestBtn.setEnabled(false);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });

                            }
                        }
                    });
                }
            }
        });




    }

    private void cancelFriendRequest() {

        friendRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    friendRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                sendFriendRequestBtn.setEnabled(true);
                                currentState="not_friends";
                                sendFriendRequestBtn.setText("Send Friend Request");

                                declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                declineFriendRequestBtn.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });


    }

    private void maintenanceOfButtons() {
        friendRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(receiverUserId)){
                    String requestType = snapshot.child(receiverUserId).child("request_type").getValue().toString();
                    if(requestType.equals("sent")){
                        currentState = "request_sent";
                        sendFriendRequestBtn.setText("Cancel friend request");

                        declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                        declineFriendRequestBtn.setEnabled(false);

                    }
                    else if(requestType.equals("received")){
                        currentState = "request_received";
                        sendFriendRequestBtn.setText("Accept Friend Request");

                        declineFriendRequestBtn.setVisibility(View.VISIBLE);
                        declineFriendRequestBtn.setEnabled(true);

                        declineFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelFriendRequest();
                            }
                        });
                    }
                }
                else{
                    friendsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(receiverUserId)){
                                currentState = "friends";
                                sendFriendRequestBtn.setText("Unfriend This Person");
                                declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                declineFriendRequestBtn.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendFriendRequest() {
        friendRequestRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    friendRequestRef.child(receiverUserId).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                sendFriendRequestBtn.setEnabled(true);
                                currentState="request_sent";
                                sendFriendRequestBtn.setText("Cancel friend request");

                                declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                declineFriendRequestBtn.setEnabled(false);
                            }
                        }
                    });
                }
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
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequest");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        currentState = "not_friends";

    }
}