package com.aris.crowdreporting.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aris.crowdreporting.Blog;
import com.aris.crowdreporting.LoginActivity;
import com.aris.crowdreporting.MainActivity;
import com.aris.crowdreporting.MyPhotoRecyclerAdapter;
import com.aris.crowdreporting.R;
import com.aris.crowdreporting.SetupActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import javax.annotation.Nullable;

import static android.support.constraint.Constraints.TAG;

public class AccountFragment extends DialogFragment implements
        GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG = "PROFILE_ACTIVITY";

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

    private Location mylocation;
    private GoogleApiClient googleApiClient;
    private final static int REQUEST_CHECK_SETTINGS_GPS=0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS=0x2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_account, container, false);

        // declare and initialize FrameLayout
        LinearLayout frameLayout = (LinearLayout) view.findViewById(R.id.lini);

        // initializing animation drawable by getting background from frame layout
        AnimationDrawable animationDrawable = (AnimationDrawable) frameLayout.getBackground();
        // setting enter fade animation duration to 5 seconds
        animationDrawable.setEnterFadeDuration(2500);

        // setting exit fade animation duration to 2 seconds
        animationDrawable.setExitFadeDuration(1000);

        animationDrawable.start();
        animationDrawable.run();

        ImageView popup = view.findViewById(R.id.popup_acc);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        user_id = firebaseAuth.getCurrentUser().getUid();
        emailuser_id = firebaseAuth.getCurrentUser().getEmail();

        profileImageV =  (CircularImageView)view.findViewById(R.id.profile_image_view);
        usernameV = (TextView)view.findViewById(R.id.username_view);
        emailV = (TextView)view.findViewById(R.id.email_view);
        phoneV = (TextView)view.findViewById(R.id.phone_view);

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
                        Glide.with(getContext()).setDefaultRequestOptions(placeholderReq).load(image).into(profileImageV);

                    }

                } else {

                    String errMSg = task.getException().getMessage();
                    Toast.makeText(getContext(), ""+ errMSg, Toast.LENGTH_SHORT).show();

                }

            }
        });

        //PHOTO POST USERNYA
        ///Recyclerview
        blog_list = new ArrayList<>();
        profileBlogListView = view.findViewById(R.id.rv_images);
        profileBlogRecyclerAdapter = new MyPhotoRecyclerAdapter(getContext(),blog_list);
        profileBlogListView.setLayoutManager(new GridLayoutManager(getContext(),3));
        profileBlogListView.setAdapter(profileBlogRecyclerAdapter);

        Query firstQuery = firebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .whereEqualTo("user_id", user_id);
        firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
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


        //POPUP
        popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu = new PopupMenu(getActivity(), popup);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_account, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.keluar:
                                signOut();
                                return true;
                            case R.id.edit_profile:
                                Intent intent = new Intent(getActivity(), SetupActivity.class);
                                startActivity(intent);
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                popupMenu.show();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected()){
            try {
                mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                        .enableAutoManage(getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return view;
    }


    //sign out method
    public void signOut() {

        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        mGoogleApiClient.disconnect();
                        mGoogleApiClient.connect();
                        // [START_EXCLUDE]
                        firebaseAuth.getInstance().signOut();
                        Intent i = new Intent(getActivity(),
                                LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        // [END_EXCLUDE]
                    }
                });


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}