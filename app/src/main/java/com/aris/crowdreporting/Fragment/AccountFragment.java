package com.aris.crowdreporting.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.aris.crowdreporting.LoginActivity;
import com.aris.crowdreporting.R;
import com.aris.crowdreporting.SetupActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountFragment extends Fragment {

    private Button mSignOut, mSetupAcc;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_account, container, false);

        mSignOut = (Button)view.findViewById(R.id.sign_out);
        mSetupAcc = (Button)view.findViewById(R.id.setup);

        mSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });


        mSetupAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SetupActivity.class);
                startActivity(intent);
            }
        });



        return view;
    }

    //sign out method
    public void signOut() {
        mAuth.getInstance().signOut();
        Intent i = new Intent(getActivity(),
                LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }


}