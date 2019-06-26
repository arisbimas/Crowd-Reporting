package com.aris.crowdreporting.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aris.crowdreporting.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dmax.dialog.SpotsDialog;

public class VerificationEmailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Button nextBtn, editEmail, exit;
    private TextView tvEmail;
    private SpotsDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_email);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        nextBtn = findViewById(R.id.btn_next);
        tvEmail = findViewById(R.id.tv_email);
        editEmail = findViewById(R.id.edit_email);
        exit = findViewById(R.id.ext);

        String emailUser = "We've sent a link. Please check your email account for " + "<b>" + currentUser.getEmail() + "</b>" + " and click the link inside.";
        tvEmail.setText(Html.fromHtml(emailUser));

        editEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(VerificationEmailActivity.this, ReAuthEmailActivity.class));
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(VerificationEmailActivity.this, "" + mAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                dialog = new SpotsDialog(VerificationEmailActivity.this, "Please Wait.");
                dialog.show();

                mAuth.getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        if (mAuth.getCurrentUser().isEmailVerified()) {
                            Toast.makeText(VerificationEmailActivity.this, "" + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
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

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new SpotsDialog(VerificationEmailActivity.this, "Please Wait");
                dialog.show();

                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                user.reload();

                if (!user.isEmailVerified()) {
                    user.sendEmailVerification()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(VerificationEmailActivity.this, "Link Sent!", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(VerificationEmailActivity.this, "Your Email is Verified, Click Next Button.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

            }
        });
    }

}
