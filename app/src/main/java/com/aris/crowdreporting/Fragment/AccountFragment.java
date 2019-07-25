package com.aris.crowdreporting.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aris.crowdreporting.Activities.AboutUsActivity;
import com.aris.crowdreporting.HelperClasses.Blog;
import com.aris.crowdreporting.Activities.LoginActivity;
import com.aris.crowdreporting.Adapters.MyPhotoRecyclerAdapter;
import com.aris.crowdreporting.R;
import com.aris.crowdreporting.Activities.SetupActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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

public class AccountFragment extends DialogFragment implements
        GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG = "PROFILE_ACTIVITY";

    private CircularImageView profileImageV;
    private TextView usernameV, emailV, phoneV, emptyTxt;
    private ImageView emptyImg;
    private Uri mainImageUri;
    private LinearLayout edit, hapus;

    private String user_id;
    private String emailuser_id;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;

    private RecyclerView profileBlogListView;
    private List<Blog> blog_list;
    private MyPhotoRecyclerAdapter myPhotoRecyclerAdapter;

    private Location mylocation;
    private GoogleApiClient googleApiClient;
    private final static int REQUEST_CHECK_SETTINGS_GPS = 0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;

    private com.aris.crowdreporting.HelperUtils.Status getstatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // declare and initialize FrameLayout
        View frameLayout = (View) view.findViewById(R.id.lini);

        // initializing animation drawable by getting background from frame layout
        AnimationDrawable animationDrawable = (AnimationDrawable) frameLayout.getBackground();
        // setting enter fade animation duration to 5 seconds
        animationDrawable.setEnterFadeDuration(2500);

        // setting exit fade animation duration to 2 seconds
        animationDrawable.setExitFadeDuration(1000);

        animationDrawable.start();
        animationDrawable.run();

        Button popup = view.findViewById(R.id.options);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        user_id = firebaseAuth.getCurrentUser().getUid();
        emailuser_id = firebaseAuth.getCurrentUser().getEmail();

        profileImageV = (CircularImageView) view.findViewById(R.id.profile_image_view);
        usernameV = (TextView) view.findViewById(R.id.username_view);
        emailV = (TextView) view.findViewById(R.id.email_view);
        phoneV = (TextView) view.findViewById(R.id.phone_view);
        emptyTxt = view.findViewById(R.id.empty);
        emptyImg = view.findViewById(R.id.emptyimg);


        //USERNYA SIAPA
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        String emailku = task.getResult().getString("email");
                        String phoneku = task.getResult().getString("phone");

                        mainImageUri = Uri.parse(image);
                        usernameV.setText("" + name);
                        emailV.setText("" + emailku);
                        phoneV.setText("" + phoneku);

                        RequestOptions placeholderReq = new RequestOptions();
                        placeholderReq.placeholder(R.drawable.defaultimage);
                        try {
                            Glide.with(getContext()).setDefaultRequestOptions(placeholderReq).load(image).into(profileImageV);
                        } catch (Exception e) {
                            Log.d(TAG, e.getMessage());
                        }

                    }

                } else {

                    String errMSg = task.getException().getMessage();
                    Toast.makeText(getContext(), "" + errMSg, Toast.LENGTH_SHORT).show();

                }

            }
        });

        //PHOTO POST USERNYA
        ///Recyclerview
        blog_list = new ArrayList<>();
        profileBlogListView = view.findViewById(R.id.rv_images);
        myPhotoRecyclerAdapter = new MyPhotoRecyclerAdapter(getContext(), blog_list);
        profileBlogListView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        profileBlogListView.setAdapter(myPhotoRecyclerAdapter);

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
                            myPhotoRecyclerAdapter.notifyDataSetChanged();

                        }
                    }

                } else {
                    profileBlogListView.setVisibility(View.GONE);
                    emptyTxt.setVisibility(View.VISIBLE);
                    emptyImg.setVisibility(View.VISIBLE);
                }

            }

        });


        //POPUP
//        popup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                PopupMenu popupMenu = new PopupMenu(getActivity(), popup);
//                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_account, popupMenu.getMenu());
//
//                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//                        switch (item.getItemId()) {
//                            case R.id.keluar:
//                                signOut();
//                                return true;
//                            case R.id.edit_profile:
//                                Intent intent = new Intent(getActivity(), SetupActivity.class);
//                                startActivity(intent);
//                                return true;
//                            default:
//                                return false;
//                        }
//                    }
//                });
//
//                popupMenu.show();
//            }
//        });

        popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(getActivity());
                View sheetView = getActivity().getLayoutInflater().inflate(R.layout.fragment_bottom_sheet_options, null);
                mBottomSheetDialog.setContentView(sheetView);
                mBottomSheetDialog.show();

                LinearLayout edit = (LinearLayout) sheetView.findViewById(R.id.bottom_sheet_edit_profile);
                LinearLayout logout = (LinearLayout) sheetView.findViewById(R.id.bottom_sheet_logout);
                LinearLayout about = (LinearLayout) sheetView.findViewById(R.id.bottom_sheet_about);

                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), SetupActivity.class);
                        startActivity(intent);
                        mBottomSheetDialog.dismiss();

                    }
                });

                logout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signOut();
                        mBottomSheetDialog.dismiss();
                    }
                });

                about.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), AboutUsActivity.class);
                        startActivity(intent);
                        mBottomSheetDialog.dismiss();
                    }
                });

            }
        });


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure to EXIT ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (firebaseAuth.getCurrentUser().getUid() != null) {
                            getstatus = new com.aris.crowdreporting.HelperUtils.Status("offline");
                        }

                        FirebaseAuth.getInstance().signOut();

                        try {
                            if (mGoogleApiClient.isConnected()) {
                                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                                        new ResultCallback<Status>() {
                                            @Override
                                            public void onResult(Status status) {
                                                mGoogleApiClient.disconnect();
                                                //mGoogleApiClient.connect();
                                                // [START_EXCLUDE]
                                                FirebaseAuth.getInstance().signOut();
                                                Intent i = new Intent(getActivity(), LoginActivity.class);
                                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(i);

                                                // [END_EXCLUDE]
                                            }
                                        });
                            } else {
                                Toast.makeText(getActivity(), "err", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "" + e.getMessage());
                        }
                    }

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResume() {
        super.onResume();
        getstatus = new com.aris.crowdreporting.HelperUtils.Status("online");
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (firebaseAuth.getCurrentUser().getUid() != null) {
                getstatus = new com.aris.crowdreporting.HelperUtils.Status("offline");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (firebaseAuth.getCurrentUser().getUid() != null) {
            getstatus = new com.aris.crowdreporting.HelperUtils.Status("offline");
        }
    }


}