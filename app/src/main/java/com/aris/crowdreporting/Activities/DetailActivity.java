package com.aris.crowdreporting.Activities;

import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.aris.crowdreporting.Adapters.CommentsRecyclerAdapter;
import com.aris.crowdreporting.HelperClasses.Comments;
import com.aris.crowdreporting.HelperUtils.Status;
import com.aris.crowdreporting.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.annotations.Until;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailsActivity";

    //private android.support.v7.widget.Toolbar mToolbar;
    private KenBurnsView blogImage;
    private AppBarLayout appBarLayout;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private Toolbar mToolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private String blogTime;
    private String mTitle = "";
    private Toolbar commentToolbar;

    private EditText comment_field;
    private ImageView comment_post_btn, thumbUserPost;
    private TextView descPost, userPost, timeAgo;

    private RecyclerView comment_list;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private List<Comments> commentsList;
    private String currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mToolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(mToolbar);
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");

        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        collapsingToolbarLayout.setElevation(10.0f);
        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(DetailActivity.this,R.color.putih));

        appBarLayout = findViewById(R.id.appbar);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                    // Collapsed
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    actionBar.setTitle("Detail");
                } else if (verticalOffset == 0) {
                    // Expanded
                    actionBar.setDisplayHomeAsUpEnabled(false);
                    actionBar.setTitle("");
                    collapsingToolbarLayout.setTitle("");
                } else {
                    actionBar.setDisplayHomeAsUpEnabled(false);
                    actionBar.setTitle("");
                    collapsingToolbarLayout.setTitle("");
                    // Somewhere in between
                }
            }
        });

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        currentUserId = firebaseAuth.getCurrentUser().getUid();

        //GET EXTRA FROM BLOGRECVIEW
        final String blogPostId = getIntent().getStringExtra("blog_id");
        final String blogUserId = getIntent().getStringExtra("user_id");
        final String imgUrl = getIntent().getStringExtra("imurl");
        final String time = getIntent().getStringExtra("time_post");
        final String desc = getIntent().getStringExtra("desc");

        blogImage = findViewById(R.id.detail_image);
        userPost = findViewById(R.id.userpost);
        descPost = findViewById(R.id.descpost);
        thumbUserPost = findViewById(R.id.thumb_userpost);
        timeAgo = findViewById(R.id.time_post);

        comment_field = findViewById(R.id.isi_komen);
        comment_post_btn = findViewById(R.id.kirim_komen);
        comment_list = findViewById(R.id.list_comment);

        //RecyclerView Firebase List
        commentsList = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList);
        comment_list.setHasFixedSize(true);
        comment_list.setLayoutManager(new LinearLayoutManager(this));
        comment_list.setAdapter(commentsRecyclerAdapter);


        //SHOW COMMENTS
        firebaseFirestore.collection("Posts/" + blogPostId + "/Comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(DetailActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (!documentSnapshots.isEmpty()) {

                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    Comments comments = doc.getDocument().toObject(Comments.class);
                                    commentsList.add(comments);
                                    commentsRecyclerAdapter.notifyItemChanged(0);
                                    commentsRecyclerAdapter.notifyDataSetChanged();

                                }
                            }

                        }

                    }
                });

        //Tekan Btn Comment
        comment_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String comment_message = comment_field.getText().toString();

                if (TextUtils.isEmpty(comment_message)){
                    Toast.makeText(DetailActivity.this, "Give some message.", Toast.LENGTH_SHORT).show();
                } else {

                    AlertDialog dialog = new SpotsDialog(DetailActivity.this);
                    dialog.show();

                    Map<String, Object> commentsMap = new HashMap<>();
                    commentsMap.put("message", comment_message);
                    commentsMap.put("user_id", currentUserId);
                    commentsMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            if(!task.isSuccessful()){
                                dialog.dismiss();
                                Toast.makeText(DetailActivity.this, "Error Posting Comment : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(DetailActivity.this, "Comment posted", Toast.LENGTH_SHORT).show();
                                comment_field.setText("");
                                dialog.dismiss();

                            }

                        }
                    });
                }

            }
        });


        //SHOW DETAIL POS
        //set Desc Post
        descPost.setText(desc);
        //set time post
        timeAgo.setText(time);
        //Set Image Post
        RequestOptions placeholderReq = new RequestOptions();
        placeholderReq.placeholder(R.drawable.rectangle);
        Glide.with(getApplicationContext()).applyDefaultRequestOptions(placeholderReq).load(imgUrl).into(blogImage);

        //Set Image User Post
        firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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

//        firebaseFirestore.collection("Posts").document(blogPostId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//
//                String imgpost = documentSnapshot.getString("image_uri");
//                String desc = documentSnapshot.getString("desc");
//                String imgthumb = documentSnapshot.getString("image_thumb");
//                String user_id = documentSnapshot.getString("user_id");
//
//                if (!documentSnapshot.exists()){
//
//                    Toast.makeText(getApplicationContext(), "error get data", Toast.LENGTH_SHORT).show();
//
//                } else {
//
//                }
//
//            }
//        });


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Status status;
    @Override
    public void onResume() {
        super.onResume();
        status = new Status("online");
    }

    @Override
    public void onPause() {
        super.onPause();
        status = new Status("offline");
    }

    @Override
    public void onStop() {
        super.onStop();
        status = new Status("offline");
    }
}
