package com.example.bookgraph;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

/**
 * Handles setup activity
 */
public class SetupActivity extends AppCompatActivity {

    private EditText userName, fullName, countryName;
    private Button saveInformationButton;
    private CircleImageView profileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String current_user_id;
    private ProgressDialog loadingBar;
    final static int gallery_pick = 1;
    private StorageReference userProfileImageRef;



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
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        saveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountSetupInformation();
            }
        });



        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,gallery_pick);
            }
        });

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileimage")){
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);
                    }
                    else{
                        Toast.makeText(SetupActivity.this,"Please select profile image first.",Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                                            Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                            startActivity(selfIntent);
                                            Toast.makeText(SetupActivity.this, "Image uploaded...", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                        else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SetupActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
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
            userMap.put("country",country);
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
                        Toast.makeText(SetupActivity.this,"Error occurred: " + message,Toast.LENGTH_LONG).show();
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