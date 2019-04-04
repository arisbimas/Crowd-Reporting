package com.aris.crowdreporting;

import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {

    private Toolbar commentToolbar;

    private EditText comment_field;
    private ImageView comment_post_btn, thumbUserPost;
    private TextView descPost, userPost;

    private RecyclerView comment_list;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private List<Comments> commentsList;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String blog_post_id;
    private String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentToolbar = findViewById(R.id.comment_toolbar);
        setSupportActionBar(commentToolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        commentToolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.putih), PorterDuff.Mode.SRC_ATOP);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();
        blog_post_id = getIntent().getStringExtra("blog_post_id");

        descPost = findViewById(R.id.descpost);
        userPost = findViewById(R.id.userpost);
        thumbUserPost = findViewById(R.id.thumb_userpost);

        comment_field = findViewById(R.id.isi_komen);
        comment_post_btn = findViewById(R.id.kirim_komen);
        comment_list = findViewById(R.id.list_comment);

        //RecyclerView Firebase List
        commentsList = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList);
        comment_list.setHasFixedSize(true);
        comment_list.setLayoutManager(new LinearLayoutManager(this));
        comment_list.setAdapter(commentsRecyclerAdapter);


        firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments")
                .addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (!documentSnapshots.isEmpty()) {

                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String commentId = doc.getDocument().getId();
                                    Comments comments = doc.getDocument().toObject(Comments.class);
                                    commentsList.add(comments);
                                    commentsRecyclerAdapter.notifyDataSetChanged();

                                }
                            }

                        }

                    }
                });

        comment_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String comment_message = comment_field.getText().toString();


                Map<String, Object> commentsMap = new HashMap<>();
                commentsMap.put("message", comment_message);
                commentsMap.put("user_id", current_user_id);
                commentsMap.put("timestamp", FieldValue.serverTimestamp());

                firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {

                        if(!task.isSuccessful()){

                            Toast.makeText(CommentsActivity.this, "Error Posting Comment : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        } else {

                            comment_field.setText("");

                        }

                    }
                });

            }
        });


        firebaseFirestore.collection("Posts").document(blog_post_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        descPost.setText("" + document.get("desc"));
                        String user_id_post = document.get("user_id").toString();

                        firebaseFirestore.collection("Users").document(user_id_post).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                if(task.isSuccessful()){

                                    String userName = task.getResult().getString("name");
                                    String userImage = task.getResult().getString("image");

                                    Glide.with(getApplicationContext()).load(userImage).into(thumbUserPost);
                                    userPost.setText(userName);

                                } else {

                                    //Firebase Exception

                                }

                            }
                        });

                    } else {
                        Toast.makeText(CommentsActivity.this, "No", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CommentsActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}