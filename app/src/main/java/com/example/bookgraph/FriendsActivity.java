package com.example.bookgraph;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView myFriendList;
    private DatabaseReference friendsRef,usersRef;
    private FirebaseAuth mAuth;
    private String onlineUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        onlineUserId = mAuth.getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(onlineUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        myFriendList = (RecyclerView)findViewById(R.id.friends_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);
        
        displayAllFriends();

    }

    public void updateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss");
        saveCurrentTime = currentDate.format(calForDate.getTime());

        Map currentStateMap = new HashMap<>();
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);

        usersRef.child(onlineUserId).child("userState").updateChildren(currentStateMap);



    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateUserStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus("offline");
    }

    private void displayAllFriends() {

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(friendsRef, Friends.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Friends, friendsViewHolder>(options) {
            @Override
            public friendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_users_display_layout, parent, false);

                return new FriendsActivity.friendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final friendsViewHolder viewHolder, int position, final Friends model) {

                viewHolder.setDate((model.getDate()));
                final String usersIDs = getRef(position).getKey();
                usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){


                            final String userName = snapshot.child("fullname").getValue().toString();
                            final String profileImage = snapshot.child("profileimage").getValue().toString();
                            final String type;

                            if(snapshot.hasChild("userState")){
                                type = snapshot.child("userState").child("type").getValue().toString();

                                if(type.equals("online")){
                                    viewHolder.onlineStatusView.setVisibility(View.VISIBLE);
                                }
                                else{
                                    viewHolder.onlineStatusView.setVisibility(View.INVISIBLE);
                                }
                            }
                            viewHolder.setFullname(userName);
                            viewHolder.setProfileimage(profileImage);
                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CharSequence options[] = new CharSequence[]{

                                            userName + "'s Profile",
                                            "Send Message"
                                    };
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select Option");

                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(which == 0){
                                                Intent profileIntent = new Intent(FriendsActivity.this,PersonProfileActivity.class);
                                                profileIntent.putExtra("visit_user_id",usersIDs);
                                                startActivity(profileIntent);
                                            }
                                            if(which == 1){
                                                Intent chatIntent = new Intent(FriendsActivity.this,ChatActivity.class);
                                                chatIntent.putExtra("visit_user_id",usersIDs);
                                                chatIntent.putExtra("userName",userName);

                                                startActivity(chatIntent);
                                            }
                                        }
                                    });

                                    builder.show();

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        };
        adapter.startListening();
        myFriendList.setAdapter(adapter);
    }


    public static class friendsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageView onlineStatusView;

        public friendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            onlineStatusView = (ImageView) itemView.findViewById(R.id.all_user_online_icon);
        }

        public void setFullname(String fullname)
        {
            TextView username = (TextView) mView.findViewById(R.id.all_users_profile_full_name);
            username.setText(fullname);
        }

        public void setProfileimage(String profileimage)
        {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.get().load(profileimage).into(image);
        }

        public void setDate(String date)
        {
            TextView friendsDate = (TextView) mView.findViewById(R.id.all_users_status);
            friendsDate.setText("Friends Since: " + date);
        }
    }

}