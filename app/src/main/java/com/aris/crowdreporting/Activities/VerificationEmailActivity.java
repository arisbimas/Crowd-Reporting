package com.aris.crowdreporting.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.aris.crowdreporting.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dmax.dialog.SpotsDialog;

public class VerificationEmailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Button nextBtn, exit;
    private SpotsDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_email);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        
        nextBtn = findViewById(R.id.btnexit);
        exit = findViewById(R.id.exit);

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent setupIntent = new Intent(VerificationEmailActivity.this, LoginActivity.class);
                startActivity(setupIntent);
                finish();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new SpotsDialog(VerificationEmailActivity.this, "Please Wait.");
                dialog.show();

                mAuth.getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        if (mAuth.getCurrentUser().isEmailVerified()){
                            Toast.makeText(VerificationEmailActivity.this, ""+currentUser.getEmail(), Toast.LENGTH_SHORT).show();
                            Intent setupIntent = new Intent(VerificationEmailActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(VerificationEmailActivity.this, "Please Verified Your Email.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });

            }
        });
    }

}
