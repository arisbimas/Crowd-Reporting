package com.aris.crowdreporting.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.aris.crowdreporting.Activities.DetailActivity;
import com.aris.crowdreporting.Adapters.NearRecyclerAdapter;
import com.aris.crowdreporting.CheckLoc.CheckLoc;
import com.aris.crowdreporting.HelperClasses.Near;
import com.aris.crowdreporting.R;
import com.aris.crowdreporting.HelperUtils.SortPlaces;
import com.aris.crowdreporting.HelperClasses.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Date;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class NearFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "NearAct";
    private RecyclerView near_list_view;
    private TextView txtEmptyPost, txtGps;
    private ImageView icnGps;
    private List<Near> near_list;
    private List<User> user_list;
    private List<SortPlaces> sortPlaces;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private NearRecyclerAdapter nearRecyclerAdapter;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    private Location mylocation;
    private LocationManager locationManager;
    private GoogleApiClient googleApiClient;
    private LocationRequest mLocationRequest;

    private final static int REQUEST_CHECK_SETTINGS_GPS = 0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;

    private SpotsDialog dialog;

    private FusedLocationProviderClient client;
    LatLng latLng;

    private Context mContex;

    private Date cDate;

    private com.aris.crowdreporting.HelperUtils.Status status;
    private SwipeRefreshLayout pullToRefresh;

    public NearFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_near, container, false);
        setHasOptionsMenu(true);

        mContex = getContext();

        near_list = new ArrayList<>();
        user_list = new ArrayList<>();

        mLocationRequest = new LocationRequest();

        near_list_view = view.findViewById(R.id.near_list_view);
        txtEmptyPost = view.findViewById(R.id.empty_post);
//        txtGps = view.findViewById(R.id.aktifkan_gps);
        icnGps = view.findViewById(R.id.icongps);

        pullToRefresh = view.findViewById(R.id.pull);
        pullToRefresh.setColorSchemeColors(Color.CYAN, Color.YELLOW, Color.MAGENTA);

        firebaseAuth = FirebaseAuth.getInstance();
        client = LocationServices.getFusedLocationProviderClient(mContex);

        dialog = new SpotsDialog(mContex, "Track Your Location, Please Wait!");

        nearRecyclerAdapter = new NearRecyclerAdapter(near_list);
        near_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        near_list_view.setAdapter(nearRecyclerAdapter);
        near_list_view.setHasFixedSize(true);

        if (firebaseAuth.getCurrentUser() != null) {

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

            nearQuery();

            pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    nearRecyclerAdapter.notifyDataSetChanged();
                    pullToRefresh.setRefreshing(false);
                }
            });

        }

        // Inflate the layout for this fragment
        return view;
    }

    private void nearQuery() {
        //FORMAT bln/tgl/thn untuk firestore emg gitu
        Date date = new Date();

        //HARI INI
        long secs = date.getTime();
        cDate = new Date(secs);

        //7 hari yg lalu
        // 7hari yang lalu
        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        long secs2 = date.getTime() - (7 * DAY_IN_MS);
        Date daysAgo = new Date(secs2);

        Query nearQuery = firebaseFirestore.collection("Posts")
                .whereEqualTo("reports", "false")
                .whereLessThanOrEqualTo("timestamp", cDate)
                .whereGreaterThanOrEqualTo("timestamp", daysAgo);

        nearQuery.addSnapshotListener( new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()) {

                    if (isFirstPageFirstLoad) {

                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        near_list.clear();

                    }

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String nearPostId = doc.getDocument().getId();
                            Near nearPost = doc.getDocument().toObject(Near.class).withId(nearPostId);


                            if (ActivityCompat.checkSelfPermission((Activity) mContex, Manifest.permission.ACCESS_FINE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission((Activity) mContex,
                                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            } else {
                                Log.d("MYLOG ", "ERR");
                            }

                            client.getLastLocation().addOnSuccessListener((Activity) mContex, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {

                                    if (location != null) {

                                        if (nearPost.getDesc().contains("bekasi")) {

                                            txtEmptyPost.setVisibility(View.GONE);
                                            icnGps.setVisibility(View.GONE);

                                            near_list.add(nearPost);
                                            Double lat_a = location.getLatitude();
                                            Double lng_a = location.getLongitude();
                                            Collections.sort(near_list, new SortPlaces(lat_a, lng_a));
                                            nearRecyclerAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            });

                        }
                    }

                } else {
                    //JIKA DALAM SEMINGGU TERAKHIR TIDAK ADA POSTS
                    near_list_view.setVisibility(View.GONE);
                    txtEmptyPost.setText("No Post In This Week");
                    txtEmptyPost.setVisibility(View.VISIBLE);
                    icnGps.setVisibility(View.GONE);
                }

            }

        });
    }

