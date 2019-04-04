package com.aris.crowdreporting.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.aris.crowdreporting.Blog;
import com.aris.crowdreporting.NearRecyclerAdapter;
import com.aris.crowdreporting.Near;
import com.aris.crowdreporting.NearRecyclerAdapter;
import com.aris.crowdreporting.R;
import com.aris.crowdreporting.SortPlaces;
import com.aris.crowdreporting.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

public class NearFragment extends Fragment {

    private static final String TAG = "NearAct";
    private RecyclerView near_list_view;
    private List<Near> near_list;
    private List<User> user_list;
    private List<SortPlaces> sortPlaces;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private NearRecyclerAdapter nearRecyclerAdapter;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    private FusedLocationProviderClient client;
    LatLng latLng;

    public NearFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_near, container, false);

        near_list = new ArrayList<>();
        user_list = new ArrayList<>();

        near_list_view = view.findViewById(R.id.near_list_view);

        firebaseAuth = FirebaseAuth.getInstance();

        nearRecyclerAdapter = new NearRecyclerAdapter(near_list);
        near_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        near_list_view.setAdapter(nearRecyclerAdapter);
        near_list_view.setHasFixedSize(true);

        if(firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            near_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

//                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
//
//                    if(reachedBottom){
//
//                        loadMorePost();
//
//                    }

                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts");
            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {

                        if (isFirstPageFirstLoad) {

                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            near_list.clear();

                        }

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String nearPostId = doc.getDocument().getId();
                                Near nearPost = doc.getDocument().toObject(Near.class).withId(nearPostId);


                                if (isFirstPageFirstLoad) {

//                                    double lat = -6.252196;
//                                    double lng = 107.002395;

                                    client = LocationServices.getFusedLocationProviderClient(getContext());

                                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                        return;
                                    }
                                    client.getLastLocation().addOnSuccessListener((Activity) getContext(), new OnSuccessListener<Location>() {
                                                @Override
                                                public void onSuccess(Location location) {

                                                    if (location != null) {

                                                        near_list.add(nearPost);


                                                        Double lat_a = location.getLatitude();
                                                        Double lng_a = location.getLongitude();

                                                        Collections.sort(near_list, new SortPlaces(lat_a, lng_a));
                                                        nearRecyclerAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                            });

                                } else {

                                    near_list.add(0, nearPost);
                                    nearRecyclerAdapter.notifyDataSetChanged();

                                }

                            }
                        }

                        isFirstPageFirstLoad = false;

                    }

                }

            });

        }

        // Inflate the layout for this fragment
        return view;
    }

    public void loadMorePost(){

        if(firebaseAuth.getCurrentUser() != null) {

            Query nextQuery = firebaseFirestore.collection("Posts")

                    .startAfter(lastVisible)
                    .limit(5);

            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {

                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String nearPostId = doc.getDocument().getId();
                                Near nearPost = doc.getDocument().toObject(Near.class).withId(nearPostId);
                                String blogUserId = doc.getDocument().getString("user_id");

                                nearRecyclerAdapter.notifyItemInserted(near_list.size());



                            }

                        }
                    }

                }
            });

        }

    }


}