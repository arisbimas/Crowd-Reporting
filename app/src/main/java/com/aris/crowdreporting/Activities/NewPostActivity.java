package com.aris.crowdreporting.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.location.Location;

import com.aris.crowdreporting.R;
import com.google.android.gms.location.LocationListener;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.developers.imagezipper.ImageZipper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;

public class NewPostActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private ImageButton mSelectImage;
    private EditText mLati, mLongi;
    private EditText mDesc;
    private Button mSubmit;
    private Toolbar newPostToolbar;
    private ProgressBar progressBar;

    private Uri postImageUri = null;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;
    private Bitmap imageBitmap;

    private Location mylocation;
    private GoogleApiClient googleApiClient;
    private final static int REQUEST_CHECK_SETTINGS_GPS=0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS=0x2;

    private AlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        newPostToolbar = findViewById(R.id.comment_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        newPostToolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.putih), PorterDuff.Mode.SRC_ATOP);


        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        mSelectImage = (ImageButton)findViewById(R.id.post_image);
        mLati = (EditText)findViewById(R.id.post_lati);
        mLongi = (EditText)findViewById(R.id.post_longi);
        mDesc = (EditText)findViewById(R.id.post_desc);

//        progressBar = (ProgressBar)findViewById(R.id.new_post_progress);
        dialog = new SpotsDialog(NewPostActivity.this, "Track Your Location");


        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(NewPostActivity.this);

            }
        });

        setUpGClient();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        AlertDialog dialog = new SpotsDialog(NewPostActivity.this, "Uploading...");


        //noinspection SimplifiableIfStatement
        if (id == R.id.postit) {

            String latitude = mLati.getText().toString();
            String longitude = mLongi.getText().toString();
            String desc = mDesc.getText().toString().toLowerCase();

            if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude) && !TextUtils.isEmpty(desc) && postImageUri != null && desc.contains("bekasi")){

//                progressBar.setVisibility(View.VISIBLE);
                dialog.show();

                String randomName = UUID.randomUUID().toString();

                StorageReference file_path = storageReference.child("post_image").child(randomName + ".jpg");
                UploadTask uploadTask = file_path.putFile(postImageUri);
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return file_path.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {

                            File newImageFile = new File(postImageUri.getPath());


                            try {
                                File imageZipperFile=new ImageZipper(NewPostActivity.this)
                                        .setQuality(10)
                                        .setMaxWidth(200)
                                        .setMaxHeight(200)
                                        .compressToFile(newImageFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                 imageBitmap =new ImageZipper(NewPostActivity.this).compressToBitmap(newImageFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                            byte[] thumbData = baos.toByteArray();

                            final StorageReference ref = storageReference.child("post_image/thumbs").child(randomName + ".jpg");
                            UploadTask uploadTask = ref.putBytes(thumbData);

                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }

                                    // Continue with the task to get the download URL
                                    return ref.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> taskThumb) {
                                    if (taskThumb.isSuccessful()) {
                                        Uri downloadUri = task.getResult();
                                        Uri downloadUriThumb = taskThumb.getResult();
                                        String imageUri = downloadUri.toString();
                                        String imageUriThumb = downloadUriThumb.toString();
                                        String lati = latitude.toString();
                                        String longi = longitude.toString();
                                        String des = desc.toString();


                                        Map<String, Object> postMap = new HashMap<>();
                                        postMap.put("image_uri", imageUri);
                                        postMap.put("image_thumb", imageUriThumb);
                                        postMap.put("latitude", lati);
                                        postMap.put("longitude", longi);
                                        postMap.put("desc", des);
                                        postMap.put("user_id", current_user_id);
                                        postMap.put("timestamp", FieldValue.serverTimestamp());
                                        postMap.put("reports", "false");

                                        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if (task.isSuccessful()){

                                                    Toast.makeText(NewPostActivity.this, "Post was Added", Toast.LENGTH_SHORT).show();
                                                    Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                                    startActivity(mainIntent);
                                                    finish();

                                                } else {

                                                }

//                                                progressBar.setVisibility(View.INVISIBLE);
                                                dialog.dismiss();

                                            }
                                        });

                                    } else {
                                        // Handle failures
                                        // ...
                                    }
                                }
                            });


                        } else {

                            String errMsg = task.getException().getMessage();
                            Toast.makeText(NewPostActivity.this, "Image Error " + errMsg, Toast.LENGTH_SHORT).show();

//                            progressBar.setVisibility(View.INVISIBLE);
                            dialog.dismiss();
                        }
                    }
                });

            }
            //JIKA TIDAK ADA KATA BEKASI
            else if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude) && !TextUtils.isEmpty(desc) && postImageUri != null && !desc.contains("bekasi")){

//                progressBar.setVisibility(View.VISIBLE);
                dialog.show();

                String randomName = UUID.randomUUID().toString();

                StorageReference file_path = storageReference.child("post_image_trash").child(randomName + ".jpg");
                UploadTask uploadTask = file_path.putFile(postImageUri);
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return file_path.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {

                            File newImageFile = new File(postImageUri.getPath());


                            try {
                                File imageZipperFile=new ImageZipper(NewPostActivity.this)
                                        .setQuality(10)
                                        .setMaxWidth(200)
                                        .setMaxHeight(200)
                                        .compressToFile(newImageFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                imageBitmap =new ImageZipper(NewPostActivity.this).compressToBitmap(newImageFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                            byte[] thumbData = baos.toByteArray();

                            final StorageReference ref = storageReference.child("post_image_trash/thumbs").child(randomName + ".jpg");
                            UploadTask uploadTask = ref.putBytes(thumbData);

                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }

                                    // Continue with the task to get the download URL
                                    return ref.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> taskThumb) {
                                    if (taskThumb.isSuccessful()) {
                                        Uri downloadUri = task.getResult();
                                        Uri downloadUriThumb = taskThumb.getResult();
                                        String imageUri = downloadUri.toString();
                                        String imageUriThumb = downloadUriThumb.toString();
                                        String lati = latitude.toString();
                                        String longi = longitude.toString();
                                        String des = desc.toString();


                                        Map<String, Object> postMap = new HashMap<>();
                                        postMap.put("image_uri", imageUri);
                                        postMap.put("image_thumb", imageUriThumb);
                                        postMap.put("latitude", lati);
                                        postMap.put("longitude", longi);
                                        postMap.put("desc", des);
                                        postMap.put("user_id", current_user_id);
                                        postMap.put("timestamp", FieldValue.serverTimestamp());
                                        postMap.put("reports", "false");

                                        firebaseFirestore.collection("PostsTrash").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if (task.isSuccessful()){

                                                    Toast.makeText(NewPostActivity.this, "Post was Added", Toast.LENGTH_SHORT).show();
                                                    Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                                    startActivity(mainIntent);
                                                    finish();

                                                } else {

                                                    Toast.makeText(NewPostActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                                }

//                                                progressBar.setVisibility(View.INVISIBLE);
                                                dialog.dismiss();

                                            }
                                        });

                                    } else {
                                        // Handle failures
                                        // ...
                                    }
                                }
                            });


                        } else {

                            String errMsg = task.getException().getMessage();
                            Toast.makeText(NewPostActivity.this, "Image Error " + errMsg, Toast.LENGTH_SHORT).show();

//                            progressBar.setVisibility(View.INVISIBLE);
                            dialog.dismiss();
                        }
                    }
                });

            } else if (TextUtils.isEmpty(latitude)){
                Toast.makeText(NewPostActivity.this, "Lokasi Tidak Terditeksi", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(longitude)){
                Toast.makeText(NewPostActivity.this, "Lokasi Tidak Terditeksi", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(desc)){
                Toast.makeText(NewPostActivity.this, "Deskripsi harus diisi!", Toast.LENGTH_SHORT).show();
            } else if (postImageUri == null){
                Toast.makeText(NewPostActivity.this, "Harus disertakan Foto", Toast.LENGTH_SHORT).show();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private synchronized void setUpGClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
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
            Double latitude=mylocation.getLatitude();
            Double longitude=mylocation.getLongitude();
            mLati.setText(""+latitude);
            mLongi.setText(""+longitude);
            //Or Do whatever you want with your location
            dialog.dismiss();

        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        checkPermissions();
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

    private void getMyLocation(){
        if(googleApiClient!=null) {
            if (googleApiClient.isConnected()) {
                int permissionLocation = ContextCompat.checkSelfPermission(NewPostActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    mylocation =                     LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setInterval(3000);
                    locationRequest.setFastestInterval(3000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);
                    LocationServices.FusedLocationApi
                            .requestLocationUpdates(googleApiClient, locationRequest, this);
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
                                            .checkSelfPermission(NewPostActivity.this,
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
                                        status.startResolutionForResult(NewPostActivity.this,
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS_GPS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getMyLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        finish();
                        break;
                }
                break;
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();

                mSelectImage.setImageURI(postImageUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void checkPermissions(){
        int permissionLocation = ContextCompat.checkSelfPermission(NewPostActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
        }else{
            getMyLocation();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        int permissionLocation = ContextCompat.checkSelfPermission(NewPostActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            getMyLocation();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        googleApiClient.stopAutoManage(this);
        googleApiClient.disconnect();
    }

}
