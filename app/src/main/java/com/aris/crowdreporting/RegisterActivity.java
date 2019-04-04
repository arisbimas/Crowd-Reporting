package com.aris.crowdreporting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aris.crowdreporting.Fragment.HomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private CheckBox term;
    private ProgressBar mProgress;

    private EditText mName, mEmail, mPass, mPassConfirm;
    private Button mAlready;
    private ImageButton mBtnRegis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


        mEmail = (EditText)findViewById(R.id.reg_email);
        mPass = (EditText)findViewById(R.id.reg_pass);
        mPassConfirm = (EditText)findViewById(R.id.reg_confirm_pass);
        mBtnRegis = (ImageButton) findViewById(R.id.btn_reg);
        mAlready = (Button) findViewById(R.id.back_login);
        term = (CheckBox)findViewById(R.id.term);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);

        mAlready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

        mBtnRegis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mEmail.getText().toString();
                String pass = mPass.getText().toString();
                String confirmpass = mPassConfirm.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)
                        && !TextUtils.isEmpty(confirmpass) && term.isChecked()){

                    if (pass.equals(confirmpass)){

                        mProgress.setVisibility(View.VISIBLE);

                        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()){

                                    Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
                                    startActivity(setupIntent);
                                    finish();

                                } else {

                                    String errMsg = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, "Error ! " + errMsg, Toast.LENGTH_SHORT).show();

                                }
                                mProgress.setVisibility(View.INVISIBLE);
                            }
                        });

                    } else {

                        Toast.makeText(RegisterActivity.this, "Password and Confirm Password not Match!", Toast.LENGTH_SHORT).show();
                        
                    }

                }else if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }else if (TextUtils.isEmpty(pass)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!term.isChecked()){
                    Toast.makeText(RegisterActivity.this, "Term and Conditions is not Checked", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            
            sendMain();
        }
    }

    private void sendMain() {

        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
