package com.example.bookgraph;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView postImage;
    private TextView postDescription;
    private Button deletePostButton,editPostButton;
    private DatabaseReference clickPostRef;
    private FirebaseAuth mAuth;

    private String postKey,currentIserId,databaseUserId,description,image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth = FirebaseAuth.getInstance();
        currentIserId = mAuth.getCurrentUser().getUid();
        postKey = getIntent().getExtras().get("PostKey").toString();
        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);

        postImage = (ImageView) findViewById(R.id.click_post_image);
        postDescription = (TextView) findViewById(R.id.click_post_description);
        deletePostButton = (Button) findViewById(R.id.delete_post_button);
        editPostButton = (Button) findViewById(R.id.edit_post_button);

        deletePostButton.setVisibility(View.INVISIBLE);
        editPostButton.setVisibility(View.INVISIBLE);

        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    description = snapshot.child("description").getValue().toString();
                    image = snapshot.child("postimage").getValue().toString();

                    databaseUserId = snapshot.child("uid").getValue().toString();
                    postDescription.setText(description);
                    Picasso.get().load(image).into(postImage);

                    if(currentIserId.equals(databaseUserId)){

                        deletePostButton.setVisibility(View.VISIBLE);
                        editPostButton.setVisibility(View.VISIBLE);
                    }

                    editPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            editCurrentPost(description);
                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        deletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCurrentPost();
            }
        });


    }

    private void editCurrentPost(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);

        builder.setView(inputField);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this,"Post has been updated.",Toast.LENGTH_LONG).show();
                sendUserToMainActivity();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);

    }

    private void deleteCurrentPost() {
        clickPostRef.removeValue();
        sendUserToMainActivity();
        Toast.makeText(ClickPostActivity.this,"Pst has been deleted." ,Toast.LENGTH_LONG).show();

    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(ClickPostActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}