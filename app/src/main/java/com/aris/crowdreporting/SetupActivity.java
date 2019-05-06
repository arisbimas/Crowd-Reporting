package com.aris.crowdreporting;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView setupImage;
    private Uri mainImageUri;

    private EditText setupName, setupEmail, setupPhone;
    private ImageButton setupBtn;
    private ProgressBar setupProgress;
    private String user_id;
    private String emailuser_id;

    private boolean is_Changed = false;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private static final int READCODE = 1;
    private static final int WRITECODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        user_id = firebaseAuth.getCurrentUser().getUid();
        emailuser_id = firebaseAuth.getCurrentUser().getEmail();

        setupImage =  findViewById(R.id.setup_image);
        setupName = (EditText)findViewById(R.id.setup_name);
        setupEmail = (EditText)findViewById(R.id.setup_email);
        setupPhone = (EditText)findViewById(R.id.setup_phone);
        setupBtn = (ImageButton) findViewById(R.id.btn_setup);
//        setupProgress = (ProgressBar)findViewById(R.id.progressBar);

//        setupProgress.setVisibility(View.VISIBLE);
        AlertDialog dialog = new SpotsDialog(SetupActivity.this);
        dialog.show();

        setupBtn.setEnabled(false);
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){

                    if (task.getResult().exists()){

                        String name = task.getResult().getString("name");
                        String phone = task.getResult().getString("phone");
                        String image = task.getResult().getString("image");

                        mainImageUri = Uri.parse(image);
                        setupName.setText(name);
                        setupEmail.setText(emailuser_id);
                        setupPhone.setText(phone);

                        RequestOptions placeholderReq = new RequestOptions();
                        placeholderReq.placeholder(R.drawable.defaultimage);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderReq).load(image).into(setupImage);

                    }

                } else {

                    String errMSg = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, ""+ errMSg, Toast.LENGTH_SHORT).show();

                }
//                setupProgress.setVisibility(View.INVISIBLE);
                dialog.dismiss();
                setupBtn.setEnabled(true);

            }
        });


        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String username = setupName.getText().toString();
                final String phone = setupPhone.getText().toString();

                if (!TextUtils.isEmpty(username) && mainImageUri != null) {
//                    setupProgress.setVisibility(View.VISIBLE);
                    dialog.show();


                    if(is_Changed) {

                        user_id = firebaseAuth.getCurrentUser().getUid();

                        StorageReference image_path = storageReference.child("profile_images").child(user_id + ".jpg");

                        UploadTask uploadTask = image_path.putFile(mainImageUri);
                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                return image_path.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {

                                    storeFirestore(task, username, emailuser_id, phone);

                                } else {

                                    String errMsg = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, "Image Error " + errMsg, Toast.LENGTH_SHORT).show();

//                                    setupProgress.setVisibility(View.INVISIBLE);
                                    dialog.dismiss();
                                }
                            }
                        });

                    }else {

                        storeFirestore(null, username, emailuser_id, phone);

                    }
                } else {
                    Toast.makeText(SetupActivity.this, "Pilih Gambar untuk Profile Image", Toast.LENGTH_SHORT).show();
                }

            }
        });


        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){

                    if (ContextCompat.checkSelfPermission(SetupActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)!=
                            PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(SetupActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                            PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(SetupActivity.this, "Please grant permissions", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},READCODE);
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITECODE);
                    } else {
                        BringImagePicker();
                    }
                } else {
                    BringImagePicker();
                }

            }
        });

    }

    private void storeFirestore(@NonNull Task<Uri> task, String username, String emailuser_id, String phone) {

        AlertDialog dialog = new SpotsDialog(SetupActivity.this);

        Uri downloadUri;

        if (task != null) {

            downloadUri = task.getResult();

        } else {

            downloadUri = mainImageUri;

        }

        Map<String, String> userMap = new HashMap<>();
        userMap.put("name" , username);
        userMap.put("email" , emailuser_id);
        userMap.put("phone" , phone);
        userMap.put("image", downloadUri.toString());

        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

//                    setupProgress.setVisibility(View.INVISIBLE);
                    dialog.show();

                    Toast.makeText(SetupActivity.this, "The User Setting are Updated", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {

                    String errMSg = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, ""+ errMSg, Toast.LENGTH_SHORT).show();

                }
//                setupProgress.setVisibility(View.INVISIBLE);
                dialog.dismiss();

            }
        });
    }

    private void BringImagePicker() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetupActivity.this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageUri = result.getUri();
                setupImage.setImageURI(mainImageUri);

                is_Changed = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
