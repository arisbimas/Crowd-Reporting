package com.aris.crowdreporting.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aris.crowdreporting.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.developers.imagezipper.ImageZipper;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class EditPostActivity extends AppCompatActivity {

    private ImageButton mSelectImage;
    private EditText mLati, mLongi;
    private EditText mDesc;
    private Button mSubmit;
    private Toolbar newPostToolbar;
    private Uri mainImageUri;
    private Uri mainImageUriThumb;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;
    private String blogPostId, blogUserId;
    private Bitmap imageBitmap;
    private SpotsDialog dialog;
    private boolean is_Changed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        newPostToolbar = findViewById(R.id.edit_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Edit Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        newPostToolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.putih), PorterDuff.Mode.SRC_ATOP);


        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        mSelectImage = (ImageButton) findViewById(R.id.post_image);
        mDesc = (EditText) findViewById(R.id.post_desc);

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        dialog = new SpotsDialog(EditPostActivity.this);

        blogPostId = getIntent().getStringExtra("blog_id");
        blogUserId = getIntent().getStringExtra("user_id");

        dialog.show();
        firebaseFirestore.collection("Posts").document(blogPostId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {

                        String desc = task.getResult().getString("desc");
                        String image = task.getResult().getString("image_uri");
                        String image_thumb = task.getResult().getString("image_thumb");

                        mainImageUri = Uri.parse(image);
                        mainImageUriThumb = Uri.parse(image_thumb);

                        mDesc.setText(desc);

                        RequestOptions placeholderReq = new RequestOptions();
                        placeholderReq.placeholder(R.drawable.profile_placeholder);
                        Glide.with(EditPostActivity.this).setDefaultRequestOptions(placeholderReq).load(image).into(mSelectImage);

                    }

                } else {

                    String errMSg = task.getException().getMessage();
                    Toast.makeText(EditPostActivity.this, "" + errMSg, Toast.LENGTH_SHORT).show();

                }
                dialog.dismiss();
            }
        });


        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BringImagePicker();
            }
        });

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
        AlertDialog dialog = new SpotsDialog(EditPostActivity.this, "Uploading...");


        //noinspection SimplifiableIfStatement
        if (id == R.id.postit) {

            final String desc = mDesc.getText().toString();

            if (!TextUtils.isEmpty(desc) && mainImageUri != null) {
//                    setupProgress.setVisibility(View.VISIBLE);
                dialog.show();

                if (is_Changed) {

                    String randomName = UUID.randomUUID().toString();

                    StorageReference file_path = storageReference.child("post_image").child(randomName + ".jpg");
                    UploadTask uploadTask = file_path.putFile(mainImageUri);
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

                                File newImageFile = new File(mainImageUri.getPath());


                                try {
                                    File imageZipperFile = new ImageZipper(EditPostActivity.this)
                                            .setQuality(10)
                                            .setMaxWidth(200)
                                            .setMaxHeight(200)
                                            .compressToFile(newImageFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    imageBitmap = new ImageZipper(EditPostActivity.this).compressToBitmap(newImageFile);
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
                                            String des = desc.toString();


                                            Map<String, Object> postMap = new HashMap<>();
                                            postMap.put("image_uri", imageUri);
                                            postMap.put("image_thumb", imageUriThumb);
                                            postMap.put("desc", des);
                                            postMap.put("user_id", current_user_id);
                                            postMap.put("timestamp", FieldValue.serverTimestamp());
                                            postMap.put("reports", "false");

                                            storeFirestore(task, taskThumb, blogPostId, desc);

                                        } else {
                                            // Handle failures
                                            // ...
                                        }
                                    }
                                });


                            } else {

                                String errMsg = task.getException().getMessage();
                                Toast.makeText(EditPostActivity.this, "Image Error " + errMsg, Toast.LENGTH_SHORT).show();

//                            progressBar.setVisibility(View.INVISIBLE);
                                dialog.dismiss();
                            }
                        }
                    });

                } else {

                    storeFirestore(null, null, blogPostId, desc);

                }
            } else if (mainImageUri == null) {

                Toast toast = Toast.makeText(EditPostActivity.this, "Please select photo", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

            } else if (TextUtils.isEmpty(desc)) {

                Toast toast = Toast.makeText(EditPostActivity.this, "Please enter Description Post", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void BringImagePicker() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(EditPostActivity.this);
    }

    private void storeFirestore(@NonNull Task<Uri> task, @NonNull Task<Uri> taskThumb, String post_id,  String desc) {

        SpotsDialog dialog = new SpotsDialog(EditPostActivity.this);

        Uri downloadUriImg;
        Uri downloadUriThumb;

        if (task != null && taskThumb != null) {

            downloadUriImg = task.getResult();
            downloadUriThumb = taskThumb.getResult();

        } else {

            downloadUriImg = mainImageUri;
            downloadUriThumb = mainImageUriThumb;

        }

        Map<String, Object> userMap = new HashMap<>();

        userMap.put("desc" , desc);
        userMap.put("image_uri", downloadUriImg.toString());
        userMap.put("image_thumb", downloadUriThumb.toString());

        firebaseFirestore.collection("Posts").document(post_id).update(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

//                    setupProgress.setVisibility(View.INVISIBLE);
                    dialog.show();

                    Toast.makeText(EditPostActivity.this, "Post Updated", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditPostActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {

                    String errMSg = task.getException().getMessage();
                    Toast.makeText(EditPostActivity.this, ""+ errMSg, Toast.LENGTH_SHORT).show();

                }
//                setupProgress.setVisibility(View.INVISIBLE);
                dialog.dismiss();

            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageUri = result.getUri();
                mSelectImage.setImageURI(mainImageUri);

                is_Changed = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