//    public void loadMorePost() {
//
//        if (firebaseAuth.getCurrentUser() != null) {
//
//            Query nextQuery = firebaseFirestore.collection("Posts")
//
//                    .startAfter(lastVisible)
//                    .limit(5);
//
//            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
//                @Override
//                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//
//                    if (!documentSnapshots.isEmpty()) {
//
//                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
//                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
//
//                            if (doc.getType() == DocumentChange.Type.ADDED) {
//
//                                String nearPostId = doc.getDocument().getId();
//                                Near nearPost = doc.getDocument().toObject(Near.class).withId(nearPostId);
//                                String blogUserId = doc.getDocument().getString("user_id");
//
//                                nearRecyclerAdapter.notifyItemInserted(near_list.size());
//
//
//                            }
//
//                        }
//                    }
//
//                }
//            });
//
//        }
//
//    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar2, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.locmu) {
//            Intent intent = new Intent(getActivity(), CheckLoc.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);

            setUpGClient();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (googleApiClient != null)
            googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.stopAutoManage((FragmentActivity) getActivity());
            googleApiClient.disconnect();
            status = new com.aris.crowdreporting.HelperUtils.Status("offline");
        }
    }

    private synchronized void setUpGClient() {
        googleApiClient = new GoogleApiClient.Builder(mContex)
                .enableAutoManage(getActivity(), 1, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

        dialog.show();
    }


    @Override
    public void onLocationChanged(Location location) {
        mylocation = location;
        if (mylocation != null) {
            nearRecyclerAdapter.notifyDataSetChanged();
//            Toast.makeText(mContex, "Get Location", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            near_list.clear();
            nearQuery();

        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        checkPermissions();
        if (ActivityCompat.checkSelfPermission(mContex, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContex, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

//        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Do whatever you need
        //You can display a message here
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //You can display a message here
    }

//    protected void startLocationUpdates() {
//        if (ActivityCompat.checkSelfPermission(mContex, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(mContex, Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        LocationServices.FusedLocationApi.requestLocationUpdates(
//                googleApiClient, mLocationRequest, this);
//    }

    private void getMyLocation() {
        if (googleApiClient != null) {
            if (googleApiClient.isConnected()) {
                int permissionLocation = ContextCompat.checkSelfPermission(mContex,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    mylocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setInterval(10 * 1000);
                    locationRequest.setFastestInterval(5 * 1000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);
//                    LocationServices.FusedLocationApi
//                            .requestLocationUpdates(googleApiClient, locationRequest, this);
                    LocationServices.getFusedLocationProviderClient((Activity)mContex).requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            // do work here
                            onLocationChanged(locationResult.getLastLocation());
                        }
                    }, Looper.myLooper());
                    PendingResult<LocationSettingsResult> result =
                            LocationServices.SettingsApi
                                    .checkLocationSettings(googleApiClient, builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final Status status = result.getStatus();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    // All location settings are satisfied.
                                    // You can initialize location requests here.
                                    int permissionLocation = ContextCompat
                                            .checkSelfPermission(mContex,
                                                    Manifest.permission.ACCESS_FINE_LOCATION);
                                    if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                                        mylocation = LocationServices.FusedLocationApi
                                                .getLastLocation(googleApiClient);
                                    }
                                    break;
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    // Location settings are not satisfied.
                                    // But could be fixed by showing the user a dialog.
                                    try {
                                        // Show the dialog by calling startResolutionForResult(),
                                        // and check the result in onActivityResult().
                                        // Ask to turn on GPS automatically
                                        status.startResolutionForResult(getActivity(),
                                                REQUEST_CHECK_SETTINGS_GPS);
                                    } catch (IntentSender.SendIntentException e) {
                                        // Ignore the error.
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    // Location settings are not satisfied.
                                    // However, we have no way
                                    // to fix the
                                    // settings so we won't show the dialog.
                                    // finish();
                                    break;
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS_GPS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getMyLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
        }
    }

    private void checkPermissions() {
        int permissionLocation = ContextCompat.checkSelfPermission(mContex,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(getActivity(),
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
        } else {
            getMyLocation();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        int permissionLocation = ContextCompat.checkSelfPermission(mContex,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            getMyLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (googleApiClient != null) {
            googleApiClient.stopAutoManage(getActivity());
            googleApiClient.disconnect();
            status = new com.aris.crowdreporting.HelperUtils.Status("offline");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        status = new com.aris.crowdreporting.HelperUtils.Status("online");
    }
}