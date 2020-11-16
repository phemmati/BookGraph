package com.example.bookgraph;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mtoolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
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
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
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
    }

    private void sendUsertoPostActivity() {
        Intent newPostIntent = new Intent(MainActivity.this,PostActivity.class);
        startActivity(newPostIntent);
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
                Toast.makeText(this,"profile",Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_home:
                Toast.makeText(this,"home",Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_friends:
                Toast.makeText(this,"friends list",Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_find_friends:
                Toast.makeText(this,"fine friends",Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_messages:
                Toast.makeText(this,"messages",Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_settings:
                Toast.makeText(this,"settings",Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_Logout:
                mAuth.signOut();
                sendUsertoLoginActivity();
                break;
        }
    }
}