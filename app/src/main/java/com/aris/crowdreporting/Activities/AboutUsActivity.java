package com.aris.crowdreporting.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.aris.crowdreporting.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class AboutUsActivity extends AppCompatActivity {

    private Button btnWa, btnKetentuan;
    private RelativeLayout c1,c2;
    private CircleImageView imgAris;
    private SpotsDialog spotsDialog;

    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;

    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        btnWa = findViewById(R.id.btnOpenWhatsapp);
        imgAris = findViewById(R.id.foto_aris);
        c1 = findViewById(R.id.c1);
        c2 = findViewById(R.id.c2);
        btnKetentuan = findViewById(R.id.showKentuntuan);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        spotsDialog = new SpotsDialog(this, "Wait.");
        spotsDialog.show();

        String uIdaris = "Ug2oAiwl0gQfPL9RBEIaK9hcF283";
        firebaseFirestore.collection("Users").document(uIdaris).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String fotoArisUrl = task.getResult().get("image").toString();

                Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
                c1.startAnimation(animation1);

                Animation an2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
                c2.startAnimation(an2);

                RequestOptions placeholderReq = new RequestOptions();
                placeholderReq.placeholder(R.drawable.profile_placeholder);
                Glide.with(AboutUsActivity.this).setDefaultRequestOptions(placeholderReq).load(fotoArisUrl).into(imgAris);

                spotsDialog.dismiss();
            }
        });


        btnWa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                String nomorHp = "895400054130";
                
                if (nomorHp.isEmpty()) {
                    Toast.makeText(AboutUsActivity.this, "Nomor Wa Tidak Ada",
                            Toast.LENGTH_SHORT).show();
                } else {
                    openWhatsApp("+62" + nomorHp);
                }
            }
        });

        btnKetentuan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog = new Dialog(AboutUsActivity.this);
                mDialog.setContentView(R.layout.popup_about_app);
                mDialog.show();
            }
        });



    }

    private void openWhatsApp(String number) {

        String url = "https://wa.me/" + number +"?text=I'm%20interested%20with%20your%20App";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);

    }
}
