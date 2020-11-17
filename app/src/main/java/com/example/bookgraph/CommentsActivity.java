package com.example.bookgraph;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {

    private ImageButton postCommentsButton;
    private RecyclerView commentsList;
    private EditText commentInputText;
    private DatabaseReference usersRef,postsRef;
    private String post_key,currentUserId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        post_key = getIntent().getExtras().get("PostKey").toString();

        commentsList = (RecyclerView) findViewById(R.id.comments_list);
        commentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentsList.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(post_key).child("Comments");
        commentInputText = (EditText) findViewById(R.id.comment_input);
        postCommentsButton = (ImageButton) findViewById(R.id.post_comment_button);

        postCommentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String userName = snapshot.child("username").getValue().toString();
                            validateComment(userName);
                            commentInputText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Comments> options =
                new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(postsRef, Comments.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Comments,CommentsViewHolder>(options)
        {

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_comments_layout, parent, false);

                return new CommentsActivity.CommentsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder commentsViewHolder, int i, @NonNull Comments comments)
            {
                commentsViewHolder.setUsername(comments.getUsername());
                commentsViewHolder.setComments(comments.getComments());
                commentsViewHolder.setDate(comments.getDate());
                commentsViewHolder.setTime(comments.getTime());
            }

        };
        adapter.startListening();
        commentsList.setAdapter(adapter);
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setUsername(String username) {

            TextView myUsername = (TextView) mView.findViewById(R.id.comment_username);
            myUsername.setText("@"+username + " ");
        }

        public void setComments(String comments) {

            TextView myComment = (TextView) mView.findViewById(R.id.comment_text);
            myComment.setText(comments);
        }

        public void setDate(String date) {
            TextView myCommentDate = (TextView) mView.findViewById(R.id.comment_date);
            myCommentDate.setText( date);

        }

        public void setTime(String time) {

            TextView myCommentTime = (TextView) mView.findViewById(R.id.comment_time);
            myCommentTime.setText("Time: "+ time);

        }
    }

    private void validateComment(String userName) {
        String commentText = commentInputText.getText().toString();
        if(commentText.isEmpty()){
            Toast.makeText(this,"Please write comment...",Toast.LENGTH_LONG).show();
        }
        else{
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calFordTime.getTime());

            final String randomKey = currentUserId + saveCurrentDate + saveCurrentTime;

            HashMap commentsMap = new HashMap();

            commentsMap.put("uid",currentUserId);
            commentsMap.put("comment",commentText);
            commentsMap.put("date",saveCurrentDate);
            commentsMap.put("time",saveCurrentTime);
            commentsMap.put("username",userName);


            postsRef.child(randomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(CommentsActivity.this,"Your comment has been posted.",Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(CommentsActivity.this,"An error occurred, try again!",Toast.LENGTH_LONG).show();
                    }
                }
            });

        }
    }
}