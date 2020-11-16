package com.example.bookgraph;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText userName,userFullName,userStatus,userCountry,userGender,userRelation,userDOB;
    private Button updateAccountButton;
    private CircleImageView userProfImage;
    private DatabaseReference settingRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private ProgressDialog loadingBar;
    final static int gallery_pick = 1;
    private StorageReference userProfileImageRef;
    private String current_user_id;
    private DatabaseReference usersRef;



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
        loadingBar = new ProgressDialog(this);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);


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

        updateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAccountInfo();
            }
        });

        userProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,gallery_pick);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // some conditions for the picture
        if(requestCode == gallery_pick && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();
            // crop the image
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        // Get the cropped image
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {       // store the cropped image into result
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Updating profile picture");
                loadingBar.setMessage("Please wait...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();

                final StorageReference filePath = userProfileImageRef.child(current_user_id + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                usersRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                            startActivity(selfIntent);
                                            Toast.makeText(SettingsActivity.this, "Image uploaded...", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                        else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });
                            }

                        });

                    }

                });
            }
            else
            {
                Toast.makeText(this, "Error Occurred: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void validateAccountInfo() {
        String username = userName.getText().toString();
        String fullname = userFullName.getText().toString();
        String status = userStatus.getText().toString();
        String country = userCountry.getText().toString();
        String DOB = userDOB.getText().toString();
        String relationship = userRelation.getText().toString();
        String gender = userGender.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this,"Please provide a username.",Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(fullname)){
            Toast.makeText(this,"Please provide your name.",Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(status)){
            Toast.makeText(this,"Please provide your account status.",Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(country)){
            Toast.makeText(this,"Please provide your country.",Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(DOB)){
            Toast.makeText(this,"Please provide your date of birth.",Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(relationship)){
            Toast.makeText(this,"Please provide your relationship status.",Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(gender)){
            Toast.makeText(this,"Please provide your gender.",Toast.LENGTH_LONG).show();
        }
        else{

            loadingBar.setTitle("Updating account information.");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            updateAccountInformation(username,fullname,status,DOB,country,relationship,gender);
        }
    }

    private void updateAccountInformation(String username, String fullname, String status, String dob, String country, String relationship, String gender) {
        HashMap userMap = new HashMap();
        userMap.put("username",username);
        userMap.put("fullname",fullname);
        userMap.put("status",status);
        userMap.put("dob",dob);
        userMap.put("relationstatus",relationship);
        userMap.put("country",country);
        userMap.put("gender",gender);

        settingRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    sendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this,"Account setting updated successfully",Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                }
                else{
                    Toast.makeText(SettingsActivity.this,"An error occurred, please try later.",Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}