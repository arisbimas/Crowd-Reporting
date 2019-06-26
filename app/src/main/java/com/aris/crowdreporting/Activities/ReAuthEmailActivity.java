package com.aris.crowdreporting.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.aris.crowdreporting.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dmax.dialog.SpotsDialog;

public class ReAuthEmailActivity extends AppCompatActivity {

    private EditText editEmail;
    private ImageButton btnEditEmail;
    private SpotsDialog spotsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_re_auth_email);

        editEmail = findViewById(R.id.edit_emailveri);
        btnEditEmail = findViewById(R.id.btn_edit_email);

        btnEditEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                spotsDialog = new SpotsDialog(ReAuthEmailActivity.this, "Please Wait.");
                spotsDialog.show();
                String email = editEmail.getText().toString();

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                user.updateEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ReAuthEmailActivity.this, "User email address updated.", Toast.LENGTH_SHORT).show();

                                    Intent setupIntent = new Intent(ReAuthEmailActivity.this, VerificationEmailActivity.class);
                                    startActivity(setupIntent);
                                    finish();
                                    spotsDialog.dismiss();
                                }
                            }

                        });
            }
        });
    }


}
