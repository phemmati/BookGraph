package com.example.bookgraph;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mtoolBar;
    Boolean likeChecker = false;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef,postsRef,likesRef;

    private CircleImageView navProfileImage;
    private TextView navProfileUserName;
    private String currentUserId;
    private ImageButton addNewPostButton;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        currentUserId = mAuth.getCurrentUser().getUid();

        mtoolBar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolBar);
        getSupportActionBar().setTitle("Home");

        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        postList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        navProfileUserName = (TextView)navView.findViewById(R.id.nav_user_full_name);
        addNewPostButton = (ImageButton) findViewById(R.id.add_new_post_button);

        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    if(dataSnapshot.hasChild("fullname")){

                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        navProfileUserName.setText(fullname);
                    }
                    if(dataSnapshot.hasChild("profileimage")){

                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(navProfileImage);

                    }
                    else{
                        Toast.makeText(MainActivity.this,"This profile does not exist.",Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                userMenuSelector(item);
                return false;
            }
        });

        addNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUsertoPostActivity();
            }
        });

        displayAllUsersPost();
    }

    private void displayAllUsersPost() {

            FirebaseRecyclerOptions<Posts> options =
                    new FirebaseRecyclerOptions.Builder<Posts>()
                            .setQuery(postsRef, Posts.class)
                            .build();

            FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Posts, postViewHolder>(options) {
                @Override
                public postViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.all_posts_layout, parent, false);

                    return new postViewHolder(view);
                }

                @Override
                protected void onBindViewHolder(postViewHolder viewHolder, int position, Posts model) {
                    final String postKey = getRef(position).getKey();

                    viewHolder.setFullname(model.getFullname());
                    viewHolder.setTime(model.getTime());
                    viewHolder.setDate(model.getDate());
                    viewHolder.setDescription(model.getDescription());
                    viewHolder.setProfileimage( model.getProfileimage());
                    viewHolder.setPostimage(model.getPostimage());

                    viewHolder.setLikeButtonStatus(postKey);

                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent clickPostIntent = new Intent(MainActivity.this,ClickPostActivity.class);
                            clickPostIntent.putExtra("PostKey",postKey);
                            startActivity(clickPostIntent);
                        }
                    });


                    viewHolder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent commentsIntent = new Intent(MainActivity.this,CommentsActivity.class);
                            commentsIntent.putExtra("PostKey",postKey);
                            startActivity(commentsIntent);
                        }
                    });

                    viewHolder.likePostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            likeChecker = true;
                            likesRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(likeChecker.equals(true)){
                                        if(snapshot.child(postKey).hasChild(currentUserId)){

                                            likesRef.child(postKey).child(currentUserId).removeValue();
                                            likeChecker = false;
                                        }
                                        else{

                                            likesRef.child(postKey).child(currentUserId).setValue(true);
                                            likeChecker = false;

                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });

                }
            };
            adapter.startListening();
            postList.setAdapter(adapter);
    }


    public static class postViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageButton likePostButton,commentPostButton;
        TextView displayNoOfLikes;
        int likesCount;
        String currentUserId;
        DatabaseReference likesRef;


        public postViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            likePostButton = (ImageButton)mView.findViewById(R.id.like_button);
            commentPostButton = (ImageButton)mView.findViewById(R.id.comment_button);
            displayNoOfLikes = (TextView) mView.findViewById(R.id.display_number_of_likes);

            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setLikeButtonStatus(final String postKey){
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.child(postKey).hasChild(currentUserId)){
                        likesCount = (int) snapshot.child(postKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.like);
                        displayNoOfLikes.setText((Integer.toString(likesCount)+("Likes")));
                    }
                    else{
                        likesCount = (int) snapshot.child(postKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.dislike);
                        displayNoOfLikes.setText((Integer.toString(likesCount)+("Likes")));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


        public void setFullname(String fullname)
        {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(String profileimage)
        {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).into(image);
        }

        public void setTime(String time)
        {
            TextView PostTime = (TextView) mView.findViewById(R.id.time);
            PostTime.setText("    " + time);
        }

        public void setDate(String date)
        {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText("    " + date);
        }

        public void setDescription(String description)
        {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(String postimage)
        {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.get().load(postimage).into(PostImage);
        }
    }

    private void sendUsertoPostActivity() {
        Intent newPostIntent = new Intent(MainActivity.this,PostActivity.class);
        startActivity(newPostIntent);
    }

    private void sendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            sendUsertoLoginActivity();
        }
        else{
            checkUserExistence();
        }

    }

    private void checkUserExistence() {
        final String current_userId = mAuth.getCurrentUser().getUid();
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_userId)){
                    sendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void sendUsertoLoginActivity() {

        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void sendUserToSettingsActivity() {

        Intent settingIntent = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingIntent);
    }

    private void sendUserToProfileActivity() {

        Intent profileIntent = new Intent(MainActivity.this,ProfileActivity.class);
        startActivity(profileIntent);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void userMenuSelector(MenuItem item){
        switch ((item.getItemId())){

            case R.id.nav_post:
                sendUsertoPostActivity();
                break;

            case R.id.nav_profile:
                sendUserToProfileActivity();
                break;

            case R.id.nav_home:
                Toast.makeText(this,"home",Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_friends:
                Toast.makeText(this,"friends list",Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_find_friends:
                sendUserToFindFriendsActivity();
                break;

            case R.id.nav_messages:
                Toast.makeText(this,"messages",Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_settings:
                sendUserToSettingsActivity();
                break;

            case R.id.nav_Logout:
                mAuth.signOut();
                sendUsertoLoginActivity();
                break;
        }
    }
}