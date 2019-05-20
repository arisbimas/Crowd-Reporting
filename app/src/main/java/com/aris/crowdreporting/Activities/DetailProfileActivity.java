package com.aris.crowdreporting.Activities;

import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aris.crowdreporting.Adapters.MyPhotoRecyclerAdapter;
import com.aris.crowdreporting.HelperClasses.Blog;
import com.aris.crowdreporting.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

public class DetailProfileActivity extends AppCompatActivity {

    private final static String TAG = "PROFILE_ACTIVITY";

    private Toolbar profileToolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;

    private CircularImageView profileImageV;
    private TextView usernameV, emailV, phoneV;
    private Uri mainImageUri;

    private String user_id;
    private String emailuser_id;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;

    private RecyclerView profileBlogListView;
    private List<Blog> blog_list;
    private MyPhotoRecyclerAdapter profileBlogRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_profile);

        profileToolbar = findViewById(R.id.detail_profile_toolbar);
        setSupportActionBar(profileToolbar);
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");

        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        collapsingToolbarLayout.setElevation(10.0f);
        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(DetailProfileActivity.this,R.color.putih));


        user_id = getIntent().getStringExtra("user_id");
        Toast.makeText(this, ""+user_id, Toast.LENGTH_SHORT).show();

        // declare and initialize FrameLayout
        View frameLayout = (View)findViewById(R.id.lini);

        // initializing animation drawable by getting background from frame layout
        AnimationDrawable animationDrawable = (AnimationDrawable) frameLayout.getBackground();
        // setting enter fade animation duration to 5 seconds
        animationDrawable.setEnterFadeDuration(2500);

        // setting exit fade animation duration to 2 seconds
        animationDrawable.setExitFadeDuration(1000);

        animationDrawable.start();
        animationDrawable.run();

        Button popup = findViewById(R.id.options);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        emailuser_id = firebaseAuth.getCurrentUser().getEmail();

        profileImageV =  (CircularImageView)findViewById(R.id.profile_image_view);
        usernameV = (TextView)findViewById(R.id.username_view);
        emailV = (TextView)findViewById(R.id.email_view);
        phoneV = (TextView)findViewById(R.id.phone_view);

        //USERNYA SIAPA
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){

                    if (task.getResult().exists()){

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        String emailku = task.getResult().getString("email");
                        String phoneku = task.getResult().getString("phone");

                        mainImageUri = Uri.parse(image);
                        usernameV.setText(""+name);
                        emailV.setText(""+emailku);
                        phoneV.setText(""+phoneku);

                        RequestOptions placeholderReq = new RequestOptions();
                        placeholderReq.placeholder(R.drawable.defaultimage);
                        Glide.with(DetailProfileActivity.this).setDefaultRequestOptions(placeholderReq).load(image).into(profileImageV);

                        appBarLayout = findViewById(R.id.appbar);

                        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                            @Override
                            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                                    // Collapsed
                                    actionBar.setDisplayHomeAsUpEnabled(true);
                                    actionBar.setTitle(name);
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
                    }

                } else {

                    String errMSg = task.getException().getMessage();
                    Toast.makeText(DetailProfileActivity.this, ""+ errMSg, Toast.LENGTH_SHORT).show();

                }

            }
        });

        //PHOTO POST USERNYA
        ///Recyclerview
        blog_list = new ArrayList<>();
        profileBlogListView = findViewById(R.id.rv_images);
        profileBlogRecyclerAdapter = new MyPhotoRecyclerAdapter(DetailProfileActivity.this,blog_list);
        profileBlogListView.setLayoutManager(new GridLayoutManager(DetailProfileActivity.this,3));
        profileBlogListView.setAdapter(profileBlogRecyclerAdapter);

        Query firstQuery = firebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .whereEqualTo("user_id", user_id);
        firstQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()) {

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String blogPostId = doc.getDocument().getId();
                            Blog blogPost = doc.getDocument().toObject(Blog.class).withId(blogPostId);
                            blog_list.add(blogPost);
                            profileBlogRecyclerAdapter.notifyDataSetChanged();

                        }
                    }

                }

            }

        });
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
}
