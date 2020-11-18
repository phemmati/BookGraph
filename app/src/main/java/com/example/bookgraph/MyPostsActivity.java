package com.example.bookgraph;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class MyPostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myPostList;
    private FirebaseAuth mAuth;
    private DatabaseReference postRef,usersRef,likesRef;
    private String currentUserId;
    Boolean likeChecker = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        mToolbar = (Toolbar)findViewById(R.id.my_post_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");

        myPostList = (RecyclerView) findViewById(R.id.my_all_posts);
        myPostList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostList.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        displayMyAllPosts();


    }

    private void displayMyAllPosts() {

        Query myPostQuery = postRef.orderByChild("uid").startAt(currentUserId).endAt(currentUserId + "\uf8ff");

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(myPostQuery, Posts.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Posts, MyPostsViewHolder>(options) {
            @Override
            public MyPostsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_posts_layout, parent, false);

                return new MyPostsViewHolder(view);
            }


            @Override
            protected void onBindViewHolder(@NonNull MyPostsViewHolder holder, int position, @NonNull Posts model) {

                final String postKey = getRef(position).getKey();

                holder.setFullname(model.getFullname());
                holder.setTime(model.getTime());
                holder.setDate(model.getDate());
                holder.setDescription(model.getDescription());
                holder.setProfileimage( model.getProfileimage());
                holder.setPostimage(model.getPostimage());

                holder.setLikeButtonStatus(postKey);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(MyPostsActivity.this,ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey",postKey);
                        startActivity(clickPostIntent);
                    }
                });


                holder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent = new Intent(MyPostsActivity.this,CommentsActivity.class);
                        commentsIntent.putExtra("PostKey",postKey);
                        startActivity(commentsIntent);
                    }
                });

                holder.likePostButton.setOnClickListener(new View.OnClickListener() {
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
        myPostList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class MyPostsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageButton likePostButton,commentPostButton;
        TextView displayNoOfLikes;
        int likesCount;
        String currentUserId;
        DatabaseReference likesRef;


        public MyPostsViewHolder(@NonNull View itemView) {
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
}