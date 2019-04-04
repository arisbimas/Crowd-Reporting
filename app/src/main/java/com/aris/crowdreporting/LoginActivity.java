package com.aris.crowdreporting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText mNama, mEmail, mPass;
    private ImageButton mSignIn;
    private Button mRegis;
    private ProgressDialog mDialog;
    private ProgressBar progressBar;


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_login);

        mEmail = (EditText) findViewById(R.id.login_email);
        mPass = (EditText) findViewById(R.id.login_password);
        mSignIn = (ImageButton) findViewById(R.id.btn_login);
        mRegis = (Button) findViewById(R.id.btn_signup);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Get Firebase auth instance
        mAuth = FirebaseAuth.getInstance();

        mRegis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pindah = new Intent(LoginActivity.this, RegisterActivity.class);
                pindah.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(pindah);
            }
        });

        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mEmail.getText().toString();
                final String password = mPass.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    progressBar.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {

                                        sendMain();

                                    } else {
                                        String errMasg = task.getException().getMessage();
                                        Toast.makeText(LoginActivity.this, "Error " + errMasg, Toast.LENGTH_SHORT).show();

                                    }
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            });

                }
                else if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {

            sendMain();
        }
    }

    private void sendMain() {

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

    }
}